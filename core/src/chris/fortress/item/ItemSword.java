package chris.fortress.item;

import chris.fortress.Game;
import chris.fortress.GameServer;
import chris.fortress.entity.player.Player;
import chris.fortress.entity.player.PlayerHandler;
import chris.fortress.socket.SendMessage;
import chris.fortress.util.CustomRectangle;
import com.badlogic.gdx.Gdx;

/**The sword ItemConfig which defines how swords behave*/
public class ItemSword extends ItemConfig {
	private static final short ATTACK_DISTANCE = 1 * Game.SIZE;
	/**Constant to add to the player y to get the approximate position of the weapon*/
	private static final int ATTACK_HEIGHT = 15;

	protected ItemSword(String imageName, boolean repeatable, boolean updateMouse, int timeGap) {
		super(imageName, repeatable, updateMouse, timeGap);
	}
	@Override
	public ItemAnimator createAnimator(Player p) {
		return new ItemAnimatorSword(p);
	}
	@Override
	public void use(int cI, short mX, short mY) {
		Player aP = PlayerHandler.getPlayer(cI);
		float attackX, attackY;
		//Determines which direction the player is attacking in based on where the player clicked
		boolean facingLeft = (mX <= aP.getX() + Player.WIDTH / 2) ? Player.FACING_LEFT : Player.FACING_RIGHT;
		attackX = facingLeft ? aP.getX() : aP.getX() + Player.WIDTH;
		//If the attacking player is moving, code below makes collision of point and rectangle feel more accurate
		attackX += aP.getXDir() * Gdx.graphics.getDeltaTime();
		attackY = aP.getY() + Player.HEIGHT - ATTACK_HEIGHT;
		for (int i = 0; i < PlayerHandler.playerCount(); i++) {
			Player rP = PlayerHandler.getPlayer(i);
			if (rP.getHealth() > 0 && PlayerHandler.getPlayer(i).getTeam() != PlayerHandler.getPlayer(cI).getTeam()) {
				if ((facingLeft && rP.getX() <= aP.getX()) || (!facingLeft && rP.getX() > aP.getX())) {
					CustomRectangle checkRect = new CustomRectangle(rP.getX(), rP.getY(), rP.getX() + Player.WIDTH, rP.getY() + Player.HEIGHT);
					//Attacking player must be near other player
					if (checkRect.overlaps(attackX, attackY, ATTACK_DISTANCE)) {
						if (rP.hurt((short) 10)) {
							SendMessage.sendHealthToClients(rP.getClientID(), rP.getHealth());
						}
						if (rP.getHealth() <= 0) {
							((GameServer) Game.getGame()).setScore(aP.getTeam(), (short) (((GameServer) Game.getGame()).getScore(aP.getTeam()) + 1));
						}
						//Only one other player can be attacked at a time
						return;
					}
				}
			}
		}
	}
}