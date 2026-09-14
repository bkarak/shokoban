import java.awt.Image;

public class ShokobanSkin{
	/* statics */
	/* private */
	private final static String WALL = "wall.gif";
	private final static String SOCKET = "socket.gif";
	private final static String POS = "player_on_socket.gif";
	private final static String PLAYER = "player.gif";
	private final static String FLOOR = "floor.gif";
	private final static String DOS = "diamond_on_socket.gif";
	private final static String DIAMOND = "diamond.gif";
	/* skin info */
	private String skinDir;
	private shokobanApplet theApplet;
	private Image wall,socket,pos,player,floor,dos,diamond;
	
	/* Constructors */
	public ShokobanSkin(String skinDir,shokobanApplet theApplet){
		this.skinDir = skinDir;
		this.theApplet = theApplet;
		wall = this.theApplet.loadImage(getWallFilename());
		socket = this.theApplet.loadImage(getSocketFilename());
		pos = this.theApplet.loadImage(getPOSFilename());
		player = this.theApplet.loadImage(getPlayerFilename());
		floor = this.theApplet.loadImage(getFloorFilename());
		dos = this.theApplet.loadImage(getDOSFilename());
		diamond = this.theApplet.loadImage(getDiamondFilename());
	}
	
	/* functions */
	public Image getWallImage(){
		return wall;	
	}
	
	public Image getSocketImage(){
		return socket;
	}	
	
	public Image getPOSImage(){
		return pos;	
	}
	
	public Image getPlayerImage(){
		return player;	
	}
	
	public Image getFloorImage(){
		return floor;	
	}
	
	public Image getDOSImage(){
		return dos;	
	}
	
	public Image getDiamondImage(){
		return diamond;	
	}
	
	/* filename functions */
	public String getSkinDir(){
		return skinDir;
	}
	
	public String getWallFilename(){
		return (theApplet.getCodeBase() + getSkinDir() + ShokobanSkin.WALL);
	}
	
	public String getSocketFilename(){
		return (theApplet.getCodeBase() + getSkinDir() + ShokobanSkin.SOCKET);
	}
	
	public String getPOSFilename(){
		return (theApplet.getCodeBase() + getSkinDir() + ShokobanSkin.POS);
	}
	
	public String getPlayerFilename(){
		return (theApplet.getCodeBase() + getSkinDir() + ShokobanSkin.PLAYER);	
	}
	
	public String getFloorFilename(){
		return (theApplet.getCodeBase() + getSkinDir() + ShokobanSkin.FLOOR);	
	}
	
	public String getDOSFilename(){
		return (theApplet.getCodeBase() + getSkinDir() + ShokobanSkin.DOS);	
	}
	
	public String getDiamondFilename(){
		return (theApplet.getCodeBase() + getSkinDir() + ShokobanSkin.DIAMOND);	
	}
}