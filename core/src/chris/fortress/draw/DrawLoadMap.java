package chris.fortress.draw;

import chris.fortress.Game;
import chris.fortress.GameMapMaker;
import chris.fortress.Level;
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

/**The Draw class used to load a level for the Map Maker mode, or to create a new map*/
public final class DrawLoadMap extends Draw {
	private static final int STATE_START = 0, STATE_OPEN = 1, STATE_NEW_NAME = 2, STATE_NEW_WIDTH = 3, STATE_NEW_HEIGHT = 4;
	private static final int STATE_FAILED = 5;
	private static int state;
	
	private String messageTyped = "";
	private String mapName = "";
	private short mapWidth;
	
	private double timePassed = 0;
	
	private final BitmapFont mapFont = new BitmapFont(Resource.getGameFont()[0], Resource.getGameFont()[1], false);
	private final GlyphLayout glyphLayout = new GlyphLayout();
	
	private final Button openMapButton = new Button(Draw.zoomedWidth() * 0.3f, Draw.zoomedHeight() * 0.2f, "Open Map", Color.GREEN, true);
	private final Button newMapButton = new Button(Draw.zoomedWidth() * 0.7f, Draw.zoomedHeight() * 0.2f, "New Map", Color.GREEN, true);
	private final Button backButton = new Button(Draw.zoomedWidth() / 2, Draw.zoomedHeight() * 0.3f, "Back", new Color(0.75f, 0.75f, 0.15f, 1), true);
	private final Button exitButton = Resource.createExitButton();
	private final Button controlButton = new Button(Draw.zoomedWidth() * 0.5f, Draw.zoomedHeight() * 0.1f, "Controls", new Color(0.75f, 0.75f, 0.15f, 1), true);

	public DrawLoadMap() {
		mapFont.getData().setScale(1.5f);
		mapFont.setColor(Color.BLACK);
		state = STATE_START;
		
		OrthographicCamera camera = ((GameMapMaker) Game.getGame()).getCamera();
		camera.setToOrtho(false);
		camera.update();
	}
	@Override
	public void draw(SpriteBatch batch, ShapeRenderer renderer) {
		Gdx.gl.glClearColor(0.7f, 0.7f, 0.7f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		OrthographicCamera camera = ((GameMapMaker) Game.getGame()).getCamera();
		
		timePassed += Gdx.graphics.getDeltaTime();
		
		renderer.setProjectionMatrix(camera.combined);
		renderer.begin(ShapeType.Filled);
		
		exitButton.drawBackground(renderer, camera);
		controlButton.drawBackground(renderer, camera);
		
		if (state == STATE_START) {
			openMapButton.drawBackground(renderer, camera);
			newMapButton.drawBackground(renderer, camera);
		} else {
			backButton.drawBackground(renderer, camera);
		}
		renderer.end();
		
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		exitButton.drawText(batch);
		controlButton.drawText(batch);
		if (state == STATE_START) {
			openMapButton.drawText(batch);
			newMapButton.drawText(batch);
		} else {
			backButton.drawText(batch);
			
			glyphLayout.setText(mapFont, messageTyped);
			mapFont.draw(batch, glyphLayout, Draw.zoomedWidth() / 2 - glyphLayout.width / 2, Draw.zoomedHeight() / 2);
			if ((int) (timePassed * 2) % 2 == 0) {
				mapFont.draw(batch, "|", Draw.zoomedWidth() / 2 + glyphLayout.width / 2, Draw.zoomedHeight() / 2);
			}
		}
		String displayMessage;
		switch (state) {
		case STATE_START:
			displayMessage = "Start a new map or open an existing map.";
			break;
		case STATE_OPEN:
			displayMessage = "Enter the file name of the map from " + Level.getLevelPath() + ":";
			break;
		case STATE_NEW_NAME:
			displayMessage = "Enter the file name of the map:";
			break;
		case STATE_NEW_WIDTH:
			displayMessage = "Enter the map width (in tiles):";
			break;
		case STATE_NEW_HEIGHT:
			displayMessage = "Enter the map height (in tiles):";
			break;
		default:
			displayMessage = "Error: Unable to load map. Ensure the map name is correct, and try again:";
		}
		glyphLayout.setText(mapFont, displayMessage);
		mapFont.draw(batch, glyphLayout, Draw.zoomedWidth() / 2 - glyphLayout.width / 2, Draw.zoomedHeight() * 0.8f);
		batch.end();
	}
	@Override
	public void dispose() {
		mapFont.dispose();
	}
	@Override
	public void keyTyped(char character) {
		if (state != STATE_START) {
			//User pressed enter
			if ((character == '\r' || character == '\n') && messageTyped.length() > 0) {
				if (state == STATE_OPEN || state == STATE_FAILED) {
					GameMapMaker.openMap(messageTyped);
				}
				else if (state == STATE_NEW_NAME) {
					mapName = new String(messageTyped);
					state = STATE_NEW_WIDTH;
				}
				else if (state == STATE_NEW_WIDTH) {
					mapWidth = Short.parseShort(messageTyped);
					state = STATE_NEW_HEIGHT;
				}
				else if (state == STATE_NEW_HEIGHT) {
					GameMapMaker.newMap(mapName, mapWidth, Short.parseShort(messageTyped));
				}
				messageTyped = "";
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
	public void mouseReleased(int mouseX, int mouseY) {
		OrthographicCamera camera = ((GameMapMaker) Game.getGame()).getCamera();
		mouseX *= camera.zoom;
		mouseY *= camera.zoom;
		
		if (state == STATE_START) {
			if (openMapButton.release(mouseX, mouseY)) {
				state = STATE_OPEN;
			}
			else if (newMapButton.release(mouseX, mouseY)) {
				state = STATE_NEW_NAME;
			}
		} else {
			if (backButton.release(mouseX, mouseY)) {
				messageTyped = "";
				if (state == STATE_NEW_HEIGHT) state = STATE_NEW_WIDTH;
				else if (state == STATE_NEW_WIDTH) state = STATE_NEW_NAME;
				else state = STATE_START;
			}
		}
		if (exitButton.release(mouseX, mouseY)) {
			Gdx.app.exit();
		}
		if (controlButton.release(mouseX, mouseY)) {
			Draw.setScreen(new DrawControls());
		}
	}
	@Override
	public void mousePressed(int mouseX, int mouseY) {
		OrthographicCamera camera = ((GameMapMaker) Game.getGame()).getCamera();
		mouseX *= camera.zoom;
		mouseY *= camera.zoom;
		
		exitButton.press(mouseX, mouseY);
		controlButton.press(mouseX, mouseY);
		if (state == STATE_START) {
			openMapButton.press(mouseX, mouseY);
			newMapButton.press(mouseX, mouseY);
		} else {
			backButton.press(mouseX, mouseY);
		}
	}
	/**GameMapMaker will call this after failing to load a map*/
	public static void setStateFailed() {
		state = STATE_FAILED;
	}
}