package chris.fortress.entity.player;

import chris.fortress.Game;
import chris.fortress.GameClient;
import chris.fortress.GameServer;
import chris.fortress.draw.Draw;
import chris.fortress.draw.DrawMain;
import chris.fortress.draw.DrawRespawn;
import chris.fortress.entity.projectile.ProjectileHandler;
import chris.fortress.item.ItemConfig;
import chris.fortress.socket.SendMessage;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;

/**Manages the players currently connected to the server. Used on both the client and server side*/
public final class PlayerHandler {
	private static final Array<Player> players = new Array<>();
	
	private static byte currentClientID = Byte.MIN_VALUE;
	
	public static void addPlayer(Player player) {
		synchronized (players) {
			players.add(player);
		}
	}
	public static void removePlayer(int cI, boolean playerLeftOnServer, boolean removeFromTeam) {
		if (cI >= 0) {
			synchronized (players) {
				if (removeFromTeam) {
					((GameServer) Game.getGame()).removeFromTeam(players.get(cI).getTeam());
				}
				Player p = players.get(cI);
				players.removeIndex(cI);
				p.dispose(playerLeftOnServer);
				if (playerLeftOnServer) {
					SendMessage.sendLeaveToClients(p.getClientID());
				}
			}
		}
	}
	/**Disposes and replaces player at given index without updating clients*/
	public static void replacePlayer(int cI, Player p) {
		synchronized (players) {
			Player oldPlayer = players.get(cI);
			players.set(cI, p);
			//serverSide = false so the socket isn't closed
			oldPlayer.dispose(false);
		}
	}
	public static Player getPlayer(int cI) {
		synchronized (players) {
			return players.get(cI);
		}
	}
	public static int playerCount() {
		synchronized (players) {
			return players.size;
		}
	}
	public static int clientIndexOf(byte clientID) {
		synchronized (players) {
			for (int i = 0; i < players.size; i++) {
				if (players.get(i).getClientID() == clientID) {
					return i;
				}
			}
		}
		return -1;
	}
	public static byte nextClientID() {
		byte clientID = currentClientID;
		currentClientID++;
		return clientID;
	}
	public static void mouseUpdate(int cI, short mX, short mY) {
		synchronized (players) {
			if (ItemConfig.getItem(players.get(cI).getEquippedItemID()).shouldUpdateMouse()) {
				SendMessage.sendMouseToClients(players.get(cI).getClientID(), mX, mY);
			}
		}
	}
	public static void respawnPlayer(int cI) {
		synchronized (players) {
			short[] spawn = Game.getLevel().getSpawn(players.get(cI).getTeam());
			players.get(cI).respawn(spawn[0], spawn[1]);
		}
	}
	/**
	 * Client side method: Set the health of the given player to newHealth
	 * @param clientID The player's clientID
	 * @param health The player's updated health
	 */
	public static void updateHealth(byte clientID, float health) {
		int clientIndex = clientIndexOf(clientID);
		if (clientIndex >= 0) {
			//If the player's health being updated is this player, and the health is being updated from <= 0 (dead) to > 0 (alive), then switch to level screen
			synchronized (players) {	
				if (clientID == ((GameClient) Game.getGame()).getClientID() && players.get(clientIndex).getHealth() <= 0 && health > 0) {
					Gdx.app.postRunnable(()->Draw.setScreen(new DrawMain()));
				}
				players.get(clientIndex).setHealth(health);
				//If this player has died
				if (players.get(clientIndex).getHealth() <= 0) {
					if (clientID == ((GameClient) Game.getGame()).getClientID()) {
						Gdx.app.postRunnable(()->Draw.setScreen(new DrawRespawn()));
					}
					ProjectileHandler.playerDeath(clientIndex);
				}
			}
		}
	}
	public static void dispose() {
		for (Player p : players) {
			p.dispose(false);
		}
		players.clear();
	}
	/**Server side method: Randomly shuffles the players in the Array so that they end up on different teams than before*/
	public static void shufflePlayers() {
		synchronized (players) {
			players.shuffle();
		}
	}
	/**Client side method: After switching from WIN screen to WAIT screen, receive clientIDs in the order that players should be in*/
	public static void receiveShuffledPlayers() {
		Array<Player> newPlayers = new Array<>();
		synchronized (players) {
			for (int i = 0; i < players.size; i++) {
				byte clientID = ((GameClient) Game.getGame()).getSocket().readByte();
				newPlayers.add(players.get(clientIndexOf(clientID)));
			}
			players.clear();
			players.addAll(newPlayers);
		}
	}
}