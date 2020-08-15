package chris.fortress.entity.projectile;

import chris.fortress.util.Resource;
import com.badlogic.gdx.graphics.Color;

/**The ProjectileConfig class used to create impact particles when a gunshot hits something*/
public class ProjectileImpact extends ProjectileConfig {
	protected ProjectileImpact(String imageName, Color colour, boolean collidesWithPlayers, boolean collidesWithTiles, float radius, int timerValue, float gravity, short damage) {
		super(imageName, colour, collidesWithPlayers, collidesWithTiles, radius, timerValue, gravity, damage);
	}
	@Override
	protected Color getColour() {
		return new Color((Resource.getRandom().nextInt(100)) / 255f, (100 + Resource.getRandom().nextInt(100)) / 255f, (200 + Resource.getRandom().nextInt(56)) / 255f, 1);
	}
	@Override
	public float getXDir() {
		return -200 + 400 * (float) Math.random();
	}
	@Override
	public float getYDir() {
		return -200 + 400 * (float) Math.random();
	}
}