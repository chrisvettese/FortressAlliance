package chris.fortress.draw;

import chris.fortress.Level;
import chris.fortress.input.InputProcessorServer;
import chris.fortress.util.Resource;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**The Draw class used to load a map for the server and to display the server's IP address*/
public class DrawServer extends Draw {
	private static final float FRAME_TIME = 1/60f;
	
	private final BitmapFont serverFont = new BitmapFont(Resource.getGameFont()[0], Resource.getGameFont()[1], false);
	private final GlyphLayout glyphLayout = new GlyphLayout();
	
	private static boolean mapSelected = false;
	private static boolean fileError = false;
	
	private double timePassed = 0;
	
	public DrawServer() {
		serverFont.setColor(Color.BLACK);
	}
	
	@Override
	public void draw(SpriteBatch batch, ShapeRenderer renderer) {
		Gdx.gl.glClearColor(0.7f, 0.7f, 0.7f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		timePassed += Gdx.graphics.getDeltaTime();
		batch.begin();
		if (mapSelected) {
			glyphLayout.setText(serverFont, "Started server.");
			serverFont.draw(batch, glyphLayout, Gdx.graphics.getWidth() / 2 - glyphLayout.width / 2, Gdx.graphics.getHeight() * 0.8f);
			try {
				glyphLayout.setText(serverFont, "Server address: " + InetAddress.getLocalHost().getHostAddress());
				serverFont.draw(batch, glyphLayout, Gdx.graphics.getWidth() / 2 - glyphLayout.width / 2, Gdx.graphics.getHeight() * 0.5f);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		} else {
			if (fileError) {
				glyphLayout.setText(serverFont, "Error: Unable to load map.");
				serverFont.draw(batch, glyphLayout, Gdx.graphics.getWidth() / 2 - glyphLayout.width / 2, Gdx.graphics.getHeight() * 0.8f);
				glyphLayout.setText(serverFont, System.lineSeparator() + "Ensure that the file name is correct and try again.");
				serverFont.draw(batch, glyphLayout, Gdx.graphics.getWidth() / 2 - glyphLayout.width / 2, Gdx.graphics.getHeight() * 0.8f);
			} else {
				glyphLayout.setText(serverFont, "Enter the file name of the map from ");
				serverFont.draw(batch, glyphLayout, Gdx.graphics.getWidth() / 2 - glyphLayout.width / 2, Gdx.graphics.getHeight() * 0.8f);
				glyphLayout.setText(serverFont, Level.getLevelPath() + ":");
				serverFont.draw(batch, glyphLayout, Gdx.graphics.getWidth() / 2 - glyphLayout.width / 2, Gdx.graphics.getHeight() * 0.8f - glyphLayout.height * 2);
			}
			glyphLayout.setText(serverFont, InputProcessorServer.getMapName());
			serverFont.draw(batch, glyphLayout, Gdx.graphics.getWidth() / 2 - glyphLayout.width / 2, Gdx.graphics.getHeight() * 0.5f);
			if ((int) (timePassed * 2) % 2 == 0) {
				serverFont.draw(batch, "|", Gdx.graphics.getWidth() / 2 + glyphLayout.width / 2, Gdx.graphics.getHeight() * 0.5f);
			}
		}
		batch.end();
		
		//Caps the frame rate so that the server doesn't slow down the player side of the game (useful if server and client on the same computer)
		if (Gdx.graphics.getDeltaTime() < FRAME_TIME) {
			try {
				Thread.sleep((long) (FRAME_TIME - Gdx.graphics.getDeltaTime()) * 1000);
			} catch (InterruptedException e) { }
		}
	}
	public static void showFileError() {
		fileError = true;
	}
	public static void showMainMessage() {
		mapSelected = true;
	}
	@Override
	public void dispose() {
		serverFont.dispose();
	}
}