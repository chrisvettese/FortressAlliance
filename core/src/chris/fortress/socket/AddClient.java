package chris.fortress.socket;

import chris.fortress.GameServer;
import chris.fortress.entity.player.Player;
import chris.fortress.entity.player.PlayerHandler;
import chris.fortress.entity.player.PlayerSocket;
import com.badlogic.gdx.Gdx;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**A server side thread class that connects players to the server*/
public class AddClient {
	private static Thread connectionThread;
	private static ServerSocket serverSocket;
	
	public static void startConnectionThread() {
		try {
			serverSocket = new ServerSocket(PlayerSocket.PORT);
			connectionThread = new Thread(()->{
				waitForClient();
			});
			connectionThread.start();
		} catch (IOException e) {
			//A server is already running: close this server
			Gdx.app.exit();
		}
	}
	private static void waitForClient() {
		try {
			while (!Thread.interrupted()) {
				Socket socket = serverSocket.accept();
				//Not good to have Nagle's algorithm for a game where updates must be received immediately
				socket.setTcpNoDelay(true);
				Player p = new Player(socket, PlayerHandler.nextClientID());
				PlayerHandler.addPlayer(p);
				SendMessage.updatePlayersAboutName(PlayerHandler.playerCount() - 1);
				p.startPlayer(GameServer.getGameState());
			}
		} catch (IOException e) {
			//Server is closing - ignore exception
		}
	}
	public static void dispose() {
		if (serverSocket != null) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				//Server is closing - ignore exception
			}
		}
		if (connectionThread != null) {
			connectionThread.interrupt();
		}
	}
}
