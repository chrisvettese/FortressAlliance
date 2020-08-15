package chris.fortress.entity.projectile;

import chris.fortress.entity.Entity;
import chris.fortress.util.Timer;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

import java.util.concurrent.TimeUnit;

/**The projectile class. Contains the projID with is used to get this projectile's ProjectileConfig,
 * and provides methods for moving and drawing projectiles*/
public class Projectile extends Entity {
	private byte projID;
	private boolean isActive = true, isDead = false;
	private Color colour;
	private float rotation;
	private float radius;
	
	public Projectile(float x, float y, float xDir, float yDir, boolean team, byte projID) {
		super(x, y, xDir, yDir, team);
		
		if (xDir == 0) {
			rotation = 90;
		} else {
			rotation = MathUtils.atan2(yDir, xDir) * MathUtils.radiansToDegrees;
		}
		this.projID = projID;
		if (ProjectileConfig.getProjectile(projID).getTimerValue() < 0) {
			Timer.getScheduledExecutorService().schedule(()->setDead(), -ProjectileConfig.getProjectile(projID).getTimerValue(), TimeUnit.MILLISECONDS);
		}
		if (!ProjectileConfig.getProjectile(projID).isImage()) {
			this.colour = ProjectileConfig.getProjectile(projID).getColour();
			this.radius = ProjectileConfig.getProjectile(projID).getRadius();
		}
	}
	public void updateYDir() {
		setYDir(getYDir() + Gdx.graphics.getDeltaTime() * ProjectileConfig.getProjectile(projID).getGravity());
	}
	public void update(float xDir, float yDir) {
		setX(getX() + xDir);
		setY(getY() + yDir);
	}
	public void drawImage(SpriteBatch batch) {
		ProjectileConfig pConfig = ProjectileConfig.getProjectile(projID);
		if (pConfig.isImage()) {
			pConfig.getImage().setRotation(rotation);
			pConfig.getImage().setPosition(getX(), getY());
			pConfig.getImage().setPosition(getX(), getY());
			pConfig.getImage().draw(batch);
		}
	}
	public void drawShape(ShapeRenderer renderer) {
		if (!ProjectileConfig.getProjectile(projID).isImage()) {
			renderer.setColor(colour);
			renderer.circle(getX(), getY(), radius);
		}
	}
	public void setInactive() {
		if (isActive) {
			isActive = false;
			if (ProjectileConfig.getProjectile(projID).getTimerValue() > 0) {
				Timer.getScheduledExecutorService().schedule(()->setDead(), ProjectileConfig.getProjectile(projID).getTimerValue(), TimeUnit.MILLISECONDS);
			} else {
				setDead();
			}
		}
	}
	private void setDead() {
		isDead = true;
	}
	public boolean isActive() {
		return isActive;
	}
	public boolean isDead() {
		return isDead;
	}
	public byte getID() {
		return projID;
	}
	public float getRotation() {
		return rotation;
	}
}