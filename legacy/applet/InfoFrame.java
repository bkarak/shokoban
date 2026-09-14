import java.awt.*;
import java.awt.event.*;

public class InfoFrame extends Frame implements ActionListener{
	private Button exitButton;
	private TextArea textArea;
	private Panel flowSouth;
	private GameCanvas theCanvas;
	
	public InfoFrame(String title,GameCanvas theCanvas){
		this.setTitle(title);
		this.theCanvas = theCanvas;
		this.setBackground(Color.lightGray);
		this.setForeground(Color.black);
		this.setLayout(new BorderLayout());
		flowSouth = new Panel(new FlowLayout());
		exitButton = new Button("Close Info Window");
		this.add(flowSouth,BorderLayout.SOUTH);
		flowSouth.add(exitButton);
		this.setBounds(150,150,320,250);
		textArea = new TextArea();
		textArea.setEditable(false);
		this.add(textArea,BorderLayout.CENTER);
		exitButton.addActionListener(this);
		this.show();
		this.fillWithInfo();	
	}
	
	public void fillWithInfo(){
		textArea.append(shokobanApplet.getVersion() + "\n\n");
		textArea.append(theCanvas.getCurrentMapInfo());
		textArea.append("\n Contact the Author: p95053@rainbow.cs.unipi.gr");
	}
	
	public void actionPerformed(ActionEvent e){
		if(e.getActionCommand().equals("Close Info Window")){
			this.dispose();	
		}
	}
}