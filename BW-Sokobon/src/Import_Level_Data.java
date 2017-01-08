import java.util.*;
import java.io.*;

public class Import_Level_Data {
    ArrayList<Wall> walls = new ArrayList<Wall>();		// creates resizable arraylists
    int levelNum;
    File sourceFile;
    public Import_Level_Data(File f, int levelNum){
        this.levelNum = levelNum;
        sourceFile = new File(f, "/src/LevelData/"+levelNum+".txt");
        try{
            loadFileData(sourceFile);	// retrieves and loads level data from text files
        }catch(IOException ioe){
            ioe.printStackTrace();
        }
    }
    public void loadFileData(File f) throws IOException
    {	// creates a buffered reader to read the text file
        BufferedReader br = new BufferedReader (new FileReader (f));
        String aLine = null;
        while (null != (aLine = br.readLine ()))
        {
            processLine (aLine);
        }
        br.close ();
    }
    public void processLine(String aLine)
    {	// creates a scanner to locate integers as separated by commas
        Scanner scan = new Scanner(aLine);
        scan.useDelimiter(",");
        int ID = scan.nextInt();
        switch (ID)		// the first integer in a line identifies the object type
        {
        case 1:
        	int x = scan.nextInt();		// other integers in a line correspond to
        	int y = scan.nextInt();		// object information such as location and type
        	int type = scan.nextInt();
        	Wall WALL = new Wall (x, y);
        	walls.add (WALL);			// a new object is created according to the data
        	break;
        }
    }
    public void reload(){	// when a level is reset, all current data is erased and
        walls.clear();		// replaced with the initial data
        try{
            loadFileData(sourceFile);
        }catch(IOException ioe){
            ioe.printStackTrace();
        }
    }
}
