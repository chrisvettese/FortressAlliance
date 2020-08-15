package chris.fortress.input;

import chris.fortress.draw.Draw;
import com.badlogic.gdx.InputProcessor;

/**The InputProcessor used for the client side and for the Map Maker. Sends all input received to the current instance of Draw, because
 * the player's input is closely related to what they see on the screen (for example, a pressed button)
 */
public class InputProcessorClient implements InputProcessor {

	@Override
	public boolean keyDown(int keycode) {
		Draw.getScreen().keyPressed(keycode);
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		Draw.getScreen().keyReleased(keycode);
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		Draw.getScreen().keyTyped(character);
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		Draw.getScreen().mousePressed(screenX, screenY);
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		Draw.getScreen().mouseReleased(screenX, screenY);
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		Draw.getScreen().mouseScrolled(amount);
		return false;
	}
}