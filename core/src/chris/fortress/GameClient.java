package chris.fortress;

import chris.fortress.draw.*;
import chris.fortress.entity.player.Player;
import chris.fortress.entity.player.PlayerAnimator;
import chris.fortress.entity.player.PlayerHandler;
import chris.fortress.entity.player.PlayerSocket;
import chris.fortress.entity.projectile.ProjectileConfig;
import chris.fortress.entity.projectile.ProjectileHandler;
import chris.fortress.input.Button;
import chris.fortress.input.InputProcessorClient;
import chris.fortress.item.ItemConfig;
import chris.fortress.socket.Protocol;
import chris.fortress.tile.TileConfig;
import chris.fortress.util.Collision;
import chris.fortress.util.Timer;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;

import java.io.IOException;
import java.net.Socket;

public final class GameClient extends Game {
	/**Each team's score*/
	private final short[] gameScore = new short[2];
	/**This player's name*/
	private String name;
	/**This player's clientID (Each player knows every clientID, but must know which one specifically is theirs)*/
	private byte clientID;
	/**A PlayerSocket for sending and receiving data from the server*/
	private PlayerSocket socket;
	/**A libgdx feature that is used to keep the view centred on the player, and adjust the game's zoom depending on the screen resolution*/
	private OrthographicCamera camera;
	/**Keeps track of how much time is left when the level starts*/
	private Timer timer;
	
	/**Remains true until dispose() is called*/
	private volatile boolean gameRunning = true;
	
	public GameClient() {
		super();
		//Initializes the camera and sets the camera's view port to the screen width and height
		camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		
		//The input processor to use when the client program starts
		Gdx.input.setInputProcessor(new InputProcessorClient());
		//Calculates the zoom needed on this screen, and sets the camera's zoom to it
		camera.zoom = Draw.setZoom();
		//Call this whenever making changes to camera
		camera.update();
		
		//Load resources needed to render the game
		Button.setup();
		ItemConfig.loadImages(false);
		TileConfig.loadImages(false);
		ProjectileConfig.loadImages();
		PlayerAnimator.setup();
	}

	@Override
	protected void updateLoop() {
		if (gameRunning) {
			//Update player positions once per frame from the data received from the server
			Protocol.updatePositions();
			//Projectile physics
			ProjectileHandler.updateProjectiles();
			//Client side projectile collision for every client that is alive
			for (int i = 0; i < PlayerHandler.playerCount(); i++) {
				Player p = PlayerHandler.getPlayer(i);
				if (p.getHealth() > 0) {
					Collision.playerProjectileCollide(p, false);
				}
			}
		}
	}
	@Override
	public void startGame() {
		//Not efficient if statement but only called once per level
		//If the current screen is not the win screen, startGame() is called to move to the level screen (DrawMain)
		if (!(Draw.getScreen() instanceof DrawWin)) {
			//Delete all items, since the level is being reset
			for (int i = getLevel().getItemCount()- 1; i >= 0; i--) {
				getLevel().removeItem(i, false);
			}
			//Set background colour of level to the new background colour
			getLevel().setColour(new Color(socket.readFloat(), socket.readFloat(), socket.readFloat(), 1));
			//Get the map size
			getLevel().createMap(socket.readShort(), socket.readShort());
			//Rendering resources like fonts can only be initialized in the main loop, which is what postRunnable does
			Gdx.app.postRunnable(()->Draw.setScreen(new DrawMain()));
			
		//Current screen must be DrawWin, so switch to the wait screen
		} else {
			//Change the order of the players in the list so they are on different teams
			PlayerHandler.receiveShuffledPlayers();
			Gdx.app.postRunnable(()->Draw.setScreen(new DrawWait()));
		}
	}
	/**Sets the name of this computer's player*/
	public void setName(String name) {
		this.name = name;
	}
	/**Attempts to connect to a server given an IP address*/
	public void tryToConnect(String address) {
		new Thread(()->{
			Socket s = null;
			try {
				s = new Socket(address, PlayerSocket.PORT);
			} catch (IOException e) {
				//Casting is allowed in this case because current screen must be DrawJoin. Tells DrawJoin to display error message
				((DrawJoin) Draw.getScreen()).setFailedToConnect();
				//End this method
				return;
			}
			//Connection was successful, create a PlayerSocket
			socket = new PlayerSocket(s);
			//Tell server this player's name
			socket.writeString(name);
			socket.flush();
			//Get this player's clientID
			clientID = socket.readByte();
			//postRunnable makes Draw.setScreen run in the main loop, which is required for creating new fonts
			Gdx.app.postRunnable(()->Draw.setScreen(new DrawWait()));
			//Keeps waiting to receive data from the server (the client's input thread)
			while (gameRunning) {
				Protocol.useClientInput(socket.readByte());
			}
		}).start();
	}
	/**@return The PlayerSocket with the connection to the server*/
	public PlayerSocket getSocket() {
		return socket;
	}
	/**
	 * Updates the score
	 * @param team The team being updated
	 * @param score The new score for the given team
	 */
	public void setScore(boolean team, short score) {
		gameScore[team == RED ? 0 : 1] = score;
	}
	/**@return The score of the given team*/
	public short getScore(boolean team) {
		return gameScore[team == RED ? 0 : 1];
	}
	/**@return The clientID of this computer's player*/
	public byte getClientID() {
		return clientID;
	}
	/**@return The camera centred on the player*/
	public OrthographicCamera getCamera() {
		return camera;
	}
	/**Leaves the server, returns to the DrawJoin screen*/
	public void leaveServer() {
		if (gameRunning) {
			//Clear all resources with GameClient
			dispose();
			Gdx.app.postRunnable(()->{
				//Creates a fresh GameClient
				setGame(new GameClient());
				//Starting screen
				Draw.setScreen(new DrawJoin());
			});
		}
	}
	/**@return The timer keeping track of how much time is left in the level*/
	public Timer getTimer() {
		return timer;
	}
	/**Creates a timer that will count down from the given time
	 * @param time The time remaining, in seconds
	 */
	public void setTimer(short time) {
		timer = new Timer(time);
	}
	@Override
	protected void dispose() {
		//Stops this method from being called twice, for example if the server closes and the player closes the window at the same  time
		if (gameRunning) {
			gameRunning = false;
			if (timer != null) timer.stopTimer();
			if (socket != null) {
				socket.dispose();
			}
			//Drawing resources must be called in the main game loop, which postRunnable does
			Gdx.app.postRunnable(()->{
				PlayerAnimator.dispose();
				ItemConfig.dispose();
				TileConfig.dispose();
				ProjectileConfig.dispose();
				PlayerHandler.dispose();
				Button.dispose();
			});
		}
	}
}