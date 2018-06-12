package generator.file.creator;

import generator.services.ServiceGeneratorConstants;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;

import storage.classes.ServiceGeneratorStorage;

public class CreateGeneratorFile {

	private static final String PROJ_SRC_DIR = "C:/workspaces/Product/WorkEnv/src/";
	
	public void createServiceFile() throws Exception {
		 String packageDir = "generated/services";
		 String businessMethodWithParams = ServiceGeneratorStorage.getInstance().getBusinessMethodBuffer().toString();
		 String businessMethodWithoutParams = businessMethodWithParams.substring(0, businessMethodWithParams.lastIndexOf("("));
		 String classFileName = businessMethodWithoutParams+"ServHandler";
		 classFileName = classFileName.substring(0, 1).toUpperCase()+classFileName.substring(1);
		 File serviceFile = new File(PROJ_SRC_DIR+packageDir+"/"+classFileName+".java");
		 
		 FileOutputStream fos = new FileOutputStream(serviceFile);
		 fos.write(("package "+packageDir.replaceAll("/", ".")+";\n\n").getBytes());
		 fos.write(ServiceGeneratorStorage.getInstance().getImportsBuffer().toString().getBytes());
		
		 fos.write(("\n\npublic class "+classFileName+"\n{\n").getBytes());
		 if(ServiceGeneratorConstants.generationMode == ServiceGeneratorConstants.GEN_MODE.LOCAL)
		 {
			 fos.write(("\t\tApplicationContext mAppCtx = null;\n").getBytes());
			 fos.write(("\t\tApplicationCustomizationUtils mCustUtils = null;\n\n").getBytes());
			 fos.write(("\t\tpublic "+classFileName+"(ApplicationContext appCtx) throws Exception\n\t\t{\n\t\t\tmAppCtx = appCtx;\n").getBytes());
			 fos.write(("\t\t\tmCustUtils = appCtx.createCustomizationUtils();\n\t\t}\n\n").getBytes());
		 }
		 if(ServiceGeneratorConstants.withMain)
			 fos.write(("\t\tpublic static void main(String[] args) throws Exception\n\t\t{\n").getBytes());
		 else
		 {
			 fos.write(("\t\tpublic void "+businessMethodWithoutParams+"(").getBytes());
			 if(ServiceGeneratorConstants.generationMode == ServiceGeneratorConstants.GEN_MODE.EJB)
				 fos.write(("InitialContext context,").getBytes());
			 fos.write(("int param) throws Exception\n\t\t{\n").getBytes());
		 }
		 fos.write(ServiceGeneratorStorage.getInstance().getBodyBuffer().toString().getBytes());
		 fos.write(ServiceGeneratorStorage.getInstance().getEjbMethodInvocationBuffer().toString().getBytes());
		 HashMap<String, String> responseMap = ServiceGeneratorStorage.getInstance().getResponseDataMap();
		 int i = 0;
		 fos.write(("\n").getBytes());
		 for(String responseGetter : responseMap.keySet())
		 {
			 i++;
			 fos.write(("\t\t\tSystem.out.println(\""+responseMap.get(responseGetter)+" == \"+"+responseGetter+");\n").getBytes());
			 if(i==5)
				 break;
		 }
		 fos.write(("\n\t\t\tSystem.out.println(\""+businessMethodWithoutParams+" - Ended Successfully..\");\n").getBytes());
		 fos.write(("\t\t}\n").getBytes());
		 fos.write(("}\n").getBytes());
		 fos.flush();
		 fos.close();
	}
}
