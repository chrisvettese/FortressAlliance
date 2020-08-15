package chris.fortress.draw;

import chris.fortress.Game;
import chris.fortress.GameClient;
import chris.fortress.input.Button;
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

/**The Draw class used to get the player's username and to connect the player to a server*/
public class DrawJoin extends Draw {
	private static final byte STATE_USERNAME = 0, STATE_ADDRESS = 1, STATE_CONNECT = 2, STATE_FAILED = 3;
	private volatile byte currentState = STATE_USERNAME;
	private double timePassed;
	private boolean takingTextInput = true;
	private String messageTyped = "";
	
	private final BitmapFont menuFont = new BitmapFont(Resource.getGameFont()[0], Resource.getGameFont()[1], false);
	private final GlyphLayout glyphLayout = new GlyphLayout();
	
	private final Button exitButton = Resource.createExitButton();
	
	public DrawJoin() {
		menuFont.getData().setScale(1.5f);
		menuFont.setColor(Color.BLACK);
		currentState = STATE_USERNAME;
		
		((GameClient) Game.getGame()).getCamera().setToOrtho(false);
		((GameClient) Game.getGame()).getCamera().update();
	}
	@Override
	public void draw(SpriteBatch batch, ShapeRenderer renderer) {
		Gdx.gl.glClearColor(0.7f, 0.7f, 0.7f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		OrthographicCamera camera = ((GameClient) Game.getGame()).getCamera();
		
		renderer.setProjectionMatrix(camera.combined);
		renderer.begin(ShapeType.Filled);
		exitButton.drawBackground(renderer, camera);
		renderer.end();
		
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		switch (currentState) {
		case STATE_USERNAME:
			drawPlayerInput(batch, "Enter a username:");
			break;
		case STATE_ADDRESS:
			drawPlayerInput(batch, "Enter the server's address:");
			break;
		case STATE_FAILED:
			drawPlayerInput(batch, "Failed to connect to server. Try again:");
			break;
		case STATE_CONNECT:
			glyphLayout.setText(menuFont, "Connecting to server...");
			menuFont.draw(batch, glyphLayout, Draw.zoomedWidth() / 2 - glyphLayout.width / 2, Draw.zoomedHeight() * 0.7f);
			break;
		default:
			//Code should never get to here
			System.out.println("Error in DrawJoin: Invalid state");
		}
		
		exitButton.drawText(batch);
		batch.end();
	}
	private void drawPlayerInput(SpriteBatch batch, String instruction) {
		glyphLayout.setText(menuFont, instruction);
		menuFont.draw(batch, glyphLayout, Draw.zoomedWidth() / 2 - glyphLayout.width / 2, Draw.zoomedHeight() * 0.7f);
		glyphLayout.setText(menuFont, messageTyped);
		timePassed += Gdx.graphics.getDeltaTime();
		menuFont.draw(batch, glyphLayout, Draw.zoomedWidth() / 2 - glyphLayout.width / 2, Draw.zoomedHeight() * 0.5f);
		if ((int) (timePassed * 2) % 2 == 0) {
			menuFont.draw(batch, "|", Draw.zoomedWidth() / 2 + glyphLayout.width / 2, Draw.zoomedHeight() * 0.5f);
		}
	}
	@Override
	public void dispose() {
		menuFont.dispose();
	}
	@Override
	public void keyTyped(char character) {
		if (takingTextInput) {
			if (character == '\r' || character == '\n') {
				if (messageTyped.length() > 0) {
					setInput(messageTyped);
					messageTyped = "";
				}
			}
			else if (character == '\b') {
				if (messageTyped.length() > 0) {
					messageTyped = messageTyped.substring(0, messageTyped.length() - 1);
				}
			} else if (character != 0) {
				messageTyped += character;
			}
		}
	}
	@Override
	public void mousePressed(int mouseX, int mouseY) {
		OrthographicCamera camera = ((GameClient) Game.getGame()).getCamera();
		exitButton.press(mouseX * camera.zoom, mouseY * camera.zoom);
	}
	@Override
	public void mouseReleased(int mouseX, int mouseY) {
		OrthographicCamera camera = ((GameClient) Game.getGame()).getCamera();
		if (exitButton.release(mouseX * camera.zoom, mouseY * camera.zoom)) {
			Gdx.app.exit();
		}
	}
	private void setInput(String messageTyped) {
		if (currentState == STATE_USERNAME) {
			((GameClient) Game.getGame()).setName(messageTyped);
			currentState = STATE_ADDRESS;
		}
		else if (currentState == STATE_ADDRESS || currentState == STATE_FAILED) {
			takingTextInput = false;
			currentState = STATE_CONNECT;
			((GameClient) Game.getGame()).tryToConnect(messageTyped);
		}
	}
	public void setFailedToConnect() {
		takingTextInput = true;
		currentState = STATE_FAILED;
	}
}