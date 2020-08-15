package chris.fortress.util;

/**A rectangle class for rectangle-circle collision*/
public class CustomRectangle {
	private float x1, y1, x2, y2;
	
	public CustomRectangle(float x1, float y1, float x2, float y2) {
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}
	/**
	 * Returns true if this rectangle overlaps with the circle
	 */
	public final boolean overlaps(float x, float y, short radius) {
	    float cX = Math.abs(x - (x1 + x2) / 2f);
	    float cY = Math.abs(y - (y1 + y2) / 2f);
	    float hW = (x2 - x1) / 2f;
	    float hH = (y2 - y1) / 2f;
	    if (cX > (hW + radius)) return false;
	    if (cY > (hH + radius)) return false;

	    if (cX <= (hW)) return true; 
	    if (cY <= (hH)) return true;

	    float distance = (float) (Math.pow(cX - hW, 2) + Math.pow(cY - hH, 2));

	    return distance <= radius * radius;
	}
}