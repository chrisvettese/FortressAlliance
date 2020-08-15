package chris.fortress;

import chris.fortress.draw.Draw;
import chris.fortress.draw.DrawJoin;
import chris.fortress.draw.DrawLoadMap;
import chris.fortress.draw.DrawServer;
import chris.fortress.util.Timer;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Start extends ApplicationAdapter {
	/**The client game mode, for joining a server*/
	public static final int CLIENT = 0;
	/**The server game mode, for starting a server*/
	public static final int SERVER = 1;
	/**The map maker game mode, for creating or editing a level*/
	public static final int MAPMAKER = 2;
	/**The game mode set when the game is run*/
	private static int gameType;
	
	/**For drawing images and fonts*/
	private SpriteBatch batch;
	/**For drawing shapes and lines*/
	private ShapeRenderer renderer;
	
	/**Creates a ligdx game, sets the game mode to gameType*/
	public Start(int gameType) {
		Start.gameType = gameType;
	}
	/**Called once to initialize objects*/
	@Override
	public void create () {
		batch = new SpriteBatch();
		renderer = new ShapeRenderer();
		renderer.setAutoShapeType(true);
		
		//Creates the correct instance of Game and Draw given gameType
		if (gameType == SERVER) {
			Game.setGame(new GameServer());
			Draw.setScreen(new DrawServer());
		}
		else if (gameType == CLIENT) {
			Game.setGame(new GameClient());
			Draw.setScreen(new DrawJoin());
		} else {
			Game.setGame(new GameMapMaker());
			Draw.setScreen(new DrawLoadMap());
		}
	}
	//The main game loop, calls Game.updateLoop() and Draw.draw()
	@Override
	public void render () {
		Game.getGame().updateLoop();
		Draw.getScreen().draw(batch, renderer);
	}
	//Called to release resources created during the game
	@Override
	public void dispose () {
		batch.dispose();
		renderer.dispose();
		Game.getGame().dispose();
		Draw.getScreen().dispose();
		Timer.dispose();
	}
}
