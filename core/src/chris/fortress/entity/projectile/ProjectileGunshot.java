package chris.fortress.entity.projectile;

import com.badlogic.gdx.graphics.Color;

/**The ProjectileConfig for gunshots fired by a player*/
public class ProjectileGunshot extends ProjectileConfig {
	protected ProjectileGunshot(String imageName, Color colour, boolean collidesWithPlayers, boolean collidesWithTiles, float radius, int timerValue, float gravity, short damage) {
		super(imageName, colour, collidesWithPlayers, collidesWithTiles, radius, timerValue, gravity, damage);
	}
	@Override
	public float getXDir() {
		return 0;
	}
	@Override
	public float getYDir() {
		return 0;
	}
	@Override
	public int getWidth() {
		return 18;
	}
	@Override
	public int getHeight() {
		return 4;
	}
}