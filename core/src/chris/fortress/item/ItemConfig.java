package chris.fortress.item;

import chris.fortress.draw.Draw;
import chris.fortress.entity.player.Player;
import chris.fortress.util.Resource;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.Array;

/**The base class for Item configurations. Contains sprite(s), and other info such as whether the item can be picked up more than once. Creates the
 * correct ItemAnimator when the player picks up an item*/
public abstract class ItemConfig {
	private static final Array<ItemConfig> itemConfigs = new Array<>();
	/**The index of the item sprite that has been flipped horizontally (Client side)*/
	public static final int FLIPPED_INDEX = 1;
	/**The index of the item sprite that has been scaled to the current zoom level (Map Maker program)*/
	public static final int ZOOMED_INDEX = 1;
	
	private Sprite[] images;
	private String imageName;
	/**If the player can pick up this item more than once*/
	private boolean repeatable = false;
	/**True if the holding player needs to continuously update the other players about the mouse position*/
	private boolean updateMouse = false;
	private int timeGap;
	
	public static final ItemConfig sword = new ItemSword("sword.png", false, false, 150);
	public static final ItemConfig gun = new ItemGun("gun.png", false, true, 300);
	
	protected ItemConfig(String imageName, boolean repeatable, boolean updateMouse, int timeGap) {
		this.imageName = imageName;
		this.repeatable = repeatable;
		this.updateMouse = updateMouse;
		this.timeGap = timeGap;
		itemConfigs.add(this);
	}
	public static void loadImages(boolean isMapMaker) {
		for (ItemConfig item : itemConfigs) {
			item.loadImage(isMapMaker);
		}
	}
	/**Changes the scale of every item sprite to the scale from Draw.getScale() (Map Maker program only)*/
	public static void rescaleImages() {
		for (ItemConfig item : itemConfigs) {
			item.images[1].setScale(Draw.getScale());
		}
	}
	public static ItemConfig getItem(int itemID) {
		return itemConfigs.get(itemID);
	}
	public static int amountOfItems() {
		return itemConfigs.size;
	}
	private void loadImage(boolean isMapMaker) {
		Sprite firstImage = new Sprite(new Texture(Resource.getFile(imageName)));
		if (isMapMaker) firstImage.setScale(Draw.getScale());
		firstImage.setOrigin(0, 0);
		images = new Sprite[] {firstImage, new Sprite(firstImage)};
		if (!isMapMaker) {
			images[0].setFlip(true, false);
		}
	}
	public Sprite getImage(int imageIndex) {
		return images[imageIndex];
	}
	public boolean isRepeatable() {
		return repeatable;
	}
	public boolean shouldUpdateMouse() {
		return updateMouse;
	}
	/**
	 * The waiting period between each use of an item (milliseconds)
	 */
	public int getTimeGap() {
		return timeGap;
	}
	public static void dispose() {
		for (ItemConfig item : itemConfigs) {
			item.disposeImage();
		}
	}
	private void disposeImage() {
		if (images != null) {
			for (Sprite s : images) {
				s.getTexture().dispose();
			}
		}
	}
	public abstract ItemAnimator createAnimator(Player p);
	public abstract void use(int cI, short mX, short mY);
}