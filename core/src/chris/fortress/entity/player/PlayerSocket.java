package chris.fortress.entity.player;

import chris.fortress.socket.Protocol;
import com.badlogic.gdx.Gdx;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**Contains methods for reading and writing to the socket, and has a server side thread for every player that reads from the socket*/
public class PlayerSocket {
	public static final int PORT = 1235;
	
	private Player player;
	private Socket socket;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private List<short[]> inActions;
	private Thread inputThread;
	
	public PlayerSocket(Socket socket, Player player) {
		this.socket = socket;
		this.player = player;
		inActions = new ArrayList<>();
		openStreams();
	}
	/**To connect the client to the server*/
	public PlayerSocket(Socket socket) {
		this.socket = socket;
		openStreams();
	}
	private void openStreams() {
		try {
			out = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
			out.flush();
			in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
			Gdx.app.exit();
		}
	}
	public void startInputThread() {
		inputThread = new Thread(()->{
			inputLoop();
		});
		inputThread.start();
	}
	private void inputLoop() {
		while (!Thread.interrupted()) {
			short[] messageReceived = Protocol.getInput(readByte(), player);
			synchronized (inActions) {
				inActions.add(messageReceived);
			}
		}
	}
	/**
	 * Reads a string
	 */
	public String readString() {
		try {
			return in.readUTF();
		} catch (IOException e) {
			return null;
		}
	}
	/**
	 * Reads a string
	 */
	public short readShort() {
		try {
			short s = in.readShort();
			return s;
		} catch (IOException e) {
			return Protocol.ERROR;
		}
	}
	/**
	 * Reads a byte
	 */
	public byte readByte() {
		try {
			byte b = in.readByte();
			return b;
		} catch (IOException e) {
			return Protocol.ERROR;
		}
	}
	public boolean readBoolean() {
		try {
			boolean b = in.readBoolean();
			return b;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	public float readFloat() {
		try {
			float f = in.readFloat();
			return f;
		} catch (IOException e) {
			return Protocol.ERROR;
		}
	}
	public void writeByte(byte message) {
		try {
			out.writeByte(message);
		} catch (IOException e) {
		}
	}
	public void writeString(String message) {
		try {
			out.writeUTF(message);
		} catch (IOException e) {
		}
	}
	public void writeBoolean(boolean message) {
		try {
			out.writeBoolean(message);
		} catch (IOException e) {
		}
	}
	public void writeShort(short message) {
		try {
			out.writeShort(message);
		} catch (IOException e) {
		}
	}
	public void writeFloat(float message) {
		try {
			out.writeFloat(message);
		} catch (IOException e) {
		}
	}
	/**Flushes the output stream*/
	public void flush() {
		try {
			out.flush();
		} catch (IOException e) {
		}
	}
	public void dispose() {
		//Not null on the server side
		if (inputThread != null) {
			inputThread.interrupt();
		}
		//Closing socket also closes the input and output streams
		try {
			if (socket != null) {
				socket.close();
			}
		} catch (IOException e) {
			//Server is closing - ignore exception
		}
	}
	public void useInActions() {
		synchronized (inActions) {
			for (short[] action : inActions) {
				Protocol.checkServerInput(action);
			}
			inActions.clear();
		}
	}
	/**Used for synchronization in the SendMessage class. Use above methods to write to the output stream, don't use the output stream directly*/
	public ObjectOutputStream getOutput() {
		return out;
	}
}