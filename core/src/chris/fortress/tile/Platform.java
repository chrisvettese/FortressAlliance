package chris.fortress.tile;

import chris.fortress.Game;
import chris.fortress.entity.Entity;
import chris.fortress.entity.player.Player;
import chris.fortress.util.Collision;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Polygon;

/**The platform TileConfig. Requires its own class because of the unique collision detection for it, and because variants that match background tiles
 * use two sprites instead of one.
 */
public class Platform extends TileConfig {
	private static int tileCount = 0;
	
	protected Platform(String fileName) {
		super(fileName);
		enableMultiplePreCollisions();
	}
	@Override
	public void preCollision(Player p, int tileX, int tileY) {
		if (!Collision.playerOnGround(p)) {
			p.setYDir(p.getYDir() + Player.GRAVITY);
			if (p.getYDir() < -Entity.MAX_SPEED) p.setYDir(-Entity.MAX_SPEED);
		}
		else if (p.jump()) {
			p.setYDir(Player.JUMP_HEIGHT);
		}
		else if (p.down()) {
			p.setYDir(Player.GRAVITY);
		}
	}
	@Override
	public byte postCollision(Player p, int tX, int tY, float yDir) {
		boolean tileBelow = isBelow(p, tY, getHeight());
		//If player is falling above the platform, it will collide. If player is falling through the platform, no collision
		if (tileBelow && yDir < Player.GRAVITY) {
			p.setYDir(0);
			p.setY(tY * Game.SIZE + getHeight());
			return Collision.Y_COL;
		}
		return Collision.NO_COL;
	}
	private static boolean isBelow(Player p, int tY, int tileHeight) {
		return tY * Game.SIZE + tileHeight <= p.getY();
	}
	@Override
	public boolean overlapsPolygon(Polygon polygon, int tX, int tY) {
		return false;
	}
	@Override
	public boolean containsPoint(int tX, int tY, float x, float y) {
		return false;
	}
	@Override
	protected void loadImage(boolean isMapMaker) {
		if (tileCount == 1) {
			if (isMapMaker) {
				setImages(new Sprite[] {stoneBack.getImage(0), platform.getImage(0), stoneBack.getImage(1), platform.getImage(1)});
			} else {
				setImages(new Sprite[] {stoneBack.getImage(0), platform.getImage(0)});
			}
			tileCount = 0;
		} else {
			super.loadImage(isMapMaker);
			tileCount = 1;
		}
	}
}