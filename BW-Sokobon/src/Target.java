public class Target extends Entity{
	
	int state;
	
	public Target (int x, int y, int s)
    {
		super(x, y);
		this.imageURL = "images/Target.png";
		super.loadImage(imageURL);
		this.state = 0;
    }
	
}
