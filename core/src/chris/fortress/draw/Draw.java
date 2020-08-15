package chris.fortress.draw;

import chris.fortress.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**The base class for the drawing code. The code for everything the user sees on the screen is in a Draw class.
 * Only one Draw class can be used at once. Some Draw classes, like DrawJoin, have different "states" depending on the situation. For example,
 * it could display an error message when the player enters the wrong IP address for the server. For large changes, like from the wait screen to the
 * actual game, a different Draw class will be used (DrawWait to DrawMain)
 */
public abstract class Draw {
	/**The current screen. Default varies depending on the game mode (GameServer, GameClient, GameMapMaker)*/
	private static Draw currentScreen;
	/**Map maker variable only: Change the scale of everything in the game based on how far in or out the user zooms*/
	private static volatile float scale = 1;
	/**The value to set OrthographicCamera.zoom to. Calculated based on the screen resolution to ensure that the game looks the same on every computer.*/
	private static float zoom;
	/**The width of the window in zoomed pixels*/
	private static float zoomedWidth;
	/**The height of the window in zoomed pixels*/
	private static float zoomedHeight;

	/**Sets the current screen*/
	public static final void setScreen(Draw screen) {
		if (currentScreen != null) currentScreen.dispose();
		Draw.currentScreen = screen;
	}
	/**Multiplies the given value by the current game scale (Map Maker only)*/
	public static float scale(float value) {
		return value * scale;
	}
	/**
	 * Calculates the zoom of the game based on the screen resolution
	 * @return The zoom that OrthographicCamera.zoom should equal
	 */
	public static final float setZoom() {
		//Default screen width and height, which is either zoomed in or out depending on the actual screen resolution
		final int DEFAULT_WIDTH = 1920;
		final int DEFAULT_HEIGHT = 1080;
		//How many actual pixels wide a tile should be. With the above screen resolution, this will end up being Game.SIZE
		float zoomedTileSize = 0;
		do {
			//Viewed tile width must be greater than 0 pixels
			zoomedTileSize++;
		//Loops until zoomedTileSize will result in the game screen's zoom matching the default resolution as close as possible
		} while (!((zoomedTileSize + 1) / (float) Game.SIZE * DEFAULT_WIDTH > Gdx.graphics.getWidth() || (zoomedTileSize + 1) / (float)  Game.SIZE * DEFAULT_HEIGHT > Gdx.graphics.getHeight()));
		//If zoom < 1, the libgdx camera will zoom out, and the above code will calculate zoomedTileSize to be larger than Game.SIZE
		zoom = (float) Game.SIZE / zoomedTileSize;
		//Might seem backward, but if the game is zoomed out, then there are less real pixels per tile, so the zoomedWidth should be smaller than actual width
		zoomedWidth = Gdx.graphics.getWidth() * zoom;
		zoomedHeight = Gdx.graphics.getHeight() * zoom;
		return zoom;
	}
	/**@return The screen's zoomed width*/
	public static final float zoomedWidth() {
		return zoomedWidth;
	}
	/**@return The screen's zoomed height*/
	public static final float zoomedHeight() {
		return zoomedHeight;
	}
	/**
	 * Map maker only: updates the game's scale
	 * @param shouldZoomIn If true, the screen zooms in. If false, the screen zooms out.
	 */
	public static final void setScale(boolean shouldZoomIn) {
		if (shouldZoomIn) scale += 1f / Game.SIZE;
		else if (scale * Game.SIZE > 1) {
			scale -= 1f / Game.SIZE;
		};
	}
	/**@return The current instance of Draw*/
	public static Draw getScreen() {
		return currentScreen;
	}
	/**@return Map Maker only: The current scale*/
	public static final float getScale() {
		return scale;
	}
	/**Called when the mouse is pressed, if InputProcessorClient is used*/
	public void mousePressed(int mouseX, int mouseY) { }
	/**Called when the mouse is released, if InputProcessorClient is used*/
	public void mouseReleased(int mouseX, int mouseY) { }
	/**Called when a key is typed, if InputProcessorClient is used*/
	public void keyTyped(char character) { }
	/**Called when a key is pressed, if InputProcessorClient is used*/
	public void keyPressed(int keycode) { }
	/**Called when a key is released, if InputProcessorClient is used*/
	public void keyReleased(int keyCode) { }
	/**Called when the mouse is scrolled, if InputProcessorClient is used*/
	public void mouseScrolled(int amount) { }
	
	public abstract void draw(SpriteBatch batch, ShapeRenderer renderer);
	public abstract void dispose();
}