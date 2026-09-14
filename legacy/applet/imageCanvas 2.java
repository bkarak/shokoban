import java.awt.*;
import java.awt.event.*;

public class imageCanvas extends Canvas implements MouseListener{
	private final static String VERSION = "ReneGade/TiS imageCanvas Class Ver. 0.01";
	private Image canvasPic;
	private int width,height;
	private GameCanvas theCanvas;
	
	/* Constructors */
	public imageCanvas(Image img,GameCanvas theCanvas){
		this.theCanvas = theCanvas;
		this.canvasPic = img;
		this.width = canvasPic.getWidth(this);
		this.height = canvasPic.getHeight(this);
		this.setSize(width,height);
		this.addMouseListener(this);
	}
	
	public imageCanvas(){
		this.canvasPic = null;	
	}
	
	/* functions */
	public static String getVersion(){
		return imageCanvas.VERSION;
	}
	
	/* events */
	/* not used */
	public void mouseClicked(MouseEvent e){}
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	public void mouseReleased(MouseEvent e){}
	
	/* used */
	public void mousePressed(MouseEvent e){
		new InfoFrame("Sokoban Info",theCanvas);
	}
	
	public void paint(Graphics g){
		if(canvasPic != null)
			g.drawImage(canvasPic,0,0,this);
	}
}