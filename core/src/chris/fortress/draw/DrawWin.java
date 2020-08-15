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

/**The Draw class used when the game ends to show the winning team*/
public class DrawWin extends Draw {
	/**The font to use for DrawWin*/
	private final BitmapFont winFont = new BitmapFont(Resource.getGameFont()[0], Resource.getGameFont()[1], false);
	/**Used to centre the font*/
	private final GlyphLayout glyphLayout = new GlyphLayout();
	
	/**The scale to use for the font*/
	private static final float largeFontScale = 1.2f;
	
	/**If clicked, send request to server to start a new game*/
	private final Button newButton = new Button(Draw.zoomedWidth() / 2.4f, Draw.zoomedHeight() * 0.25f, "New Game", Color.GREEN, true);
	/**If clicked, leave the server*/
	private final Button leaveButton = new Button(Draw.zoomedWidth() / 1.6f, Draw.zoomedHeight() * 0.25f, "Leave Server", Color.GREEN, true);
	/**If clicked, close the program*/
	private final Button exitButton = Resource.createExitButton();
	
	public DrawWin() {
		//Resets all the players, now that the game is over. Names and clientIDs are moved to the new player objects
		for (int i = PlayerHandler.playerCount() - 1; i >= 0; i--) {
			Player p = PlayerHandler.getPlayer(i);
			PlayerHandler.removePlayer(i, false, false);
			PlayerHandler.addPlayer(new Player(p.getName(), p.getClientID(), Player.MAX_HEALTH));
		}
		((GameClient) Game.getGame()).getCamera().setToOrtho(false);
		((GameClient) Game.getGame()).getCamera().update();
		winFont.getData().setScale(largeFontScale);
	}
	
	@Override
	public void draw(SpriteBatch batch, ShapeRenderer renderer) {
		Gdx.gl.glClearColor(0.7f, 0.7f, 0.7f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		GameClient game = ((GameClient) Game.getGame());
		OrthographicCamera camera = ((GameClient) Game.getGame()).getCamera();
		renderer.setProjectionMatrix(camera.combined);
		renderer.begin(ShapeType.Filled);
		
		newButton.drawBackground(renderer, camera);
		leaveButton.drawBackground(renderer, camera);
		exitButton.drawBackground(renderer, camera);
		
		renderer.end();
		
		batch.setProjectionMatrix(game.getCamera().combined);
		batch.begin();
		
		newButton.drawText(batch);
		leaveButton.drawText(batch);
		exitButton.drawText(batch);
		
		String winningTeam;
		if (game.getScore(Game.RED) > game.getScore(Game.BLUE)) {
			winningTeam = "Red team wins";
		}
		else if (game.getScore(Game.RED) < game.getScore(Game.BLUE)) {
			winningTeam = "Blue team wins";
		} else {
			winningTeam = "Both teams have the same score";
		}
		glyphLayout.setText(winFont, winningTeam);
		winFont.setColor(Color.BLACK);
		winFont.draw(batch, winningTeam, Draw.zoomedWidth() / 2 - glyphLayout.width / 2, Draw.zoomedHeight() * 0.7f);
		batch.end();
	}
	@Override
	public void mousePressed(int mouseX, int mouseY) {
		OrthographicCamera camera = ((GameClient) Game.getGame()).getCamera();
		mouseX *= camera.zoom;
		mouseY *= camera.zoom;
		newButton.press(mouseX, mouseY);
		leaveButton.press(mouseX, mouseY);
		exitButton.press(mouseX, mouseY);
	}
	@Override
	public void mouseReleased(int mouseX, int mouseY) {
		OrthographicCamera camera = ((GameClient) Game.getGame()).getCamera();
		mouseX *= camera.zoom;
		mouseY *= camera.zoom;
		if (newButton.release(mouseX, mouseY)) {
			((GameClient) Game.getGame()).getSocket().writeByte(Protocol.START);
			((GameClient) Game.getGame()).getSocket().flush();
		}
		else if (leaveButton.release(mouseX, mouseY)) {
			((GameClient) Game.getGame()).leaveServer();
		}
		else if (exitButton.release(mouseX, mouseY)) {
			Gdx.app.exit();
		}
	}
	@Override
	public void dispose() {
		winFont.dispose();
	}
}