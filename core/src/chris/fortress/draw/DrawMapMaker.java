package chris.fortress.draw;

import chris.fortress.Game;
import chris.fortress.GameMapMaker;
import chris.fortress.Level;
import chris.fortress.item.ItemConfig;
import chris.fortress.tile.TileConfig;
import chris.fortress.util.CollisionMapMaker;
import chris.fortress.util.Resource;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector3;

/**The Draw class used in the Map Maker mode that allows the user to edit the map*/
public class DrawMapMaker extends Draw {
	private static final String ENTER_RED = "Enter the red component of the background (0-255) and press enter: ";
	private static final String ENTER_GREEN = "Enter the green component of the background (0-255) and press enter: ";
	private static final String ENTER_BLUE = "Enter the blue component of the background (0-255) and press enter: ";
	private static final String ENTER_TILE_ID = "Enter a new tile ID (0-" + (TileConfig.amountOfTiles() - 1) + ") and press enter: ";
	private static final String ENTER_ITEM_ID = "Enter a new item ID (0-" + ItemConfig.amountOfItems() + ") and press enter: ";

	private int selectionBorder;
	
	private float scaledSize;

	private final BitmapFont mapFont = new BitmapFont(Resource.getGameFont()[0], Resource.getGameFont()[1], false);
	private final GlyphLayout glyphLayout = new GlyphLayout();
	
	private boolean mousePressed;
	private String currentInstruction;
	private String userInput = "";
	
	private Color translucentBlack = new Color(0, 0, 0, 0.5f);
	
	private Vector3 vector = new Vector3();
	
	public DrawMapMaker() {
		selectionBorder = Math.round(8);
		currentInstruction = ENTER_TILE_ID;
		scaledSize = scale(Game.SIZE);
	}
	
	@Override
	public void draw(SpriteBatch batch, ShapeRenderer renderer) {
		Color backColour = Level.getColour();
		Gdx.gl.glClearColor(backColour.r, backColour.g, backColour.b, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		OrthographicCamera camera = ((GameMapMaker) Game.getGame()).getCamera();
		float halfWidth = Draw.zoomedWidth() / 2;
		float halfHeight = Draw.zoomedHeight() / 2;
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		for (int i = (int) ((camera.position.x - halfWidth - scaledSize * 2) / scaledSize); i < (camera.position.x + halfWidth) / scaledSize; i++) {
			if (i >= 0 && i < Game.getLevel().getMapWidth()) {	
				for (int j = (int) ((camera.position.y - halfHeight) / scaledSize); j < (camera.position.y + halfHeight) / scaledSize; j++) {
					if (j >= 0 && j < Game.getLevel().getMapHeight()) {
						TileConfig t = TileConfig.getTile(Game.getLevel().getTileAt(i, j));
						if (t.isVisible()) {
							for (int k = t.imageCount() / 2; k < t.imageCount(); k++) {
								t.getImage(k).setX(i * scaledSize);
								t.getImage(k).setY(j * scaledSize);
								t.getImage(k).draw(batch);
							}
						}
					}
				}
			}
		}
		float scale = getScale();
		for (int i = 0; i < Game.getLevel().getItemCount(); i++) {
			short[] item = Game.getLevel().getItemAt(i);
			if (item[0] > (camera.position.x - halfWidth - scaledSize) / scale && item[0] < (camera.position.x + halfWidth) / scale &&
					item[1] > (camera.position.y - halfHeight - scaledSize) / scale && item[1] < (camera.position.y + halfHeight) / scale) {
				Sprite s = ItemConfig.getItem(item[2]).getImage(ItemConfig.ZOOMED_INDEX);
				s.setX(scale(item[0]));
				s.setY(scale(item[1]));
				s.draw(batch);
			}
		}
		batch.end();
		
		Gdx.gl.glEnable(GL20.GL_BLEND);
		renderer.setProjectionMatrix(camera.combined);
		renderer.setColor(Color.LIGHT_GRAY);
		renderer.begin(ShapeType.Line);
		for (float i = 0; i <= Game.getLevel().getMapWidth() * scaledSize; i+=scaledSize) {
			renderer.line(i, 0, i, Game.getLevel().getMapHeight() * scaledSize);
		}
		for (float i = 0; i <= Game.getLevel().getMapHeight() * scaledSize; i += scaledSize) {
			renderer.line(0, i, Game.getLevel().getMapWidth() * scaledSize, i);
		}
		
		renderer.set(ShapeType.Filled);
		byte selectedID = ((GameMapMaker) Game.getGame()).getSelectedID();
		Sprite selectedSprite;
		if (((GameMapMaker) Game.getGame()).isItemMode()) {
			selectedSprite = ItemConfig.sword.getImage(0);
		} else {
			TileConfig selectedTile = TileConfig.getTile(selectedID);
			if (!selectedTile.isVisible()) {
				selectedTile = TileConfig.stone;
			}
			selectedSprite = selectedTile.getImage(0);
		}
		float scaledSpriteHeight = selectedSprite.getHeight() * selectedSprite.getScaleY();
		float scaledSpriteWidth = selectedSprite.getWidth() * selectedSprite.getScaleX();
		vector.set(Gdx.graphics.getHeight() * 0.05f, Gdx.graphics.getHeight() * 0.05f + scaledSpriteHeight + selectionBorder, 0);
		camera.unproject(vector);
		renderer.setColor(translucentBlack);
		renderer.rect(vector.x, vector.y,
				scaledSpriteWidth + selectionBorder * 2, scaledSpriteHeight + selectionBorder * 2);
		renderer.end();
		
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		if (((GameMapMaker) Game.getGame()).isItemMode()) {
			if (selectedID > 0 && selectedID <= ItemConfig.amountOfItems()) {
				Sprite itemSprite = ItemConfig.getItem(selectedID - 1).getImage(0);
				itemSprite.setX(vector.x + selectionBorder);
				itemSprite.setY(vector.y + selectionBorder);
				itemSprite.draw(batch);
			}
		} else {
			if (TileConfig.getTile(selectedID).isVisible()) {
				for (int i = 0; i < TileConfig.getTile(selectedID).imageCount() / 2; i++) {
					Sprite tileSprite = TileConfig.getTile(selectedID).getImage(i);
					tileSprite.setX(vector.x + selectionBorder);
					tileSprite.setY(vector.y + selectionBorder);
					tileSprite.draw(batch);
				}
			}
		}
		String displayMessage = currentInstruction + userInput;
		glyphLayout.setText(mapFont, displayMessage);
		mapFont.draw(batch, displayMessage, vector.x + Draw.zoomedWidth() * 0.05f, vector.y + scaledSpriteHeight);

		batch.end();
	}
	@Override
	public void dispose() {
		mapFont.dispose();
	}
	
	@Override
	public void keyPressed(int keycode) {
		((GameMapMaker) Game.getGame()).keyPressed(keycode);
	}
	@Override
	public void keyReleased(int keycode) {
		((GameMapMaker) Game.getGame()).keyReleased(keycode);
	}
	@Override
	public void keyTyped(char character) {
		if (Resource.isStringInt(userInput) && character == '\r' || character == '\n') {
			if (currentInstruction == ENTER_RED) {
				Game.getLevel().setColour(new Color(Short.parseShort(userInput) / 255f, 0, 0, 1));
				currentInstruction = ENTER_GREEN;
			}
			else if (currentInstruction == ENTER_GREEN) {
				Color oldColour = Level.getColour();
				Game.getLevel().setColour(new Color(oldColour.r, Short.parseShort(userInput) / 255f, 0, 1));
				currentInstruction = ENTER_BLUE;
			}
			else if (currentInstruction == ENTER_BLUE) {
				Color oldColour = Level.getColour();
				Game.getLevel().setColour(new Color(oldColour.r, oldColour.g, Short.parseShort(userInput) / 255f, 1));
				currentInstruction = ENTER_TILE_ID;
			} else {
				try {
					byte selectedID = Byte.parseByte(userInput);
					((GameMapMaker) Game.getGame()).setSelectedID(selectedID);
				//If the user did not enter a valid number
				} catch (NumberFormatException e) { }
			}
			userInput = "";
		}
		else if (character == 'm' || character == 'M') {
			GameMapMaker.saveMap();
		}
		else if (character == 'b' || character == 'B') {
			currentInstruction = ENTER_RED;
		}
		//Copy the left half of the level to the right half
		else if (character == 'c' || character == 'C') {
			for (int i = Game.getLevel().getMapWidth() / 2 - 1; i >= 0; i--) {
				for (int j = 0; j < Game.getLevel().getMapHeight(); j++) {
					TileConfig t = TileConfig.getTile(Game.getLevel().getTileAt(i, j));
					//Ramps should be flipped
					if (t.getID() == TileConfig.rightStoneRamp.getID()) {
						t = TileConfig.leftStoneRamp;
					}
					else if (t.getID() == TileConfig.leftStoneRamp.getID()) {
						t = TileConfig.rightStoneRamp;
					}
					Game.getLevel().setTileAt(Game.getLevel().getMapWidth() - i - t.getTWidth(), j, t.getID());
				}
			}
			for (int i = Game.getLevel().getItemCount() - 1; i >= 0; i--) {
				short[] item = Game.getLevel().getItemAt(i);
				if (item[0] < Game.getLevel().getMapWidth() / 2 * Game.SIZE - ItemConfig.getItem(item[2]).getImage(0).getWidth() / 2) {
					Game.getLevel().addItem((short) (Game.getLevel().getMapWidth() * Game.SIZE - item[0] - ItemConfig.getItem(item[2]).getImage(0).getWidth()),
							item[1], (byte) item[2]);
				} else {
					Game.getLevel().removeItem(i, false);
				}
			}
		}
		else if (character == '+') {
			setScale(true);
			updateScale();
		}
		else if (character == '-') {
			setScale(false);
			updateScale();
		}
		else if (character == 'i' || character == 'I') {
			if (currentInstruction == ENTER_TILE_ID || currentInstruction == ENTER_ITEM_ID) {
				((GameMapMaker) Game.getGame()).toggleItemMode();
				currentInstruction = ((GameMapMaker) Game.getGame()).isItemMode() ? ENTER_ITEM_ID : ENTER_TILE_ID;
			}
		}
		else if (character >= '0' && character <= '9') {
			userInput += character;
		}
		else if (character == '\b') {
			if (userInput.length() > 0) userInput = userInput.substring(0, userInput.length() - 1);
		}
	}
	@Override
	public void mousePressed(int mouseX, int mouseY) {
		mousePressed = true;
		Vector3 mousePos = new Vector3(mouseX, mouseY, 0);
		((GameMapMaker) Game.getGame()).getCamera().unproject(mousePos);
		if (((GameMapMaker) Game.getGame()).isItemMode()) {
			CollisionMapMaker.placeItem((short) (mousePos.x / getScale()), (short) (mousePos.y / getScale()));
		}
	}
	@Override
	public void mouseReleased(int mouseX, int mouseY) {
		mousePressed = false;
		CollisionMapMaker.allowPlaceItem();
	}
	public boolean isMousePressed() {
		return mousePressed;
	}
	public void updateScale() {
		scaledSize = scale(Game.SIZE);
		((GameMapMaker) Game.getGame()).updateScale();
		ItemConfig.rescaleImages();
		TileConfig.rescaleImages();
	}
}