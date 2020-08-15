package chris.fortress.draw;

import chris.fortress.Game;
import chris.fortress.GameClient;
import chris.fortress.Level;
import chris.fortress.entity.player.Player;
import chris.fortress.entity.player.PlayerAnimator;
import chris.fortress.entity.player.PlayerHandler;
import chris.fortress.entity.projectile.Projectile;
import chris.fortress.entity.projectile.ProjectileHandler;
import chris.fortress.item.ItemConfig;
import chris.fortress.socket.SendMessage;
import chris.fortress.tile.TileConfig;
import chris.fortress.util.Resource;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector3;

/**The Draw class used when the game is running*/
public final class DrawMain extends Draw {
	private boolean left = false, right = false, down = false, jump = false;
	
	private final Vector3 unprojectVector = new Vector3();
	
	private final BitmapFont gameFont = new BitmapFont(Resource.getGameFont()[0], Resource.getGameFont()[1], false);
	private final BitmapFont smallFont = new BitmapFont(Resource.getSmallFont()[0], Resource.getSmallFont()[1], false);
	
	private final GlyphLayout glyphLayout = new GlyphLayout();
	
	@Override
	public void draw(SpriteBatch batch, ShapeRenderer renderer) {
		Gdx.gl.glClearColor(Level.getColour().r, Level.getColour().g, Level.getColour().b, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		GameClient game = (GameClient) Game.getGame();
		OrthographicCamera camera = game.getCamera();
		
		int cI = PlayerHandler.clientIndexOf(game.getClientID());
		camera.position.set(PlayerHandler.getPlayer(cI).getX() + PlayerAnimator.getImage(0).getRegionWidth() / 2,
				PlayerHandler.getPlayer(cI).getY() + PlayerAnimator.getImage(0).getRegionHeight() / 2, 0);
		camera.update();
		
		batch.setColor(Color.WHITE);
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		
		int leftBound = (int) (camera.position.x - zoomedWidth() / 2 - Game.SIZE * 2);
		float rightBound = camera.position.x + zoomedWidth() / 2;
		int bottomBound = (int) (camera.position.y - zoomedHeight() / 2 - Game.SIZE * 2);
		float topBound = camera.position.y + zoomedHeight() / 2;
		//Draw tiles
		for (int i = leftBound / Game.SIZE; i < rightBound / Game.SIZE; i++) {
			if (i >= 0 && i < Game.getLevel().getMapWidth()) {	
				for (int j = bottomBound / Game.SIZE; j < topBound / Game.SIZE; j++) {
					if (j >= 0 && j < Game.getLevel().getMapHeight()) {
						TileConfig t = TileConfig.getTile(Game.getLevel().getTileAt(i, j));
						if (t.isVisible()) {
							for (int k = 0; k < t.imageCount(); k++) {
								t.getImage(k).setX(i * Game.SIZE);
								t.getImage(k).setY(j * Game.SIZE);
								t.getImage(k).draw(batch);
							}
						}
					}
				}
			}
		}
		//Draw items
		for (int i = 0; i < Game.getLevel().getItemCount(); i++) {
			short[] item = Game.getLevel().getItemAt(i);
			if (item[0] > leftBound && item[0] < rightBound && item[1] > bottomBound && item[1] < topBound) {
				Sprite s = ItemConfig.getItem(item[2]).getImage(ItemConfig.ZOOMED_INDEX);
				s.setOrigin(0, 0);
				s.setX(item[0]);
				s.setY(item[1]);
				s.setRotation(0);
				s.draw(batch);
			}
		}
		//Draw projectiles (SpriteBatch projectiles only)
		for (int i = 0; i < ProjectileHandler.projectileCount(); i++) {
			ProjectileHandler.getProjectile(i).drawImage(batch);
		}
		//Draw players (SpriteBatch part)
		for (int i = 0; i < PlayerHandler.playerCount(); i++) {
			Player p = PlayerHandler.getPlayer(i);
			if (p.getHealth() > 0 && p.getX() > leftBound && p.getX() < rightBound && p.getY() > bottomBound && p.getY() < topBound) {
				p.getAnimator().drawPlayerImage(batch, smallFont, glyphLayout);
			}
		}
		batch.end();
		renderer.setProjectionMatrix(camera.combined);
		renderer.begin(ShapeType.Filled);
		//Draw projectiles (ShapeRenderer projectiles only)
		for (int i = 0; i < ProjectileHandler.projectileCount(); i++) {
			Projectile p = ProjectileHandler.getProjectile(i);
			if (p.getX() > leftBound && p.getX() < rightBound &&
					p.getY() > bottomBound && p.getY() < topBound) {
				ProjectileHandler.getProjectile(i).drawShape(renderer);
			}
		}
		
		//Draw players (ShapeRenderer part)
		for (int i = 0; i < PlayerHandler.playerCount(); i++) {
			Player p = PlayerHandler.getPlayer(i);
			if (p.getHealth() > 0 && p.getX() > leftBound && p.getX() < rightBound && p.getY() > bottomBound && p.getY() < topBound) {
				p.getAnimator().drawPlayerShape(renderer);
			}
		}
		renderer.end();
		
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		for (int i = 0; i <= 1; i++) {
			String scoreText = (i == 0 ? "Red: " : "Blue: ") + game.getScore(i == 0 ? Game.RED : Game.BLUE);
			glyphLayout.setText(gameFont, scoreText);
			unprojectVector.set(Gdx.graphics.getWidth() * (i == 0 ? 0.1f : 0.9f) - glyphLayout.width / 2, Gdx.graphics.getHeight() * 0.08f, 0);
			game.getCamera().unproject(unprojectVector);
			gameFont.setColor(i == 0 ? Color.RED : Color.BLUE);
			gameFont.draw(batch, scoreText, unprojectVector.x, unprojectVector.y);
		}
		String timeLeft = "" + game.getTimer().getTimeRemaining();
		gameFont.setColor(Color.WHITE);
		glyphLayout.setText(gameFont, timeLeft);
		unprojectVector.set(Gdx.graphics.getWidth() / 2 - glyphLayout.width / 2, Gdx.graphics.getHeight() * 0.08f, 0);
		game.getCamera().unproject(unprojectVector);
		gameFont.draw(batch, timeLeft, unprojectVector.x, unprojectVector.y);
		batch.end();
	}
	@Override
	public void mousePressed(int mouseX, int mouseY) {
		unprojectVector.x = mouseX;
		unprojectVector.y = mouseY;
		((GameClient) Game.getGame()).getCamera().unproject(unprojectVector);
		SendMessage.sendUseToServer((short) unprojectVector.x, (short) unprojectVector.y);
	}
	@Override
	public void keyPressed(int keycode) {
		switch (keycode) {
		case Keys.A:
			if (!left) {
				left = true;
				SendMessage.sendKeyPressedToServer(Player.LEFT);
			}
			return;
		case Keys.D:
			if (!right) {
				right = true;
				SendMessage.sendKeyPressedToServer(Player.RIGHT);
			}
			return;
		case Keys.S:
			if (!down) {
				down = true;
				SendMessage.sendKeyPressedToServer(Player.DOWN);
			}
			return;
		case Keys.SPACE:
			if (!jump) {
				jump = true;
				SendMessage.sendKeyPressedToServer(Player.JUMP);
			}
			return;
		}
	}
	@Override
	public void keyReleased(int keycode) {
		switch (keycode) {
		case Keys.A:
			if (left) {
				left = false;
				SendMessage.sendKeyReleasedToServer(Player.LEFT);
			}
			return;
		case Keys.D:
			if (right) {
				right = false;
				SendMessage.sendKeyReleasedToServer(Player.RIGHT);
			}
			return;
		case Keys.S:
			if (down) {
				down = false;
				SendMessage.sendKeyReleasedToServer(Player.DOWN);
			}
			return;
		case Keys.SPACE:
			if (jump) {
				jump = false;
				SendMessage.sendKeyReleasedToServer(Player.JUMP);
			}
			return;
		}
	}
	@Override
	public void mouseScrolled(int amount) {
		Player player = PlayerHandler.getPlayer(PlayerHandler.clientIndexOf(((GameClient) Game.getGame()).getClientID()));
		int index = player.getEquippedItemIndex();
		if (index >= 0) {
			//Scrolling down
			if (amount > 0) {
				if ((index -= 1) < 0) {
					index = player.gatheredItemsCount() - 1;
				}
			//Scrolling up
			} else {
				if ((index += 1) >= player.gatheredItemsCount()) {
					index = 0;
				}
			}
			//Can't change items if there is only one item
			if (player.gatheredItemsCount() > 1) {
				SendMessage.sendEquipToServer((short) index);
			}
		}
	}
	@Override
	public void dispose() {
		gameFont.dispose();
		smallFont.dispose();
	}
}