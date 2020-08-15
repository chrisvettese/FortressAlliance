package chris.fortress.item;

import chris.fortress.Game;
import chris.fortress.GameClient;
import chris.fortress.entity.player.Player;
import chris.fortress.entity.player.PlayerAnimator;
import chris.fortress.socket.SendMessage;
import chris.fortress.util.Timer;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**The code for drawing a gun being held by the player*/
public class ItemAnimatorGun extends ItemAnimator {
	private static final float ANGLE_DOWN = 270, ANGLE_UP = 90;
	private static final float GUN_DISTANCE = ItemGun.STARTING_RADIUS;
	private final Vector3 mousePos = new Vector3();
	private ScheduledFuture<?> sendServer;
	private volatile boolean shouldSendToServer = false;
	
	public ItemAnimatorGun(Player p) {
		super(p);
		//Sets temporary mouse x position so that the player doesn't turn when equipping a gun
		mousePos.x = (short) (p.getAnimator().getDirection() == Player.FACING_LEFT ? p.getX() - 200 : p.getY() + 200);
		if (p.getClientID() == ((GameClient) Game.getGame()).getClientID()) {
			sendServer = Timer.getScheduledExecutorService().scheduleAtFixedRate(()->sendToServer(), 50, 50, TimeUnit.MILLISECONDS);
		}
	}
	@Override
	public void use(short mX, short mY) { }

	@Override
	public void draw(SpriteBatch batch, Sprite currentPlayerImage) {
		float pCentreX = getPlayer().getX() + PlayerAnimator.getImage(0).getRegionWidth() / 2;
		float pCentreY = getPlayer().getY() + PlayerAnimator.getImage(0).getRegionHeight() * ItemGun.GUN_HEIGHT_ADJUST;
		boolean direction = mousePos.x < pCentreX;
		float mouseDistanceX = pCentreX - mousePos.x;
		float mouseDistanceY = pCentreY - mousePos.y;
		float weaponRotation;
		if (mouseDistanceX != 0) {
			weaponRotation = MathUtils.radiansToDegrees * MathUtils.atan2(mouseDistanceY, mouseDistanceX);
			setRequestedDirection(direction == Player.FACING_LEFT ? REQUEST_LEFT : REQUEST_RIGHT);
		} else {
			if (mouseDistanceY > 0) weaponRotation = ANGLE_UP;
			else weaponRotation = ANGLE_DOWN;
			setRequestedDirection(NO_REQUEST);
		}
		
		Sprite weapon;
		if (direction == Player.FACING_LEFT) {
			weapon = ItemConfig.gun.getImage(0);
		} else {
			weapon = ItemConfig.gun.getImage(ItemConfig.FLIPPED_INDEX);
		}
		float xChange = GUN_DISTANCE * MathUtils.cosDeg(weaponRotation);
		float yChange = GUN_DISTANCE * MathUtils.sinDeg(weaponRotation);
		if (direction == Player.FACING_RIGHT) weaponRotation += 180;
		weapon.setOriginCenter();
		weapon.setRotation(weaponRotation);
		weapon.setX(pCentreX - xChange - weapon.getWidth() / 2);
		weapon.setY(pCentreY - yChange - weapon.getHeight() / 2);
		weapon.draw(batch);
		
		if (shouldSendToServer) sendMousePosition();
	}
	@Override
	public void updateWeapon(short mX, short mY) {
		mousePos.x = mX;
		mousePos.y = mY;
	}
	@Override
	public void dispose() {
		//sendServer is only initialized for the player object that is connected to the actual player, so for other player objects sendServer will be null
		if (sendServer != null) sendServer.cancel(true);
	}

	private void sendToServer() {
		shouldSendToServer = true;
	}
	/**Send the mouse position to the server (client side only)*/
	private void sendMousePosition() {
		shouldSendToServer = false;
		mousePos.x = Gdx.input.getX();
		mousePos.y = Gdx.input.getY();
		((GameClient) Game.getGame()).getCamera().unproject(mousePos);
		SendMessage.sendMouseToServer((short) mousePos.x, (short) mousePos.y);
	}
}