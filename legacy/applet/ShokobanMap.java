/*
Map Symbols - 

1 - wall
2 - Socket
3 - Floor
4 - Diamond
5 - Player
6 - Player On Socket
7 - Diamond On Socket
*/
import java.util.StringTokenizer;
import java.util.Hashtable;
import java.util.Vector;

public class ShokobanMap{
	/* statics */
	public static final String MAP_DATA = "DATA";
	public static final String MAP_X = "Width";
	public static final String MAP_Y = "Height";
	public static final String MAP_NAME = "Name";
	public static final String MAP_AUTHOR = "Author";
	/* map symbols */
	public static final int MAP_WALL = 1;
	public static final int MAP_SOCKET = 2;
	public static final int MAP_FLOOR = 3;
	public static final int MAP_DIAMOND = 4;
	public static final int MAP_PLAYER = 5;
	public static final int MAP_POS = 6;
	public static final int MAP_DOS = 7;
	
	/* Map Info */
	private Hashtable mapInfo;
	private int[] mapValues;
	private Vector diamPlay;
	
	/* Constructors */
	public ShokobanMap(String theMap){
		mapInfo = new Hashtable();
		diamPlay = new Vector();
		parseMapFile(theMap);
	}
	
	/* parsing methods */
	private void parseMapFile(String theMap){
		StringTokenizer strtok = new StringTokenizer(theMap,"%%");
		while(strtok.hasMoreElements()){
			getMapInfo(strtok.nextToken().trim());	
		}
	}
	
	private void getMapInfo(String data){
		StringTokenizer strtok = new StringTokenizer(data,"=");
		String firstArg = ((String)strtok.nextElement()).trim();
		String secondArg = ((String)strtok.nextElement()).trim();
		if(firstArg.equalsIgnoreCase(MAP_DATA)){
			parseMapData(secondArg);
		}else{ mapInfo.put(firstArg,secondArg); }
	}
	
	private void parseMapData(String mapData){
		StringTokenizer strtok = new StringTokenizer(mapData,";;");
		String input = "";
		while(strtok.hasMoreElements()){
			input += " " + strtok.nextToken().trim();
		}
		assignToTable(input.trim());
	}
	
	private void assignToTable(String map){	
		int counter = 0;
		
		mapValues = new int[getMapX()*getMapY()];
		StringTokenizer strtok = new StringTokenizer(map," ");
		while(strtok.hasMoreElements()){
			mapValues[counter] = Integer.parseInt(strtok.nextToken().trim());
			if(mapValues[counter] >= 4){
				int x = oneToTwo(counter,getMapX(),false),y = oneToTwo(counter,getMapX(),true);
				
				switch(mapValues[counter]){
					case MAP_DIAMOND:
						diamPlay.addElement(new ShokobanObj(ShokobanObj.TYPE_DIAMOND,x,y));
						mapValues[counter] = MAP_FLOOR;
						break;
					case MAP_PLAYER:
						diamPlay.addElement(new ShokobanObj(ShokobanObj.TYPE_PLAYER,x,y));
						mapValues[counter] = MAP_FLOOR;
						break;
					case MAP_POS:
						diamPlay.addElement(new ShokobanObj(ShokobanObj.TYPE_PLAYER,x,y));
						mapValues[counter] = MAP_SOCKET;
						break;
					case MAP_DOS:
						diamPlay.addElement(new ShokobanObj(ShokobanObj.TYPE_DIAMOND,x,y));
						mapValues[counter] = MAP_SOCKET;						
						break;
				}
			}
			++counter;
		}
	}
	
	/* */
	public static String debugMap(ShokobanMap skMap){
		String result = "";
		int[] tmp = skMap.getMapArray();
		int map_x = skMap.getMapX();
		Vector pd;
		
		result += "MAP NAME: " + skMap.getName() + "\n";
		result += "MAP AUTHOR: " + skMap.getAuthor() + "\n";
		result += "MAP X: " + skMap.getMapX() + " Y: " + skMap.getMapY() + "\n";
		for(int counter = 0;counter < tmp.length;counter++){
			result += String.valueOf(tmp[counter]);
			if(((counter + 1) % map_x) == 0){ result += "\n"; }
		}
		result += "\nObjects List\n";
		pd = skMap.getDPVec();
		result += "Total Number:" + pd.size() + "\n";
		for(int counter = 0;counter < pd.size();counter++){
			ShokobanObj _tmp = (ShokobanObj)pd.elementAt(counter);
			switch(_tmp.getType()){
				case ShokobanObj.TYPE_DIAMOND:
					result += ("OBJ TYPE: DIAMOND X=" + _tmp.getX() + ",Y=" + _tmp.getY() + "\n");
					break;
				case ShokobanObj.TYPE_PLAYER:
					result += ("OBJ TYPE: PLAYER X=" + _tmp.getX() + ",Y=" + _tmp.getY() + "\n");
					break;
			}
		}
		
		return result;
	}
	
	/* general methods */
	public Hashtable getHashInfo(){
		return mapInfo;	
	}
	
	public String getName(){
		return (String)mapInfo.get(MAP_NAME);
	}
	
	public String getAuthor(){
		return (String)mapInfo.get(MAP_AUTHOR);	
	}
	
	public int getMapX(){
		return Integer.parseInt((String)mapInfo.get(MAP_X));
	}
	
	public int getMapY(){
		return Integer.parseInt((String)mapInfo.get(MAP_Y));
	}
	
	public int[] getMapArray(){
		return mapValues;	
	}
	
	public int[][] getMap2DArray(){
		int x = getMapX();
		int[][] result = new int[getMapY()][x];
		
		for(int counter = 0;counter < mapValues.length;counter++){
			result[counter / x][counter % x] = mapValues[counter];
		}
		
		return result;
	}
	
	public int[][] getMap2DArrayFull(){
		int x = getMapX();
		int[][] result = new int[getMapY()][x];
		
		for(int counter = 0;counter < mapValues.length;counter++){
			result[counter / x][counter % x] = mapValues[counter];
		}
		
		for(int counter = 0;counter < diamPlay.size();counter++){
			ShokobanObj sObj = (ShokobanObj)diamPlay.get(counter);	
			int theX = sObj.getX(),theY = sObj.getY();

			switch(sObj.getType()){				
				case ShokobanObj.TYPE_DIAMOND:
					if(result[theX][theY] == MAP_FLOOR) result[theX][theY] = MAP_DIAMOND;
					else result[theX][theY] = MAP_DOS;
					break;
				case ShokobanObj.TYPE_PLAYER:
					if(result[theX][theY] == MAP_FLOOR) result[theX][theY] = MAP_PLAYER;
					else result[theX][theY] = MAP_POS;					
					break;
			}
		}
		
		return result;		
	}
		
	public int oneToTwo(int target,int max_x,boolean showY){
		int x = target / max_x;
		int y = target % max_x;
			
		if(showY){ return y; }
		else{ return x; }
	}
	
	public Vector getDPVec(){
		return diamPlay;	
	}
	
	public ShokobanObj[] getDPArray(){
		ShokobanObj[] result = new ShokobanObj[diamPlay.size()];
		
		for(int counter = 0;counter < diamPlay.size();counter++){
			result[counter] = (ShokobanObj)diamPlay.elementAt(counter);	
		}
		
		return result;
	}
	
	public ShokobanObj getPlayerObj(){
		for(int counter = 0;counter < diamPlay.size();counter++){
			ShokobanObj res = (ShokobanObj)diamPlay.elementAt(counter);
			if(res.getType() == ShokobanObj.TYPE_PLAYER){ return res; }
		}
		
		return null;
	}
	
	public ShokobanObj[] getDiamondObj(){
		ShokobanObj[] res = new ShokobanObj[diamPlay.size() - 1];
		int inner = 0;
		
		for(int counter = 0;counter < diamPlay.size();counter++){
			ShokobanObj tmp = (ShokobanObj)diamPlay.elementAt(counter);
			if(tmp.getType() == ShokobanObj.TYPE_DIAMOND){
				res[inner] = tmp;
				inner++;
			}
		}
		
		return res;
	}
	
}