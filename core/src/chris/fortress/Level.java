package chris.fortress;

import chris.fortress.entity.player.PlayerSocket;
import chris.fortress.socket.SendMessage;
import chris.fortress.tile.TileConfig;
import com.badlogic.gdx.graphics.Color;

import java.io.File;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**A class that stores all the data about the level: the tile map, items, spawn points, and background colour*/
public class Level implements Serializable {
	private static final long serialVersionUID = 7810919608369150306L;
	
	/**The folder inside the user folder, where levels are loaded from and saved to*/
	private static String LEVEL_PATH = null;

	static {
		try {
			LEVEL_PATH = new File(Level.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
			LEVEL_PATH = LEVEL_PATH.substring(0, LEVEL_PATH.lastIndexOf("\\") + 1);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	/**The game's background colour*/
	private static Color backgroundColour;
	
	/**The red team's spawn point*/
	private short[] redSpawn;
	/**The blue team's spawn point*/
	private short[] blueSpawn;
	/**The tile map*/
	private byte[][] map;
	/**The positions and IDs of the items in the game (index 0 = x, 1 = y, 2 = itemID)*/
	private List<short[]> items = new ArrayList<>();
	/**The background colour, stored as a float (RGB values between 0 and 1)*/
	private float[] backgroundColourValues;
	
	/**Creates a level with an empty map*/
	public Level() {
		//Creates empty map so there's no null pointer exceptions when a player leaves a server
		map = new byte[1][1];
	}
	
	/**Converts the spawn tiles into coordinates for the red team's spawn and blue team's spawn. Sets the static backgroundColour to the saved values.
	  * @return True if there were 2 spawn points set (1 for each team)
	  */
	public boolean setLevelToGameState() {
		for (int j = 0; j < map.length; j++) {
			for (int i = 0; i < map[j].length; i++) {
				//If a spawn tile is placed here
				if (map[j][i] == TileConfig.spawn.getID()) {
					//Set the red spawn, if it hasn't been set yet
					if (redSpawn == null) {
						//Multiply i and j by Game.SIZE, to convert from tile coordinates to game coordinates
						redSpawn = new short[] {(short) (i * Game.SIZE), (short) (j * Game.SIZE)};
					//Set the blue spawn for the second spawn point found
					} else {
						blueSpawn = new short[] {(short) (i * Game.SIZE), (short) (j * Game.SIZE)};
					}
					//Replace the spawn tile with the tile ID above it, since the spawns are now saved as coordinates*/
					if (j + 1 < map.length) {
						map[j][i] = map[j + 1][i];
					} else {
						//If there is no tile above the spawn, set the spawn tile to air*/
						map[j][i] = TileConfig.air.getID();
					}
				}
			}
		}
		if (redSpawn != null && blueSpawn != null) {
			return true;
		} else {
			//Can't save, revert the level back to the map editing state
			setLevelToMapState();
			return false;
		}
	}
	/**Converts from spawn coordinates to spawn tiles and initialises the background colour. The level is now ready to be edited*/
	public void setLevelToMapState() {
		//Sets the red team spawn tile, if red spawn coordinates exist
		if (redSpawn != null) {
			redSpawn[0] /= Game.SIZE;
			redSpawn[1] /= Game.SIZE;
			map[redSpawn[1]][redSpawn[0]] = TileConfig.spawn.getID();
			redSpawn = null;
		}
		//Sets the blue team spawn tile, if blue spawn coordinates exist
		if (blueSpawn != null) {
			blueSpawn[0] /= Game.SIZE;
			blueSpawn[1] /= Game.SIZE;
			map[blueSpawn[1]][blueSpawn[0]] = TileConfig.spawn.getID();
			blueSpawn = null;
		}
		//Sets background colour from backgroundColourValues
		backgroundColour = new Color(backgroundColourValues[0], backgroundColourValues[1], backgroundColourValues[2], 1);
	}
	/**@return The game coordinates of the given team's spawn*/
	public short[] getSpawn(boolean team) {
		return team == Game.RED ? redSpawn : blueSpawn;
	}
	/**@return The level width, in tile units*/
	public int getMapWidth() {
		return map[0].length;
	}
	/**@return The level height, in tile units*/
	public int getMapHeight() {
		return map.length;
	}
	/**Creates a 2D array for the map
	 * @param width The map's width, in tile units
	 * @param height The map's height, in tile units
	 */
	public void createMap(int width, int height) {
		map = new byte[height][width];
	}
	/**Sets the tile at the given position in tile units to tileID*/
	public void setTileAt(int x, int y, byte tileID) {
		map[y][x] = tileID;
	}
	/**Returns the tileID at the given location (in tile units)*/
	public byte getTileAt(int x, int y) {
		return map[y][x];
	}
	/**Sets the background colour of this level*/
	public void setColour(Color backgroundColour) {
		backgroundColourValues = new float[] {backgroundColour.r, backgroundColour.g, backgroundColour.b};
		Level.backgroundColour = backgroundColour;
	}
	/**@return The level's background colour*/
	public static Color getColour() {
		return backgroundColour;
	}
	/**Adds an item at the given location, in game coordinates*/
	public void addItem(short x, short y, byte itemID) {
		items.add(new short[] {x, y, itemID});
	}
	/**Removes an item from the item list
	 * @param itemIndex The item index in the item list
	 * @param serverSide If the clients should be updated about the removed item
	 */
	public void removeItem(int itemIndex, boolean serverSide) {
		items.remove(itemIndex);
		if (serverSide) {
			SendMessage.sendRemoveItemToClients((short) itemIndex);
		}
	}
	/**@return The item at the given index (index [0] = x, [1] = y, [2] = itemID)*/
	public short[] getItemAt(int itemIndex) {
		return items.get(itemIndex);
	}
	/**@return The number of items in the level*/
	public int getItemCount() {
		return items.size();
	}
	/**@return The folder where levels are loaded from and saved to*/
	public static String getLevelPath() {
		return LEVEL_PATH;
	}
	/**Sends map into to the given player so the level on the client side can be initialized*/
	public void sendMapInfo(PlayerSocket playerSocket) {
		SendMessage.sendMapInfoToClient(backgroundColourValues[0], backgroundColourValues[1], backgroundColourValues[2],
				(short) getMapWidth(), (short) getMapHeight(), playerSocket);
	}
}