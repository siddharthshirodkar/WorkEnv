package generator.services;

public class ServiceGeneratorConstants {
	public static enum GEN_MODE {LOCAL, EJB};
	
	public static final boolean withMain = false;
	public static final boolean validateJNDI = true;
	public static final GEN_MODE generationMode = GEN_MODE.EJB;
	
	public static final boolean generateEntireBean = false; 
	
	public static final String URL = "ilrtvit114:21111";
	public static final String MODULE_PACKAGE = "csm3g";
	public static final String API_TO_GENERATE = "l3CreateNewSubscriber";
	
}
