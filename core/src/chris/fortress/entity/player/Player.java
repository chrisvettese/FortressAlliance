package chris.fortress.entity.player;

import chris.fortress.Game;
import chris.fortress.GameServer;
import chris.fortress.entity.Entity;
import chris.fortress.item.ItemConfig;
import chris.fortress.socket.Protocol;
import chris.fortress.socket.SendMessage;
import chris.fortress.tile.TileConfig;
import chris.fortress.util.Timer;
import com.badlogic.gdx.utils.ByteArray;

import java.net.Socket;
import java.util.concurrent.TimeUnit;

/**The player class, which stores information such as the player's name, ID, and inventory*/
public class Player extends Entity {
	public static final float MAX_HEALTH = 100;
	public static final byte LEFT = 0, RIGHT = 1, DOWN = 2, JUMP = 3;
	public static final int WIDTH = 36, HEIGHT = 60, TILE_WIDTH = 2;
	public static final float SPEED = 240;
	public static final float JUMP_HEIGHT = 11.5f;
	public static final float GRAVITY = -0.52f;
	public static final boolean FACING_LEFT = true, FACING_RIGHT = false;
	public static final byte FALLING_FAST = -10;
	public static final float SWIM_UP_SPEED = 2;
	
	private PlayerSocket playerSocket;
	private PlayerAnimator animator;
	
	private byte clientID;
	private float health;
	private String name;
	
	private byte equippedItemID = -1;
	private ByteArray gatheredItems = new ByteArray();
	
	private boolean left = false, right = false, down = false, jump = false;
	private boolean canUseItem = true;
	private volatile boolean invulnerable;
	private boolean blockOutput = true;
	
	public Player(Socket socket, byte clientID) {
		super(-100, -100, 0, 0, false);
		playerSocket = new PlayerSocket(socket, this);
		this.clientID = clientID;
		health = MAX_HEALTH;
		name = playerSocket.readString();
		playerSocket.writeByte(clientID);
		playerSocket.startInputThread();
	}
	/**For creating a client side player (doesn't have a PlayerSocket, but does have a PlayerAnimator)*/
	public Player(String name, byte clientID, float health) {
		super(-100, -100, 0, 0, false);
		this.name = name;
		this.clientID = clientID;
		this.health = health;
		animator = new PlayerAnimator(this);
	}
	/**For creating a new player object based on a previous one (to reset the player after a level ends)*/
	public Player(String name, byte clientID, float health, PlayerSocket playerSocket) {
		super(-100, -100, 0, 0, false);
		this.name = name;
		this.clientID = clientID;
		this.health = health;
		this.playerSocket = playerSocket;
	}
	public byte getClientID() {
		return clientID;
	}
	public String getName() {
		return name;
	}
	public PlayerSocket getPSocket() {
		return playerSocket;
	}
	public void dispose(boolean serverSide) {
		if (serverSide) {
			if (playerSocket != null) playerSocket.dispose();
		} else {
			if (animator != null) animator.stopTimers();
		}
	}
	public boolean jump() { return jump; }
	public boolean left() { return left; }
	public boolean right() { return right; }
	public boolean down() { return down; }
	
	public void respawn(short spawnX, short spawnY) {
		if (health <= 0) {
			setX(spawnX);
			setY(spawnY);
			//Server sets the player movement variables to false. If the player is moving, it will send a new request to move when respawning
			down = false;
			jump = false;
			right = false;
			left = false;
			setHealth(Player.MAX_HEALTH);
			SendMessage.sendHealthToClients(clientID, health);
			SendMessage.sendPositionToAllClients(this);
		}
	}
	public void startMoving(byte movement) {
		switch (movement) {
		case LEFT:
			left = true;
			break;
		case RIGHT:
			right = true;
			break;
		case DOWN:
			down = true;
			break;
		case JUMP:
			jump = true;
			break;
		}
	}
	public void stopMoving(byte movement) {
		switch (movement) {
		case LEFT:
			left = false;
			break;
		case RIGHT:
			right = false;
			break;
		case DOWN:
			down = false;
			break;
		case JUMP:
			jump = false;
			break;
		}
	}
	public byte getEquippedItemID() {
		return equippedItemID;
	}
	public float getHealth() {
		return health;
	}
	public void setHealth(float health) {
		this.health = health;
	}
	public boolean canUseItem() {
		return canUseItem;
	}
	/**Called server side when the player just used an item. Player can't use the item again until the ItemConfig.getTimeGap() passes*/
	protected void startItemCooldown() {
		canUseItem = false;
		Timer.getScheduledExecutorService().schedule(()->allowItem(), ItemConfig.getItem(equippedItemID).getTimeGap(), TimeUnit.MILLISECONDS);
	}
	public boolean hurt(float damage) {
		boolean tookDamage = false;
		if (!invulnerable) {
			tookDamage = true;
			invulnerable = true;
			float newHealth = health - damage;
			if (newHealth <= 0) {
				newHealth = 0;
			}
			this.health = newHealth;
			Timer.getScheduledExecutorService().schedule(()->setVulnerable(), 200, TimeUnit.MILLISECONDS);
		}
		return tookDamage;
	}
	private void setVulnerable() {
		invulnerable = false;
	}
	private void allowItem() {
		canUseItem = true;
	}
	/**Server side: set the equipped item for this player, and update all the client side players about the new equipped item*/
	public void setEquippedItemIndex(int itemIDIndex) {
		equippedItemID = gatheredItems.get(itemIDIndex);
		SendMessage.sendEquipToClients(clientID, equippedItemID);
	}
	/**For setting the equipped item on the client side*/
	public void setEquippedItem(byte itemID) {
		equippedItemID = itemID;
	}
	public int getEquippedItemIndex() {
		return gatheredItems.indexOf(equippedItemID);
	}
	public int gatheredItemsCount() {
		return gatheredItems.size;
	}
	public void addGatheredItem(byte itemID, boolean serverSide) {
		if (gatheredItems.indexOf(itemID) == -1) {
			gatheredItems.add(itemID);
			if (serverSide) {
				SendMessage.sendGatherToClients(clientID, itemID);
				setEquippedItemIndex(gatheredItems.size - 1);
			} else {
				animator.setEquippedItem(itemID);
			}
		}
	}
	public boolean hasItem(byte itemID) {
		return gatheredItems.contains(itemID);
	}
	/**Server side code for updating the players when a new player joins.
	 * Used every time a player joins during the LEVEL or WIN state. Called for every player when the level starts.
	 * @param state The current game state*/
	public void startPlayer(byte state) {
		synchronized (playerSocket.getOutput()) {
			if (state == GameServer.STATE_LEVEL) {
				playerSocket.writeByte(Protocol.TIMER);
				playerSocket.writeShort((short) ((GameServer) Game.getGame()).getTimer().getTimeRemaining());
				playerSocket.writeByte(Protocol.START);
				Game.getLevel().sendMapInfo(playerSocket);
				setTeam(((GameServer) Game.getGame()).addToTeam());
				short[] spawn = Game.getLevel().getSpawn(getTeam());
				setX(spawn[0]);
				setY(spawn[1]);
				for (int i = 0; i < PlayerHandler.playerCount(); i++) {
					Player p = PlayerHandler.getPlayer(i);
					if (getClientID() != p.getClientID()) {
						SendMessage.sendTeamToClient(p.getPSocket(), getClientID(), getTeam());
						SendMessage.sendPositionToClient(p.getPSocket(), getClientID(), (short) getX(), (short) getY());
					}
					SendMessage.sendTeamToClient(playerSocket, p.getClientID(), p.getTeam());
					SendMessage.sendPositionToClient(playerSocket, p.getClientID(), (short) p.getX(), (short) p.getY());
					PlayerHandler.getPlayer(i).getPSocket().flush();
				}
				for (int i = 0; i <= 1; i++) {
					playerSocket.writeByte(Protocol.SCORE);
					playerSocket.writeBoolean(i == 0 ? Game.RED : Game.BLUE);
					playerSocket.writeShort(((GameServer) Game.getGame()).getScore(i == 0 ? Game.RED : Game.BLUE));
				}
				//Sends all the tiles in the game
				for (short x = 0; x < Game.getLevel().getMapWidth(); x++) {
					for (short y = 0; y < Game.getLevel().getMapHeight(); y++) {
						if (Game.getLevel().getTileAt(x, y) != TileConfig.air.getID()) {
							playerSocket.writeByte(Protocol.TILE);
							playerSocket.writeShort(x);
							playerSocket.writeShort(y);
							playerSocket.writeByte(Game.getLevel().getTileAt(x, y));
						}
					}
				}
				//Sends all the items in the game
				for (int i = 0; i < Game.getLevel().getItemCount(); i++) {
					//item[0]=x, item[1]=y, item[2]=itemID
					playerSocket.writeByte(Protocol.ADD_ITEM);
					playerSocket.writeShort(Game.getLevel().getItemAt(i)[0]);
					playerSocket.writeShort(Game.getLevel().getItemAt(i)[1]);
					playerSocket.writeByte((byte) Game.getLevel().getItemAt(i)[2]);
				}
				for (int i = 0; i < PlayerHandler.playerCount(); i++) {
					for (int j = 0; j < PlayerHandler.getPlayer(i).gatheredItems.size; j++) {
						playerSocket.writeByte(Protocol.GATHER);
						playerSocket.writeByte(PlayerHandler.getPlayer(i).getClientID());
						playerSocket.writeByte(PlayerHandler.getPlayer(i).gatheredItems.get(j));
					}
					if (PlayerHandler.getPlayer(i).getEquippedItemID() >= 0) {
						playerSocket.writeByte(Protocol.EQUIP);
						playerSocket.writeByte(PlayerHandler.getPlayer(i).getClientID());
						playerSocket.writeByte(PlayerHandler.getPlayer(i).getEquippedItemID());
					}
				}
				playerSocket.flush();
			}
			else if (state == GameServer.STATE_WIN) {
				for (int i = 0; i <= 1; i++) {
					playerSocket.writeByte(Protocol.SCORE);
					playerSocket.writeBoolean(i == 0 ? Game.RED : Game.BLUE);
					playerSocket.writeShort(((GameServer) Game.getGame()).getScore(i == 0 ? Game.RED : Game.BLUE));
				}
				playerSocket.writeByte(Protocol.WIN);
				playerSocket.flush();
			}
		}
		unblock();
	}
	private void unblock() {
		blockOutput = false;
	}
	public boolean outputBlocked() {
		return blockOutput;
	}
	public PlayerAnimator getAnimator() {
		return animator;
	}
	public void useItem(int clientIndex, short mouseX, short mouseY) {
		if (GameServer.getGameState() == GameServer.STATE_LEVEL && equippedItemID >= 0 && canUseItem) {
			ItemConfig.getItem(equippedItemID).use(clientIndex, mouseX, mouseY);
			startItemCooldown();
			SendMessage.sendUseToClients(clientID, mouseX, mouseY);
		}
	}
}