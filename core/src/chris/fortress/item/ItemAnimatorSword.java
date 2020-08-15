package chris.fortress.item;

import chris.fortress.Game;
import chris.fortress.entity.player.Player;
import chris.fortress.entity.player.PlayerAnimator;
import chris.fortress.util.Timer;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**The code for drawing a sword being held by a player*/
public class ItemAnimatorSword extends ItemAnimator {
	private static final int ANGLE_RIGHT = -90, ANGLE_LEFT = 90;
	private static final int CHANGE_ANGLE = 8;
	private static final int ANGLE_LOWEST = 128;
	private static final float SWORD_X_RADIUS_RIGHT = Game.SIZE * 0.48f;
	private static final float SWORD_X_RADIUS_LEFT = Game.SIZE * 0.38f;
	private static final float SWORD_Y_RADIUS = Game.SIZE;
	
	private volatile float weaponRotation;
	
	private ScheduledFuture<?> weaponTimer;
	
	public ItemAnimatorSword(Player p) {
		super(p);
		weaponRotation = p.getAnimator().getDirection() == Player.FACING_LEFT ? ANGLE_LEFT : ANGLE_RIGHT;
	}

	@Override
	public void use(short mX, short mY) {
		boolean direction = mX < getPlayer().getX() + PlayerAnimator.getImage(0).getRegionWidth() / 2;
		if (direction == Player.FACING_LEFT) {
			setRequestedDirection(REQUEST_LEFT);
		} else {
			setRequestedDirection(REQUEST_RIGHT);
		}
		weaponRotation = 0;
		if (weaponTimer != null) weaponTimer.cancel(true);
		weaponTimer = Timer.getScheduledExecutorService().scheduleAtFixedRate(()->update(direction), 8, 8, TimeUnit.MILLISECONDS);
	}
	@Override
	public void draw(SpriteBatch batch, Sprite currentPlayerImage) {
		byte direction;
		if (weaponRotation == ANGLE_LEFT || weaponRotation == ANGLE_RIGHT) {
			direction = getPlayer().getAnimator().getDirection() == Player.FACING_LEFT ? REQUEST_LEFT : REQUEST_RIGHT;
			weaponRotation = direction == REQUEST_LEFT ? ANGLE_LEFT : ANGLE_RIGHT;
		} else {
			direction = getRequestedDirection();
		}
		
		float xChange = MathUtils.sinDeg(weaponRotation) * (direction == REQUEST_LEFT ? SWORD_X_RADIUS_LEFT : SWORD_X_RADIUS_RIGHT);
		float yChange = MathUtils.cosDeg(weaponRotation) * SWORD_Y_RADIUS;
		Sprite s;
		if (direction == REQUEST_LEFT) {
			s = ItemConfig.sword.getImage(0);
			s.setX(getPlayer().getX() + currentPlayerImage.getRegionWidth() / 2 - PlayerAnimator.getImage(0).getRegionWidth() - xChange);
		} else {
			s = ItemConfig.sword.getImage(ItemConfig.FLIPPED_INDEX);
			s.setX(getPlayer().getX() + currentPlayerImage.getRegionWidth() / 2 - xChange);
		}
		s.setY(getPlayer().getY() + currentPlayerImage.getRegionHeight() * 0.35f + yChange);
		s.setRotation(weaponRotation);
		s.setOriginCenter();
		s.draw(batch);
	}
	/**Runs through the sword animation when the sword is used*/
	private synchronized void update(boolean direction) {
		try {
			if (getRequestedDirection() == REQUEST_LEFT) {
				weaponRotation+=CHANGE_ANGLE;
				if (weaponRotation >= ANGLE_LOWEST) {
					weaponTimer.cancel(true);
					weaponTimer = null;
					setRequestedDirection(NO_REQUEST);
					weaponRotation = ANGLE_LEFT;
				}
			} else {
				weaponRotation-=CHANGE_ANGLE;
				if (weaponRotation <= -ANGLE_LOWEST) {
					weaponTimer.cancel(true);
					weaponTimer = null;
					setRequestedDirection(NO_REQUEST);
					weaponRotation = ANGLE_RIGHT;
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	//These methods are never called for swords
	@Override
	public void updateWeapon(short mX, short mY) { }
	@Override
	public void dispose() { }
}