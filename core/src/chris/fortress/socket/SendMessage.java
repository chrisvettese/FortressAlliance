package chris.fortress.socket;

import chris.fortress.Game;
import chris.fortress.GameClient;
import chris.fortress.entity.player.Player;
import chris.fortress.entity.player.PlayerHandler;
import chris.fortress.entity.player.PlayerSocket;

/**A class containing many methods for sending data between the server and clients. Keeping all the output methods in one class makes other classes more
 * readable (ex. SendMessage.sendPositionToClient(...) rather than a bunch of lines), and ensures that the output code is consistent
 * (ex. synchronized(pSocket.getOutput) rather than synchronized(pSocket)).
 */
public class SendMessage {
	public static void sendPositionToClient(PlayerSocket pSocket, byte clientID, short x, short y) {
		synchronized (pSocket.getOutput()) {
			pSocket.writeByte(Protocol.POS);
			pSocket.writeByte(clientID);
			pSocket.writeShort(x);
			pSocket.writeShort(y);
		}
	}
	public static void sendTeamToClient(PlayerSocket pSocket, byte clientID, boolean team) {
		synchronized (pSocket.getOutput()) {
			pSocket.writeByte(Protocol.TEAM);
			pSocket.writeByte(clientID);
			pSocket.writeBoolean(team);
		}
	}
	public static void sendPositionToAllClients(Player p) {
		for (int i = 0; i < PlayerHandler.playerCount(); i++) {
			PlayerSocket pSocket = PlayerHandler.getPlayer(i).getPSocket();
			synchronized (pSocket.getOutput()) {
				pSocket.writeByte(Protocol.POS);
				pSocket.writeByte(p.getClientID());
				pSocket.writeShort((short) p.getX());
				pSocket.writeShort((short) p.getY());
				pSocket.flush();
			}
		}
	}
	public static void sendUseToServer(short mouseX, short mouseY) {
		PlayerSocket pSocket = ((GameClient) Game.getGame()).getSocket();
		synchronized (pSocket.getOutput()) {
			pSocket.writeByte(Protocol.USE);
			pSocket.writeShort(mouseX);
			pSocket.writeShort(mouseY);
			pSocket.flush();
		}
	}
	public static void sendKeyPressedToServer(byte keyPressed) {
		PlayerSocket pSocket = ((GameClient) Game.getGame()).getSocket();
		synchronized (pSocket.getOutput()) {
			pSocket.writeByte(Protocol.PRESS);
			pSocket.writeByte(keyPressed);
			pSocket.flush();
		}
	}
	public static void sendKeyReleasedToServer(byte keyReleased) {
		PlayerSocket pSocket = ((GameClient) Game.getGame()).getSocket();
		synchronized (pSocket.getOutput()) {
			pSocket.writeByte(Protocol.RELEASE);
			pSocket.writeByte(keyReleased);
			pSocket.flush();
		}
	}
	public static void sendEquipToServer(short equippedItemIndex) {
		PlayerSocket pSocket = ((GameClient) Game.getGame()).getSocket();
		synchronized (pSocket.getOutput()) {
			pSocket.writeByte(Protocol.EQUIP);
			pSocket.writeShort(equippedItemIndex);
			pSocket.flush();
		}
	}
	/**Sends the given player the names, client IDs, and health values of all the other players, and tells other players
	 * that a new player has joined
	 */
	public static void updatePlayersAboutName(int clientIndex) {
		PlayerSocket pSocket = PlayerHandler.getPlayer(clientIndex).getPSocket();
		for (int i = 0; i < PlayerHandler.playerCount(); i++) {
			synchronized (pSocket.getOutput()) {
				pSocket.writeByte(Protocol.ADD);
				pSocket.writeString(PlayerHandler.getPlayer(i).getName());
				pSocket.writeByte(PlayerHandler.getPlayer(i).getClientID());
				pSocket.writeFloat(PlayerHandler.getPlayer(i).getHealth());
			}
			if (i != clientIndex) {
				PlayerSocket socket = PlayerHandler.getPlayer(i).getPSocket();
				synchronized (socket.getOutput()) {
					socket.writeByte(Protocol.JOIN);
					socket.writeString(PlayerHandler.getPlayer(clientIndex).getName());
					socket.writeByte(PlayerHandler.getPlayer(clientIndex).getClientID());
					socket.writeFloat(PlayerHandler.getPlayer(clientIndex).getHealth());
					socket.flush();
				}
			}
		}
		pSocket.flush();
	}
	public static void sendRemoveItemToClients(short itemIndex) {
		for (int i = 0; i < PlayerHandler.playerCount(); i++) {
			PlayerSocket pSocket = PlayerHandler.getPlayer(i).getPSocket();
			synchronized (pSocket.getOutput()) {
				pSocket.writeByte(Protocol.REMOVE_ITEM);
				pSocket.writeShort(itemIndex);
				pSocket.flush();
			}
		}
	}
	public static void sendHealthToClients(byte clientID, float health) {
		for (int i = 0; i < PlayerHandler.playerCount(); i++) {
			PlayerSocket pSocket = PlayerHandler.getPlayer(i).getPSocket();
			synchronized (pSocket.getOutput()) {
				pSocket.writeByte(Protocol.HEALTH);
				pSocket.writeByte(clientID);
				pSocket.writeFloat(health);
				pSocket.flush();
			}
		}
	}
	public static void sendMouseToServer(short mX, short mY) {
		PlayerSocket pSocket = ((GameClient) Game.getGame()).getSocket();
		synchronized (pSocket.getOutput()) {
			pSocket.writeByte(Protocol.MOUSE);
			pSocket.writeShort(mX);
			pSocket.writeShort(mY);
			pSocket.flush();
		}
	}
	public static void sendScoreToClients(boolean team, short score) {
		for (int i = 0; i < PlayerHandler.playerCount(); i++) {
			PlayerSocket pSocket = PlayerHandler.getPlayer(i).getPSocket();
			synchronized (pSocket.getOutput()) {
				pSocket.writeByte(Protocol.SCORE);
				pSocket.writeBoolean(team);
				pSocket.writeShort(score);
				pSocket.flush();
			}
		}
	}
	public static void sendEquipToClients(byte clientID, byte equippedItemID) {
		for (int i = 0; i < PlayerHandler.playerCount(); i++) {
			PlayerSocket pSocket = PlayerHandler.getPlayer(i).getPSocket();
			synchronized (pSocket.getOutput()) {
				pSocket.writeByte(Protocol.EQUIP);
				pSocket.writeByte(clientID);
				pSocket.writeByte(equippedItemID);
				pSocket.flush();
			}
		}
	}
	public static void sendGatherToClients(byte clientID, byte itemID) {
		for (int i = 0; i < PlayerHandler.playerCount(); i++) {
			PlayerSocket pSocket = PlayerHandler.getPlayer(i).getPSocket();
			synchronized (pSocket.getOutput()) {
				pSocket.writeByte(Protocol.GATHER);
				pSocket.writeByte(clientID);
				pSocket.writeByte(itemID);
				pSocket.flush();
			}
		}
	}
	public static void sendUseToClients(byte clientID, short mouseX, short mouseY) {
		for (int i = 0; i < PlayerHandler.playerCount(); i++) {
			PlayerSocket pSocket = PlayerHandler.getPlayer(i).getPSocket();
			synchronized (pSocket.getOutput()) {
				pSocket.writeByte(Protocol.USE);
				pSocket.writeByte(clientID);
				pSocket.writeShort(mouseX);
				pSocket.writeShort(mouseY);
				pSocket.flush();
			}
		}
	}
	public static void sendProjectileToClients(short x, short y, float xDir, float yDir, byte projID) {
		for (int i = 0; i < PlayerHandler.playerCount(); i++) {
			PlayerSocket pSocket = PlayerHandler.getPlayer(i).getPSocket();
			synchronized (pSocket.getOutput()) {
				pSocket.writeByte(Protocol.PROJECTILE);
				pSocket.writeShort(x);
				pSocket.writeShort(y);
				pSocket.writeFloat(xDir);
				pSocket.writeFloat(yDir);
				pSocket.writeByte(projID);
				pSocket.flush();
			}
		}
	}
	public static void sendLeaveToClients(byte clientID) {
		for (int i = 0; i < PlayerHandler.playerCount(); i++) {
			PlayerSocket pSocket = PlayerHandler.getPlayer(i).getPSocket();
			synchronized (pSocket.getOutput()) {
				pSocket.writeByte(Protocol.LEAVE);
				pSocket.writeByte(clientID);
				pSocket.flush();
			}
		}
	}
	public static void sendExitToClients() {
		for (int i = 0; i < PlayerHandler.playerCount(); i++) {
			PlayerSocket pSocket = PlayerHandler.getPlayer(i).getPSocket();
			synchronized (pSocket.getOutput()) {
				pSocket.writeByte(Protocol.EXIT);
				pSocket.flush();
			}
		}
	}
	public static void sendWinToClients() {
		for (int i = 0; i < PlayerHandler.playerCount(); i++) {
			PlayerSocket pSocket = PlayerHandler.getPlayer(i).getPSocket();
			synchronized (pSocket.getOutput()) {
				pSocket.writeByte(Protocol.WIN);
				pSocket.flush();
			}
		}
	}
	public static void sendRespawnToServer() {
		PlayerSocket pSocket = ((GameClient) Game.getGame()).getSocket();
		synchronized (pSocket.getOutput()) {
			pSocket.writeByte(Protocol.HEALTH);
			pSocket.flush();
		}
	}
	public static void sendMouseToClients(byte clientID, short mouseX, short mouseY) {
		for (int i = 0; i < PlayerHandler.playerCount(); i++) {
			PlayerSocket pSocket = PlayerHandler.getPlayer(i).getPSocket();
			synchronized (pSocket.getOutput()) {
				pSocket.writeByte(Protocol.MOUSE);
				pSocket.writeByte(clientID);
				pSocket.writeShort(mouseX);
				pSocket.writeShort(mouseY);
				pSocket.flush();
			}
		}
	}
	public static void sendWaitToClient(PlayerSocket pSocket) {
		synchronized (pSocket.getOutput()) {
			pSocket.writeByte(Protocol.START);
			for (int j = 0; j < PlayerHandler.playerCount(); j++) {
				pSocket.writeByte(PlayerHandler.getPlayer(j).getClientID());
			}
			pSocket.flush();
		}
	}
	public static void sendMapInfoToClient(float r, float g, float b, short mapWidth, short mapHeight, PlayerSocket pSocket) {
		synchronized (pSocket.getOutput()) {
			pSocket.writeFloat(r);
			pSocket.writeFloat(g);
			pSocket.writeFloat(b);
			pSocket.writeShort(mapWidth);
			pSocket.writeShort(mapHeight);
		}
	}
}