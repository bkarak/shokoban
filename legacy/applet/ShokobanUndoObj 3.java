public class ShokobanUndoObj{
	/* statics */
	public final static int NORTH = 0;
	public final static int SOUTH = 1;
	public final static int WEST = 2;
	public final static int EAST = 3;
	/* variables */
	private int x,y,direction;
	private boolean diamMoved;
	
	public ShokobanUndoObj(int x,int y,int movedTo,boolean diamMoved){
		this.x = x;
		this.y = y;
		this.direction = movedTo;
		this.diamMoved = diamMoved;
	}
	
	public int getX(){
		return x;	
	}
	
	public int getY(){
		return y;
	}
	
	public int getDirection(){
		return direction;	
	}
	
	public boolean getDiamMoved(){
		return diamMoved;	
	}
}