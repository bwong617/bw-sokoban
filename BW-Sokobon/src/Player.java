public class Player extends Entity
{
	Direction move_dir;
	
	public Player (int x, int y)
    {
		super(x, y);
		this.imageURL = "images/Player.png";
		super.loadImage(imageURL);
		this.move_dir = Direction.NONE;
    }
}
