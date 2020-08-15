package chris.fortress.entity.projectile;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;

/**The ProjectileConfig used to create blood particles*/
public class ProjectileBlood extends ProjectileConfig {
	protected ProjectileBlood(String imageName, Color colour, boolean collidesWithPlayers, boolean collidesWithTiles, float radius, int timerValue, float gravity, short damage) {
		super(imageName, colour, collidesWithPlayers, collidesWithTiles, radius, timerValue, gravity, damage);
	}
	@Override
	protected float getRadius() {
		return super.getRadius() + 3.5f * MathUtils.random();
	}
	@Override
	public float getXDir() {
		return -100 + 200 * MathUtils.random();
	}
	@Override
	public float getYDir() {
		return -100 + 700 * MathUtils.random();
	}
}