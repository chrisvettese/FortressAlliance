package chris.fortress.draw;

import chris.fortress.Game;
import chris.fortress.GameMapMaker;
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

public final class DrawControls extends Draw {
	
	private final BitmapFont controlFont = new BitmapFont(Resource.getGameFont()[0], Resource.getGameFont()[1], false);
	private final GlyphLayout glyphLayout = new GlyphLayout();
	
	private final Button backButton = new Button(Draw.zoomedWidth() / 2, Draw.zoomedHeight() * 0.3f, "Back", new Color(0.75f, 0.75f, 0.15f, 1), true);
	
	public DrawControls() {
		controlFont.setColor(Color.BLACK);
	}
	@Override
	public void draw(SpriteBatch batch, ShapeRenderer renderer) {
		Gdx.gl.glClearColor(0.7f, 0.7f, 0.7f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		OrthographicCamera camera = ((GameMapMaker) Game.getGame()).getCamera();
		
		renderer.begin(ShapeType.Filled);
		backButton.drawBackground(renderer, camera);
		renderer.end();
		
		batch.begin();
		glyphLayout.setText(controlFont, "Map Editing Controls");
		controlFont.draw(batch, glyphLayout, Draw.zoomedWidth() / 2 - glyphLayout.width / 2, Draw.zoomedHeight() * 0.8f);

		controlFont.getData().setScale(1f);
		glyphLayout.setText(controlFont, "WASD: Move around the map");
		controlFont.draw(batch, glyphLayout, Draw.zoomedWidth() * 0.1f, Draw.zoomedHeight() * 0.7f);
		glyphLayout.setText(controlFont, "-  : Zoom out, + : Zoom in");
		controlFont.draw(batch, glyphLayout, Draw.zoomedWidth() * 0.1f, Draw.zoomedHeight() * 0.66f);
		glyphLayout.setText(controlFont, "I : Switch between Tile mode and Item mode");
		controlFont.draw(batch, glyphLayout, Draw.zoomedWidth() * 0.1f, Draw.zoomedHeight() * 0.62f);
		glyphLayout.setText(controlFont, "Mouse click: Place the selected tile or item. If the selected ID is 0, it will delete tiles/items.");
		controlFont.draw(batch, glyphLayout, Draw.zoomedWidth() * 0.1f, Draw.zoomedHeight() * 0.58f);
		glyphLayout.setText(controlFont, "C : Copy and paste the left side of the map onto the right side");
		controlFont.draw(batch, glyphLayout, Draw.zoomedWidth() * 0.1f, Draw.zoomedHeight() * 0.54f);
		glyphLayout.setText(controlFont, "B : Change the background colour");
		controlFont.draw(batch, glyphLayout, Draw.zoomedWidth() * 0.1f, Draw.zoomedHeight() * 0.50f);
		glyphLayout.setText(controlFont, "M : Save the map and close the program. You must have two spawn points in order to save the map (tile ID 8).");
		controlFont.draw(batch, glyphLayout, Draw.zoomedWidth() * 0.1f, Draw.zoomedHeight() * 0.46f);
		controlFont.getData().setScale(1.5f);
		
		backButton.drawText(batch);
		batch.end();
	}
	@Override
	public void dispose() {
		controlFont.dispose();
	}
	@Override
	public void mousePressed(int mouseX, int mouseY) {
		OrthographicCamera camera = ((GameMapMaker) Game.getGame()).getCamera();
		mouseX *= camera.zoom;
		mouseY *= camera.zoom;
		
		backButton.press(mouseX, mouseY);
	}
	@Override
	public void mouseReleased(int mouseX, int mouseY) {
		OrthographicCamera camera = ((GameMapMaker) Game.getGame()).getCamera();
		mouseX *= camera.zoom;
		mouseY *= camera.zoom;
		
		if (backButton.release(mouseX, mouseY)) {
			Draw.setScreen(new DrawLoadMap());
		}
	}
}