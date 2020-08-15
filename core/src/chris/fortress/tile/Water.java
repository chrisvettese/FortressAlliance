package chris.fortress.tile;

import chris.fortress.entity.Entity;
import chris.fortress.entity.player.Player;
import chris.fortress.util.Collision;

public class Water extends TileConfig {
	protected Water(String fileName) {
		super(fileName);
	}
	@Override
	public void preCollision(Player p, int tX, int tY) {
		if (p.jump() && Collision.getLiquidState() == Collision.FULL_LIQUID) {
			p.setYDir(Player.SWIM_UP_SPEED);
		} else {
			p.setYDir(p.getYDir() + Player.GRAVITY);
			if (p.getYDir() < -Entity.MAX_SPEED) p.setYDir(-Entity.MAX_SPEED);
		}
	}
	@Override
	public byte postCollision(Player p, int tX, int tY, float yDir) {
		p.setXDir(p.getXDir() / 3);
		p.setYDir(p.getYDir() / 2);
		return Collision.NO_COL;
	}
}