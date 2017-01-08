public class Block extends Wall
{
	public Block (int x, int y)
	{
		super (x, y);
		this.imageURL = "images/Wall_Block.png";
		super.loadImage(imageURL);
	}
	public void setImage(int state){
		switch (state){
			case 0: imageURL = "images/Wall_Block.png"; break;
			case 1: imageURL = "images/Wall_Block_Set.png"; break;
		}
		super.loadImage(imageURL);
	}
}
