package chris.fortress.tile;

import chris.fortress.Game;
import chris.fortress.GameServer;
import chris.fortress.entity.Entity;
import chris.fortress.entity.player.Player;
import chris.fortress.util.Collision;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;

public class LeftRamp extends TileConfig {
	protected LeftRamp(String fileName) {
		super(fileName);
	}
	@Override
	public void preCollision(Player p, int tX, int tY) {
		int tileY = tY * Game.SIZE;
		//Check if the player is standing on the ramp
		float rampPosition = Game.SIZE - (p.getX() - GameServer.getOldXDir()) % Game.SIZE - p.getY() % Game.SIZE;
		rampPosition = (int) (rampPosition * 100) / 100f;
		if ((rampPosition == 0 || rampPosition == Game.SIZE || p.getX() < tX * Game.SIZE)
				&& p.getY() <= tileY + Game.SIZE) {
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
		
		if (p.getX() <= tileRight && p.getX() > tileX && p.getY() >= tileY) {
			Collision.stopStepping();
			p.setY(tileY - p.getX() + tileRight);
			if (p.getYDir() != Player.JUMP_HEIGHT) {
				p.setYDir(0);
			}
			return Collision.Y_COL;
		}
		else if (p.getX() <= tileX && playerRight > tileX && p.getY() >= tileY) {
			Collision.stopStepping();
			p.setY(tileY + getHeight());
			if (p.getYDir() != Player.JUMP_HEIGHT) {
				p.setYDir(0);
			}
			return Collision.Y_COL;
		}
		else if (playerRight <= tileX) {
			Collision.stopStepping();
			p.setXDir(0);
			p.setX(tileX - Player.WIDTH);
			return Collision.X_COL;
		}
		else if (p.getX() >= tileRight) {
			Collision.stopStepping();
			p.setXDir(0);
			p.setX(tileRight);
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
		if (pRect.x < tileX) {
			return pRect.x + Player.WIDTH >= tileX && pRect.y + Player.HEIGHT >= tileY && pRect.y < tileY + Game.SIZE;
		}
		return pRect.x < tileX + getWidth() && pRect.y + Player.HEIGHT >= tileY && pRect.y < tileY + Game.SIZE - (pRect.x - tileX);
	}
	@Override
	public boolean overlapsPolygon(Polygon polygon, int tX, int tY) {
		int tileX = tX * Game.SIZE;
		int tileY = tY * Game.SIZE;
		Polygon rampBounds = new Polygon(new float[] {tileX, tileY, tileX + Game.SIZE, tileY, tileX, tileY + Game.SIZE});
		return Intersector.overlapConvexPolygons(rampBounds, polygon);
	}
	@Override
	public boolean containsPoint(int tX, int tY, float x, float y) {
		int tileX = tX * Game.SIZE;
		int tileY = tY * Game.SIZE;
		return x >= tileX && x < tileX + getWidth() && y >= tileY && y < tileY + Game.SIZE - (x - tileX);
	}
}