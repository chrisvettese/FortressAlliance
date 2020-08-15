package chris.fortress.entity.player;

import chris.fortress.Game;
import chris.fortress.entity.projectile.Projectile;
import chris.fortress.entity.projectile.ProjectileConfig;
import chris.fortress.item.ItemAnimator;
import chris.fortress.item.ItemConfig;
import chris.fortress.util.Resource;
import chris.fortress.util.Timer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.Array;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**A client side class for drawing the player*/
public class PlayerAnimator {
	private Player p;
	private static final Array<Sprite> images = new Array<>();
	private ScheduledFuture<?> updateTimer, keepMovingTimer;
	private int playerAnimation;
	private boolean direction;
	private ItemAnimator itemAnimator;
	private boolean keepMoving;
	private static int colourIndex;
	private static final byte MOVE_LEFT = 1, MOVE_RIGHT = 2, STAND_LEFT = 3, STAND_RIGHT = 4;
	private static final float HEALTH_HEIGHT = 4;
	private static final float HEALTH_POSITION = 7;
	private static final float NAME_POSITION = 14;
	
	public PlayerAnimator(Player p) {
		this.p = p;
		updateTimer = Timer.getScheduledExecutorService().scheduleAtFixedRate(()->update(), 50, 50, TimeUnit.MILLISECONDS);
	}
	private void update() {
		try {
			//The amount of player images for one colour facing one way (divided by 2 twice = divided by 4)
			int animationSize = images.size / 4 - 1;
			if (p.getEquippedItemID() >= 0) {
				if (itemAnimator.getRequestedDirection() == ItemAnimator.REQUEST_LEFT && direction == Player.FACING_RIGHT || itemAnimator.getRequestedDirection() == ItemAnimator.REQUEST_RIGHT && direction == Player.FACING_LEFT) {
					if (playerAnimation <= 0) {
						playerAnimation = animationSize;
					} else {
						playerAnimation--;
					}
				} else {
					if (playerAnimation >= animationSize) {
						playerAnimation = 0;
					} else {
						playerAnimation++;
					}
				}
			} else {
				if (playerAnimation >= animationSize) playerAnimation = 0;
				else playerAnimation++;
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	public Projectile createParticle() {
		return new Projectile(p.getX() + images.get(0).getRegionWidth() / 2, p.getY() + images.get(0).getRegionHeight() / 2,
				ProjectileConfig.bloodParticle.getXDir(), ProjectileConfig.bloodParticle.getYDir(),
				p.getTeam(), ProjectileConfig.bloodParticle.getID());
	}
	public void setDirection(boolean direction) {
		if (!keepMoving) this.direction = direction;
	}
	public boolean getDirection() {
		return direction;
	}
	public static Sprite getImage(int imageIndex) {
		return images.get(imageIndex);
	}
	public void stopTimers() {
		updateTimer.cancel(true);
		if (itemAnimator != null) itemAnimator.dispose();
	}
	public void updateWeapon(short mouseX, short mouseY) {
		//If statement fixes thread/socket synchronization problem where the player is told to update an item just after the item has been deleted
		if (p.getEquippedItemID() >= 0) {
			itemAnimator.updateWeapon(mouseX, mouseY);
		}
	}
	public void useWeapon(short mouseX, short mouseY) {
		itemAnimator.use(mouseX, mouseY);
	}
	public static final void setup() {
		Sprite image = new Sprite(new Texture(Resource.getFile("player.png")));
		image.setColor(Color.RED);
		int widthCounter = 0;
		boolean playerAtX = false;

		if (!image.getTexture().getTextureData().isPrepared()) image.getTexture().getTextureData().prepare();
		Pixmap pixmap = image.getTexture().getTextureData().consumePixmap();
		for (int x = (int) image.getWidth() - 1; x >= 0; x--) {
			for (int y = 0; y < image.getHeight(); y++) {
				//If pixel has red value greater than 0 (if it's part of the player texture)
				if ((pixmap.getPixel(x, y) >> 16 & 0xFF) > 0) {
					playerAtX = true;
					break;
				}
			}
			if (playerAtX) {
				playerAtX = false;
				widthCounter++;
			}
			else if (!playerAtX && widthCounter > 0) {
				Sprite playerImage = new Sprite(image);
				//x-1 and widthCounter+3 include the border around each player image
				playerImage.setRegion(x - 1, 0, widthCounter + 3, (int) image.getHeight());
				Sprite flippedPlayer = new Sprite(playerImage);
				flippedPlayer.flip(true, false);
				//Original image faced right
				images.add(playerImage);
				//Flips image so player can walk to the right
				images.add(flippedPlayer);
				
				widthCounter = 0;
				colourIndex += 2;
			}
		}
		image.getTexture().getTextureData().disposePixmap();
		
		for (int i = 0; i < colourIndex; i++) {
			Sprite playerBlue = new Sprite(images.get(i));
			playerBlue.setColor(Color.BLUE);
			images.add(playerBlue);
		}
	}
	public void drawPlayerImage(SpriteBatch batch, BitmapFont font, GlyphLayout glyphLayout) {
		Sprite playerImage = getPlayerImage();
		batch.setColor(playerImage.getColor());
		batch.draw(playerImage, p.getX(), p.getY());
		batch.setColor(Color.WHITE);
		glyphLayout.setText(font, p.getName());
		font.draw(batch, p.getName(), p.getX() + images.get(0).getRegionWidth() / 2 - glyphLayout.width / 2, p.getY() + images.get(0).getRegionHeight() + NAME_POSITION + font.getCapHeight());
		//Weapon draw code
		if (p.getEquippedItemID() >= 0) itemAnimator.draw(batch, playerImage);
	}
	public void drawPlayerShape(ShapeRenderer renderer) {
		final float percentHealth = p.getHealth() / (float) Player.MAX_HEALTH;
		final float percentInverseHealth = (Player.MAX_HEALTH - p.getHealth()) / (float) Player.MAX_HEALTH;
		Color healthColour = new Color(percentInverseHealth, percentHealth, 0, 0);
		renderer.set(ShapeType.Filled);
		renderer.setColor(healthColour);
		renderer.rect(p.getX(),
				p.getY() + images.get(0).getRegionHeight() + HEALTH_POSITION,
				percentHealth * images.get(0).getRegionWidth(),
				HEALTH_HEIGHT);
		renderer.setColor(Color.BLACK);
		//Rectangle outline
		renderer.set(ShapeType.Line);
		renderer.rect(p.getX(),
				p.getY() + images.get(0).getRegionHeight() + HEALTH_HEIGHT + 3,
				images.get(0).getRegionWidth(),
				HEALTH_HEIGHT);
	}
	/**
	 * @return The current image for this player
	 */
	private Sprite getPlayerImage() {
		int colourIndex = p.getTeam() == Game.RED ? 0 : PlayerAnimator.colourIndex;
		int animationType;
		if (p.getEquippedItemID() >= 0 && itemAnimator.getRequestedDirection() != ItemAnimator.NO_REQUEST) {
			animationType = (itemAnimator.getRequestedDirection() == ItemAnimator.REQUEST_LEFT ? 1 : 2) + (keepMoving ? 0 : 2);
		} else {
			animationType = (this.direction == Player.FACING_LEFT ? 1 : 2) + (keepMoving ? 0 : 2);
		}
		switch (animationType) {
		case MOVE_LEFT:
			return images.get(playerAnimation * 2 + colourIndex);
		case MOVE_RIGHT:
			return images.get(playerAnimation * 2 + 1 + colourIndex);
		case STAND_LEFT:
			return images.get(colourIndex);
		case STAND_RIGHT:
			return images.get(1 + colourIndex);
		default:
			return null;
		}
	}
	public void updatePosition(short x, short y) {
		if (x < p.getX()) {
			direction = Player.FACING_LEFT;
			startMoving();
		}
		else if (x > p.getX()) {
			direction = Player.FACING_RIGHT;
			startMoving();
		}
		p.setX(x);
		p.setY(y);
	}
	private void startMoving() {
		keepMoving = true;
		if (keepMovingTimer != null && !keepMovingTimer.isDone()) keepMovingTimer.cancel(true);
		keepMovingTimer = Timer.getScheduledExecutorService().schedule(()->stopMoving(), 50, TimeUnit.MILLISECONDS);
	}
	private void stopMoving() {
		keepMoving = false;
	}
	public void setEquippedItem(byte itemID) {
		if (p.getEquippedItemID() >= 0) itemAnimator.dispose();
		itemAnimator = ItemConfig.getItem(itemID).createAnimator(p);
		p.setEquippedItem(itemID);
	}
	public static void dispose() {
		for (Sprite s : images) {
			s.getTexture().dispose();
		}
		images.clear();
		colourIndex = 0;
	}
}