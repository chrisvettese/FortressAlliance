package chris.fortress;

import chris.fortress.draw.Draw;
import chris.fortress.draw.DrawLoadMap;
import chris.fortress.draw.DrawMapMaker;
import chris.fortress.input.Button;
import chris.fortress.input.InputProcessorClient;
import chris.fortress.item.ItemConfig;
import chris.fortress.tile.TileConfig;
import chris.fortress.util.CollisionMapMaker;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;

import java.io.*;

public final class GameMapMaker extends Game {
	/**The speed the camera moves at when the user controls it*/
	private static final float SPEED = 150;
	/**The name of the map chosen by the user, or the name of the file the map was loaded from*/
	private static String mapName = "";
	/**The camera that moves around the map and zooms to fit the screen resolution*/
	private final OrthographicCamera camera = new OrthographicCamera();
	
	/**Camera movement*/
	private boolean up, down, left, right;
	
	/**The current selected tile or item ID*/
	private byte selectedID;
	/**If the above selectedID is an itemID (true) or tileID (false)*/
	private boolean itemMode;
	/**If the map has been loaded/created and can now be edited*/
	private boolean mapMakerMode = false;
	
	/**The size to scale the game by, based on how much the user zooms in or out*/
	private float scaledSize;
	
	public GameMapMaker() {
		super();
		Gdx.input.setInputProcessor(new InputProcessorClient());
		//Sets the camera at the starting position
		camera.setToOrtho(false);
		//Sets the camera's zoom to the one calculated in Draw.setZoom()
		camera.zoom = Draw.setZoom();
		//Call this whenever the camera is changed
		camera.update();
		
		//Sets the default scaled size of the level
		scaledSize = Draw.scale(SIZE);
		
		//Loads resources needed to render the level
		TileConfig.loadImages(true);
		ItemConfig.loadImages(true);
		Button.setup();
	}
	@Override
	protected void updateLoop() {
		//Camera movement based on user input
		if (up) camera.position.y += Gdx.graphics.getDeltaTime() * SPEED;
		if (down) camera.position.y -= Gdx.graphics.getDeltaTime() * SPEED;
		if (left) camera.position.x -= Gdx.graphics.getDeltaTime() * SPEED;
		if (right) camera.position.x += Gdx.graphics.getDeltaTime() * SPEED;
		camera.update();
		
		//Level editing logic if the level has been loaded
		if (mapMakerMode) {
			//If the mouse is pressed, place/remove a tile. Items are only placed/removed when the mouse is clicked once, not held down
			if (((DrawMapMaker) Draw.getScreen()).isMousePressed()) {
				Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
				//Converts from screen coordinates (Gdx.input.getX() and getY()) to game coordinates
				mousePos = camera.unproject(mousePos);

				if (mousePos.x >= 0 && mousePos.y >= 0) {
					//If the user wants to place a tile, not an item
					if (!itemMode) {
						//Convert from game coordinates to tile units (scaledSize is Game.SIZE scaled depending on how far the user zoomed in)
						int x = (int) (mousePos.x / scaledSize);
						int y = (int) (mousePos.y / scaledSize);
						//If x and y is in bounds
						if (x < getLevel().getMapWidth() && y < getLevel().getMapHeight()) {
							//Attempt to place a tile (or remove one if TileConfig.air is selected)
							CollisionMapMaker.tileMouseCollision(x, y, selectedID);
						}
					}
				}
			}
		}
	}
	//A map has been loaded, switch to map editing mode
	@Override
	public void startGame() {
		Draw.setScreen(new DrawMapMaker());
		mapMakerMode = true;
	}
	/**Loads a map file with the given name*/
	public static void openMap(String mapName) {
		//Gets file from the level directory 
		File file = new File(Level.getLevelPath() + mapName);
		try (ObjectInputStream fileIn = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)))) {
			//Tries to read the level object from the file
			Level level = (Level) fileIn.readObject();
			//Converts from "game state" (for servers) to "map state" (for editing)
			level.setLevelToMapState();
			//Sets the game level to the loaded level
			setLevel(level);
			//Sets the map name to the name of the level file
			GameMapMaker.mapName = mapName;
			//Successfully loaded the level, change to map editing mode
			getGame().startGame();
		} catch (IOException | ClassNotFoundException e) {
			//Displays error message
			DrawLoadMap.setStateFailed();
		}
	}
	/**Creates a new map
	 * @param mapName The map file's name
	 * @param width Map width in tile units
	 * @param height Map height in tile units
	 */
	public static void newMap(String mapName, short width, short height) {
		//Creates an empty level
		setLevel(new Level());
		//Sets tile array dimensions
		getLevel().createMap(width, height);
		//Default background colour
		getLevel().setColour(Color.GRAY);
		GameMapMaker.mapName = mapName;
		getGame().startGame();
	}
	/**Saves the map and closes the program if there are two spawn points (one for each team) and if there is no problem writing to the directory*/
	public static void saveMap() {
		//Creates a file with mapName in directory where levels are loaded/saved
		File file = new File(Level.getLevelPath() + mapName);
		file.getParentFile().mkdirs();
		try (ObjectOutputStream fileOut = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
			//Sets the level object to the "game state" where it is ready to be loaded by a server
			//If the method returns false, then 1 or 2 spawns are missing
			if (getLevel().setLevelToGameState()) {
				//Writes the level object
				fileOut.writeObject(getLevel());
				fileOut.flush();
				fileOut.close();
				//Map saved, close the program
				Gdx.app.exit();
			}
		} catch (IOException e) {
			System.out.println("Error: Failed to save map");
			e.printStackTrace();
		}
	}
	/**@return The selected tile or item ID*/
	public byte getSelectedID() {
		return selectedID;
	}
	/**@return True if item mode is on, false if tile mode is on*/
	public boolean isItemMode() {
		return itemMode;
	}
	/**Switches between itemMode and tileMode, resets the selected ID to air/remove*/
	public void toggleItemMode() {
		itemMode = !itemMode;
		selectedID = 0;
	}
	/**@return The camera controlled by the user to view a part of the level*/
	public OrthographicCamera getCamera() {
		return camera;
	}
	/**When WASD is pressed*/
	public void keyPressed(int keycode) {
		if (keycode == Keys.W) up = true;
		else if (keycode == Keys.S) down = true;
		else if (keycode == Keys.A) left = true;
		else if (keycode == Keys.D) right = true;
	}
	/**When WASD is released*/
	public void keyReleased(int keycode) {
		if (keycode == Keys.W) up = false;
		else if (keycode == Keys.S) down = false;
		else if (keycode == Keys.A) left = false;
		else if (keycode == Keys.D) right = false;
	}
	public void setSelectedID(byte selectedID) {
		//Switch to default tile (air) if tile mode is on and an invalid tileID is entered
		if (!itemMode && selectedID >= TileConfig.amountOfTiles()) {
			this.selectedID = TileConfig.air.getID();
		} else {
			this.selectedID = selectedID;
		}
	}
	/**Called after the player zooms in or out*/
	public void updateScale() {
		float oldScaledSize = scaledSize;
		//New scaledSize since Draw.getScale() is different now
		scaledSize = Draw.scale(SIZE);
		//Adjusts the camera position so it looks like the camera is zooming into the centre of the screen
		camera.position.x = camera.position.x / oldScaledSize * scaledSize;
		camera.position.y = camera.position.y / oldScaledSize * scaledSize;
	}
	@Override
	protected void dispose() {
		TileConfig.dispose();
		ItemConfig.dispose();
		Button.dispose();
	}
}