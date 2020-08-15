package chris.fortress.item;

import chris.fortress.entity.player.Player;
import chris.fortress.entity.player.PlayerHandler;
import chris.fortress.entity.projectile.ProjectileConfig;
import chris.fortress.entity.projectile.ProjectileHandler;

/**The gun ItemConfig which defines how guns behave*/
public class ItemGun extends ItemConfig {
	private static final float SPEED_ADJUST = 20;
	private static final float DISTANCE_STRETCH = 1.3f;
	protected static final float STARTING_RADIUS = 30;
	protected static final float GUN_HEIGHT_ADJUST = 0.65f;
	
	protected ItemGun(String imageName, boolean repeatable, boolean updateMouse, int timeGap) {
		super(imageName, repeatable, updateMouse, timeGap);
	}
	@Override
	public ItemAnimator createAnimator(Player p) {
		return new ItemAnimatorGun(p);
	}
	@Override
	public void use(int cI, short mX, short mY) {
		Player p = PlayerHandler.getPlayer(cI);
		float pCentreX = p.getX() + Player.WIDTH / 2;
		float pCentreY = p.getY() + Player.HEIGHT * GUN_HEIGHT_ADJUST;
		float mouseDistanceX = pCentreX - mX;
		float mouseDistanceY = pCentreY - mY;
		double angle = 0;
		if (mouseDistanceX != 0) {
			angle = Math.atan(mouseDistanceY / mouseDistanceX);
		}
		float xChange = (float) (STARTING_RADIUS * Math.cos(angle));
		float yChange = (float) (STARTING_RADIUS * Math.sin(angle));
		if (mX < pCentreX) {
			yChange = -yChange;
			xChange = -xChange;
		}
		ProjectileHandler.addUpdateProjectile(pCentreX + xChange * DISTANCE_STRETCH - ProjectileConfig.gunshot.getWidth() / 2,
				pCentreY + yChange * DISTANCE_STRETCH - ProjectileConfig.gunshot.getHeight() / 2,
				xChange * SPEED_ADJUST, yChange * SPEED_ADJUST,
				PlayerHandler.getPlayer(cI).getTeam(), ProjectileConfig.gunshot.getID());
	}
}