package chris.fortress.tile;

import chris.fortress.Game;
import chris.fortress.GameServer;
import chris.fortress.entity.Entity;
import chris.fortress.entity.player.Player;
import chris.fortress.util.Collision;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;

public class RightRamp extends TileConfig {
	protected RightRamp(String fileName) {
		super(fileName);
	}
	@Override
	protected void loadImage(boolean isMapMaker) {
		Sprite flippedRamp = new Sprite(TileConfig.leftStoneRamp.getImage(0));
		flippedRamp.flip(true, false);
		if (isMapMaker) {
			Sprite zoomedFlippedRamp = new Sprite(TileConfig.leftStoneRamp.getImage(1));
			zoomedFlippedRamp.flip(true, false);
			setImages(new Sprite[] {flippedRamp, zoomedFlippedRamp});
		} else {
			setImages(new Sprite[] {flippedRamp});
		}
	}
	@Override
	public void preCollision(Player p, int tX, int tY) {
		//See if the player is standing on the ramp
		float rampPosition = (p.getX() + Player.WIDTH - GameServer.getOldXDir()) % Game.SIZE - p.getY() % Game.SIZE;
		rampPosition = (int) (rampPosition * 100) / 100f;
		if ((rampPosition == 0 || rampPosition == Game.SIZE || p.getX() + Player.WIDTH >= tX * Game.SIZE + Game.SIZE) && p.getY() <= tY * Game.SIZE + Game.SIZE) {
			if (p.jump()) {
				p.setYDir(Player.JUMP_HEIGHT);
			}
		} else {
			p.setYDir(p.getYDir() + Player.GRAVITY);
			if (p.getYDir() < -Entity.MAX_SPEED) p.setYDir(-Entity.MAX_SPEED);
		}
	}
	@Override
	public byte postCollision(Player p, int tX, int tY, float yDir) {
		int tileX = tX * Game.SIZE;
		int tileY = tY * Game.SIZE;
		float playerRight = p.getX() + Player.WIDTH;
		int tileRight = tileX + getWidth();
		
		if (playerRight >= tileX && playerRight < tileRight && p.getY() >= tileY) {
			Collision.stopStepping();
			p.setY(tileY + playerRight - tileX);
			if (p.getYDir() != Player.JUMP_HEIGHT) {
				p.setYDir(0);
			}
			return Collision.Y_COL;
		}
		else if (playerRight >= tileRight && p.getX() < tileRight && p.getY() >= tileY) {
			Collision.stopStepping();
			p.setY(tileY + getHeight());
			if (p.getYDir() != Player.JUMP_HEIGHT) {
				p.setYDir(0);
			}
			return Collision.Y_COL;
		}
		else if (p.getX() >= tileRight) {
			Collision.stopStepping();
			p.setXDir(0);
			p.setX(tileRight);
			return Collision.X_COL;
		}
		else if (playerRight <= tileX) {
			Collision.stopStepping();
			p.setXDir(0);
			p.setX(tileX - Player.WIDTH);
			return Collision.X_COL;
		}
		else if (p.getY() + Player.HEIGHT <= tileY) {
			p.setYDir(0);
			p.setY(tileY - Player.HEIGHT);
			return Collision.Y_COL;
		}
		return Collision.NO_COL;
	}
	@Override
	public boolean overlapsRectangle(Rectangle pRect, int tX, int tY) {
		int tileX = tX * Game.SIZE;
		int tileY = tY * Game.SIZE;
		if (pRect.x + Player.WIDTH >= tileX + Game.SIZE) {
			return pRect.x <= tileX + Game.SIZE && pRect.y + Player.HEIGHT >= tileY && pRect.y < tileY + Game.SIZE;
		}
		return (pRect.x + Player.WIDTH) > tileX && pRect.y + Player.HEIGHT >= tileY && pRect.y < tileY + (pRect.x + Player.WIDTH) - tileX;
	}
	@Override
	public boolean overlapsPolygon(Polygon polygon, int tX, int tY) {
		int tileX = tX * Game.SIZE;
		int tileY = tY * Game.SIZE;
		Polygon rampBounds = new Polygon(new float[] {tileX, tileY, tileX + Game.SIZE, tileY + Game.SIZE, tileX, tileY + Game.SIZE});
		return Intersector.overlapConvexPolygons(rampBounds, polygon);
	}
	@Override
	public boolean containsPoint(int tX, int tY, float x, float y) {
		int tileX = tX * Game.SIZE;
		int tileY = tY * Game.SIZE;
		return x >= tileX && x < tileX + getWidth() && y >= tileY && y < tileY + (x - tileX);
	}
}