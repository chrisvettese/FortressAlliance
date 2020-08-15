package chris.fortress.socket;

import chris.fortress.Game;
import chris.fortress.GameClient;
import chris.fortress.draw.Draw;
import chris.fortress.draw.DrawWin;
import chris.fortress.entity.player.Player;
import chris.fortress.entity.player.PlayerHandler;
import chris.fortress.entity.player.PlayerSocket;
import chris.fortress.entity.projectile.ProjectileHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;

/**A class that interprets messages received for the server and client*/
public final class Protocol {
	/**To client: Send tileID, x, and y of a tile*/
	public static final byte TILE = -1;
	/**To client: Server will send updated player x and y. Send clientID, x, and y*/
	public static final byte POS = -2;
	/**To client: Tell clients that a new client has joined. Send string name, byte clientID, and float health*/
	public static final byte JOIN = -3;
	/**To client: Tell clients that a client is leaving. The client's ID must be sent after this.*/
	public static final byte LEAVE = -4;
	/**To client: Tell client to add another client to clients list. Send string name, byte clientID, and float health*/
	public static final byte ADD = -5;
	/**To client: Send how much time is remaining as short (in seconds)*/
	public static final byte TIMER = -6;
	/**From client: tell the server to go to the next game screen. To client: Switch to the next game screen (what client must receive after this varies)*/
	public static final byte START = -7;
	/**From client: Player will send requested movement to server*/
	public static final byte PRESS = -8, RELEASE = -9;
	/**To client: Server is closing, client must exit*/
	public static final byte EXIT = -10;
	/**To client: Server will send clientID and team*/
	public static final byte TEAM = -11;
	/**From client: player wants to respawn after dying. To client: send health of an entity to all the players*/
	public static final byte HEALTH = -12;
	/**From client: Client is sending mouse x and y for weapon animation. To client: Send clientID, and mouse x and y as short to update other players about the weapon animation*/
	public static final byte MOUSE = -13;
	/**To client: Send team (boolean) and the score of that team*/
	public static final byte SCORE = -14;
	/**To client: Send clientID and itemID (tell every player that a player picked up a new item)*/
	public static final byte GATHER = -15;
	/**From client: player requests to hold a different item (receive itemIDIndex). To client: Send clientID and itemID (tell every player which item the player is now holding)*/
	public static final byte EQUIP = -16;
	/**To client: Add an item to the level. Send itemID, x, y*/
	public static final byte ADD_ITEM = -17;
	/**To client: itemIndex to remove*/
	public static final byte REMOVE_ITEM = -18;
	/**From client: player wants to use the equipped weapon, read mouse x and y as short. To client: Send clientID, and mouse x and y as short to update other players about the weapon attack animations*/
	public static final byte USE = -19;
	/**To client: Create a new projectile, send x, y, xDir, yDir, projID*/
	public static final byte PROJECTILE = -20;
	/**Connection has been lost*/
	public static final byte ERROR = -21;
	/**To client: Game is over, switch to win screen*/
	public static final byte WIN = -22;
	
	/**Client side list for storing updated positions. The positions will be set once each game loop*/
	private static final Array<short[]> positions = new Array<>();
	
	/**When a server side Player receives input from the client side, determines what to read next*/
	public static short[] getInput(byte message, Player player) {
		switch (message) {
		case MOUSE:
		case USE:
			return new short[] {message, player.getClientID(), player.getPSocket().readShort(), player.getPSocket().readShort()};
		case PRESS:
		case RELEASE:
			return new short[] {message, player.getClientID(), player.getPSocket().readByte()};
		case EQUIP:
			return new short[] {message, player.getClientID(), player.getPSocket().readShort()};
		default:
			return new short[] {message, player.getClientID()};
		}
	}
	/**
	 * Called to follow through on input from the client (this method is server side only)
	 * action[0] is the instruction, 
	 * action[1] is the clientID,
	 * Higher numbers will vary for each action
	 */
	public static void checkServerInput(short[] action) {
		int cI = PlayerHandler.clientIndexOf((byte) action[1]);
		
		switch ((byte) action[0]) {
		case MOUSE:
			PlayerHandler.mouseUpdate(cI, action[2], action[3]);
			return;
		case PRESS:
			PlayerHandler.getPlayer(cI).startMoving((byte) action[2]);
			return;
		case RELEASE:
			PlayerHandler.getPlayer(cI).stopMoving((byte) action[2]);
			return;
		case USE:
			PlayerHandler.getPlayer(cI).useItem(cI, action[2], action[3]);
			return;
		case HEALTH:
			PlayerHandler.respawnPlayer(cI);
			return;
		case EQUIP:
			PlayerHandler.getPlayer(cI).setEquippedItemIndex(action[2]);
			return;
		case START:
			Game.getGame().startGame();
			return;
		case ERROR:
			PlayerHandler.removePlayer(cI, true, true);
			return;
		default:
			System.out.println(action[0] +": action not recognized");
			new Exception().printStackTrace();
			return;
		}
	}
	/**Client reads input it receives from the server*/
	public static void useClientInput(byte message) {
		PlayerSocket in = ((GameClient) Game.getGame()).getSocket();
		switch (message) {
		case POS:
			addPosition(new short[] {in.readByte(), in.readShort(), in.readShort()});
			return;
		case MOUSE:
			PlayerHandler.getPlayer(PlayerHandler.clientIndexOf(in.readByte())).getAnimator().updateWeapon(in.readShort(), in.readShort());
			return;
		case USE:
			PlayerHandler.getPlayer(PlayerHandler.clientIndexOf(in.readByte())).getAnimator().useWeapon(in.readShort(), in.readShort());
			return;
		case PROJECTILE:
			ProjectileHandler.addProjectile(in.readShort(), in.readShort(), in.readFloat(), in.readFloat(), false, in.readByte());
			return;
		case HEALTH:
			PlayerHandler.updateHealth(in.readByte(), in.readFloat());
			return;
		case SCORE:
			((GameClient) Game.getGame()).setScore(in.readBoolean(), in.readShort());
			return;
		case EQUIP:
			PlayerHandler.getPlayer(PlayerHandler.clientIndexOf(in.readByte())).getAnimator().setEquippedItem(in.readByte());
			return;
		case ADD_ITEM:
			Game.getLevel().addItem(in.readShort(), in.readShort(), in.readByte());
			return;
		case REMOVE_ITEM:
			Game.getLevel().removeItem(in.readShort(), false);
			return;
		case GATHER:
			PlayerHandler.getPlayer(PlayerHandler.clientIndexOf(in.readByte())).addGatheredItem(in.readByte(), false);
			return;
		case TILE:
			Game.getLevel().setTileAt(in.readShort(), in.readShort(), in.readByte());
			return;
		case JOIN:
			PlayerHandler.addPlayer(new Player(in.readString(), in.readByte(), in.readFloat()));
			return;
		case LEAVE:
			PlayerHandler.removePlayer(PlayerHandler.clientIndexOf(in.readByte()), false, false);
			return;
		case ADD:
			PlayerHandler.addPlayer(new Player(in.readString(), in.readByte(), in.readFloat()));
			return;
		case WIN:
			Gdx.app.postRunnable(()->Draw.setScreen(new DrawWin()));
			return;
		case TIMER:
			((GameClient) Game.getGame()).setTimer(in.readShort());
			return;
		case START:
			Game.getGame().startGame();
			return;
		case EXIT:
			Gdx.app.exit();
			return;
		case TEAM:
			PlayerHandler.getPlayer(PlayerHandler.clientIndexOf(in.readByte())).setTeam(in.readBoolean());
			return;
		case ERROR:
			((GameClient) Game.getGame()).leaveServer();
			return;
		default:
			System.out.println(message +": action not recognized");
			new Exception().printStackTrace();
		}
	}
	private static void addPosition(short[] pos) {
		synchronized (positions) {
			positions.add(pos);
		}
	}
	public static void updatePositions() {
		synchronized (positions) {
			for (short[] pos : positions) {
				int cI = PlayerHandler.clientIndexOf((byte) pos[0]);
				if (cI >= 0) {
					PlayerHandler.getPlayer(cI).getAnimator().updatePosition(pos[1], pos[2]);
				}
			}
			positions.clear();
		}
	}
}