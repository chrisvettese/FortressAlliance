package chris.fortress.input;

import chris.fortress.draw.Draw;
import chris.fortress.util.Resource;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**A simple button class which draws a rectangle with text. Updated whenever the mouse is clicked, released, or hovered over the button*/
public final class Button {
	private float x, y, width;
	/**The average of glyphLayout.height and buttonFont.height, which centres the text inside the rectangle*/
	private float centreHeight;
	private Color colour, hover, dark;
	private boolean pressed = false, active;
	
	private static BitmapFont buttonFont;
	private final GlyphLayout glyphLayout = new GlyphLayout();
	
	/**
	 * Creates a new button.
	 * @param x Scaled, translated x. The centre of the button.
	 * @param y Scaled, translated y. The bottom of the button.
	 * @param message Button Label
	 * @param colour Unpressed button colour
	 * @param active If the button can be pressed at first
	 */
	public Button(float x, float y, String message, Color colour, boolean active) {
		glyphLayout.setText(buttonFont, message);
		width = glyphLayout.width + 2;
		x -= glyphLayout.width / 2;
		centreHeight = (buttonFont.getLineHeight() + glyphLayout.height) / 2;
		this.x = x - 1;
		this.y = y - 1;
		this.colour = colour;
		hover = new Color(colour.r / 1.2f, colour.g / 1.2f, colour.b / 1.2f, 1);
		dark = new Color(colour.r / 2, colour.g / 2, colour.b / 2, 1);
		this.active = active;
	}
	public static void setup() {
		buttonFont = new BitmapFont(Resource.getGameFont()[0], Resource.getGameFont()[1], false);
	}
	/**
	 * Called every frame, determines what shade the button should be. Receives mouseX and mouseY
	 * from a different place than the other methods, so they do not need to be adjusted
	 */
	private Color getColour(OrthographicCamera camera) {
		float mouseX = Gdx.input.getX() * camera.zoom;
		float mouseY = Draw.zoomedHeight() - Gdx.input.getY() * camera.zoom;
		if (active) {
			if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + buttonFont.getLineHeight()) {
				if (pressed) {
					return dark;
				}
				return hover;
			}
			return colour;
		}
		return Color.GRAY;
	}
	/**
	 * Called when the mouse presses down, checks if the button was pressed
	 * @return If the button is pressed
	 * @param mouseX Zoomed mouseX
	 * @param mouseY Zoomed mouseY
	 */
	public void press(float mouseX, float mouseY) {
		mouseY = Draw.zoomedHeight() - mouseY;
		if (active) {
			if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + buttonFont.getLineHeight()) {
				pressed = true;
				return;
			}
			pressed = false;
		}
	}
	/**
	 * Called when the mouse is released on this screen, returns true if the button was clicked
	 * @param mouseX Zoomed mouseX
	 * @param mouseY Zoomed mouseY
	 */
	public boolean release(float mouseX, float mouseY) {
		mouseY = Draw.zoomedHeight() - mouseY;
		if (pressed) {
			pressed = false;
			if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + buttonFont.getLineHeight()) {
				return true;
			}
		}
		pressed = false;
		return false;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	public void drawBackground(ShapeRenderer renderer, OrthographicCamera camera) {
		renderer.setColor(getColour(camera));
		renderer.rect(x, y, width, buttonFont.getLineHeight());
	}
	public void drawText(SpriteBatch batch) {
		buttonFont.setColor(Color.WHITE);
		buttonFont.draw(batch, glyphLayout, x + 1, y + centreHeight);
	}
	public static void dispose() {
		if (buttonFont != null) {
			buttonFont.dispose();
		}
	}
}
