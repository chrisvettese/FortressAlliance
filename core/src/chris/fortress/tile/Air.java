package chris.fortress.tile;

import chris.fortress.entity.Entity;
import chris.fortress.entity.player.Player;

public class Air extends TileConfig {
	protected Air(String fileName) {
		super(fileName);
		setNonSolid();
		enableMultiplePreCollisions();
	}
	@Override
	public void preCollision(Player p, int tX, int tY) {
		p.setYDir(p.getYDir() + Player.GRAVITY);
		if (p.getYDir() < -Entity.MAX_SPEED) p.setYDir(-Entity.MAX_SPEED);
	}
}