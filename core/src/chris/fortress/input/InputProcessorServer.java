package chris.fortress.input;

import chris.fortress.Game;
import chris.fortress.GameServer;
import chris.fortress.Level;
import chris.fortress.draw.DrawServer;
import chris.fortress.socket.AddClient;
import com.badlogic.gdx.InputProcessor;

import java.io.*;

/**The InputProcessor for the server, which allows the user to enter the level file name*/
public final class InputProcessorServer implements InputProcessor {
	private static String mapName = "";
	
	public static String getMapName() {
		return mapName;
	}
	@Override
	public boolean keyDown(int keycode) {
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		//Only change the map name before the map file is loaded
		if (GameServer.getGameState() == GameServer.STATE_LOAD) {
			if (character == '\r' || character == '\n') {
				if (mapName.length() > 0) {
					loadMap(true);
				}
			}
			else if (character == '\b') {
				if (mapName.length() > 0) mapName = mapName.substring(0, mapName.length() - 1);
			} else if (character != 0) {
				mapName += character;
			}
		}
		return false;
	}

	public static void loadMap(boolean newServer) {
		//When stream is put in brackets beside try, it will always close even if there is an exception
		try (ObjectInputStream fileIn = new ObjectInputStream(new BufferedInputStream(new FileInputStream(new File(Level.getLevelPath() + mapName))))) {
			Level level = (Level) fileIn.readObject();
			Game.setLevel(level);
			if (newServer) {
				AddClient.startConnectionThread();
				DrawServer.showMainMessage();
			}
			GameServer.setGameState(GameServer.STATE_WAIT);
		} catch (IOException | ClassNotFoundException e) {
			mapName = "";
			DrawServer.showFileError();
		}
	}
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		return false;
	}

}