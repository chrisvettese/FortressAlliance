package chris.fortress.tile;

import chris.fortress.Game;
import chris.fortress.GameServer;
import chris.fortress.entity.Entity;
import chris.fortress.entity.player.Player;
import chris.fortress.socket.SendMessage;
import chris.fortress.util.Collision;
import com.badlogic.gdx.Gdx;

public class Lava extends TileConfig {
	private static final float DAMAGE = Player.MAX_HEALTH / 2;
	
	protected Lava(String fileName) {
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
		//Player can be hurt by lava even if recently damaged by another player, so don't use p.hurt()
		p.setHealth(p.getHealth() - DAMAGE * Gdx.graphics.getDeltaTime());
		SendMessage.sendHealthToClients(p.getClientID(), p.getHealth());
		if (p.getHealth() <= 0) {
			((GameServer) Game.getGame()).setScore(!p.getTeam(), (short) (((GameServer) Game.getGame()).getScore(!p.getTeam()) + 1));
		}
		return Collision.NO_COL;
	}
}