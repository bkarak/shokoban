import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.util.Enumeration;

public class shokobanApplet extends Applet implements MouseListener{
	/* statics */
	/* private */
	private static final String VERSION = "Sokoban Ver. 1.00 - Java Port By ReneGade/TiS";
	/* Components */
	private imageCanvas logoCanvas;
	private imageButton newButton;
	private imageButton undoButton;
	private imageButton loadSkin;
	private imageButton loadLevel;
	private GameCanvas gameCanvas;
	private Choice skinChoice;
	private Choice levelChoice;
	/* Configuration */
	private Config shokConfig;

	public void init(){
		MessageFrame msgDlg = new MessageFrame("Loading Sokoban Applet","Loading Config Files ...",false);
		
		/* Load The Config Files */
		shokConfig = new Config(getTextFileURL(this.getCodeBase() + "shokoban.cfg"),
												 getTextFileURL(this.getCodeBase() + "skins.cfg"),
												 getTextFileURL(this.getCodeBase() + "maps.cfg"));
		msgDlg.setLabel("Loading Images ...");
		/* init controls */
		setLayout(null);
		setSize(664,485);
		
		/* the new Button */
		newButton = new imageButton(loadImage(shokConfig.getConfigImageValue(Config.NEW_IMAGE)),
		                            loadImage(shokConfig.getConfigImageValue(Config.NEW_IMAGE_SEL)));
		newButton.setBounds(12,12,96,37);
		add(newButton);
		
		/* the undoButton */
		undoButton = new imageButton(loadImage(shokConfig.getConfigImageValue(Config.UNDO_IMAGE)),
		                             loadImage(shokConfig.getConfigImageValue(Config.UNDO_IMAGE_SEL)));
		undoButton.setBounds(120,12,96,37);
		add(undoButton);
	
		/* skin choice */
		msgDlg.setLabel("Loading Skins ...");
		skinChoice = new Choice();
		Enumeration enum = shokConfig.getSkinKeys();
		while(enum.hasMoreElements()){
			skinChoice.addItem((String)enum.nextElement());
		}
		skinChoice.select(0);
		skinChoice.setBounds(228,6,144,20);
		add(skinChoice);
		/* level choice */
		msgDlg.setLabel("Loading Maps ...");
		levelChoice = new Choice();
		enum = shokConfig.getMapKeys();
		while(enum.hasMoreElements()){
			levelChoice.addItem((String)enum.nextElement());
		}
		levelChoice.select(0);
		levelChoice.setBounds(228,30,144,20);
		add(levelChoice);
		/* load Skin Buttons */
		
		loadSkin = new imageButton(loadImage(shokConfig.getConfigImageValue(Config.LOAD_SKIN_IMAGE)),
		                           loadImage(shokConfig.getConfigImageValue(Config.LOAD_SKIN_IMAGE_SEL)));
		loadSkin.setBounds(377,12,72,16);
		add(loadSkin);
		/* load level Buttons */
		loadLevel = new imageButton(loadImage(shokConfig.getConfigImageValue(Config.LOAD_LEVEL_IMAGE)),
		                            loadImage(shokConfig.getConfigImageValue(Config.LOAD_LEVEL_IMAGE_SEL)));
		loadLevel.setBounds(377,36,72,16);
		add(loadLevel);
		
		/* init game Canvas*/
		msgDlg.setLabel("Initializing Game ... ");
		gameCanvas = new GameCanvas(new ShokobanSkin(shokConfig.getSkinDir(skinChoice.getSelectedItem()),this));
		gameCanvas.setBounds(12,60,640,420);
		add(gameCanvas);
		gameCanvas.assignMap(new ShokobanMap(getTextFileURL(getCodeBase() + shokConfig.getMapFile(levelChoice.getSelectedItem()))));

		/* init logo Canvas */
		logoCanvas = new imageCanvas(loadImage(shokConfig.getConfigImageValue(Config.LOGO_IMAGE)),gameCanvas);
		logoCanvas.setBounds(456,12,192,40);
		add(logoCanvas);

		newButton.addMouseListener(this);
		undoButton.addMouseListener(this);
		loadSkin.addMouseListener(this);
		loadLevel.addMouseListener(this);
		setBackground(Color.darkGray);
		setForeground(Color.white);
		
		Cursor hand = new Cursor(Cursor.HAND_CURSOR),arrow = new Cursor(Cursor.DEFAULT_CURSOR);
		
		gameCanvas.setCursor(arrow);
		newButton.setCursor(hand);
		undoButton.setCursor(hand);
		loadSkin.setCursor(hand);
		loadLevel.setCursor(hand);
		logoCanvas.setCursor(hand);
		skinChoice.setCursor(arrow);
		levelChoice.setCursor(arrow);
		this.setCursor(arrow);
		msgDlg.setLabel("Done!!");
		msgDlg.dispose();
		gameCanvas.requestFocus();
		System.out.println(this.getVersion());
	}
	
	public static String getVersion(){
		return VERSION;	
	}
	
	/* Events */
	/* used */
	public void mousePressed(MouseEvent e){
		if(e.getComponent() == newButton){
			gameCanvas.restartMap();
			gameCanvas.requestFocus();
		}
		if(e.getComponent() == undoButton){
			gameCanvas.undoMove();
		}
		if(e.getComponent() == loadSkin)
			gameCanvas.requestFocus();
			gameCanvas.setGameCanvasSkin(new ShokobanSkin(shokConfig.getSkinDir(skinChoice.getSelectedItem()),this));
		if(e.getComponent() == loadLevel){
			gameCanvas.requestFocus();
			gameCanvas.assignMap(new ShokobanMap(getTextFileURL(getCodeBase() + shokConfig.getMapFile(levelChoice.getSelectedItem()))));
		}
	}
	
	/* Not used */
	public void mouseClicked(MouseEvent e){}
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	public void mouseReleased(MouseEvent e){}
		
	/* Net Functions */
	public String getTextFileURL(String targetUrl){
		byte[] result;

		try{
			URL theURL = new URL(targetUrl);
			URLConnection theCon = theURL.openConnection();
			InputStream is = theCon.getInputStream();
			result = new byte[theCon.getContentLength()];
			for(int counter = 0;counter < theCon.getContentLength();counter++){
				result[counter] = new Integer(is.read()).byteValue();
			}
		}catch(Exception e){ 
			System.out.println("getTextFileURL Error - Cannot Retrieve '" + targetUrl + "'");
			return null;
		}
				
		return (new String(result));	
	}

	public Image loadImage(String filename){
		MediaTracker mt = new MediaTracker(this);
		Image img = this.getImage(this.getCodeBase(),filename);
		try{
			mt.addImage(img,0);
			mt.waitForID(0);
		}catch(Exception e){
			System.out.println("LoadImage Error - Cannot Retrieve '" + filename + "'");
			return null;
		}
		
		return img;
	}
}