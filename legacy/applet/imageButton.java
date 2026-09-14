import java.awt.*;
import java.awt.event.*;

public class imageButton extends Component implements MouseListener{
	private final static String VERSION = "ReneGade/TiS imageButton Ver. 0.01";
	private Image original;
	private Image focused;
	private int width,height,mode;
	
	/* Constructors */
	public imageButton(Image original,Image focused){
		this.original = original;
		this.focused = focused;
		this.width = this.original.getWidth(this);
		this.height = this.original.getHeight(this);
		if(!checkImages()) System.out.println("imgButton::Constructor - WARNING -> PICS SIZE NOT EQUAL");
		this.setSize(width,height);
		this.mode = 0;
		this.addMouseListener(this);
	}
	
	/* functions */
	public void paint(Graphics g){
			g.drawImage(original,0,0,this);
	}
		
	private boolean checkImages(){
		if(original.getWidth(this) != focused.getWidth(this)) return false;
		if(original.getHeight(this) != focused.getHeight(this)) return false;
		return true;
	}
	
	public static String getVersion(){
		return imageButton.VERSION;	
	}
	
	/* Events */
	public void mouseClicked(MouseEvent e){}
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	public void mousePressed(MouseEvent e){
		this.getGraphics().drawImage(focused,0,0,this);
	}
	public void mouseReleased(MouseEvent e){
		this.getGraphics().drawImage(original,0,0,this);
	}
}