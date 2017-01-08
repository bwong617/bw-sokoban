import java.awt.Image;
import java.awt.Toolkit;

public class Entity {
	
	int x, y;
	Image image;
	String imageURL;
	
	public Entity (int x, int y)
    {
		this.x = x; // x-coordinate
		this.y = y; // y-coordinate
    }
	public void loadImage (String imageURL){
		this.image = Toolkit.getDefaultToolkit().getImage(imageURL);
	}
	public static boolean EntityCollision (Entity A, Entity B, Direction dir, int T){
		if (dir == Direction.LEFT  && (A.x == B.x + T) && (A.y == B.y) ||
			dir == Direction.RIGHT && (A.x == B.x - T) && (A.y == B.y) ||
			dir == Direction.UP    && (A.x == B.x) && (A.y == B.y + T) ||
			dir == Direction.DOWN  && (A.x == B.x) && (A.y == B.y - T))
			return true;
		else
			return false;
	}
	public static boolean EntityOverlap (Entity A, Entity B){
		if ((A.x == B.x) && (A.y == B.y))
			return true;
		else
			return false;
	}
}
