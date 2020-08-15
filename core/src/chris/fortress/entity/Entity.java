package chris.fortress.entity;

/**A base class for everything in the game that has an x, y, x speed, y speed, and belongs to a team*/
public abstract class Entity {
	public static final float MAX_SPEED = 20;
	
	private float x, y, xDir, yDir;
	private boolean team;
	
	public Entity(float x, float y, float xDir, float yDir, boolean team) {
		this.x = x;
		this.y = y;
		this.xDir = xDir;
		this.yDir = yDir;
		this.team = team;
	}
	public float getX() {
		return x;
	}
	public void setX(float x) {
		this.x = x;
	}
	public float getY() {
		return y;
	}
	public void setY(float y) {
		this.y = y;
	}
	public float getXDir() {
		return xDir;
	}
	public void setXDir(float xDir) {
		this.xDir = xDir;
	}
	public float getYDir() {
		return yDir;
	}
	public void setYDir(float yDir) {
		this.yDir = yDir;
	}
	public boolean getTeam() {
		return team;
	}
	public void setTeam(boolean team) {
		this.team = team;
	}
}