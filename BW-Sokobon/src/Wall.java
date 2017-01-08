public class Wall extends Entity
{
    public Wall (int x, int y)
    {
		super(x, y);
		this.imageURL = "images/Wall_Solid.png";
		super.loadImage(imageURL);
    }
}
