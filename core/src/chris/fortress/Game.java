package chris.fortress;

/**The base version of the game physics class. Used for updating the game before drawing it.*/
public abstract class Game {
	/**The standard size of one game tile. Actual size varies depending on the screen resolution*/
	public static final int SIZE = 32;
	/**Constant representing the red team*/
	public static final boolean RED = true;
	/**Constant representing the blue team*/
	public static final boolean BLUE = false;
	/**The current instance of game (either GameClient, GameServer, or GameMapMaker)*/
	private static Game gameType;
	/**The game map, either loaded from a file or created if using the map maker program*/
	private static Level level;
	
	/**Creates an instance of Game with an empty level*/
	public Game() {
		level = new Level();
	}
	/**Sets the current game type (either GameClient, GameServer, or GameMapMaker)*/
	public static void setGame(Game gameType) {
		Game.gameType = gameType;
	}
	/**@return The instance of the current game type*/
	public static Game getGame() {
		return gameType;
	}
	/**@return The game map object*/
	public static Level getLevel() {
		return level;
	}
	/**Sets the game level to a instance of Level*/
	public static void setLevel(Level level) {
		Game.level = level;
	}
	/**The main game loop, called once per frame*/
	protected abstract void updateLoop();
	/**Releases any resources associated with the instance of Game*/
	protected abstract void dispose();
	/**Usage varies with each type of Game. Usually involves changing to the game level mode*/
	public abstract void startGame();
}