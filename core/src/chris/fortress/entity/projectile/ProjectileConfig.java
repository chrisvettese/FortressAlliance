package chris.fortress.entity.projectile;

import chris.fortress.util.Resource;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.Array;

/**The base class for various projectile configurations. ProjectileConfig defines the characteristics of a projectile, such as its radius, colour,
 * gravity, and speed (if applicable)
 */
public abstract class ProjectileConfig {
	private static final Array<ProjectileConfig> projectileConfigs = new Array<>();
	
	private Sprite sprite;
	private String imageName;
	private Color colour;
	private boolean collidesWithPlayers, collidesWithTiles, isImage;
	private float radius;
	private float gravity;
	private short damage;
	private byte projID;
	/**If negative, the projectile disappears after |timer| milliseconds.
	If 0, the projectile disappears instantly on impact.
	If positive, the projectile disappears |timer| milliseconds after impact*/
	private int timerValue;
	
	public static final ProjectileConfig bloodParticle = new ProjectileBlood(null, Color.RED, false, true, 2.5f, 2000, -820, (short) 0);
	public static final ProjectileConfig impactParticle = new ProjectileImpact(null, null, false, false, 0.5f, -100, 0, (short) 0);
	public static final ProjectileConfig gunshot = new ProjectileGunshot("projectile.png", null, true, true, 0, 0, 0, (short) 30);
	/**
	 * Creates a new projectile configuration
	 * @param imageName The projectile's image (if applicable)
	 * @param colour If the projectile is just a circle, what colour it is
	 * @param collidesWithPlayers If the projectile collides with players
	 * @param collidesWithTiles If the projectile collides with tiles
	 * @param radius If the projectile is a circle, the radius of it
	 * @param timerValue If negative, the projectile disappears after |timer| milliseconds. If 0, the projectile disappears instantly on impact. If positive, the projectile disappears |timer| milliseconds after impact.
	 * @param gravity How much the projectile is affected by gravity
	 */
	protected ProjectileConfig(String imageName, Color colour, boolean collidesWithPlayers, boolean collidesWithTiles, float radius, int timerValue, float gravity, short damage) {
		projID = (byte) projectileConfigs.size;
		projectileConfigs.add(this);
		if (imageName == null) {
			isImage = false;
			this.colour = colour;
		} else {
			isImage = true;
			this.imageName = imageName;
		}
		this.collidesWithPlayers = collidesWithPlayers;
		this.collidesWithTiles = collidesWithTiles;
		this.damage = damage;
		if (radius < 0.5f) radius = 0.5f;
		this.radius = radius;
		this.timerValue = timerValue;
		this.gravity = gravity;
	}
	public static void loadImages() {
		for (ProjectileConfig p : projectileConfigs) {
			if (p.isImage()) {
				p.loadImage();
			}
		}
	}
	private void loadImage() {
		sprite = new Sprite(new Texture(Resource.getFile(imageName)));
		sprite.setOriginCenter();
	}
	protected float getRadius() {
		return radius;
	}
	protected float getGravity() {
		return gravity;
	}
	public short getDamage() {
		return damage;
	}
	public boolean doesCollideWithPlayers() {
		return collidesWithPlayers;
	}
	public boolean doesCollideWithTiles() {
		return collidesWithTiles;
	}
	protected int getTimerValue() {
		return timerValue;
	}
	public byte getID() {
		return projID;
	}
	protected Color getColour() {
		return colour;
	}
	public abstract float getXDir();
	public abstract float getYDir();
	public void initialize() {
		
	}
	public static ProjectileConfig getProjectile(byte projID) {
		return projectileConfigs.get(projID);
	}
	protected Sprite getImage() {
		return sprite;
	}
	public boolean isImage() {
		return isImage;
	}
	/**Returns the projectile's width, if the projectile is an image*/
	public int getWidth() {
		return 0;
	}
	/**Returns the projectile's height, if the projectile is an image*/
	public int getHeight() {
		return 0;
	}
	public static void dispose() {
		for (ProjectileConfig p : projectileConfigs) {
			if (p.getImage() != null) {
				p.getImage().getTexture().dispose();
			}
		}
	}
}