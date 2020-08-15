package chris.fortress.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import chris.fortress.Start;

/**The class that starts the game on a desktop computer*/
public class DesktopLauncher {
	public static void main(String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		//CHANGE THE GAME MODE HERE (Start.MAPMAKER, Start.SERVER, or Start.CLIENT)
		int gameType = Start.CLIENT;
		if (arg.length != 0) {
			//The first argument should be either Start.SERVER, Start.CLIENT, or Start.MAPMAKER
			gameType = Integer.parseInt(arg[0]);
		}
		//Client side is full screen
		if (gameType == Start.CLIENT) {
			config.fullscreen = true;
			config.width = LwjglApplicationConfiguration.getDesktopDisplayMode().width;
			config.height = LwjglApplicationConfiguration.getDesktopDisplayMode().height;
		}
		//Server side is a small window
		else if (gameType == Start.SERVER) {
			config.resizable = false;
			config.width = LwjglApplicationConfiguration.getDesktopDisplayMode().width / 2;
			config.height = config.width / 2;
		}
		//Map maker is full screen, but can be minimized
		else if (gameType == Start.MAPMAKER) {
			config.resizable = false;
			config.width = LwjglApplicationConfiguration.getDesktopDisplayMode().width;
			config.height = LwjglApplicationConfiguration.getDesktopDisplayMode().height;
		}
		//Creates the window, and creates an object of Start, the class that starts the game logic
		new LwjglApplication(new Start(gameType), config);
	}
}