package chris.fortress.draw;

import chris.fortress.Game;
import chris.fortress.GameClient;
import chris.fortress.input.Button;
import chris.fortress.socket.SendMessage;
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

/**The Draw class used when the player dies*/
public class DrawRespawn extends Draw {
	/**The font to use to display text*/
	private final BitmapFont respawnFont = new BitmapFont(Resource.getGameFont()[0], Resource.getGameFont()[1], false);
	/**For centring text*/
	private final GlyphLayout glyphLayout = new GlyphLayout();
	
	/**Set respawnFont to this for larger text*/
	private static final float largeFontScale = 1.2f;
	
	/**When clicked, send a request to the server to respawn*/
	private final Button respawnButton = new Button(Draw.zoomedWidth() / 2.4f, Draw.zoomedHeight() * 0.25f, "Respawn", Color.GREEN, true);
	/**When clicked, leave the server*/
	private final Button leaveButton = new Button(Draw.zoomedWidth() / 1.6f, Draw.zoomedHeight() * 0.25f, "Leave Server", Color.GREEN, true);
	/**When clicked, close the program*/
	private final Button exitButton = Resource.createExitButton();
	
	public DrawRespawn() {
		//Sets the camera to a default position so the drawing code works
		((GameClient) Game.getGame()).getCamera().setToOrtho(false);
		((GameClient) Game.getGame()).getCamera().update();
		
		//Sets the font scale and colour
		respawnFont.getData().setScale(largeFontScale);
		respawnFont.setColor(Color.BLACK);
	}
	@Override
	public void draw(SpriteBatch batch, ShapeRenderer renderer) {
		Gdx.gl.glClearColor(0.7f, 0.7f, 0.7f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		OrthographicCamera camera = ((GameClient) Game.getGame()).getCamera();
		
		renderer.setProjectionMatrix(camera.combined);
		renderer.begin(ShapeType.Filled);
		//Draw the rectangles of the buttons
		respawnButton.drawBackground(renderer, camera);
		leaveButton.drawBackground(renderer, camera);
		exitButton.drawBackground(renderer, camera);
		renderer.end();

		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		//Draw the labels on the buttons
		respawnButton.drawText(batch);
		leaveButton.drawText(batch);
		exitButton.drawText(batch);
		
		//Draw the message
		final String message = "You died.";
		glyphLayout.setText(respawnFont, message);
		respawnFont.draw(batch, message, Draw.zoomedWidth() / 2 - glyphLayout.width / 2, Draw.zoomedHeight() * 0.7f);
		batch.end();
	}
	@Override
	public void dispose() {
		respawnFont.dispose();
	}
	@Override
	public void mousePressed(int mouseX, int mouseY) {
		//Gets camera from GameClient, since only clients use DrawRespawn
		OrthographicCamera camera = ((GameClient) Game.getGame()).getCamera();
		//Must account for the camera's zoom so that the mouse lines up with the buttons on the screen
		mouseX *= camera.zoom;
		mouseY *= camera.zoom;
		//Button press code
		respawnButton.press(mouseX, mouseY);
		leaveButton.press(mouseX, mouseY);
		exitButton.press(mouseX, mouseY);
	}
	@Override
	public void mouseReleased(int mouseX, int mouseY) {
		OrthographicCamera camera = ((GameClient) Game.getGame()).getCamera();
		//Account for the camera's zoom
		mouseX *= camera.zoom;
		mouseY *= camera.zoom;
		//If the respawn button is clicked
		if (respawnButton.release(mouseX, mouseY)) {
			//Tells the server that the player wants to respawn
			SendMessage.sendRespawnToServer();
		}
		//If the leave button is clicked
		else if (leaveButton.release(mouseX, mouseY)) {
			((GameClient) Game.getGame()).leaveServer();
		}
		//If the exit button is clicked
		else if (exitButton.release(mouseX, mouseY)) {
			Gdx.app.exit();
		}
	}
}