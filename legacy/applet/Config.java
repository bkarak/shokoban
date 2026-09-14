import java.util.StringTokenizer;
import java.util.Hashtable;
import java.util.Enumeration;

public class Config{
	/* statics */
	/* public */
	public static final String LOGO_IMAGE = "logoImage";
	public static final String DIR_IMAGE = "imageDir";
	public static final String NEW_IMAGE = "newImage";
	public static final String NEW_IMAGE_SEL = "newImageSel";
	public static final String UNDO_IMAGE = "undoImage";
	public static final String UNDO_IMAGE_SEL = "undoImageSel";
	public static final String LOAD_SKIN_IMAGE = "loadSkin";
	public static final String LOAD_SKIN_IMAGE_SEL = "loadSkinSel";
	public static final String LOAD_LEVEL_IMAGE = "loadLevel";
	public static final String LOAD_LEVEL_IMAGE_SEL = "loadLevelSel";
	/* private */
	private static final int IMAGE_TYPE = 0;
	private static final int SKIN_TYPE = 1;
	private static final int MAP_TYPE = 2;
	
	/* main Configuration structures */
	private Hashtable configImage;
	private Hashtable configSkins;
	private Hashtable configMaps;
	
	public Config(String configFile,String skinFile,String mapFile){
		configImage = new Hashtable();
		configSkins = new Hashtable();
		configMaps = new Hashtable();
		parseConfiguration(configFile,IMAGE_TYPE);
		parseConfiguration(skinFile,SKIN_TYPE);
		parseConfiguration(mapFile,MAP_TYPE);
	}
	
	private void parseConfiguration(String theFile,int type){
		StringTokenizer strtok = new StringTokenizer(theFile,"%%");
		while(strtok.hasMoreTokens()){
			switch(type){
				case IMAGE_TYPE:
					this.assignDataImage(strtok.nextToken().trim());	
					break;
				case SKIN_TYPE:
					this.assignDataSkin(strtok.nextToken().trim());
					break;
				case MAP_TYPE:
					this.assignDataMap(strtok.nextToken().trim());
			}
		}
	}
	
	private void assignDataImage(String data){
		StringTokenizer strtok = new StringTokenizer(data,"=");
		configImage.put(strtok.nextToken().trim(),strtok.nextToken().trim());
	}
	
	private void assignDataSkin(String data){
		StringTokenizer strtok = new StringTokenizer(data,"=");
		configSkins.put(strtok.nextToken().trim(),strtok.nextToken().trim());
	}
	
	private void assignDataMap(String data){
		StringTokenizer strtok = new StringTokenizer(data,"=");
		configMaps.put(strtok.nextToken().trim(),strtok.nextToken().trim());
	}
	
	public Hashtable getConfigImageHashtable(){
		return configImage;
	}
	
	public Enumeration getMapKeys(){
		return configMaps.keys();	
	}
	
	public String getConfigImageValue(String ImageKey){
		return ((String)this.configImage.get(Config.DIR_IMAGE) + (String)this.configImage.get(ImageKey));
	}
	
	public Enumeration getSkinKeys(){
		return configSkins.keys();
	}
	
	public String getSkinDir(String key){
		return (String)configSkins.get(key);
	}
	
	public String getMapFile(String key){
		return (String)configMaps.get(key);
	}
}