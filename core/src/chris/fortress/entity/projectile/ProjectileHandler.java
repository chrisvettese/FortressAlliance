package chris.fortress.entity.projectile;

import chris.fortress.Game;
import chris.fortress.entity.Entity;
import chris.fortress.entity.player.PlayerHandler;
import chris.fortress.socket.SendMessage;
import chris.fortress.util.Collision;
import chris.fortress.util.Resource;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;

/**A class used by both the server and client to manage the projectiles in the game*/
public final class ProjectileHandler {
	private static final short LIMIT = 2000;
	
	private static final Array<Projectile> projectiles = new Array<>();
	
	/**Add a projectile (client side method)*/
	public static void addProjectile(float x, float y, float xDir, float yDir, boolean team, byte projID) {
		projectiles.add(new Projectile(x, y, xDir, yDir, team, projID));
	}
	/**For adding a projectile on the server side - add the projectile and update every player about it*/
	public static void addUpdateProjectile(float x, float y, float xDir, float yDir, boolean team, byte projID) {
		addProjectile(x, y, xDir, yDir, team, projID);
		SendMessage.sendProjectileToClients((short) x, (short) y, xDir, yDir, projID);
	}
	public static void removeProjectile(int pIndex) {
		projectiles.removeIndex(pIndex);
	}
	public static Projectile getProjectile(int pIndex) {
		return projectiles.get(pIndex);
	}
	public static int projectileCount() {
		return projectiles.size;
	}
	public static void updateProjectiles() {
		for (int i = projectiles.size - 1; i >= 0; i--) {
			Projectile p = projectiles.get(i);
			p.updateYDir();
			float[] dir = new float[] {p.getXDir() * Gdx.graphics.getDeltaTime(), p.getYDir() * Gdx.graphics.getDeltaTime()};
			//Max speed for projectiles so they don't travel through tiles
			if (dir[0] > Entity.MAX_SPEED) dir[0] = Entity.MAX_SPEED;
			else if (dir[0] < -Entity.MAX_SPEED) dir[0] = -Entity.MAX_SPEED;
			if (dir[1] > Entity.MAX_SPEED) dir[1] = Entity.MAX_SPEED;
			else if (dir[1] < -Entity.MAX_SPEED) dir[1] = -Entity.MAX_SPEED;
			
			if ((ProjectileConfig.getProjectile(p.getID()).doesCollideWithTiles() && Collision.tileProjectileCollide(p, dir)) ||
					p.getX() < -LIMIT || p.getX() > Game.getLevel().getMapWidth() * Game.SIZE + LIMIT ||
					p.getY() < -LIMIT || p.getY() > Game.getLevel().getMapHeight() * Game.SIZE + LIMIT) {
				p.setInactive();
			} else {
				p.update(dir[0], dir[1]);
			}
			if (p.isDead()) {
				//Create impact particles
				if (p.getID() == ProjectileConfig.gunshot.getID()) {
					addImpactParticles(p);
				}
				projectiles.removeIndex(i);
			}
		}
	}
	/**Creates blood particles on player death (client side only)*/
	public static void playerDeath(int clientIndex) {
		synchronized (projectiles) {
			for (int i = 0; i < 40 + Resource.getRandom().nextInt(15); i++) {
				projectiles.add(PlayerHandler.getPlayer(clientIndex).getAnimator().createParticle());
			}
		}
	}
	public static void addImpactParticles(Projectile p) {
		for (int j = 0; j < 30; j++) {
			projectiles.add(new Projectile(p.getX(), p.getY(),
					ProjectileConfig.impactParticle.getXDir(), ProjectileConfig.impactParticle.getYDir(),
					p.getTeam(), ProjectileConfig.impactParticle.getID()));
		}
	}
}