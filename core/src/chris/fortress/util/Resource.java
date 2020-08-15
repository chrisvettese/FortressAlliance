package chris.fortress.util;

import chris.fortress.draw.Draw;
import chris.fortress.input.Button;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;

import java.util.Random;

/**A class with useful methods that are used in other classes*/
public final class Resource {
	private static final Random RANDOM = new Random();
	private static final FileHandle[] GAME_FONT = new FileHandle[] {getFile("game_font.fnt"), getFile("game_font.png")};
	private static final FileHandle[] SMALL_FONT = new FileHandle[] {getFile("small_font.fnt"), getFile("small_font.png")};
	
	public static Random getRandom() {
		return RANDOM;
	}
	public static FileHandle[] getGameFont() {
		return GAME_FONT;
	}
	public static FileHandle[] getSmallFont() {
		return SMALL_FONT;
	}
	public static boolean isStringInt(String str) {
		if (str.length() == 0) return false;
		for (int i = 0; i < str.length(); i++) {
			if (!(str.charAt(i) >= '0' && str.charAt(i) <= '9')) {
				return false;
			}
		}
		return true;
	}
	public static FileHandle getFile(String fileName) {
		return Gdx.files.internal(fileName);
	}
	public static Button createExitButton() {
		return new Button(Draw.zoomedWidth() * 0.9f, Draw.zoomedHeight() * 0.1f, "Exit", new Color(1, 0, 0.1f, 1), true);
	}
}