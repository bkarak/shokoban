import java.awt.*;
import java.awt.event.*;

public class MessageFrame extends Frame implements WindowListener{
	private Label messageLabel;
	private boolean closing;
	
	public MessageFrame(String title,String firstLabel,boolean closing){
		super(title);
		this.addWindowListener(this);
		this.setBackground(Color.black);
		this.setForeground(Color.green);
		this.setBounds(350,250,300,90);
		this.setResizable(false);
		this.setLayout(new BorderLayout());
		messageLabel = new Label(firstLabel,Label.CENTER);
		this.add(messageLabel,BorderLayout.CENTER);
		this.closing = closing;
		this.show();
	}
		
	public void setLabel(String message){
		messageLabel.setText(message);
	}	
	
	public void windowActivated(WindowEvent e){}
	public void windowClosed(WindowEvent e){}
	public void windowClosing(WindowEvent e){
		if(closing){ this.dispose(); }
	}
	public void windowDeactivated(WindowEvent e){}
	public void windowDeiconified(WindowEvent e){}
	public void windowIconified(WindowEvent e){}
	public void windowOpened(WindowEvent e){}
}