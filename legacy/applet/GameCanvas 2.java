import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import java.util.Stack;
import java.util.Hashtable;
import java.util.EmptyStackException;

public class GameCanvas extends Canvas implements KeyListener{
	/* statics */
	/* public */
	public final static int DEBUG_MODE = 1;
	public final static int NORMAL_MODE = 0;
	/* private */
	private final static int NORTH = 0;
	private final static int SOUTH = 1;
	private final static int WEST = 2;
	private final static int EAST = 3;
	
	/* GameCanvas variables */
	private ShokobanSkin gameCanvasSkin;
	private int max_x,max_y;
	private int tileSet_X,tileSet_Y;
	private int mode = 0;
	/* skin specific */
	private Image wall,socket,pos,player,floor,dos,diamond;
	/* map specific */
	private int[][] mapData;
	private ShokobanObj playerObj;
	private ShokobanObj[] diamondObjs;
	private Stack undoStack;
	private int map_x,map_y;
	private boolean diamondMoved;
	private Hashtable mapInfo;
	
	/* Constructors */
	public GameCanvas(ShokobanSkin theSkin){
		Dimension dim = this.getSize();
		max_x = dim.width;
		max_y = dim.height;
		this.undoStack = new Stack();
		this.addKeyListener(this);
		this.mode = 0;
		this.setBackground(Color.black);
		this.assignSkin(theSkin);
		this.diamondMoved = false;
	}
	
	/* assign methods */
	public void undoMove(){
		int x = playerObj.getX(),y = playerObj.getY();
		int tmp_x = x,tmp_y = y;
		
		try{
			ShokobanUndoObj undoObj = (ShokobanUndoObj)undoStack.pop();
			paintMapTile(x,y);
			playerObj.setX(undoObj.getX());
			playerObj.setY(undoObj.getY());
			paintShokobanObj(playerObj);
			if(undoObj.getDiamMoved()){
				switch(undoObj.getDirection()){
					case ShokobanUndoObj.NORTH:
						tmp_x--;
						break;
					case ShokobanUndoObj.SOUTH:
						tmp_x++;
						break;
					case ShokobanUndoObj.EAST:
						tmp_y--;
						break;
					case ShokobanUndoObj.WEST:
						tmp_y++;
						break;
				}
				int pos = ifDiamondExists(tmp_x,tmp_y);
				if(pos != -1){
					paintMapTile(tmp_x,tmp_y);
					diamondObjs[pos].setX(x);
					diamondObjs[pos].setY(y);
					paintShokobanObj(diamondObjs[pos]);
				}
			}
		}catch(EmptyStackException e){
			new MessageFrame("Sokoban Message","No More moves",true);
		}
	}
	
	private void assignSkin(ShokobanSkin theSkin){
		this.gameCanvasSkin = theSkin;
		this.tileSet_X = this.gameCanvasSkin.getWallImage().getWidth(this);
		this.tileSet_Y = this.gameCanvasSkin.getWallImage().getHeight(this);
		this.wall = this.gameCanvasSkin.getWallImage();
		this.socket = this.gameCanvasSkin.getSocketImage();
		this.pos = this.gameCanvasSkin.getPOSImage();
		this.player = this.gameCanvasSkin.getPlayerImage();
		this.floor = this.gameCanvasSkin.getFloorImage();
		this.dos = this.gameCanvasSkin.getDOSImage();
		this.diamond = this.gameCanvasSkin.getDiamondImage();
		repaint();
	}
	
	public void assignMap(ShokobanMap skMap){
		mapData = skMap.getMap2DArray();
		map_x = skMap.getMapX();
		map_y = skMap.getMapY();
		playerObj = skMap.getPlayerObj();
		diamondObjs = skMap.getDiamondObj();
		undoStack.removeAllElements();
		mapInfo = skMap.getHashInfo();
		repaint();
	}
	
	/* general methods */
	public String getCurrentMapInfo(){
		String res = "";
		
		res += ("Current MAP Name: " + (String)mapInfo.get(ShokobanMap.MAP_NAME) + "\n");
		res += ("Author: " + (String)mapInfo.get(ShokobanMap.MAP_AUTHOR) + "\n");
		res += ("Width: " + (String)mapInfo.get(ShokobanMap.MAP_X) + " tiles\n");
		res += ("Height: " + (String)mapInfo.get(ShokobanMap.MAP_Y) + " tiles\n");
		
		return res;
	}	
	
	public void restartMap(){
		playerObj.setX(playerObj.getStartX());
		playerObj.setY(playerObj.getStartY());
		for(int counter = 0;counter < diamondObjs.length;counter++){
			diamondObjs[counter].setX(diamondObjs[counter].getStartX());
			diamondObjs[counter].setY(diamondObjs[counter].getStartY());
		}
		undoStack.removeAllElements();
		repaint();
	}
	
	public int getX(){
		return max_x;	
	}
	
	public int getY(){
		return max_y;	
	}
	
	public void setGameCanvasSkin(ShokobanSkin selSkin){
		this.assignSkin(selSkin);
	}
	
	public ShokobanSkin getGameCanvasSkin(){
		return gameCanvasSkin;
	}
	
	/* debug functions */
	private void debugSkin(Graphics g){		
		mode = 1;
		g.drawImage(wall,0,0,this);
		g.drawImage(socket,20,0,this);
		g.drawImage(pos,40,0,this);
		g.drawImage(player,60,0,this);
		g.drawImage(floor,0,20,this);
		g.drawImage(dos,20,20,this);
		g.drawImage(diamond,40,20,this);
	}
	
	public void setMode(int mode){
		this.mode = mode;
		repaint();
	}
	
	public int getMode(){
		return mode;
	}
	
	/* events */
	/* used */
	public void paint(Graphics g){
		switch(mode){
			case 1:
				debugSkin(g);
				break;
			case 0:
				paintMap(g);
				paintObjs(g);
				break;
		}
	}
	
	public void keyPressed(KeyEvent e){
		int tmp_x = playerObj.getX(),tmp_y = playerObj.getY();
		int x = tmp_x,y = tmp_y;
		
		try{
			switch(e.getKeyCode()){
				case 73:
				case 38:
					tmp_x--;
					if(canPlay(tmp_x,tmp_y,NORTH)){
						paintMapTile(x,y);
						undoStack.push(new ShokobanUndoObj(x,y,ShokobanUndoObj.NORTH,diamondMoved));
						playerObj.setX(tmp_x);
						playerObj.setY(tmp_y);
						paintShokobanObj(playerObj);
					}
					break;
				case 75:
				case 40:
					tmp_x++;
					if(canPlay(tmp_x,tmp_y,SOUTH)){
						paintMapTile(x,y);
						undoStack.push(new ShokobanUndoObj(x,y,ShokobanUndoObj.SOUTH,diamondMoved));
						playerObj.setX(tmp_x);
						playerObj.setY(tmp_y);					
						paintShokobanObj(playerObj);
					}
					break;
				case 74:
				case 37:
					tmp_y--;
					if(canPlay(tmp_x,tmp_y,EAST)){
						paintMapTile(x,y);
						undoStack.push(new ShokobanUndoObj(x,y,ShokobanUndoObj.EAST,diamondMoved));
						playerObj.setX(tmp_x);
						playerObj.setY(tmp_y);
						paintShokobanObj(playerObj);
					}
					break;
				case 76:
				case 39:
					tmp_y++;
					if(canPlay(tmp_x,tmp_y,WEST)){
						paintMapTile(x,y);
						undoStack.push(new ShokobanUndoObj(x,y,ShokobanUndoObj.WEST,diamondMoved));
						playerObj.setX(tmp_x);
						playerObj.setY(tmp_y);
						paintShokobanObj(playerObj);
					}
					break;
				case 85:
					undoMove();
					break;
				default:
					System.out.println("Unknown KeyCode (" + e.getKeyCode() + ")");
			}
		}catch(Exception ex){}
		if(won()){ 
			new MessageFrame("Sokoban Message","You won!!",true);
		}
	}
	
	/* not used */
	public void keyReleased(KeyEvent e){}
	public void keyTyped(KeyEvent e){}
	
	/* gameplay methods */
	private boolean won(){
		for(int counter = 0;counter < diamondObjs.length;counter++){
			int x = diamondObjs[counter].getX(),y = diamondObjs[counter].getY();
			if(mapData[x][y] != ShokobanMap.MAP_SOCKET){ return false; }
		}
		
		return true;
	}
	
	private int ifDiamondExists(int x,int y){
		for(int counter = 0;counter < diamondObjs.length;counter++){
			if((diamondObjs[counter].getX() == x) && (diamondObjs[counter].getY() == y)){
				return counter;
			}
		}
		
		return -1;
	}
	
	private boolean canPlay(int x,int y,int direction){
		int pos = ifDiamondExists(x,y);
		
		switch(mapData[x][y]){
			case ShokobanMap.MAP_FLOOR:
			case ShokobanMap.MAP_SOCKET:
				if(pos == -1){ diamondMoved = false;return true; }
				else{ 
					diamondMoved = canMoveDiamond(pos,direction);
					return diamondMoved;
				}
			case ShokobanMap.MAP_WALL:
				return false;
		}
		
		return false;
	}
	
	private boolean canMoveDiamond(int diamondPosition,int direction){
		int x = diamondObjs[diamondPosition].getX(),y = diamondObjs[diamondPosition].getY();
		
		switch(direction){
			case NORTH:
				x--;
				break;
			case SOUTH:
				x++;
				break;
			case EAST:
				y--;
				break;
			case WEST:
				y++;
		}
		
		switch(mapData[x][y]){
			case ShokobanMap.MAP_WALL:
				return false;
			case ShokobanMap.MAP_FLOOR:
			case ShokobanMap.MAP_SOCKET:
				if(ifDiamondExists(x,y) == -1){
					diamondObjs[diamondPosition].setX(x);
					diamondObjs[diamondPosition].setY(y);
					paintShokobanObj(diamondObjs[diamondPosition]);
					return true;
				}else{ return false; }
		}
		
		return false;
	}
		
	/* Paint methods */
	private void paintMap(Graphics g){
		for(int counter = 0;counter < map_x;counter++){
			for(int inner = 0;inner < map_y;inner++){
				Image img;
				
				switch(mapData[inner][counter]){
					case ShokobanMap.MAP_WALL:
						img = wall;
						break;
					case ShokobanMap.MAP_FLOOR:
						img = floor;
						break;
					case ShokobanMap.MAP_SOCKET:
						img = socket;
						break;
					default:
						System.out.println("GameCanvas::paintMap Unknown Type (" + mapData[inner][counter] + ")");
						img = null;
				}
				g.drawImage(img,counter*tileSet_X,inner*tileSet_Y,this);
			}
		}
	}
		
	private void paintObjs(Graphics g){
		int x,y;
		Image img;
		
		/* first draw the player */
		x = playerObj.getX();
		y = playerObj.getY();
		if(mapData[x][y] == ShokobanMap.MAP_SOCKET) img = pos;
		else img = player;
		g.drawImage(img,y*tileSet_X,x*tileSet_Y,this);
		/* now draw the dimaonds */
		for(int counter = 0;counter < diamondObjs.length;counter++){
			x = diamondObjs[counter].getX();
			y = diamondObjs[counter].getY();
			
			if(mapData[x][y] == ShokobanMap.MAP_SOCKET) img = dos;
			else img = diamond;
			g.drawImage(img,y*tileSet_X,x*tileSet_Y,this);
		}
	}
	
	private void paintShokobanObj(ShokobanObj targetObj){
		int x = targetObj.getX(),y = targetObj.getY();
		Image img = null;
		
		switch(targetObj.getType()){
			case ShokobanObj.TYPE_DIAMOND:
				if(mapData[x][y] == ShokobanMap.MAP_SOCKET) img = dos;
				else img = diamond;
				break;
			case ShokobanObj.TYPE_PLAYER:
				if(mapData[x][y] == ShokobanMap.MAP_SOCKET) img = pos;
				else img = player;	
		}
		this.getGraphics().drawImage(img,y*tileSet_X,x*tileSet_Y,this);
	}
	
	private void paintMapTile(int x,int y){
		switch(mapData[x][y]){
			case ShokobanMap.MAP_FLOOR:
				this.getGraphics().drawImage(floor,y*tileSet_X,x*tileSet_Y,this);
				break;
			case ShokobanMap.MAP_SOCKET:
				this.getGraphics().drawImage(socket,y*tileSet_X,x*tileSet_Y,this);
		}
	}
	
}