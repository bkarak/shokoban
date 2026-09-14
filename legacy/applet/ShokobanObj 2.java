public class ShokobanObj{
	/* statics */
	public static final int TYPE_PLAYER = 0;
	public static final int TYPE_DIAMOND = 1;
	/* obj info */
	private int type;
	private int x,y;
	private int start_x,start_y;
	
	/* Constructors */
	public ShokobanObj(int type,int x,int y){
		this.type = type;
		this.x = x;
		this.y = y;
		this.start_x = x;
		this.start_y = y;
	}
	
	/* methods */
	public int getType(){
		return type;	
	}
	
	public int getStartX(){
		return start_x;	
	}
	
	public int getStartY(){
		return start_y;	
	}
	
	public int getX(){
		return x;
	}
	
	public void setX(int new_x){
		x = new_x;
	}
	
	public int getY(){
		return y;
	}
	
	public void setY(int new_y){
		y = new_y;	
	}
}