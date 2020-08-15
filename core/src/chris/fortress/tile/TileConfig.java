package chris.fortress.tile;

import chris.fortress.Game;
import chris.fortress.draw.Draw;
import chris.fortress.entity.player.Player;
import chris.fortress.util.Collision;
import chris.fortress.util.Resource;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

/**The base class for tile configurations. TileConfig defines the characteristics of each tileID, such as the sprite(s) and size of the tile.
 * Many TileConfigs use this class directly since creating their own classes is not necessary
 */
public class TileConfig {
	private boolean visible = true;
	private boolean solid = true;
	private boolean liquid = false;
	private boolean singlePreCollision = true;
	
	private Sprite[] images;
	private String fileName;
	private byte id;
	/**Width of the tile in game units*/
	private int tileWidth = 1;
	/**Height of the tile in game units*/
	private int tileHeight = 1;
	private int width = Game.SIZE;
	private int height = Game.SIZE;
	
	private static final Array<TileConfig> tiles = new Array<>();

	public static final TileConfig air = new Air(null).setInvisible();
	public static final TileConfig border = new TileConfig("border.png");
	public static final TileConfig largeBorder = new TileConfig("border_large.png").setWidthHeight(Game.SIZE * 2, Game.SIZE * 2);
	public static final TileConfig platform = new Platform("platform.png");
	public static final TileConfig stone = new TileConfig("stone.png");
	public static final TileConfig stoneBack = new Air("stone_back.png");
	public static final TileConfig stoneBackPlatform = new Platform(null);
	public static final TileConfig ground = new TileConfig("ground.png");
	
	public static final TileConfig spawn = new TileConfig("spawn.png");
	
	public static final TileConfig water = new Water("water.png").enableMultiplePreCollisions().setLiquid();
	public static final TileConfig lava = new Lava("lava.png").enableMultiplePreCollisions().setLiquid();
	public static final TileConfig leftStoneRamp = new LeftRamp("stone_ramp.png").enableMultiplePreCollisions();
	public static final TileConfig rightStoneRamp = new RightRamp(null).enableMultiplePreCollisions();
	
	protected TileConfig(String fileName) {
		id = (byte) tiles.size;
		this.fileName = fileName;
		tiles.add(this);
	}
	protected TileConfig setWidthHeight(int width, int height) {
		this.width = width;
		this.height = height;
		this.tileWidth = width / Game.SIZE;
		this.tileHeight = height / Game.SIZE;
		return this;
	}
	public int getWidth() {
		return width;
	}
	public int getHeight() {
		return height;
	}
	public final boolean isSolid() {
		return solid;
	}
	protected TileConfig setNonSolid() {
		solid = false;
		return this;
	}
	public final boolean isLiquid() {
		return liquid;
	}
	private TileConfig setLiquid() {
		liquid = true;
		solid = false;
		return this;
	}
	/**true: only check preCollision on this tile if it is found to be below the player
	 * false: allow preCollision for multiple tiles, unless another tile below the player has singlePreCollision = true*/
	public final boolean singlePreCollision() {
		return singlePreCollision;
	}
	protected TileConfig enableMultiplePreCollisions() {
		singlePreCollision = false;
		return this;
	}
	protected TileConfig setInvisible() {
		visible = false;
		return this;
	}
	protected String getFileName() {
		return fileName;
	}
	public Sprite getImage(int imageIndex) {
		return images[imageIndex];
	}
	public int imageCount() {
		return images.length;
	}
	/**Adjusts the player's movement if this tile is below the player (for example, gravity or jumping).
	 * Default is to have the player jump if the player is on the ground and is pressing the jump key.*/
	public void preCollision(Player p, int tX, int tY) {
		if (p.jump() && Collision.playerOnGround(p)) {
			p.setYDir(Player.JUMP_HEIGHT);
		}
	}
	/**
	 * Returns true if the collision algorithm should end
	 * @param p The player that is colliding
	 * @param tY The tile's y index in the map array
	 * @param tX The tile's x index in the map array
	 * @param yDir The entity's yDir
	 */
	public byte postCollision(Player p, int tX, int tY, float yDir) {
		int tileX = tX * Game.SIZE;
		int tileY = tY * Game.SIZE;

		if (p.getX() >= tileX + getWidth()) {
			p.setXDir(0);
			p.setX(tileX + getWidth());
			return Collision.X_COL;
		}
		else if (p.getX() + Player.WIDTH <= tileX) {
			p.setXDir(0);
			p.setX(tileX - Player.WIDTH);
			return Collision.X_COL;
		}
		if (p.getY() >= tileY + getHeight()) {
			p.setYDir(0);
			p.setY(tileY + getHeight());
			return Collision.Y_COL;
		}
		else if (p.getY() + Player.HEIGHT <= tileY) {
			p.setYDir(0);
			p.setY(tileY - Player.HEIGHT);
			return Collision.Y_COL;
		}
		return Collision.FULL_COL;
	}
	public static void loadImages(boolean isMapMaker) {
		for (TileConfig t : tiles) {
			t.loadImage(isMapMaker);
		}
	}
	protected void loadImage(boolean isMapMaker) {
		if (visible) {
			Sprite s = new Sprite(new Texture(Resource.getFile(fileName)));
			s.setOrigin(0, 0);
			if (isMapMaker) s.setScale(Draw.getScale());
			if (isMapMaker) {
				images = new Sprite[] {s, new Sprite(s)};
			} else {
				images = new Sprite[] {s};
			}
		}
	}
	/**Updates the scales of every tile image. Only affects the second half of the items in each array*/
	public static void rescaleImages() {
		for (TileConfig t : tiles) {
			if (t.isVisible()) {
				for (int i = t.images.length / 2; i < t.images.length; i++) {
					t.images[i].setScale(Draw.getScale());
				}
			}
		}
	}
	public static TileConfig getTile(int id) {
		return tiles.get(id);
	}
	public final byte getID() {
		return id;
	}
	public final boolean isVisible() {
		return visible;
	}
	public static void dispose() {
		for (TileConfig t : tiles) {
			if (t.images != null) {
				for (Sprite s : t.images) {
					s.getTexture().dispose();
				}
			}
		}
	}
	public int getTWidth() {
		return tileWidth;
	}
	public int getTHeight() {
		return tileHeight;
	}
	protected void setImages(Sprite[] images) {
		this.images = images;
	}
	public static int amountOfTiles() {
		return tiles.size;
	}
	public boolean overlapsRectangle(Rectangle pRect, int tX, int tY) {
		int tileX = tX * Game.SIZE;
		int tileY = tY * Game.SIZE;
		return pRect.x + pRect.width > tileX && pRect.x < tileX + width && pRect.y + pRect.height > tileY && pRect.y < tileY + height;
	}
	public boolean overlapsPolygon(Polygon polygon, int tX, int tY) {
		Polygon tilePolygon = new Polygon();
		int tileX = tX * Game.SIZE;
		int tileY = tY * Game.SIZE;
		tilePolygon.setVertices(new float[] {tileX, tileY, tileX, tileY + getHeight(), tileX + getWidth(), tileY + getHeight(), tileX + getWidth(), tileY});
		return Intersector.overlapConvexPolygons(polygon, tilePolygon);
	}
	public boolean containsPoint(int tX, int tY, float x, float y) {
		Rectangle tileRect = new Rectangle(tX * Game.SIZE, tY * Game.SIZE, getWidth(), getHeight());
		return tileRect.contains(x, y);
	}
}