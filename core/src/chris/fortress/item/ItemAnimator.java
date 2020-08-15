package chris.fortress.item;

import chris.fortress.entity.player.Player;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**The base class for drawing items that are being held by a player*/
public abstract class ItemAnimator {
	public static final byte REQUEST_LEFT = -1, NO_REQUEST = 0, REQUEST_RIGHT = 1;
	
	private Player player;
	private byte requestedDirection = 0;
	
	public ItemAnimator(Player p) {
		this.player = p;
	}
	public final Player getPlayer() {
		return player;
	}
	public abstract void use(short mX, short mY);
	/**Updates the weapon given mouseX and mouseY (in default game coordinates)*/
	public abstract void updateWeapon(short mX, short mY);
	public abstract void draw(SpriteBatch batch, Sprite currentPlayerImage);
	public abstract void dispose();

	public final byte getRequestedDirection() {
		return requestedDirection;
	}
	protected final void setRequestedDirection(byte requestedDirection) {
		this.requestedDirection = requestedDirection;
		if (requestedDirection != 0) {
			player.getAnimator().setDirection(requestedDirection == REQUEST_LEFT ? Player.FACING_LEFT : Player.FACING_RIGHT);
		}
	}
}