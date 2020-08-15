package chris.fortress;

import chris.fortress.entity.Entity;
import chris.fortress.entity.player.Player;
import chris.fortress.entity.player.PlayerHandler;
import chris.fortress.entity.projectile.ProjectileHandler;
import chris.fortress.input.InputProcessorServer;
import chris.fortress.socket.AddClient;
import chris.fortress.socket.SendMessage;
import chris.fortress.tile.TileConfig;
import chris.fortress.util.Collision;
import chris.fortress.util.Timer;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ObjectMap.Entries;
import com.badlogic.gdx.utils.ObjectMap.Entry;

public class GameServer extends Game {
	/**The original state, before the map has been loaded*/
	public static final byte STATE_LOAD = 0;
	/**When the players see the waiting screen with a list of all the players*/
	public static final byte STATE_WAIT = 1;
	/**When the game is in progress*/
	public static final byte STATE_LEVEL = 2;
	/**When the game ends, and the players see which team won*/
	public static final byte STATE_WIN = 3;
	/**How many seconds the game should last*/
	private static final short LEVEL_TIME = 5 * 60;
	
	/**The score of each team, index 0 = red, 1 = blue*/
	private static final short[] gameScore = new short[2];
	/**Keeps track of how many players are on each team*/
	private final short[] teams = new short[2];
	
	/**The current game state. Default is STATE_LOAD, which is when the server loads the map file*/
	private static volatile byte state = STATE_LOAD;
	/**Keeps track of how much time is left in the game*/
	private Timer timer;
	
	private static float oldXDir;
	
	public GameServer() {
		super();
		Gdx.input.setInputProcessor(new InputProcessorServer());
	}
	@Override
	protected void updateLoop() {
		//Game physics loop
		if (state == STATE_LEVEL) {
			for (int i = 0; i < PlayerHandler.playerCount(); i++) {
				Player p = PlayerHandler.getPlayer(i);
				//If the player is up to date on the game, and is alive, then it has game physics
				if (!p.outputBlocked() && p.getHealth() > 0) {
					oldXDir = p.getXDir();
					if (p.left() && !p.right()) p.setXDir(Gdx.graphics.getDeltaTime() * -Player.SPEED);
					else if (p.right() && !p.left()) p.setXDir(Gdx.graphics.getDeltaTime() * Player.SPEED);
					else p.setXDir(0);
					//Cap speed so collision works properly
					if (p.getXDir() > Entity.MAX_SPEED) p.setXDir(Entity.MAX_SPEED);
					else if (p.getXDir() < -Entity.MAX_SPEED) p.setXDir(-Entity.MAX_SPEED);
					Collision.playerInLiquid(p);
					Entries<Byte, int[]> tilesBelow = Collision.getTilesBelow(p).iterator();
					while (tilesBelow.hasNext) {
						Entry<Byte, int[]> tileAt = tilesBelow.next();
						TileConfig tile = TileConfig.getTile(tileAt.key);
						tile.preCollision(p, tileAt.value[0], tileAt.value[1]);
					}
					float oldX = p.getX(), oldY = p.getY();
					//Checks player collision with tiles, items and projectiles
					//Adjusts the player's position and speed so that the player does not collide
					Collision.checkCollision(p, p.getYDir());

					/*If player is moving or if position was adjusted*/
					if (p.getYDir() != 0 || p.getXDir() != 0 || p.getX() != oldX || p.getY() != oldY) {
						//Update x and y with collision-adjusted xDir and yDir
						p.setX(p.getX() + p.getXDir());
						p.setY(p.getY() + p.getYDir());
						//Kill the player if it falls below the map
						if (p.getY() < -Player.HEIGHT) {
							//Kill the player
							p.hurt(p.getHealth());
							//Increase score of the other team
							setScore(!p.getTeam(), (short) (getScore(!p.getTeam()) + 1));
							//Tell every client that the player has 0 health left
							SendMessage.sendHealthToClients(p.getClientID(), p.getHealth());
						}
						//Updates all the clients on this player's position
						else SendMessage.sendPositionToAllClients(p);
					}
				}
			}
			//Projectile physics
			ProjectileHandler.updateProjectiles();
			//If time has run out, tell all the clients to go to the WIN screen to show the winning team
			if (timer.getTimeRemaining() <= 0) {
				state = STATE_WIN;
				SendMessage.sendWinToClients();
				//Create a fresh GameServer instance to reset the game
				Game.setGame(new GameServer());
			}
		}
		//Follow through on data received from the clients
		for (int i = 0; i < PlayerHandler.playerCount(); i++) {
			PlayerHandler.getPlayer(i).getPSocket().useInActions();
		}
	}
	@Override
	protected void dispose() {
		if (timer != null) timer.stopTimer();
		AddClient.dispose();
		//Tell clients that the server is closing (as opposed to them just losing connection)
		SendMessage.sendExitToClients();
		for (int i = PlayerHandler.playerCount() - 1; i >= 0; i--) {
			PlayerHandler.removePlayer(i, true, false);
		}
	}
	@Override
	public void startGame() {
		//If currently the WAIT state, move to the level state (where the game actually happens)
		if (state == STATE_WAIT) {
			//Start the countdown until the the end of the level
			timer = new Timer(LEVEL_TIME);
			for (int i = 0; i < PlayerHandler.playerCount(); i++) {
				PlayerHandler.getPlayer(i).startPlayer(STATE_LEVEL);
			}
			state = STATE_LEVEL;
		}
		//If the current state is WIN, change to WAIT
		else if (state == STATE_WIN) {
			//Resets the score for the next level
			setScore(RED, (short) 0);
			setScore(BLUE, (short) 0);
			//Loads a new map and sets the state to STATE_WAIT (the previous map was changed by the players)
			InputProcessorServer.loadMap(false);
			//Switches up the order of the players so they are on different teams for the next level
			PlayerHandler.shufflePlayers();
			//Refreshes all the players while keeping the connections, names, and clientIDs
			for (int i = 0; i < PlayerHandler.playerCount(); i++) {
				Player p = PlayerHandler.getPlayer(i);
				PlayerHandler.replacePlayer(i, new Player(p.getName(), p.getClientID(), Player.MAX_HEALTH, p.getPSocket()));
				SendMessage.sendWaitToClient(p.getPSocket());
			}
		}
	}
	/**Updates the server side score, sends the new score to all the clients*/
	public void setScore(boolean team, short score) {
		gameScore[team == RED ? 0 : 1] = score;
		SendMessage.sendScoreToClients(team, score);
	}
	/**@return The score of the given team*/
	public short getScore(boolean team) {
		return gameScore[team == RED ? 0 : 1];
	}
	/**@return The team to add a player to in order to keep the teams balanced*/
	public boolean addToTeam() {
		if (teams[0] <= teams[1]) {
			teams[0]++;
			return RED;
		} else {
			teams[1]++;
			return BLUE;
		}
	}
	/**Keeps track of when a player leaves, so that new players that join will be placed on the smaller team*/
	public void removeFromTeam(boolean team) {
		teams[team == RED ? 0 : 1]--;
	}
	/**@return The number of players on the given team*/
	public short getTeamCount(boolean team) {
		return teams[team == RED ? 0 : 1];
	}
	/**@return The current game state (ex. STATE_WIN or STATE_LEVEL)*/
	public static byte getGameState() {
		return state;
	}
	/**Updates the current game state to the one provided*/
	public static void setGameState(byte state) {
		GameServer.state = state;
	}
	/**@return The timer instance keeping track of how much time is remaining in the level*/
	public Timer getTimer() {
		return timer;
	}
	public static float getOldXDir() {
		return oldXDir;
	}
}