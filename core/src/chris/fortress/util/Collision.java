package chris.fortress.util;

import chris.fortress.Game;
import chris.fortress.GameServer;
import chris.fortress.entity.player.Player;
import chris.fortress.entity.projectile.Projectile;
import chris.fortress.entity.projectile.ProjectileConfig;
import chris.fortress.entity.projectile.ProjectileHandler;
import chris.fortress.item.ItemConfig;
import chris.fortress.socket.SendMessage;
import chris.fortress.tile.TileConfig;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;

/**A class containing many collision methods, such as player-tile collision and projectile-player collision*/
public final class Collision {
	private static final byte NO_LIQUID = 0;
	private static final byte PARTIAL_LIQUID = 1;
	public static final byte FULL_LIQUID = 2;
	
	public static final byte NO_COL = 0;
	public static final byte X_COL = 1;
	public static final byte Y_COL = 2;
	public static final byte FULL_COL = 3;
	
	private static byte liquidTile = -1;
	private static byte liquidState = NO_LIQUID;
	
	private static boolean canStep = true;

	private static byte collides(Player p, Rectangle pRect, float yDir) {
		float pX = p.getX() / Game.SIZE;
		int pY = (int) (p.getY() / Game.SIZE);
		
		boolean xCollides = false, yCollides = false;
		
		for (int x = (int) pX - 2; x < pX + 2; x++) {
			//If tile position is outside the map
			if (x >= 0 && x < Game.getLevel().getMapWidth()) {
				for (int y = pY + 3; y >= pY - 2; y--) {
					//If tile y is out of range
					if (y >= 0 && y < Game.getLevel().getMapHeight()) {
						byte collisionType = checkPlayerTileOverlap(p, pRect, yDir, x, y);
						if (collisionType == X_COL) {
							if (yCollides) return FULL_COL;
							xCollides = true;
						}
						else if (collisionType == Y_COL) {
							if (xCollides) return FULL_COL;
							yCollides = true;
						}
						else if (collisionType == FULL_COL) {
							return FULL_COL;
						}
					}
				}
			}
		}
		if (xCollides) return X_COL;
		else if (yCollides) return Y_COL;
		else return NO_COL;
	}
	private static byte checkPlayerTileOverlap(Player p, Rectangle pRect, float yDir, int x, int y) {
		TileConfig tileAtCoord = TileConfig.getTile(Game.getLevel().getTileAt(x, y));
		if (tileAtCoord.isSolid()) {
			if (tileAtCoord.overlapsRectangle(pRect, x, y)) {
				return tileAtCoord.postCollision(p, x, y, yDir);
			}
		}
		return NO_COL;
	}
	private static boolean fastCollides(Player p, Rectangle pRect) {
		float pX = p.getX() / Game.SIZE;
		int pY = (int) (p.getY() / Game.SIZE);
		
		for (int x = (int) pX - 2; x < pX + 2; x++) {
			//If tile position is outside the map
			if (x >= 0 && x < Game.getLevel().getMapWidth()) {
				for (int y = pY + 3; y >= pY - 2; y--) {
					//If tile y is out of range
					if (y >= 0 && y < Game.getLevel().getMapHeight()) {
						TileConfig tileAtCoord = TileConfig.getTile(Game.getLevel().getTileAt(x, y));
						if (tileAtCoord.isSolid() && tileAtCoord.overlapsRectangle(pRect, x, y)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	/**Returns 0 if the player is not in a liquid, PARTIAL_LIQUID if the player can sink but not swim up (top layer of liquid) and FULL_LIQUID if the player is fully in a liquid*/
	public static void playerInLiquid(Player p) {
		int pX = (int) (p.getX() / Game.SIZE);
		int pY = (int) (p.getY() / Game.SIZE);
		for (int y = pY + 1;  y >= pY; y--) {
			if (y >= 0 && y < Game.getLevel().getMapHeight()) {
				for (int x = pX; x < pX + 2; x++) {
					if (x >= 0 && x < Game.getLevel().getMapWidth() && TileConfig.getTile(Game.getLevel().getTileAt(x, y)).isLiquid()) {
						TileConfig tileAtCoord = TileConfig.getTile(Game.getLevel().getTileAt(x, y));
						int tileX = x * Game.SIZE;
						int tileY = y * Game.SIZE;
						if (p.getX() + Player.WIDTH > tileX && p.getX() < tileX + tileAtCoord.getWidth() &&
							p.getY() + Player.HEIGHT > tileY && p.getY() < tileY + tileAtCoord.getHeight()) {
							liquidTile = tileAtCoord.getID();
							liquidState = y == pY ? PARTIAL_LIQUID : FULL_LIQUID;
							return;
						}
					}
				}
			}
		}
		liquidState = NO_LIQUID;
	}
	public static OrderedMap<Byte, int[]> getTilesBelow(Player p) {
		//If player is not exactly above a tile, then the player is falling (in the air)
		//Tile coordinate to check first (in tile units)
		final int tX = (int) (p.getX() / Game.SIZE);
		final int tY = (int) (p.getY() / Game.SIZE);
		OrderedMap<Byte, int[]> tilesBelow = new OrderedMap<>();
		for (int x = tX - 1; x <= tX + Player.TILE_WIDTH; x++) {
			for (int y = tY; y >= tY - 2; y--) {
				if (x >= 0 && x < Game.getLevel().getMapWidth() && y >= 0 && y < Game.getLevel().getMapHeight()) {
					TileConfig tile = TileConfig.getTile(Game.getLevel().getTileAt(x, y));
					//If the tile is directly under the player
					if (y * Game.SIZE + tile.getHeight() >= p.getY()) {
						//The current tile's location in game coordinates
						int tileX = x * Game.SIZE;
						//If the player is not to the left or right of the tile (horizontal collision detection)
						if (p.getX() + Player.WIDTH > tileX && p.getX() < tileX + tile.getWidth()) {
							if (tile.singlePreCollision()) {
								tilesBelow.clear();
								tilesBelow.put(tile.getID(), new int[] {x, y});
								return tilesBelow;
							} else {
								tilesBelow.put(tile.getID(), new int[] {x, y});
							}
						}
					}
				}
			}
		}
		//If there are multiple tiles below and air is one of them, remove the air since it won't do anything
		if (tilesBelow.size > 1) {
			ObjectMap.Entries<Byte, int[]> tilesBelowIterator = tilesBelow.iterator();
			while (tilesBelowIterator.hasNext) {
				ObjectMap.Entry<Byte, int[]> tileAt = tilesBelowIterator.next();
				if (!TileConfig.getTile(tileAt.key).isLiquid() && !TileConfig.getTile(tileAt.key).isSolid()) {
					tilesBelowIterator.remove();
				}
			}
		}
		if (tilesBelow.size == 0) tilesBelow.put(TileConfig.air.getID(), new int[] {0, 0});
		return tilesBelow;
	}
	public static void itemCollision(Player p) {
		Rectangle pRect = new Rectangle(p.getX(), p.getY(), Player.WIDTH, Player.HEIGHT);
		for (int i = Game.getLevel().getItemCount() - 1; i >= 0; i--) {
			short[] item = Game.getLevel().getItemAt(i);
			if (ItemConfig.getItem((byte) item[2]).isRepeatable() || !p.hasItem((byte) item[2])) {
				Rectangle itemRect = new Rectangle(item[0], item[1], Game.SIZE, Game.SIZE);
				if (pRect.overlaps(itemRect)) {
					Game.getLevel().removeItem((short) i, true);
					p.addGatheredItem((byte) item[2], true);
				}
			}
		}
	}
	public static final void playerProjectileCollide(Player p, boolean serverSide) {
		Rectangle pRect = new Rectangle(p.getX(), p.getY(), Player.WIDTH, Player.HEIGHT);
		for (int i = ProjectileHandler.projectileCount() - 1; i >= 0; i--) {
			if (pRect.contains(ProjectileHandler.getProjectile(i).getX() + Gdx.graphics.getDeltaTime() * ProjectileHandler.getProjectile(i).getXDir(), ProjectileHandler.getProjectile(i).getY() + Gdx.graphics.getDeltaTime() * ProjectileHandler.getProjectile(i).getYDir())) {
				if (serverSide && p.getTeam() != ProjectileHandler.getProjectile(i).getTeam()) {
					if (p.hurt(ProjectileConfig.getProjectile(ProjectileHandler.getProjectile(i).getID()).getDamage())) {
						SendMessage.sendHealthToClients(p.getClientID(), p.getHealth());
					}
					if (p.getHealth() <= 0) {
						boolean team = ProjectileHandler.getProjectile(i).getTeam();
						((GameServer) Game.getGame()).setScore(team, (short) (((GameServer) Game.getGame()).getScore(team) + 1));
					}
				}
				if (!serverSide && ProjectileHandler.getProjectile(i).getID() == ProjectileConfig.gunshot.getID()) {
					ProjectileHandler.addImpactParticles(ProjectileHandler.getProjectile(i));
				}
				ProjectileHandler.removeProjectile(i);
				return;
			}
		}
	}
	public static boolean tileProjectileCollide(Projectile projectile, float[] dir) {
		float pX = projectile.getX() / Game.SIZE;
		float pY = projectile.getY() / Game.SIZE;
		ProjectileConfig projConfig = ProjectileConfig.getProjectile(projectile.getID());
		Polygon projPolygon = null;
		if (projConfig.isImage()) {
			projPolygon = new Polygon();
			projPolygon.setPosition(projectile.getX(), projectile.getY());
			projPolygon.setVertices(new float[] {0, 0, 0, projConfig.getHeight(), projConfig.getWidth(), projConfig.getHeight(), projConfig.getWidth(), 0});
			projPolygon.setOrigin(projConfig.getWidth() / 2, projConfig.getHeight() / 2);
			projPolygon.setRotation(projectile.getRotation());
		}
		for (int x = (int) pX - 1; x < pX + 2; x++) {
			//If tile position is outside the map
			if (x < 0 || x >= Game.getLevel().getMapWidth()) continue;
			for (int y = (int) pY - 1; y < pY + 2; y++) {
				//If the tile y position is within bounds and the tile is not a platform or air-type, check collision between projectile and this tile
				if (y >= 0 && y < Game.getLevel().getMapHeight() && TileConfig.getTile(Game.getLevel().getTileAt(x, y)).isSolid())	{
					TileConfig tileAtCoord = TileConfig.getTile(Game.getLevel().getTileAt(x, y));
					//If projectile is an image, do rotated rectangle collision using polygons
					if (projConfig.isImage()) {
						if (tileAtCoord.overlapsPolygon(projPolygon, x, y)) {
							return true;
						}
					} else {
						if (tileAtCoord.containsPoint(x, y, projectile.getX() + dir[0], projectile.getY() + dir[1])) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	/** Checks if the given entity will collide with any tiles or items.
	 * Adjusts the speed of the entity so it will not collide.
	 * Removes items that the player picks up
	 * @return True if player position should be updated.
	 */
	public static void checkCollision(Player p, float yDir) {
		tileCollision(p, yDir);
		itemCollision(p);
		playerProjectileCollide(p, true);
	}
	private static void tileCollision(Player p, float yDir) {
		if (p.getXDir() != 0 || p.getYDir() != 0) {
			canStep = true;
			byte collisionType = collides(p, new Rectangle(p.getX() + p.getXDir(), p.getY() + p.getYDir(), Player.WIDTH, Player.HEIGHT), yDir);
			if (collisionType == X_COL && p.getYDir() == 0 && p.getY() % Game.SIZE == 0) {
				if (p.getX() % Game.SIZE == 0) {
					if (canStep && (liquidState == NO_LIQUID || liquidState == PARTIAL_LIQUID)) {
						tryToStep(p, p.getX() - 1, p.getY() + Game.SIZE);
					}
				}
				else if ((p.getX() + Player.WIDTH) % Game.SIZE == 0) {
					if (canStep && (liquidState == NO_LIQUID || liquidState == PARTIAL_LIQUID)) {
						tryToStep(p, p.getX() + 1, p.getY() + Game.SIZE);
					}
				}
			}
			if (liquidState != NO_LIQUID) {
				TileConfig.getTile(liquidTile).postCollision(p, 0, 0, 0);
			}
		}
	}
	private static void tryToStep(Player p, float newX, float newY) {
		float oldX = p.getX();
		float oldY = p.getY();
		p.setX(newX);
		p.setY(newY);
		if (!fastCollides(p, new Rectangle(p.getX(), p.getY(), Player.WIDTH, Player.HEIGHT))) {
			p.setXDir(0);
		} else {
			p.setX(oldX);
			p.setY(oldY);
		}
	}
	public static boolean playerOnGround(Player p) {
		return p.getY() % Game.SIZE == 0;
	}
	public static byte getLiquidState() {
		return liquidState;
	}
	public static void stopStepping() {
		canStep = false;
	}
}