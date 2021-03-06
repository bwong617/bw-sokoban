import java.applet.*;
import java.awt.*;
import java.awt.event.*;
//import java.util.Random;
import java.awt.Image;
//import java.io.File;
//import java.net.*;

/*TODO
 * - stage boundary conditions
 * - sticky when changing directions
 * - cleanup old code
 * - make goal entity
 */

public class Sokobon extends Applet implements KeyListener, Runnable
{
	int R = 2; 		//resolution
	int T = R*10; 	//tile dimension
	
    boolean pressed, left, right, up, down;
    boolean escape, select;
    boolean leftWall, rightWall, upWall, downWall;
    
    int target_active_count;
    boolean goal_active;
    boolean level_win;
    boolean level_lose;
    
    AppState state;
    Direction menu_select_dir;
    
    int stage_x = 0;
    int stage_y = 0;
    int stage_width = 15;
    int stage_height = 15;
    int max_stage_width = 15;
    int max_stage_height = 15;
    
    int sidebar_x = stage_x + stage_width*T;
    int sidebar_y = 0;
    
    int window_width = sidebar_x + 50*R;
    int window_height = max_stage_height*T;
    
    //int player_x = stage_x + 3*T;
    //int player_y = stage_y + 3*T;
    Player player = new Player(3*T,3*T);
    
    int goal_x = stage_x + 1*T;
    int goal_y = stage_y + 1*T;
    
    int timeUnit = 0;
    
    // IMAGES
    Image floor_tile_image, player_image, wall_solid_image, wall_block_image;
    Image wall_block_key_image, wall_block_set_image, target_image, goal_active_image, goal_locked_image;
    Image sidebar_image, menu_image, menu_option_image, menu_option_selected_image;
    
    // FONTS
    Font menu_title_font = new Font("Century Gothic", Font.BOLD, 12*R);
    Font menu_options_font = new Font("Century Gothic", Font.BOLD, 10*R);
    Font sidebar_plain_font = new Font("Century Gothic", Font.PLAIN, 8*R);
    Font sidebar_bold_font = new Font("Century Gothic", Font.BOLD, 8*R);
    
    ///////////////////
    int currentLevel = 0;
    boolean beginLevel = false, LevelSelected = false;
    int oneSecond, gameTime = 0, timeLimit = 100;
	///////////////////

    // GRAPHICS BUFFER
    Image backbuffer;
    Graphics bg;
    Thread t = null;
    
    int[][] WallArray = {
	//  { x, y, type}
    	{ 0, 0}, { 1, 0}, { 2, 0}, { 3, 0}, { 4, 0}, { 5, 0}, { 6, 0}, { 7, 0}, { 8, 0}, { 9, 0},
        { 0, 1}, { 9, 1},
        { 0, 2}, { 9, 2}, 
        { 0, 3}, { 9, 3},
        { 0, 4}, { 9, 4}, { 4, 4}, { 5, 4}, 
        { 0, 5}, { 9, 5}, { 4, 5}, { 5, 5}, 
        { 0, 6}, { 9, 6}, 
        { 0, 7}, { 9, 7}, 
    	{ 0, 8}, { 9, 8},
    	{ 0, 9}, { 1, 9}, { 2, 9}, { 3, 9}, { 4, 9}, { 5, 9}, { 6, 9}, { 7, 9}, { 8, 9}, { 9, 9}
	    /*{ 0, 0, 0}, { 1, 0, 0}, { 2, 0, 0}, { 3, 0, 0}, { 4, 0, 0}, { 5, 0, 0}, { 6, 0, 0}, { 7, 0, 0}, { 8, 0, 0}, { 9, 0, 0},
	    { 0, 1, 0}, { 9, 1, 0},
	    { 0, 2, 0}, { 9, 2, 0}, 
	    { 0, 3, 0}, { 9, 3, 0},
	    { 0, 4, 0}, { 9, 4, 0}, { 4, 4, 0}, { 5, 4, 0}, 
	    { 0, 5, 0}, { 9, 5, 0}, { 4, 5, 0}, { 5, 5, 0}, 
	    { 0, 6, 0}, { 9, 6, 0}, 
	    { 0, 7, 0}, { 9, 7, 0}, 
	    { 0, 8, 0}, { 9, 8, 0},
	    { 0, 9, 0}, { 1, 9, 0}, { 2, 9, 0}, { 3, 9, 0}, { 4, 9, 0}, { 5, 9, 0}, { 6, 9, 0}, { 7, 9, 0}, { 8, 9, 0}, { 9, 9, 0}
		*/
	};
    
    int[][] BlockArray = {
    		{ 5, 2},
    		{ 5, 3},
    		{ 5, 6},
    		{ 5, 7},
    };
    
    int[][] TargetArray = {
    //  { x, y, s}
    	{ 2, 2, 0}, { 7, 2, 0},
    	{ 2, 7, 0}, { 7, 7, 0}
    };

    Wall[] WALL = new Wall [WallArray.length];
    Block[] BLOCK = new Block [BlockArray.length];
    Target[] TARGET = new Target [TargetArray.length];

    public void init ()
    {
		setSize (window_width, window_height);
		addKeyListener (this);
		backbuffer = createImage (window_width, window_height);
		bg = backbuffer.getGraphics ();

		///////////////////////
		/*try{
    		File f = new File (getCodeBase().toURI());
    		for (int i = 0; i<2;i++){
    			LEVEL[i] = new LevelCode(f.getParentFile(), i+1);
    		}
    	}catch(URISyntaxException FE){
    		FE.printStackTrace();
    	}*/
		///////////////////////
		
		//int[][] StageArray = new int[stage_width][stage_height];
		//for (int i = 0; i < stage_width; i++){
		//	for (int j = 0; j < stage_height; j++){
		//		StageArray[i][j] = 0;
		//	}
		//}
		
		
		for (int i = 0 ; i < WallArray.length ; i++)
		{
		    //Wall nWALL = new Wall (stage_x + (WallArray [i] [0])*T, stage_y + (WallArray [i] [1])*T, WallArray [i] [2]);
			Wall nWALL = new Wall (stage_x + (WallArray [i] [0])*T, stage_y + (WallArray [i] [1])*T);
			WALL [i] = nWALL;
		}
		
		for (int i = 0 ; i < BlockArray.length ; i++)
		{
		    Block nBLOCK = new Block (stage_x + (BlockArray [i] [0])*T, stage_y + (BlockArray [i] [1])*T);
		    BLOCK [i] = nBLOCK;
		}
		
		for (int i = 0 ; i < TargetArray.length ; i++)
		{
		    Target nTARGET = new Target (stage_x + (TargetArray [i] [0])*T, stage_y + (TargetArray [i] [1])*T, TargetArray [i] [2]);
		    TARGET [i] = nTARGET;
		}

		
		
		
		//floor_image = getImage (getCodeBase (), "images/Floor.png");
		floor_tile_image = getImage (getCodeBase (), "images/Floor_Tile.png");
		player_image = getImage (getCodeBase (), "images/Player.png");
		wall_solid_image = getImage (getCodeBase (), "images/Wall_Solid.png");
		wall_block_image = getImage (getCodeBase (), "images/Wall_Block.png");
		wall_block_key_image = getImage (getCodeBase (), "images/Wall_Block_Key.png");
		wall_block_set_image = getImage (getCodeBase (), "images/Wall_Block_Set.png");
		target_image = getImage (getCodeBase (), "images/Target.png");
		goal_active_image = getImage (getCodeBase (), "images/Goal_Active.png");
		goal_locked_image = getImage (getCodeBase (), "images/Goal_Locked.png");
		
		sidebar_image = getImage (getCodeBase (), "images/Sidebar.png");
		menu_image = getImage (getCodeBase (), "images/Menu.png");
		menu_option_image = getImage (getCodeBase (), "images/Menu_Option.png");
		menu_option_selected_image = getImage (getCodeBase (), "images/Menu_Option_Selected.png");
		
		state = AppState.GAME_ACTIVE;
		
		level_win = false;
		level_lose = false;
    }


    public void start ()
    {
    	new Thread (this).start ();
    }
    
    public void run ()
    {
		try
		{
		    while (true)
		    {
		    	///////////////////////////
		    	/*if (beginLevel)
    			{
    				LEVEL [currentLevel].reload();
    				beginLevel = false;
    				LevelSpecific ();
    			}*/
		    	////////////////////////////
		    	
		    	// GRAPHICS BACKBUFFER
				bg.setColor (Color.white);
				bg.fillRect (0, 0, window_width, window_height);
				//bg.drawImage (floor_image, 0, 0, 10*T, 10*T, this);
				
				//if (!gameplay_active){
				//	bg.drawImage (menu_image, stage_x, stage_y, 15*T, 15*T, this);
				//}
				
//				if (state == State.MAIN_MENU)
//				{
//					bg.drawImage (menu_image, stage_x, stage_y, 15*T, 15*T, this);
//					repaint ();
//				}
				if (state == AppState.GAME_ACTIVE)
				{
					// DRAW STAGE
					for (int i = 0 ; i < stage_width ; i++)
					{
						for (int j = 0 ; j < stage_height ; j++)
						{
							bg.drawImage (floor_tile_image, stage_x + i*T, stage_y + j*T, T, T, this);
						}
					}
					
					// GAMEPLAY FUNCTIONS
					
					// Player -> Solid Wall
					// Player -> Block
						// Block -> Solid Wall
						// Block -> Block
					
					if (player.move_dir != Direction.NONE){
						for (int i = 0 ; i < WallArray.length ; i++)
						{
							if (Entity.EntityCollision(player, WALL[i], player.move_dir, T))
								player.move_dir = Direction.NONE;
						}
					}
					if (player.move_dir != Direction.NONE)
					{
						for (int i = 0 ; i < BlockArray.length ; i++)
						{
							if (Entity.EntityCollision(player, BLOCK[i], player.move_dir, T)){
								for (int j = 0 ; j < WallArray.length ; j++)
								{
									if (Entity.EntityCollision(BLOCK[i], WALL[j], player.move_dir, T))
										player.move_dir = Direction.NONE;
								}
								for (int j = 0 ; j < BlockArray.length ; j++)
								{
									if (Entity.EntityCollision(BLOCK[i], BLOCK[j], player.move_dir, T))
										player.move_dir = Direction.NONE;
								}
								if (player.move_dir != Direction.NONE)
									MoveEntity(BLOCK[i], player.move_dir);
							}
						}
					}
					
					for (int i = 0 ; i < WallArray.length ; i++)
						bg.drawImage (WALL [i].image, WALL [i].x, WALL [i].y, T, T, this);
					for (int i = 0 ; i < BlockArray.length ; i++)
						bg.drawImage (BLOCK [i].image, BLOCK [i].x, BLOCK [i].y, T, T, this);
					
					MoveEntity(player, player.move_dir);
					
					bg.drawImage (player.image, player.x, player.y, T, T, this);
					
					//////
					
					
					//WallCollision ();
					//PlayerMovement ();
					CheckTargets ();
					UpdateTimer ();
					
					repaint ();
					
					if (level_win)
						state = AppState.GAME_WIN;
					if (level_lose)
						state = AppState.GAME_LOSE;
				}
				else if (state == AppState.GAME_LOSE)
				{
					while (true){
						bg.drawImage (menu_image, stage_x, stage_y, 15*T, 15*T, this);
						repaint ();
					}
				}
				
				Thread.sleep (100);
			}
		}
		catch (InterruptedException ie){}
    }
    
    public void MoveEntity (Entity E, Direction dir){
    	switch (dir){
    	case LEFT:
    		// if the player will not exit the stage boundaries
    		if (E.x > stage_x)
    			// move the player left one tile
    			E.x -= T;
    		break;
    	case RIGHT:
    		if (E.x < stage_x + (stage_width - 1)*T)
    			E.x += T;
    		break;
    	case UP:
    		if (E.y > stage_y)
    			E.y -= T;
    		break;
    	case DOWN:
    		if (E.y < stage_y + (stage_height - 1)*T)
    			E.y += T;
    		break;
    	case NONE:
    		break;
    	}
    }
    
    /*
    public void WallCollision ()
    {
    	leftWall = false;
		rightWall = false;
		upWall = false;
		downWall = false;
		
		// for each wall entity
		for (int i = 0 ; i < WallArray.length ; i++)
		{
			if (WALL [i].type == 0) // Type 0: Solid Wall
    		{
				// if the player is trying to go left
				if (left)
				{
					// if there is a solid wall one tile to the left of the player
			    	if ((player_x == WALL [i].x + T) && (player_y == WALL [i].y))
				    	leftWall = true; // don't allow the player to move
				}
				if (right)
				{
					if ((player_x == WALL [i].x - T) && (player_y == WALL [i].y))
				    	rightWall = true;
				}
				if (up)
				{
					if ((player_x == WALL [i].x) && (player_y == WALL [i].y + T))
				    	upWall = true;
				}
				if (down)
				{
					if ((player_x == WALL [i].x) && (player_y == WALL [i].y - T))
				    	downWall = true;
				}
				
				// draw the wall
				bg.drawImage (wall_solid_image, WALL [i].x, WALL [i].y, T, T, this);
    		}
			else if (WALL [i].type == 1) // Type 1: Movable Block
    		{
				// if the player is trying to go left
				if (left)
				{
					// if there is a movable block one tile to the left of the player
					if ((player_x == WALL [i].x + T) && (player_y == WALL [i].y))
	    			{
	    				for (int j = 0 ; j < WallArray.length ; j++)
	    				{
	    					// if there is a wall entity to the left of the movable block
    				    	if ((WALL [i].x == WALL [j].x + T) && (WALL [i].y == WALL [j].y))
    					    	leftWall = true; // don't allow the player to move
	    				}
	    				// if the stage boundary is to the left of the movable block
	    				if (WALL [i].x <= stage_x)
	    		    		leftWall = true; // don't allow the player to move
	    				
	    				// if the player is allowed to move, also allow the movable block to move with the player
	    				if (!leftWall)
	    		    		WALL [i].x -= T;
	    			}
				}
				else if (right)
				{
					if ((player_x == WALL [i].x - T) && (player_y == WALL [i].y))
	    			{
	    				for (int j = 0 ; j < WallArray.length ; j++)
	    				{
    					    if ((WALL [i].x == WALL [j].x - T) && (WALL [i].y == WALL [j].y))
    					    	rightWall = true;
	    				}
	    				if (WALL [i].x >= stage_x + (stage_width - 1)*T)
	    					rightWall = true;

	    				if (!rightWall)
	    		    		WALL [i].x += T;
	    			}
				}
				else if (up)
				{
					if ((player_x == WALL [i].x) && (player_y == WALL [i].y + T))
	    			{
	    				for (int j = 0 ; j < WallArray.length ; j++)
	    				{
    					    if ((WALL [i].x == WALL [j].x) && (WALL [i].y == WALL [j].y + T))
    					    	upWall = true;
	    				}
	    				if (WALL [i].y <= stage_y)
	    					upWall = true;

	    				if (!upWall)
	    					WALL [i].y -= T;
	    			}
				}
				else if (down)
				{
					if ((player_x == WALL [i].x) && (player_y == WALL [i].y - T))
	    			{
	    				for (int j = 0 ; j < WallArray.length ; j++)
	    				{
    					    if ((WALL [i].x == WALL [j].x) && (WALL [i].y == WALL [j].y - T))
    					    	downWall = true;
	    				}
	    				if (WALL [i].y >= stage_y + (stage_height - 1)*T)
	    					downWall = true;

	    				if (!downWall)
	    					WALL [i].y += T;
	    			}
				}
				
				bg.drawImage (wall_block_key_image, WALL [i].x, WALL [i].y, T, T, this);
    		}
		}
    }
    */
    /*
    public void WallCollision ()
    {
    	leftWall = false;
		rightWall = false;
		upWall = false;
		downWall = false;
    	
    	for (int i = 0 ; i < WallArray.length ; i++)
		{
    		if (WALL [i].t == 0) //Type 0: Solid Wall
    		{
    			WallCollision (player_x, player_y, true);
    			bg.drawImage (wall_solid_image, WALL [i].x, WALL [i].y, T, T, this);
    		}
    		else if (WALL [i].t == 1) //Type 1: Movable Block
    		{
    			if ((player_x == WALL [i].x + T) && (player_y == WALL [i].y) && left && !leftWall)
    			{
    				WallCollision (WALL [i].x, WALL [i].y, false);
    				if (WALL [i].x > 0 && !leftWall)
    		    		WALL [i].x -= T;
    			}
    			else if ((player_x == WALL [i].x - T) && (player_y == WALL [i].y) && right && !rightWall)
    			{
    				WallCollision (WALL [i].x, WALL [i].y, false);
    				if (WALL [i].x < (stage_width - 1)*T && !rightWall)
    					WALL [i].x += T;
    			}
    			else if ((player_x == WALL [i].x) && (player_y == WALL [i].y + T) && up && !upWall)
    			{
    				WallCollision (WALL [i].x, WALL [i].y, false);
    				if (WALL [i].y > 0 && !upWall)
    					WALL [i].y -= T;
    			}
    			else if ((player_x == WALL [i].x) && (player_y == WALL [i].y - T) && down && !downWall)
    			{
    				WallCollision (WALL [i].x, WALL [i].y, false);
    				if (WALL [i].y < (stage_height - 1)*T && !downWall)
    					WALL [i].y += T;
    			}
			    bg.drawImage (wall_block_image, WALL [i].x, WALL [i].y, T, T, this);
    		}	
		}
    }
    
    public void WallCollision (int obj_x, int obj_y, boolean player)
    {
    	for (int i = 0 ; i < WallArray.length ; i++)
		{
    		if ((player && WALL [i].t == 0) || !player) //The player only collides with solid walls
    		{
		    	if ((obj_x == (WALL [i].x + T)) && (obj_y == WALL [i].y))
			    	leftWall = true;
			    if ((obj_x == (WALL [i].x - T)) && (obj_y == WALL [i].y))
			    	rightWall = true;
			    if ((obj_x == WALL [i].x) && (obj_y == WALL [i].y + T))
			    	upWall = true;
			    if ((obj_x == WALL [i].x) && (obj_y == WALL [i].y - T))
			    	downWall = true;
    		}
		}
    }  
    */
    /*
    public void PlayerMovement () // cannot up/down if against a side wall
    {
    	// if the player is trying to go left
    	switch (player.move_dir){
    	case LEFT:
    		// if the player will not exit the stage boundaries
    		if (player.x > stage_x && !leftWall)
    			// move the player left one tile
    			player.x -= T;
    		break;
    	case RIGHT:
    		if (player.x < stage_x + (stage_width - 1)*T && !rightWall)
    			player.x += T;
    		break;
    	case UP:
    		if (player.y > stage_y && !upWall)
    			player.y -= T;
    		break;
    	case DOWN:
    		if (player.y < stage_y + (stage_height - 1)*T && !downWall)
    			player.y += T;
    		break;
    	case NONE:
    		break;
    	}
	    // draw the player
		bg.drawImage (player.image, player.x, player.y, T, T, this);
    }
    */
    
    public void CheckTargets ()
    {
    	target_active_count = 0;
    	
    	for (int i = 0 ; i < TargetArray.length ; i++)
		{
    		TARGET [i].state = 0;
			//bg.drawImage (target_image_inactive, TARGET [i].x, TARGET [i].y, T, T, this);
			
    		for (int j = 0; j < BlockArray.length; j++)
    		{
    			if (Entity.EntityOverlap(BLOCK[j], TARGET[i]))
    			{
    				TARGET[i].state = 1;
    				BLOCK[j].setImage(TARGET[i].state);
    				bg.drawImage (BLOCK [j].image, BLOCK [j].x, BLOCK [j].y, T, T, this);
    				target_active_count++;
    			}
    		}
    		bg.drawImage (TARGET [i].image, TARGET [i].x, TARGET [i].y, T, T, this);
		}
    	
    	if (target_active_count == TargetArray.length)
    	{
    		goal_active = true;
    		bg.drawImage (goal_active_image, goal_x, goal_y, T, T, this);
    	}
    	else
    	{
    		goal_active = false;
    		bg.drawImage (goal_locked_image, goal_x, goal_y, T, T, this);
    	}
    	
    	if (goal_active && player.x == goal_x && player.y == goal_y)
    		level_win = true;
    		
    }
    
    public void UpdateTimer ()
    {
    	bg.drawImage (sidebar_image, sidebar_x, sidebar_y, 50*R, 150*R, this);
		
		oneSecond++;
		if (oneSecond == 10)
        {
        	gameTime++;
        	oneSecond = 0;
        }
		
		bg.setColor (Color.red);
        bg.setFont (sidebar_bold_font);
        bg.drawString ("Time: " + (timeLimit - gameTime), sidebar_x + T/2, T/8 + T);
		
        if ((timeLimit - gameTime) == 0)
        	level_lose = true;
    }
    
    public void keyPressed (KeyEvent e){
    	int key = e.getKeyCode ();
    	if (state == AppState.MAIN_MENU){
    		if (key == KeyEvent.VK_ENTER)								{pressed = true; select = true;}
    		if (key == KeyEvent.VK_UP 		|| key == KeyEvent.VK_W)	{pressed = true; up = true;}
    		if (key == KeyEvent.VK_DOWN 	|| key == KeyEvent.VK_S)	{pressed = true; down = true;}
    		
    		if (key == KeyEvent.VK_LEFT 	|| key == KeyEvent.VK_A)	{pressed = true; left = true;}
    		if (key == KeyEvent.VK_RIGHT 	|| key == KeyEvent.VK_D)	{pressed = true; right = true;}
    	}
    	else if (state == AppState.GAME_ACTIVE){
    		if (key == KeyEvent.VK_UP 		|| key == KeyEvent.VK_W)	{player.move_dir = Direction.UP;}
    		if (key == KeyEvent.VK_LEFT 	|| key == KeyEvent.VK_A)	{player.move_dir = Direction.LEFT;}
    		if (key == KeyEvent.VK_DOWN 	|| key == KeyEvent.VK_S)	{player.move_dir = Direction.DOWN;}
    		if (key == KeyEvent.VK_RIGHT 	|| key == KeyEvent.VK_D)	{player.move_dir = Direction.RIGHT;}
    		
    		if (key == KeyEvent.VK_ESCAPE)								{pressed = true; escape = true;}
        }
    	else if (state == AppState.VIEW_PAGE){
    		if (key == KeyEvent.VK_ESCAPE)								{pressed = true; escape = true;}
    	}
    	else {
    		if (key == KeyEvent.VK_UP 		|| key == KeyEvent.VK_W)	{pressed = true; up = true;}
    		if (key == KeyEvent.VK_DOWN 	|| key == KeyEvent.VK_S)	{pressed = true; down = true;}
        }
    	
    }
    
    public void keyReleased (KeyEvent e){
	    int key = e.getKeyCode ();

	    if (key == KeyEvent.VK_LEFT 	|| key == KeyEvent.VK_A ||
			key == KeyEvent.VK_UP 		|| key == KeyEvent.VK_W ||
			key == KeyEvent.VK_RIGHT 	|| key == KeyEvent.VK_D ||
			key == KeyEvent.VK_DOWN 	|| key == KeyEvent.VK_S)
		{
	    	player.move_dir = Direction.NONE;
	    	menu_select_dir = Direction.NONE;
		}
		if (key == KeyEvent.VK_ENTER)								{pressed = false; select = false;}
		if (key == KeyEvent.VK_ESCAPE)								{pressed = false; escape = false;}
    }
    
    public void keyTyped (KeyEvent e)
    {
    }
    
    public void update (Graphics g)
    {
    	g.drawImage (backbuffer, 0, 0, this);
    }
    
    public void paint (Graphics g)
    {
    	update (g);
    }
}



/*
public class Gamma_Jaeve extends Applet implements KeyListener, Runnable
{

    boolean pressed, left, right, up, down, gameOver = false;
    int x = 500, y = 250, timeUnit = 0, score = 0;
    int stage = 3, addStage = 0;
    int numToken = 1, addToken = 0;
    int freezeTime = 0, invincible = 0;
    Random rd = new Random ();
    Color darkGreen = new Color (0, 50, 0);
    Color purple = new Color (50, 0, 50);
    Color tokenBlue = new Color (180, 200, 250);
    Color tokenGreen = new Color (100, 250, 120);
    Color yellow = new Color (254, 254, 0);
    Image FTokenImage, ITokenImage, ScoreTokenImage;

    Image backbuffer;
    Graphics bg;
    Thread t = null;
    int width = 1000, height = 500;

    // 1 - blue (normal)
    // 2 - orange (chaser)
    // 3 - green (non-square)
    // 4 - purple (speedy)

    int[] [] EnemyData = {
*/
	/*
		//  {i, x, y,dx,dy, lx, ly, s, t},
		    {0, 0, 0, 0, 0, 50, 50, 1, 1},  //0
		    {0, 0, 0, 0, 0, 25, 25, 2, 1},  //0
		    {0, 0, 0, 0, 0, 25, 25, 3, 1},  //0
		    {0, 0, 0, 0, 0, 10, 10, 1, 2},  //1000
		    {0, 0, 0, 0, 0, 50, 50, 4, 1},  //2000
		    {0, 0, 0, 0, 0, 15, 15, 5, 4},  //3000
		    {0, 0, 0, 0, 0, 15, 15, 6, 4},  //4000
		    {0, 0, 0, 0, 0, 5, 100, 3, 3},  //5000
		    {0, 0, 0, 0, 0, 100, 5, 3, 3},  //6000
		    {0, 0, 0, 0, 0, 25, 25, 1, 2},  //7000
		    {0, 0, 0, 0, 0, 15, 15, 7, 4},  //8000
		    {0, 0, 0, 0, 0, 15, 15, 8, 4},  //9000
		    {0, 0, 0, 0, 0, 50, 50, 5, 1},  //10000
		    {0, 0, 0, 0, 0, 80, 40, 1, 3},  //11000
		    {0, 0, 0, 0, 0, 40, 80, 1, 3},  //12000
		    {0, 0, 0, 0, 0, 25, 25, 2, 2},  //13000
		    {0, 0, 0, 0, 0, 15, 15, 9, 4},  //14000
	*/

	/*
	//  {i, x, y,dx,dy, lx, ly, s, t},
	    {0, 0, 0, 0, 0, 50, 50, 1, 1},  //0
	    {0, 0, 0, 0, 0, 50, 50, 1, 1},  //0
	    {0, 0, 0, 0, 0, 50, 50, 1, 1},  //0
	    {0, 0, 0, 0, 0, 50, 50, 1, 1},  //1000
	    {0, 0, 0, 0, 0, 50, 50, 1, 1},  //2000
	    {0, 0, 0, 0, 0, 50, 50, 2, 1},  //3000
	    {0, 0, 0, 0, 0, 50, 50, 2, 1},  //4000
	    {0, 0, 0, 0, 0, 50, 50, 2, 1},  //5000
	    {0, 0, 0, 0, 0, 50, 50, 2, 1},  //6000
	    {0, 0, 0, 0, 0, 50, 50, 2, 1},  //7000
	    {0, 0, 0, 0, 0, 50, 50, 3, 1},  //8000
	    {0, 0, 0, 0, 0, 50, 50, 3, 1},  //9000
	    {0, 0, 0, 0, 0, 50, 50, 3, 1},  //10000
	    {0, 0, 0, 0, 0, 50, 50, 3, 1},  //11000
	    {0, 0, 0, 0, 0, 50, 50, 3, 1},  //12000
	    {0, 0, 0, 0, 0, 50, 50, 4, 1},  //13000
	    {0, 0, 0, 0, 0, 50, 50, 4, 1},  //14000
	*/
/*
	//  {i, x, y,dx,dy, lx, ly, s, t},
	    {0, 0, 0, 0, 0, 75, 75, 1, 1},  //0
	    {0, 0, 0, 0, 0, 50, 50, 2, 1},  //0
	    {0, 0, 0, 0, 0, 25, 25, 3, 1},  //0
	    {0, 0, 0, 0, 0, 15, 15, 1, 2},  //1000
	    {0, 0, 0, 0, 0, 15, 15, 5, 4},  //2000
	    {0, 0, 0, 0, 0, 50, 50, 1, 5},  //3000
	    {0, 0, 0, 0, 0, 15, 15, 5, 4},  //4000
	    {0, 0, 0, 0, 0, 50, 50, 1, 5},  //5000
	    {0, 0, 0, 0, 0, 100, 5, 9, 3},  //6000
	    {0, 0, 0, 0, 0, 5, 100, 1, 3},  //7000
	    {0, 0, 0, 0, 0, 100, 20, 1, 5},  //8000
	    {0, 0, 0, 0, 0, 25, 25, 2, 1},  //9000
	    {0, 0, 0, 0, 0, 35, 35, 3, 1},  //10000
	    {0, 0, 0, 0, 0, 15, 15, 6, 4},  //11000
	    {0, 0, 0, 0, 0, 25, 25, 1, 2},  //12000
	    {0, 0, 0, 0, 0, 45, 45, 2, 1},  //13000
	    {0, 0, 0, 0, 0, 55, 55, 3, 1},  //14000
	};

    // 1 - blue
    // 2 - green (time freeze)
    // 3 - green (invincible)

    int[] [] TokenData = {
	//  {x, y, v, t},
	    {0, 0, 0, 1},  //0
	    {0, 0, 0, 1},  //750
	    {0, 0, 0, 1},  //1500
	    {0, 0, 0, 2},  //2250
	    {0, 0, 0, 1},  //3000
	    {0, 0, 0, 1},  //3750
	    {0, 0, 0, 1},  //4500
	    {0, 0, 0, 3},  //5250
	    {0, 0, 0, 1},  //6000
	    {0, 0, 0, 1},  //6750
	    {0, 0, 0, 1},  //7500
	    {0, 0, 0, 2},  //8250
	    {0, 0, 0, 1},  //9000
	    {0, 0, 0, 1},  //9750
	    {0, 0, 0, 1},  //10500
	    {0, 0, 0, 3},  //11250
	    {0, 0, 0, 1},  //12000
	    {0, 0, 0, 1},  //12750
	    {0, 0, 0, 1},  //13500
	    {0, 0, 0, 2},  //14250
	};

    Enemy1[] ENEMY = new Enemy1 [EnemyData.length];
    Item1[] TOKEN = new Item1 [TokenData.length];

    public void init ()
    {
	setSize (width, height);
	addKeyListener (this);
	backbuffer = createImage (width, height);
	bg = backbuffer.getGraphics ();

	for (int i = 0 ; i < EnemyData.length ; i++)
	{
	    Enemy1 E = new Enemy1 (EnemyData [i] [0], EnemyData [i] [1], EnemyData [i] [2], EnemyData [i] [3], EnemyData [i] [4], EnemyData [i] [5], EnemyData [i] [6], EnemyData [i] [7], EnemyData [i] [8]);
	    ENEMY [i] = E;
	}
	for (int i = 0 ; i < TokenData.length ; i++)
	{
	    Item1 T = new Item1 (TokenData [i] [0], TokenData [i] [1], TokenData [i] [2], TokenData [i] [3]);
	    TOKEN [i] = T;
	}

	FTokenImage = getImage (getCodeBase (), "images/FTokenImage.png");
	ITokenImage = getImage (getCodeBase (), "images/ITokenImage.png");
	ScoreTokenImage = getImage (getCodeBase (), "images/ScoreTokenImage.png");
    }


    public void start ()
    {
	new Thread (this).start ();
    }


    public void run ()
    {
	try
	{

	    while (true)
	    {
		bg.setColor (Color.white);
		bg.fillRect (0, 0, width, height);

		TokenBehaviour ();
		PlayerMovement ();
		EnemyBehaviour ();

		timeUnit++;
		if (timeUnit == 35)
		{
		    score += 50;
		    addStage += 50;
		    addToken += 50;
		    timeUnit = 0;
		}
		if (addStage == 1000)
		{
		    stage++;
		    addStage = 0;
		}
		if (addToken == 750)
		{
		    numToken++;
		    addToken = 0;
		}

		bg.setColor (Color.red);
		bg.drawString ("Stage: " + (stage - 2), 800, 430);
		bg.drawString ("Score: " + score, 800, 450);

		if (freezeTime > 0)
		{
		    freezeTime--;
		    bg.drawString ("Frozen Time Remaining: " + freezeTime, 800, 470);
		}
		if (invincible > 0)
		{
		    invincible--;
		    bg.drawString ("Invincible Time Remaining: " + invincible, 800, 490);
		}

		if (gameOver)
		{
		    repaint ();

		    while (gameOver)
		    {
		    }
		}

		repaint ();
		Thread.sleep (5);
	    }
	}
	catch (InterruptedException ie)
	{
	}
    }


    public void TokenBehaviour ()
    {
	for (int i = 0 ; i < numToken ; i++)
	{
	    if (TOKEN [i].i == 0)
	    {
		TOKEN [i].x = rd.nextInt (985);
		TOKEN [i].y = rd.nextInt (485);
		TOKEN [i].i = 1;
	    }
	    if ((x + 20) >= TOKEN [i].x && x <= (TOKEN [i].x + 15) && (y + 20) >= TOKEN [i].y && y <= (TOKEN [i].y + 15) && TOKEN [i].v == true)
	    {
		TOKEN [i].v = false;

		if (TOKEN [i].t == 1)
		    score += 500;
		else if (TOKEN [i].t == 2)
		    freezeTime = 500;
		else if (TOKEN [i].t == 3)
		    invincible = 500;

	    }
	    else if (TOKEN [i].v == true)
	    {
		if (TOKEN [i].t == 1)
		    bg.drawImage (ScoreTokenImage, TOKEN [i].x, TOKEN [i].y, 20, 20, this);
		if (TOKEN [i].t == 2)
		    bg.drawImage (FTokenImage, TOKEN [i].x, TOKEN [i].y, 20, 20, this);
		if (TOKEN [i].t == 3)
		    bg.drawImage (ITokenImage, TOKEN [i].x, TOKEN [i].y, 20, 20, this);
	    }
	}
    }


    public void EnemyBehaviour ()
    {
	for (int i = 0 ; i < stage ; i++)
	{

	    if (ENEMY [i].i == 0)
	    {
		ENEMY [i].x = rd.nextInt ((1000 - ENEMY [i].lx) / ENEMY [i].s) * ENEMY [i].s;
		ENEMY [i].y = rd.nextInt ((500 - ENEMY [i].ly) / ENEMY [i].s) * ENEMY [i].s;
		ENEMY [i].dx = rd.nextInt (2) + 1;
		ENEMY [i].dy = rd.nextInt (2) + 1;
	    }
	    if (ENEMY [i].i < 70)
		ENEMY [i].i++;
	    else
	    {

		if (ENEMY [i].t != 5)
		{

		    if (ENEMY [i].t == 2)
		    {
			if (ENEMY [i].x > x)
			    ENEMY [i].dx = 1;
			else if (ENEMY [i].x < x)
			    ENEMY [i].dx = 2;
			if (ENEMY [i].y > y)
			    ENEMY [i].dy = 1;
			else if (ENEMY [i].y < y)
			    ENEMY [i].dy = 2;
		    }

		    if (freezeTime == 0)
		    {
			if (ENEMY [i].dx == 2 && ENEMY [i].x < (1000 - ENEMY [i].lx))
			    ENEMY [i].x += ENEMY [i].s;
			else if (ENEMY [i].x > 0)
			    ENEMY [i].x -= ENEMY [i].s;
			if (ENEMY [i].dy == 2 && ENEMY [i].y < (500 - ENEMY [i].ly))
			    ENEMY [i].y += ENEMY [i].s;
			else if (ENEMY [i].y > 0)
			    ENEMY [i].y -= ENEMY [i].s;
		    }

		    if (ENEMY [i].x >= (1000 - ENEMY [i].lx))
			ENEMY [i].dx = 1;
		    else if (ENEMY [i].x <= 0)
			ENEMY [i].dx = 2;
		    if (ENEMY [i].y >= (500 - ENEMY [i].ly))
			ENEMY [i].dy = 1;
		    else if (ENEMY [i].y <= 0)
			ENEMY [i].dy = 2;

		}

		if ((x + 20) >= ENEMY [i].x && x <= (ENEMY [i].x + ENEMY [i].lx) && (y + 20) >= ENEMY [i].y && y <= (ENEMY [i].y + ENEMY [i].ly) && invincible == 0)
		{
		    gameOver = true;
		}
	    }

	    if (ENEMY [i].t == 1)
		bg.setColor (Color.blue);
	    else if (ENEMY [i].t == 2)
		bg.setColor (Color.orange);
	    else if (ENEMY [i].t == 3)
		bg.setColor (darkGreen);
	    else if (ENEMY [i].t == 4)
		bg.setColor (purple);
	    else if (ENEMY [i].t == 5)
		bg.setColor (Color.black);
	    bg.fillRect (ENEMY [i].x, ENEMY [i].y, ENEMY [i].lx, ENEMY [i].ly);
	}
    }


    public void PlayerMovement ()
    {
	if (left && x > 0)
	    x -= 4;
	else if (right && x < 980)
	    x += 4;
	if (up && y > 0)
	    y -= 4;
	else if (down && y < 480)
	    y += 4;

	if (invincible > 0)
	    bg.setColor (yellow);
	else
	    bg.setColor (Color.red);
	bg.fillRect (x, y, 20, 20);
    }


    public void keyPressed (KeyEvent e)
    {
	int key = e.getKeyCode ();
	if (key == 37)
	{
	    pressed = true;
	    left = true;
	}
	if (key == 39)
	{
	    pressed = true;
	    right = true;
	}
	if (key == 38)
	{
	    pressed = true;
	    up = true;
	}
	if (key == 40)
	{
	    pressed = true;
	    down = true;
	}
    }


    public void keyReleased (KeyEvent e)
    {
	int key = e.getKeyCode ();
	if (key == 37)
	{
	    pressed = false;
	    left = false;
	}
	if (key == 39)
	{
	    pressed = true;
	    right = false;
	}
	if (key == 38)
	{
	    pressed = true;
	    up = false;
	}
	if (key == 40)
	{
	    pressed = true;
	    down = false;
	}
    }


    public void keyTyped (KeyEvent e)
    {
    }


    public void update (Graphics g)
    {
	g.drawImage (backbuffer, 0, 0, this);
    }


    public void paint (Graphics g)
    {
	update (g);
    }
}


*/
/*

package shenanigans;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.*;

@SuppressWarnings("serial")
public class MainCode extends Applet implements KeyListener, Runnable
{
	//VARIABLES DECLARED:

    //player variables
	boolean pressed, left = false, right = false, up = false, down = false, control = true, restore = false;
	int playerx, playery, PlayerDirection;
	//miscellaneous concerning game components
	int finishx, finishy, BKcounter, TBcounter;
    boolean teleportation = false, force = false;
	//time
    int oneSecond, gameTime = 0, timeLimit;
    //menus
    int MainMenuIntChoice;
    String PopupMenuSelection = "NULL", MenuSelection = "NULL";
    boolean MainMenuPage = true, LevelSelectPage = false, CreditsPage = false;
    //in-game menus
    boolean gamePaused = false, gameOver = false, levelCleared = false;
    //level
    int currentLevel = 0;
    boolean beginLevel = false, LevelSelected = false;
    //fonts
    Font Arial20 = new Font ("Arial", Font.BOLD, 20);
    Font Arial24 = new Font ("Arial", Font.BOLD, 24);
    Font Arial36 = new Font ("Arial", Font.BOLD, 36);
    //images
    Image backbuffer;
    Image MainMenuImage, LevelSelectMenuImage, CreditsPageImage;
    Image FloorImage, PlayerImage, ExitImage;
    Image WallImage, OpenWallImage, BlueDoorImage;
    Image ToggleButtonImage, ToggleDoorOpenImage, ToggleDoorClosedImage;
    Image TeleporterImage1, TeleporterImage2;
    Image BlueKeyImage1, BlueKeyImage2, BlueKeyImage3, BlueKeyImage4, BlueKeyImage5;
    Image ForceFloorLeftImage, ForceFloorRightImage, ForceFloorUpImage, ForceFloorDownImage;
    Image FireImage1, FireImage2, EnemyType1Image, EnemyType2Image;
    //window
    static Graphics bg;
    int width = 1000, height = 550;		// sets applet window size

    LevelCode[] LEVEL = new LevelCode[10];   			//Creates an array of levels

    //RETRIEVES LEVEL DATA & CREATES BACKBUFFER:
    @Override
    public void init ()
    {
    	setSize (width, height);
    	addKeyListener (this);

    	try{
    		File f = new File (getCodeBase().toURI());	// assigns data for each level
    		for (int i = 0; i<10;i++){					// as an element of an array
    			LEVEL[i] = new LevelCode(f.getParentFile(), i+1);
    		}
    	}catch(URISyntaxException FE){
    		FE.printStackTrace();
    	}

    	backbuffer = createImage (width, height);
    	bg = backbuffer.getGraphics ();
    	RetrieveImages ();

    }
    //RETRIEVES IMAGE FILES
    public void RetrieveImages ()
    {
    	MainMenuImage = getImage (getCodeBase (), "MenuScreens/MainMenuImage.png");
    	LevelSelectMenuImage = getImage (getCodeBase (), "MenuScreens/LevelSelectMenuImage.png");
    	CreditsPageImage = getImage (getCodeBase (), "MenuScreens/CreditsPageImage.png");
    	PlayerImage = getImage (getCodeBase (), "Graphics/PlayerImage.png");
    	FloorImage = getImage (getCodeBase (), "Graphics/FloorImage.png");
    	WallImage = getImage (getCodeBase (), "Graphics/WallImage.png");
    	OpenWallImage = getImage (getCodeBase (), "Graphics/OpenWallImage.png");
    	ToggleButtonImage = getImage (getCodeBase (), "Graphics/ToggleButtonImage.png");
    	ToggleDoorOpenImage = getImage (getCodeBase (), "Graphics/ToggleDoorOpenImage.png");
    	ToggleDoorClosedImage = getImage (getCodeBase (), "Graphics/ToggleDoorClosedImage.png");
    	FireImage1 = getImage (getCodeBase (), "Graphics/FireImage1.png");
    	FireImage2 = getImage (getCodeBase (), "Graphics/FireImage2.png");
    	TeleporterImage1 = getImage (getCodeBase (), "Graphics/TeleporterImage1.png");
    	TeleporterImage2 = getImage (getCodeBase (), "Graphics/TeleporterImage2.png");
    	BlueKeyImage1 = getImage (getCodeBase (), "Graphics/BlueKeyImage1.png");
    	BlueKeyImage2 = getImage (getCodeBase (), "Graphics/BlueKeyImage2.png");
    	BlueKeyImage3 = getImage (getCodeBase (), "Graphics/BlueKeyImage3.png");
    	BlueKeyImage4 = getImage (getCodeBase (), "Graphics/BlueKeyImage4.png");
    	BlueKeyImage5 = getImage (getCodeBase (), "Graphics/BlueKeyImage5.png");
    	BlueDoorImage = getImage (getCodeBase (), "Graphics/BlueDoorImage.png");
    	ForceFloorLeftImage = getImage (getCodeBase (), "Graphics/ForceFloorLeftImage.png");
    	ForceFloorRightImage = getImage (getCodeBase (), "Graphics/ForceFloorRightImage.png");
    	ForceFloorUpImage = getImage (getCodeBase (), "Graphics/ForceFloorUpImage.png");
    	ForceFloorDownImage = getImage (getCodeBase (), "Graphics/ForceFloorDownImage.png");
    	ExitImage = getImage (getCodeBase (), "Graphics/ExitImage.png");
    	EnemyType1Image = getImage (getCodeBase (), "Graphics/EnemyType1Image.png");
    	EnemyType2Image = getImage (getCodeBase (), "Graphics/EnemyType2Image.png");
    }
    //CREATES NEW THREAD:
    @Override
    public void start ()
    {
    	new Thread (this).start ();
    }
    //MAIN MENU:
    public void run ()
    {
    	ResetConditions ();

    	while (MainMenuPage = true)		// infinite loop waits for keyboard input
    	{
    		ResetConditions ();
    		bg.drawImage (MainMenuImage, 0, 0, width, height, this);
    		repaint ();
    		if (MainMenuIntChoice == 1)			// activates gameplay
    		{
    			beginLevel = true;
    			Play ();
    		}
    		else if (MainMenuIntChoice == 2)	// displays level selection menu
    		{
    			bg.drawImage (LevelSelectMenuImage, 0, 0, width, height, this);
    			repaint ();
    			MainMenuPage = false;
    			LevelSelectPage = true;
    			while (LevelSelectPage)
    			{
    				if (MenuSelection.equals ("ESC"))
    					LevelSelectPage = false;
    				if (LevelSelected)
    				{
    					beginLevel = true;
    					Play ();
    				}
    			}
    		}
    		else if (MainMenuIntChoice == 3)	// displays credits page
    		{
    			bg.drawImage (CreditsPageImage, 0, 0, width, height, this);
    			repaint ();
    			MainMenuPage = false;
    			CreditsPage = true;
    			while (CreditsPage)
    			{
    				if (MenuSelection.equals ("ESC"))
    					CreditsPage = false;
    			}
    		}
    		else if (MainMenuIntChoice == 4)	// closes the applet window
    			System.exit (0);
    	}
    }
    //RESETS MENU & LEVEL CONDITIONS:
    public void ResetConditions ()
    {
    	MainMenuPage = true;
    	LevelSelectPage = false;
    	CreditsPage = false;
    	LevelSelected = false;
    	MainMenuIntChoice = 0;
    	MenuSelection = "NULL";
    	PopupMenuSelection = "NULL";
    	gamePaused = false;
    	gameOver = false;
    	gameTime = 0;
    	levelCleared = false;
    }
    //GAMEPLAY:
    public void Play ()
    {
    	try
    	{
    		while (true)	// infinite loop of gameplay (until time runs out, death, etc.)
    		{
    			if (beginLevel)	// loads level data only once at the beginning
    			{
    				LEVEL [currentLevel].reload();
    				beginLevel = false;
    				LevelSpecific ();
    			}

    			bg.drawImage (FloorImage, 0, 0, width, height, this);	// draws tiled floor

    			bg.setColor (Color.black);
    			bg.setFont (Arial24);
    			bg.drawString ("LEVEL " + (currentLevel + 1), 20, 533);	// draws level number display

    			TutorialText ();				// coding for object behavior is organized into methods
    			SolidWall ();
    			BlueKeyAndDoor ();
    			ToggleButtonANDDoor ();
    			Teleporter ();
    			LevelCleared ();
    			PlayerControlANDForceFloor ();
    			Fire ();
    			EnemyBouncer ();

                oneSecond++;					// controls the rate at which the displayed time decreases
                if (oneSecond == 8)
                {
                	gameTime++;
                	oneSecond = 0;
                }
                bg.setColor (Color.red);		// draws 'time remaining' display
                bg.setFont (Arial24);
                bg.drawString ("Time Remaining: " + (timeLimit - gameTime), 753, 533);


                repaint ();						// all graphics are repainted every 1/8 of a second
                Thread.sleep (125);				// (i.e. the player can travel 8 tiles in one second)

                if (gamePaused)					// displays certain pop-up menus when appropriate
                	GamePausedDisplay ();
                if (levelCleared == true)
                	LevelClearedDisplay ();
                else if (gameTime == timeLimit)
                	OutOfTimeDisplay ();
                else if (gameOver)
                	GameOverDisplay ();

                while (gamePaused || gameOver || levelCleared)	// waits for keyboard input, then carries
                {												// out an action based on selection
                	if (PopupMenuSelection.equals ("ESC"))
                		run ();
                	if (PopupMenuSelection.equals ("R"))
                	{
                		beginLevel = true;
                		ResetConditions ();
                	}
                	if (levelCleared && PopupMenuSelection.equals ("N"))
                	{
                		currentLevel++;
                		beginLevel = true;
                		ResetConditions ();
                	}
                }
            }
        }
        catch (InterruptedException ie)
        {
        }
    }
    //POP-UP MENU DISPLAYS:
    public void GamePausedDisplay ()
    {
    	bg.setColor (Color.darkGray);
    	bg.fillRect (290, 90, 420, 320);
    	bg.setColor (Color.lightGray);
    	bg.fillRect (300, 100, 400, 300);
    	bg.setFont (Arial36);
    	bg.setColor (Color.black);
    	bg.drawString ("GAME PAUSED", 368, 160);
    	bg.setFont (Arial24);
    	bg.drawString ("[P] Resume Game", 320, 250);
    	bg.drawString ("[R] Restart Level", 320, 280);
    	bg.drawString ("[ESC] Return to Main Menu", 320, 340);
    	repaint ();
    }
    public void LevelClearedDisplay ()
    {
    	bg.setColor (Color.darkGray);
    	bg.fillRect (290, 90, 420, 320);
    	bg.setColor (Color.lightGray);
    	bg.fillRect (300, 100, 400, 300);
    	bg.setFont (Arial36);
    	bg.setColor (Color.black);
    	bg.drawString ("LEVEL CLEARED", 358, 160);
    	bg.setFont (Arial24);
    	if (currentLevel != 8)
    		bg.drawString ("[N] Next Level", 320, 250);
    	bg.drawString ("[R] Restart Level", 320, 280);
    	bg.drawString ("[ESC] Return to Main Menu", 320, 340);
    	repaint ();
    }
    public void OutOfTimeDisplay ()
    {
    	gameOver = true;
    	bg.setColor (Color.darkGray);
    	bg.fillRect (290, 90, 420, 320);
    	bg.setColor (Color.lightGray);
    	bg.fillRect (300, 100, 400, 300);
    	bg.setFont (Arial36);
    	bg.setColor (Color.black);
    	bg.drawString ("OUT OF TIME!", 378, 160);
    	bg.setFont (Arial24);
    	bg.drawString ("[R] Restart Level", 320, 280);
    	bg.drawString ("[ESC] Return to Main Menu", 320, 340);
    	repaint ();
    }
    public void GameOverDisplay ()
    {
    	bg.setColor (Color.darkGray);
    	bg.fillRect (290, 90, 420, 320);
    	bg.setColor (Color.lightGray);
    	bg.fillRect (300, 100, 400, 300);
    	bg.setFont (Arial36);
    	bg.setColor (Color.black);
    	bg.drawString ("GAME OVER!", 378, 160);
    	bg.setFont (Arial24);
    	bg.drawString ("[R] Restart Level", 320, 280);
    	bg.drawString ("[ESC] Return to Main Menu", 320, 340);
    	repaint ();
    }
    //DATA WHICH IS LEVEL-SPECIFIC:
    public void LevelSpecific ()
    {
    	switch (currentLevel)
    	{
    	case 0:
    		playerx = 50;		//player starting positions,
    		playery = 50;		//exit portal positions, and
    		finishx = 550;		//time limit are established
    		finishy = 400;		//according to level
    		timeLimit = 30;
    		break;
    	case 1:
    		playerx = 350;
    		playery = 200;
    		finishx = 550;
    		finishy = 200;
    		timeLimit = 45;
    		break;
    	case 2:
    		playerx = 50;
    		playery = 400;
    		finishx = 550;
    		finishy = 50;
    		timeLimit = 45;
    		break;
    	case 3:
    		playerx = 50;
    		playery = 50;
    		finishx = 550;
    		finishy = 400;
    		timeLimit = 60;
    		break;
    	case 4:
    		playerx = 650;
    		playery = 400;
    		finishx = 900;
    		finishy = 450;
    		timeLimit = 120;
    		break;
    	case 5:
    		playerx = 950;
    		playery = 200;
    		finishx = 0;
    		finishy = 450;
    		timeLimit = 180;
    		break;
    	case 6:
    		playerx = 250;
    		playery = 250;
    		finishx = 950;
    		finishy = 100;
    		timeLimit = 200;
    		break;
    	case 7:
    		playerx = 450;
    		playery = 200;
    		finishx = 450;
    		finishy = 0;
    		timeLimit = 240;
    		break;
    	case 8:
    		playerx = 50;
    		playery = 350;
    		finishx = 950;
    		finishy = 0;
    		timeLimit = 120;
    		break;
    	case 9:
    		playerx = 0;
    		playery = 50;
    		finishx = 950;
    		finishy = 400;
    		timeLimit = 240;
    		break;
    	}
    }
    public void TutorialText ()
    {
    	if (currentLevel == 0)
    	{
    		bg.setFont (Arial24);									//different instructions are
    		bg.setColor (Color.black);								//displayed for each of the
    		bg.drawString ("Use the arrow keys or", 710, 30);		//four tutorial levels
    		bg.drawString ("WASD keys to move.", 710, 70);
    		bg.drawString ("         ---", 710, 110);
    		bg.drawString ("Reach the red portal to", 710, 150);
    		bg.drawString ("complete the level.", 710, 190);
    		bg.drawString ("         ---", 710, 230);
    		bg.drawString ("Every level has a time", 710, 270);
    		bg.drawString ("limit.", 710, 310);
    		bg.drawString ("Press \"P\" to open the Pause Menu", 250, 533);
    	}
    	else if (currentLevel == 1)
    	{
    		bg.setFont (Arial24);
    		bg.setColor (Color.black);
    		bg.drawString ("Green buttons toggle", 710, 30);
    		bg.drawString ("the green walls.", 710, 70);
    		bg.drawString ("         ---", 710, 110);
    		bg.drawString ("Collect every blue key", 710, 150);
    		bg.drawString ("to open every blue door.", 710, 190);
    		bg.drawString ("         ---", 710, 230);
    		bg.drawString ("Fire is dangerous, do", 710, 270);
    		bg.drawString ("not touch it!", 710, 310);
    		bg.drawString ("Press \"P\" to open the Pause Menu", 250, 533);
    	}
    	else if (currentLevel == 2)
    	{
    		bg.setFont (Arial24);
    		bg.setColor (Color.black);
    		bg.drawString ("Arrows force you to", 710, 30);
    		bg.drawString ("move in the direction", 710, 70);
    		bg.drawString ("that they depict.", 710, 110);
    		bg.drawString ("         ---", 710, 150);
    		bg.drawString ("Blue teleporters warp", 710, 190);
    		bg.drawString ("you to their related", 710, 230);
    		bg.drawString ("teleporter.", 710, 270);
    		bg.drawString ("         ---", 710, 310);
    		bg.drawString ("Black pads transform", 710, 350);
    		bg.drawString ("into solid walls after", 710, 390);
    		bg.drawString ("they are walked on.", 710, 430);
    		bg.drawString ("Press \"P\" to open the Pause Menu", 250, 533);
    	}
    	else if (currentLevel == 3)
    	{
    		bg.drawString ("Do not touch the", 710, 30);
    		bg.drawString ("enemy bouncers!", 710, 70);
    		bg.drawString ("         ---", 710, 110);
    		bg.drawString ("Purple bouncers always", 710, 150);
    		bg.drawString ("bounce backwards off", 710, 190);
    		bg.drawString ("of walls.", 710, 230);
    		bg.drawString ("         ---", 710, 270);
    		bg.drawString ("Blue bouncers always", 710, 310);
    		bg.drawString ("turn when they hit a", 710, 350);
    		bg.drawString ("wall.", 710, 390);
    		bg.drawString ("Press \"P\" to open the Pause Menu", 250, 533);
    	}
    }
    //KEYEVENT - KEYPRESSED:
    public void keyPressed (KeyEvent e)
    {
    	int key = e.getKeyCode ();

    	if (MainMenuPage)
    	{
    		if (key == 49)					//based on main menu selection
    			MainMenuIntChoice = 1;
    		else if (key == 50)
    			MainMenuIntChoice = 2;
    		else if (key == 51)
    			MainMenuIntChoice = 3;
    		else if (key == 52)
    			MainMenuIntChoice = 4;
    	}
    	else if (LevelSelectPage)
    	{
    		if (key == 27)
    			MenuSelection = "ESC";
    		else if (key == 49)				//chooses a level and starts gameplay
    		{
    			currentLevel = 0;
    			LevelSelected = true;
    		}
    		else if (key == 50)
    		{
    			currentLevel = 1;
    			LevelSelected = true;
    		}
    		else if (key == 51)
    		{
    			currentLevel = 2;
    			LevelSelected = true;
    		}
    		else if (key == 52)
    		{
    			currentLevel = 3;
    			LevelSelected = true;
    		}
    		else if (key == 53)
    		{
    			currentLevel = 4;
    			LevelSelected = true;
    		}
    		else if (key == 54)
    		{
    			currentLevel = 5;
    			LevelSelected = true;
    		}
    		else if (key == 55)
    		{
    			currentLevel = 6;
    			LevelSelected = true;
    		}
    		else if (key == 56)
    		{
    			currentLevel = 7;
    			LevelSelected = true;
    		}
    		else if (key == 57)
    		{
    			currentLevel = 8;
    			LevelSelected = true;
    		}
    		else if (key == 65)
    		{
    			currentLevel = 9;
    			LevelSelected = true;
    		}
    	}
    	else if (CreditsPage)						// options to return to the main
    	{											// menu from the credits page
    		if (key == 27)
    			MenuSelection = "ESC";
    	}
    	if (key == 80)								// 'P' pauses/resumes gameplay
    	{
    		if (!gamePaused)
    			gamePaused = true;
    		else
    			gamePaused = false;
    	}
    	if (gamePaused || gameOver || levelCleared)	// common options from different
    	{											// pop-up menus
    		if (key == 27)
    			PopupMenuSelection = "ESC";
    		if (key == 82)
    			PopupMenuSelection = "R";
    		if (levelCleared && currentLevel != 10 && key == 78)
    			PopupMenuSelection = "N";
    	}
    	if (key == 37 || key == 65 && !up && !down)	// used to control player movement
    	{
    		pressed = true;
    		left = true;
    	}
    	if (key == 39 || key == 68 && !up && !down)
    	{
    		pressed = true;
    		right = true;
    	}
    	if (key == 38 || key == 87 && !left && !right)
    	{
    		pressed = true;
    		up = true;
    	}
    	if (key == 40 || key == 83 && !left && !right)
    	{
    		pressed = true;
    		down = true;
    	}
    }
    //OBJECT BEHAVIORS:
    public void SolidWall ()
    {
    	for (int iW = 0 ; iW < LEVEL[currentLevel].walls.size(); iW++)
    	{
    		Wall WALL = LEVEL[currentLevel].walls.get(iW);
    		if (WALL.type == 0)		// refers to one-way pads
    		{
    			if (playerx == (WALL .x) && playery == (WALL .y))
    				WALL .pressed = true;
    			if ((playerx != (WALL .x) || playery != (WALL .y)) && WALL .pressed)
    				WALL .type = 1;
    			else
    			{	//displays graphics:
    				bg.drawImage (OpenWallImage, WALL .x, WALL .y, 50, 50, this);
    			}
    		}
    		if (WALL .type == 1)	// refers to solid wall blocks
    		{
    			if (playerx == (WALL .x - 50) && playery == WALL .y)
    				right = false;
    			if (playerx == (WALL .x + 50) && playery == WALL .y)
    				left = false;
    			if (playerx == WALL .x && playery == (WALL .y - 50))
    				down = false;
    			if (playerx == WALL .x && playery == (WALL .y + 50))
    				up = false;
    				//displays graphics:
    			bg.drawImage (WallImage, WALL .x, WALL .y, 50, 50, this);
    		}
    	}
    }
    public void BlueKeyAndDoor ()
    {
    	BKcounter = 0;
    	// coding for blue keys
    	for (int iBK = 0 ; iBK < LEVEL[currentLevel].bluekeys.size(); iBK++)
    	{
    		BlueKey BLUEKEY = LEVEL[currentLevel].bluekeys.get(iBK);
    		if (playerx == (BLUEKEY .x) && playery == (BLUEKEY .y))
    			BLUEKEY .visible = false;
    		if (BLUEKEY .visible)
    		{
    			BKcounter++;	// counts the current number of blue keys

    			if (BLUEKEY .graphics > 16)	// creates a loop of five images which create
    				BLUEKEY .graphics = 1;	// the illusion of a spinning key
    			if (BLUEKEY .graphics > 8 && BLUEKEY .graphics < 11)
    				bg.drawImage (BlueKeyImage5, BLUEKEY .x, BLUEKEY .y, 50, 50, this);
    			else if ((BLUEKEY .graphics > 6 && BLUEKEY .graphics < 9) || (BLUEKEY .graphics > 10 && BLUEKEY .graphics < 13))
    				bg.drawImage (BlueKeyImage4, BLUEKEY .x, BLUEKEY .y, 50, 50, this);
    			else if ((BLUEKEY .graphics > 4 && BLUEKEY .graphics < 7) || (BLUEKEY .graphics > 12 && BLUEKEY .graphics < 15))
    				bg.drawImage (BlueKeyImage3, BLUEKEY .x, BLUEKEY .y, 50, 50, this);
    			else if ((BLUEKEY .graphics > 2 && BLUEKEY .graphics < 5) || (BLUEKEY .graphics > 14 && BLUEKEY .graphics < 17))
    				bg.drawImage (BlueKeyImage2, BLUEKEY .x, BLUEKEY .y, 50, 50, this);
    			else if (BLUEKEY .graphics > 0 && BLUEKEY .graphics < 3)
    				bg.drawImage (BlueKeyImage1, BLUEKEY .x, BLUEKEY .y, 50, 50, this);
    			BLUEKEY .graphics++;
    		}
    		if (!BLUEKEY .visible && BLUEKEY .addcount)
    			BLUEKEY .addcount = false;
    	}
    	// coding for blue locked doors
    	for (int iBD = 0 ; iBD < LEVEL[currentLevel].bluedoors.size(); iBD++)
    	{
    		BlueDoor BLUEDOOR = LEVEL[currentLevel].bluedoors.get(iBD);
    		if (BKcounter > 0)
    		{	// player & wall-type block collisions:
    			if (playerx == (BLUEDOOR .x - 50) && playery == BLUEDOOR .y)
    				right = false;
    			if (playerx == (BLUEDOOR .x + 50) && playery == BLUEDOOR .y)
    				left = false;
    			if (playerx == BLUEDOOR .x && playery == (BLUEDOOR .y - 50))
    				down = false;
    			if (playerx == BLUEDOOR .x && playery == (BLUEDOOR .y + 50))
    				up = false;
    				//displays graphics:
    			bg.drawImage (BlueDoorImage, BLUEDOOR .x, BLUEDOOR .y, 50, 50, this);
    		}
    	}
    }
    public void ToggleButtonANDDoor ()
    {
    	TBcounter = 0;
    	// coding for toggle buttons
    	for (int iTB = 0 ; iTB < LEVEL[currentLevel].togglebuttons.size(); iTB++)
    	{
    		ToggleButton TOGGLEBUTTON = LEVEL[currentLevel].togglebuttons.get(iTB);
    		if (playerx == (TOGGLEBUTTON .x) && playery == (TOGGLEBUTTON .y) && TOGGLEBUTTON .released)
    		{
    			TOGGLEBUTTON .pressed = true;
    			TOGGLEBUTTON .released = false;
    		}
    		else if ((playerx != (TOGGLEBUTTON .x) || playery != (TOGGLEBUTTON .y)) && !TOGGLEBUTTON .released)
    		{
    			TOGGLEBUTTON .released = true;
    		}
    		if (TOGGLEBUTTON .pressed)		// if a button is pressed, doors are only toggled once
    		{
    			TBcounter++;
    			TOGGLEBUTTON .pressed = false;
    		}
    		//displays graphics:
    		bg.drawImage (ToggleButtonImage, TOGGLEBUTTON .x, TOGGLEBUTTON .y, 50, 50, this);
    	}
    	// coding for toggle doors
    	for (int iTD = 0 ; iTD < LEVEL[currentLevel].toggledoors.size() ; iTD++)
    	{
    		ToggleDoor TOGGLEDOOR = LEVEL[currentLevel].toggledoors.get(iTD);
    		if (TBcounter > 0)
    		{
    			if (TOGGLEDOOR .open == 0)	// open doors become closed and vice versa
    				TOGGLEDOOR .open = 1;
    			else
    				TOGGLEDOOR .open = 0;
    		}
    		if (TOGGLEDOOR .open == 0)
    		{	// player & wall-type block collisions:
    			if (playerx == (TOGGLEDOOR .x - 50) && playery == TOGGLEDOOR .y)
    				right = false;
    			if (playerx == (TOGGLEDOOR .x + 50) && playery == TOGGLEDOOR .y)
    				left = false;
    			if (playerx == TOGGLEDOOR .x && playery == (TOGGLEDOOR .y - 50))
    				down = false;
    			if (playerx == TOGGLEDOOR .x && playery == (TOGGLEDOOR .y + 50))
    				up = false;
    			//displays graphics for closed door:
    			bg.drawImage (ToggleDoorClosedImage, TOGGLEDOOR .x, TOGGLEDOOR .y, 50, 50, this);
    		}
    		else
    		{	//displays graphics for open door:
    			bg.drawImage (ToggleDoorOpenImage, TOGGLEDOOR .x, TOGGLEDOOR .y, 50, 50, this);
    		}
    	}
    }
    public void Teleporter ()
    {
    	if (restore)	// restores the players ability to move by will
    	{
    		restore = false;
    		control = true;
    	}
    	for (int iTELE = 0 ; iTELE < LEVEL[currentLevel].teleporters.size() ; iTELE++)
    	{
    		Teleporter TELEPORTER = LEVEL[currentLevel].teleporters.get(iTELE);
    		if (playerx == TELEPORTER .x1 && playery == TELEPORTER .y1)
    		{
    			playerx = TELEPORTER .x2;	// warps the player to the corresponding teleporter
    			playery = TELEPORTER .y2;
    			teleportation = true;
    		}
    		else if (playerx == TELEPORTER .x2 && playery == TELEPORTER .y2)
    		{
    			playerx = TELEPORTER .x1;   // warps the player to the corresponding teleporter
    			playery = TELEPORTER .y1;
    			teleportation = true;
    		}
    		if (teleportation)				// maintains character movement in the proper
    		{								// direction upon exiting the second teleporter
    			if (PlayerDirection == 9)
    				playerx -= 50;
    			else if (PlayerDirection == 3)
    				playerx += 50;
    			else if (PlayerDirection == 12)
    				playery -= 50;
    			else if (PlayerDirection == 6)
    				playery += 50;

    			teleportation = false;
    			restore = true;
    			control = false;
    		}
    			// creates animation which switches between two images every 1/4 second
    		if (TELEPORTER .graphics > 4)
    			TELEPORTER .graphics = 1;
    		if (TELEPORTER .graphics > 2)
    		{
    			bg.drawImage (TeleporterImage2, TELEPORTER .x1, TELEPORTER .y1, 50, 50, this);
    			bg.drawImage (TeleporterImage2, TELEPORTER .x2, TELEPORTER .y2, 50, 50, this);
    		}
    		else if (TELEPORTER .graphics > 0)
    		{
    			bg.drawImage (TeleporterImage1, TELEPORTER .x1, TELEPORTER .y1, 50, 50, this);
    			bg.drawImage (TeleporterImage1, TELEPORTER .x2, TELEPORTER .y2, 50, 50, this);
    		}
    		TELEPORTER .graphics++;
    	}
    }
    public void LevelCleared ()
    {
    	if (playerx == finishx && playery == finishy)
    		levelCleared = true; // conditions that allow you to proceed to the next level
    		// displays graphics
    	bg.drawImage (ExitImage, finishx, finishy, 50, 50, this);
    }
    public void PlayerControlANDForceFloor ()
    {
    	if (control)
    	{
    		if (left && playerx != 0)
    		{
    			playerx -= 50;
    			PlayerDirection = 9;
    		}
    		else if (right && playerx != 950)
    		{
    			playerx += 50;
    			PlayerDirection = 3;
    		}
    		else if (up && playery != 0)
    		{
    			playery -= 50;
    			PlayerDirection = 12;
    		}
    		else if (down && playery != 450)
    		{
    			playery += 50;
    			PlayerDirection = 6;
    		}
    	}
    	for (int iFF = 0 ; iFF < LEVEL[currentLevel].forcefloors.size() ; iFF++)
    	{
    		ForceFloor FORCEFLOOR = LEVEL[currentLevel].forcefloors.get(iFF);
    		if (playerx == FORCEFLOOR .x && playery == FORCEFLOOR .y)
    		{
    			if (force)
    			{
    				force = false;
    				control = true;

    				switch (FORCEFLOOR .direction)
    				{
    				case 3:
    					playerx += 50;
    					break;
    				case 6:
    					playery += 50;
    					break;
    				case 9:
    					playerx -= 50;
    					break;
    				case 12:
    					playery -= 50;
    					break;
    				}
    			}
    		}
    		if (playerx == FORCEFLOOR .x && playery == FORCEFLOOR .y)
    		{
    			force = true;
    			control = false;
    		}
    		switch (FORCEFLOOR .direction)
    		{
    		case 3:
    			bg.drawImage (ForceFloorRightImage, FORCEFLOOR .x, FORCEFLOOR .y, 50, 50, this);
    			break;
    		case 6:
    			bg.drawImage (ForceFloorDownImage, FORCEFLOOR .x, FORCEFLOOR .y, 50, 50, this);
    			break;
    		case 9:
    			bg.drawImage (ForceFloorLeftImage, FORCEFLOOR .x, FORCEFLOOR .y, 50, 50, this);
    			break;
    		case 12:
    			bg.drawImage (ForceFloorUpImage, FORCEFLOOR .x, FORCEFLOOR .y, 50, 50, this);
    			break;
    		}
    	}
    	bg.drawImage (PlayerImage, playerx, playery, 50, 50, this);
    }
    public void Fire ()
    {
    	for (int iF = 0 ; iF < LEVEL[currentLevel].fires.size() ; iF++)
    	{
    		Fire FIRE = LEVEL[currentLevel].fires.get(iF);
    		if (playerx == (FIRE .x) && playery == (FIRE .y))
    			gameOver = true;	// if the player touches fire they die
    		// creates animation which switches between two images every 3/4 seconds
    		if (FIRE .graphics > 12)
    			FIRE .graphics = 1;
    		if (FIRE .graphics > 6)
    			bg.drawImage (FireImage1, FIRE .x, FIRE .y, 50, 50, this);
    		else if (FIRE .graphics > 0)
    			bg.drawImage (FireImage2, FIRE .x, FIRE .y, 50, 50, this);
    		FIRE .graphics++;
    	}
    }
    public void EnemyBouncer ()

    {
    	for (int iEB = 0 ; iEB < LEVEL[currentLevel].enemybouncers.size() ; iEB++)
    	{
    		EnemyBouncer ENEMYBOUNCER = LEVEL[currentLevel].enemybouncers.get(iEB);
    		for (int iW = 0 ; iW < LEVEL[currentLevel].walls.size() ; iW++)
    		{
    			// an enemy bouncer can collide with a wall from four different directions:
    			Wall WALL = LEVEL[currentLevel].walls.get(iW);
    			if (((ENEMYBOUNCER .x == (WALL .x - 50) && ENEMYBOUNCER .y == WALL .y) || ENEMYBOUNCER .x == 950) && ENEMYBOUNCER .direction == 3)
    			{
    				if (ENEMYBOUNCER .type == 2)		// if a purple bouncer collides with a wall,
    					ENEMYBOUNCER .direction = 9;	// it turns and travels in the opposite direction
    				else
    				{
    					for (int iW2 = 0 ; iW2 < LEVEL[currentLevel].walls.size() ; iW2++)
    					{
    						WALL = LEVEL[currentLevel].walls.get(iW2);
    						if ((ENEMYBOUNCER .x == (WALL .x) && ENEMYBOUNCER .y == WALL .y - 50) || ENEMYBOUNCER .y == 450)
    						{
    							ENEMYBOUNCER .direction = 12;	// if a blue bouncer collides with a wall and there is
    						}									// another wall on its right side, it turns left
    						else
    							ENEMYBOUNCER .direction = 6;	// if a blue bouncer collides with a wall and there is
    					}										// open space on its right side, it turns right
    				}
    			}
    			if (((ENEMYBOUNCER .x == (WALL .x + 50) && ENEMYBOUNCER .y == WALL .y) || ENEMYBOUNCER .x == 0) && ENEMYBOUNCER .direction == 9)
    			{
    				if (ENEMYBOUNCER .type == 2)
    					ENEMYBOUNCER .direction = 3;
    				else
    				{
    					for (int iW2 = 0 ; iW2 < LEVEL[currentLevel].walls.size() ; iW2++)
    					{
    						WALL = LEVEL[currentLevel].walls.get(iW2);
    						if ((ENEMYBOUNCER .x == (WALL .x) && ENEMYBOUNCER .y == WALL .y + 50) || ENEMYBOUNCER .y == 0)
    						{
    							ENEMYBOUNCER .direction = 6;
    						}
    						else
    							ENEMYBOUNCER .direction = 12;
    					}
    				}
    			}
    			if (((ENEMYBOUNCER .x == WALL .x && ENEMYBOUNCER .y == (WALL .y - 50)) || ENEMYBOUNCER .y == 450) && ENEMYBOUNCER .direction == 6)
    			{
    				if (ENEMYBOUNCER .type == 2)
    					ENEMYBOUNCER .direction = 12;
    				else
    					for (int iW2 = 0 ; iW2 < LEVEL[currentLevel].walls.size() ; iW2++)
    					{
    						WALL = LEVEL[currentLevel].walls.get(iW2);
    						if ((ENEMYBOUNCER .x == (WALL .x + 50) && ENEMYBOUNCER .y == WALL .y) || ENEMYBOUNCER .x == 0)
    						{
    							ENEMYBOUNCER .direction = 3;
    						}
    						else
    							ENEMYBOUNCER .direction = 9;
    					}
    			}

    			if (((ENEMYBOUNCER .x == WALL .x && ENEMYBOUNCER .y == (WALL .y + 50)) || ENEMYBOUNCER .y == 0) && ENEMYBOUNCER .direction == 12)
    			{
    				if (ENEMYBOUNCER .type == 2)
    					ENEMYBOUNCER .direction = 6;
    				else
    					for (int iW2 = 0 ; iW2 < LEVEL[currentLevel].walls.size() ; iW2++)
    					{
    						WALL = LEVEL[currentLevel].walls.get(iW2);
    						if ((ENEMYBOUNCER .x == (WALL .x - 50) && ENEMYBOUNCER .y == WALL .y) || ENEMYBOUNCER .x == 950)
    						{
    							ENEMYBOUNCER .direction = 9;
    						}
    						else
    							ENEMYBOUNCER .direction = 3;
    					}
    			}
    		}
    		// coding for collision with solid togle doors
    		for (int iTD = 0 ; iTD < LEVEL[currentLevel].toggledoors.size() ; iTD++)
    		{
    			ToggleDoor TOGGLEDOOR = LEVEL[currentLevel].toggledoors.get(iTD);
    			if (TOGGLEDOOR .open == 0)
    			{
    				if (((ENEMYBOUNCER .x == (TOGGLEDOOR .x - 50) && ENEMYBOUNCER .y == TOGGLEDOOR .y) || ENEMYBOUNCER .x == 950) && ENEMYBOUNCER .direction == 3)
    				{
    					if (ENEMYBOUNCER .type == 2)
    						ENEMYBOUNCER .direction = 9;
    					else
    						ENEMYBOUNCER .direction = 6;
    				}
    				if (((ENEMYBOUNCER .x == (TOGGLEDOOR .x + 50) && ENEMYBOUNCER .y == TOGGLEDOOR .y) || ENEMYBOUNCER .x == 0) && ENEMYBOUNCER .direction == 9)
    				{
    					if (ENEMYBOUNCER .type == 2)
    						ENEMYBOUNCER .direction = 3;
    					else
    						ENEMYBOUNCER .direction = 12;
    				}
    				if (((ENEMYBOUNCER .x == TOGGLEDOOR .x && ENEMYBOUNCER .y == (TOGGLEDOOR .y - 50)) || ENEMYBOUNCER .y == 450) && ENEMYBOUNCER .direction == 6)
    				{
    					if (ENEMYBOUNCER .type == 2)
    						ENEMYBOUNCER .direction = 12;
    					else
    						ENEMYBOUNCER .direction = 9;
    				}
    				if (((ENEMYBOUNCER .x == TOGGLEDOOR .x && ENEMYBOUNCER .y == (TOGGLEDOOR .y + 50)) || ENEMYBOUNCER .y == 0) && ENEMYBOUNCER .direction == 12)
    				{
    					if (ENEMYBOUNCER .type == 2)
    						ENEMYBOUNCER .direction = 6;
    					else
    						ENEMYBOUNCER .direction = 3;
    				}
    			}
    		}
    		// coding for enemy movement based on direction
    		switch (ENEMYBOUNCER .direction)
    		{
    		case 3:
    			ENEMYBOUNCER .x += 50;
    			break;
    		case 6:
    			ENEMYBOUNCER .y += 50;
    			break;
    		case 9:
    			ENEMYBOUNCER .x -= 50;
    			break;
    		case 12:
    			ENEMYBOUNCER .y -= 50;
    			break;
    		}

    		if (playerx == ENEMYBOUNCER .x && playery == ENEMYBOUNCER .y)
    		{
    			gameOver = true;	// if the player collides with an enemy bouncer they die
    		}

    		if (ENEMYBOUNCER .type == 2)
    		{
    			// displays purple bouncers
    			bg.drawImage (EnemyType1Image, ENEMYBOUNCER .x, ENEMYBOUNCER .y, 50, 50, this);
    		}

    		else
    		{
    			// displays blue bouncers
    			bg.drawImage (EnemyType2Image, ENEMYBOUNCER .x, ENEMYBOUNCER .y, 50, 50, this);
    		}
    	}
    }
    //OTHER KEYEVENTS:
    public void keyReleased (KeyEvent e)
    {
    	int key = e.getKeyCode ();
    	if (key == 37 || key == 65)
    	{
    		pressed = false;
    		left = false;
    	}
    	if (key == 39 || key == 68)
    	{
    		pressed = true;
    		right = false;
    	}
    	if (key == 38 || key == 87)
    	{
    		pressed = true;
    		up = false;
    	}
    	if (key == 40 || key == 83)
    	{
    		pressed = true;
    		down = false;
    	}
    }
    public void keyTyped (KeyEvent e)
    {
    }
    //UPDATE GRAPHICS:
    @Override
    public void update (Graphics g)
    {
    	g.drawImage (backbuffer, 0, 0, this);
    }
    @Override
    public void paint (Graphics g)
    {
    	update (g);
    }
}
*/