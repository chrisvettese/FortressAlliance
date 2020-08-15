package chris.fortress.draw;

import chris.fortress.Game;
import chris.fortress.GameClient;
import chris.fortress.entity.player.Player;
import chris.fortress.entity.player.PlayerHandler;
import chris.fortress.input.Button;
import chris.fortress.socket.Protocol;
import chris.fortress.util.Resource;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

/**The Draw class used to show a list of players and the teams they are on before the game starts*/
public class DrawWait extends Draw {
	private final BitmapFont menuFont = new BitmapFont(Resource.getGameFont()[0], Resource.getGameFont()[1], false);
	private final GlyphLayout glyphLayout = new GlyphLayout();
	
	private static final float largeFontScale = 1.2f;
	private static final float smallFontScale = 1;
	
	private final Button startButton = new Button(Draw.zoomedWidth() / 2, Draw.zoomedHeight() * 0.2f, "Start", Color.GREEN, false);
	private final Button exitButton = Resource.createExitButton();
	
	public DrawWait() {
		menuFont.setColor(Color.BLACK);
	}
	@Override
	public void draw(SpriteBatch batch, ShapeRenderer renderer) {
		Gdx.gl.glClearColor(0.7f, 0.7f, 0.7f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		OrthographicCamera camera = ((GameClient) Game.getGame()).getCamera();
		
		if (PlayerHandler.playerCount() >= 1) startButton.setActive(true);
		else startButton.setActive(false);
		
		menuFont.setColor(Color.WHITE);
		glyphLayout.setText(menuFont, "Blue Team");
		renderer.setProjectionMatrix(camera.combined);
		renderer.begin(ShapeType.Filled);
		renderer.setColor(Color.RED);
		float rectWidth = glyphLayout.width * 1.1f;
		renderer.ellipse(Draw.zoomedWidth() * 0.15f, Draw.zoomedHeight() * 0.75f, rectWidth, menuFont.getLineHeight() * 1.2f);
		renderer.setColor(Color.BLUE);
		renderer.ellipse(Draw.zoomedWidth() * 0.85f - rectWidth, Draw.zoomedHeight() * 0.75f, rectWidth, menuFont.getLineHeight() * 1.2f);
		
		startButton.drawBackground(renderer, camera);
		exitButton.drawBackground(renderer, camera);
		
		renderer.end();

		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		menuFont.draw(batch, "Blue Team", Draw.zoomedWidth() * 0.85f - rectWidth / 2 - glyphLayout.width / 2, Draw.zoomedHeight() * 0.75f + menuFont.getLineHeight() * 0.6f + glyphLayout.height / 2);
		glyphLayout.setText(menuFont, "Red Team");
		menuFont.draw(batch, "Red Team", Draw.zoomedWidth() * 0.15f + rectWidth / 2 - glyphLayout.width / 2, Draw.zoomedHeight() * 0.75f + menuFont.getLineHeight() * 0.6f + glyphLayout.height / 2);
		
		menuFont.setColor(Color.BLACK);
		menuFont.getData().setScale(largeFontScale);
		String waitMessage = "Waiting for players...";
		glyphLayout.setText(menuFont, waitMessage);
		menuFont.draw(batch, waitMessage, Draw.zoomedWidth() / 2 - glyphLayout.width / 2, Draw.zoomedHeight() * 0.85f);
		menuFont.getData().setScale(smallFontScale);
		
		float nameYR = Draw.zoomedHeight() * 0.70f;
		float nameYB = nameYR;
		for (int i = 0; i < PlayerHandler.playerCount(); i++) {
			Player p = PlayerHandler.getPlayer(i);
			boolean team = i % 2 == 0 ? Game.RED : Game.BLUE;
			float nameX;
			glyphLayout.setText(menuFont, p.getName());
			if (team == Game.RED) {
				nameX = Draw.zoomedWidth() * 0.15f + rectWidth / 2 - glyphLayout.width / 2;
			} else {
				nameX = Draw.zoomedWidth() * 0.85f - rectWidth / 2 - glyphLayout.width / 2;
			}
			if (team == Game.RED) {
				menuFont.draw(batch, p.getName(), nameX, nameYR);
				nameYR -= menuFont.getLineHeight() * 1.3f;
			} else {
				menuFont.draw(batch, p.getName(), nameX, nameYB);
				nameYB -= menuFont.getLineHeight() * 1.3f;
			}	
		}
		
		startButton.drawText(batch);
		exitButton.drawText(batch);
		batch.end();
	}
	@Override
	public void mousePressed(int mouseX, int mouseY) {
		OrthographicCamera camera = ((GameClient) Game.getGame()).getCamera();
		mouseX *= camera.zoom;
		mouseY *= camera.zoom;
		startButton.press(mouseX, mouseY);
		exitButton.press(mouseX, mouseY);
	}
	@Override
	public void mouseReleased(int mouseX, int mouseY) {
		OrthographicCamera camera = ((GameClient) Game.getGame()).getCamera();
		mouseX *= camera.zoom;
		mouseY *= camera.zoom;
		if (startButton.release(mouseX, mouseY)) {
			((GameClient) Game.getGame()).getSocket().writeByte(Protocol.START);
			((GameClient) Game.getGame()).getSocket().flush();
		}
		else if (exitButton.release(mouseX, mouseY)) {
			Gdx.app.exit();
		}
	}
	@Override
	public void dispose() {
		menuFont.dispose();
	}
}