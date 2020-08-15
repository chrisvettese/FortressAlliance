package chris.fortress.util;

import chris.fortress.Game;
import chris.fortress.GameMapMaker;
import chris.fortress.item.ItemConfig;
import chris.fortress.tile.TileConfig;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

/**A class that handles Map Maker collision, which is when a tile or item is placed or removed. Ensures that tiles do not overlap,
 * such as small tiles underneath a large tile. Determines which tile or item to remove when ID 0 is selected
 */
public final class CollisionMapMaker {
	private static boolean canPlaceItem = false;
	
	public static boolean tileMouseCollision(int tileX, int tileY, byte selectedID) {	
		Array<int[]> tilesToRemove = new Array<>();
		TileConfig newTile = TileConfig.getTile(selectedID);
		Rectangle newRect = new Rectangle(tileX, tileY, newTile.getTWidth(), newTile.getTHeight());
		
		for (int i = tileX - 1; i <= tileX + 1; i++) {
			for (int j = tileY - 1; j <= tileY + 1; j++) {
				if (i >= 0 && i < Game.getLevel().getMapWidth()
						&& j >= 0 && j < Game.getLevel().getMapHeight() && Game.getLevel().getTileAt(i, j) != TileConfig.air.getID()) {
					
					TileConfig oldTile = TileConfig.getTile(Game.getLevel().getTileAt(i, j));
					Rectangle oldRect = new Rectangle(i, j, oldTile.getTWidth(), oldTile.getTHeight());
					if (newRect.overlaps(oldRect)) {
						if (Game.getLevel().getTileAt(i, j) == selectedID) return true;
						tilesToRemove.add(new int[] {i, j});
					}
				}
			}
		}
		for (int i = 0; i < tilesToRemove.size; i++) {
			Game.getLevel().setTileAt(tilesToRemove.get(i)[0], tilesToRemove.get(i)[1], TileConfig.air.getID());
		}
		Game.getLevel().setTileAt(tileX, tileY, selectedID);
		return false;
	}
	
	public static void placeItem(short mouseX, short mouseY) {
		byte selectedID = (byte) (((GameMapMaker) Game.getGame()).getSelectedID() - 1);
		if (canPlaceItem && (selectedID < 0 || selectedID >= ItemConfig.amountOfItems())) {
			canPlaceItem = false;
			//Starting from top of the item list will remove items that have been added more recently first
			for (int i = Game.getLevel().getItemCount() - 1; i >= 0; i--) {
				short[] item = Game.getLevel().getItemAt(i);
				ItemConfig itemConfig = ItemConfig.getItem(item[2]);
				//If the mouse clicks on an item
				Rectangle itemRect = new Rectangle(item[0], item[1], itemConfig.getImage(0).getWidth(), itemConfig.getImage(0).getHeight());
				if (itemRect.contains(mouseX, mouseY)) {
					Game.getLevel().removeItem(i, false);
					break;
				}
			}
		}
		else if (canPlaceItem) {
			ItemConfig item = ItemConfig.getItem(selectedID);
			Game.getLevel().addItem((short) (mouseX - item.getImage(0).getWidth() / 2), (short) (mouseY - item.getImage(0).getHeight() / 2), selectedID);
		}
	}

	public static void allowPlaceItem() {
		canPlaceItem = true;
	}
}