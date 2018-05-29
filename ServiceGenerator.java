package generator.services;

import generator.file.creator.CreateGeneratorFile;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import storage.classes.ServiceGeneratorStorage;

public class ServiceGenerator {
	
	private static String HOST_PORT = "illinqw353:11601";
	/*
	private static String REMOTE_CLASS = "com.amdocs.cih.services.contactrole.interfaces.IContactRoleServiceRemote";
	private static String HOME_CLASS = "com.amdocs.cih.services.contactrole.interfaces.IContactRoleServiceRemoteHome";
	private static String JNDI_NAME = "com.amdocs.cih.services.contactrole.interfaces.IContactRoleServiceCrmRemote";
	private static String BUSINESS_METHOD = "retrieveContactRoleForSubscription";
	*/
	public static final String MODULE_PACKAGE = "crossabp";
	
	private static String BUSINESS_METHOD = "getProratedQuote";
	//private static String BUSINESS_METHOD = "viewRecharge";
	
	private static ArrayList<String> ignoreSetMethodsList = new ArrayList<String>();
	private static HashMap<String,String> primitiveTypeDefaultValueMap = new HashMap<String,String>();
	private static HashMap<String,String> abstractToConcreteMap = new HashMap<String,String>();
	private static HashMap<String,String> noDefaultConstructorMap = new HashMap<String,String>();
	
	private HashSet<String> arrayParams = null;
	private HashSet<String> simpleParams = null;
	private HashSet<String> abstractParamTypes = null;
	private HashSet<String> getterMethodsSet = null;
	private HashMap<String,String> parameterTypeToVariableNameMap = null;
	private HashMap<String,String> parameterTypeToActualParameterTypeMap = null;
	private HashMap<String,String> parameterTypeToCanonicalParameterTypeMap = null;
	private HashMap<String,ArrayList<String>> variableNameToSettersListMap = null;
	private HashMap<String,String> setterNameToParameterMap = null;
	private ArrayList<String> setterNamesArrayParameterList = null;
	private HashMap<String,String> responseGettersToVariableNameMap = null;
	private HashSet<String> importsList = null;
	
	private static String REMOTE_CLASS = null;
	private static String HOME_CLASS = null;
	private static String JNDI_NAME = null;
	
	private static HashSet<String> staticImportsList = new HashSet<String>();
	
	private ServiceGeneratorStorage storage = ServiceGeneratorStorage.getInstance();
	private String responseDatatype;
	private boolean isResponseDatatypeArray = false;
	private static final String responseVarName = "output";
	private static final String RETURN_VOID = "void";
	
	private static final String JAVA_CLASS_PATH = "java.class.path";
	private static final String JAVA_PATH_SEPERATOR = "path.separator";
	private static final String JAR_FILE_EXTENSION = ".jar";
	private static final String CLASS_FILE_EXTENSION = ".class";
	private static final boolean generateEntireBean = false; 
	
	static
	{
		staticImportsList.add("java.util.Date");
		if(ServiceGeneratorConstants.withMain)
		{
			staticImportsList.add("java.util.Properties");
			staticImportsList.add("javax.naming.Context");
		}
		if(ServiceGeneratorConstants.generationMode == ServiceGeneratorConstants.GEN_MODE.LOCAL)
		{
			staticImportsList.add("amdocs.acmarch.core.ApplicationContext");
			staticImportsList.add("amdocs.acmarch.customization.ApplicationCustomizationUtils");
		}
		else
		{
			staticImportsList.add("javax.naming.InitialContext");
			staticImportsList.add("javax.rmi.PortableRemoteObject");
		}
		
		noDefaultConstructorMap.put("Locale", "Locale.ENGLISH");
		ignoreSetMethodsList.add("setCloneToPublic");
		ignoreSetMethodsList.add("setFields");
		primitiveTypeDefaultValueMap.put("boolean", "true");
		primitiveTypeDefaultValueMap.put("short", "(short)48");
		primitiveTypeDefaultValueMap.put("byte", "(byte)48");
		primitiveTypeDefaultValueMap.put("int", "0");
		primitiveTypeDefaultValueMap.put("double", "0d");
		primitiveTypeDefaultValueMap.put("long", "0l");
		primitiveTypeDefaultValueMap.put("float", "0F");
		primitiveTypeDefaultValueMap.put("String", "\"\"");
		primitiveTypeDefaultValueMap.put("Date", "new Date()");
		primitiveTypeDefaultValueMap.put("boolean[]", "new boolean[]{true}");
		primitiveTypeDefaultValueMap.put("short[]", "new short[]{(short)48}");
		primitiveTypeDefaultValueMap.put("byte[]", "new byte[]{(byte)48}");
		primitiveTypeDefaultValueMap.put("int[]", "new int[]{0}");
		primitiveTypeDefaultValueMap.put("double[]", "new double[]{0d}");
		primitiveTypeDefaultValueMap.put("long[]", "new long[]{0l}");
		primitiveTypeDefaultValueMap.put("float[]", "new float[]{0F}");
		primitiveTypeDefaultValueMap.put("String[]", "new String[]{\"\"}");
		primitiveTypeDefaultValueMap.put("Date", "new Date()");
		abstractToConcreteMap.put("amdocs.rpm.datatypes.BaseChannel", "amdocs.rpm.datatypes.IVRChannelInfo");
		abstractToConcreteMap.put("amdocs.rpm.datatypes.BaseRegularRechargeMethod", "amdocs.rpm.datatypes.CashRechargeMethodInfo");
		abstractToConcreteMap.put("amdocs.rpm.datatypes.BaseBucketIdent", "amdocs.rpm.datatypes.PcnInfo");
		abstractToConcreteMap.put("amdocs.rpm.datatypes.BaseSubscriberIdent", "amdocs.rpm.datatypes.SubscriberPrimaryResourceInfo");
		abstractToConcreteMap.put("amdocs.rpm.datatypes.BaseSetExpDateRechargeMethod", "amdocs.rpm.datatypes.SetBalExpDateByDaysRechargeMethodInfo");
	}
	
	public void generate() throws Exception
	{
		generateSettersForProxy();
	}
	
	public void generateSettersForProxy() throws Exception
	{
		Class clazz = null;
		if(REMOTE_CLASS == null)
		{
			clazz = findRemoteInterface(BUSINESS_METHOD);
			if(clazz == null)
			{
				System.out.println("API - "+BUSINESS_METHOD+" not found in the classpath");
				return;
			}
			REMOTE_CLASS = clazz.getName();
		}
		if(HOME_CLASS == null)
			HOME_CLASS = getHomeClassFromRemoteInteface(REMOTE_CLASS);
		if(JNDI_NAME == null)
			JNDI_NAME = getJndiNameFromHomeClass(HOME_CLASS);
		
		boolean foundBusinessMethod = false;
		for(Method method : clazz.getDeclaredMethods())
		{
			StringBuffer importsBuffer = new StringBuffer();
			StringBuffer bodyBuffer = new StringBuffer();
			StringBuffer businessMethodInvocationBuffer = new StringBuffer();
			
			storage = ServiceGeneratorStorage.getInstance();
			arrayParams = new HashSet<String>();
			simpleParams = new HashSet<String>();
			abstractParamTypes = new HashSet<String>();
			getterMethodsSet = new HashSet<String>();
			parameterTypeToVariableNameMap = new HashMap<String,String>();
			parameterTypeToActualParameterTypeMap = new HashMap<String,String>();
			parameterTypeToCanonicalParameterTypeMap = new HashMap<String,String>();
			variableNameToSettersListMap = new HashMap<String,ArrayList<String>>();
			setterNameToParameterMap = new HashMap<String,String>();
			setterNamesArrayParameterList = new ArrayList<String>();
			responseGettersToVariableNameMap = new HashMap<String,String>();
			importsList = new HashSet<String>();
			responseDatatype = "";
			isResponseDatatypeArray = false;
			
			if(method.getName().equalsIgnoreCase(BUSINESS_METHOD) || generateEntireBean)
			{
				System.out.println("Business method :: "+method.getName());
				foundBusinessMethod = true;
				//if(method.getParameterTypes().length!=12)
				//	continue;
				
				//System.out.println("Found business method :: "+method.getName());
				//System.out.println("Number of parameters :: "+method.getParameterTypes().length);
				
				importsList.add(HOME_CLASS);
				
				for(Class parameterType : method.getParameterTypes())
					generateSettersForClass(parameterType);
				
				for(String importClass : importsList)
					importsBuffer.append("import "+importClass+";\n");
				
				responseDatatype = method.getReturnType().getSimpleName();
				String responseDatatypeClass = method.getReturnType().getCanonicalName();
				if(responseDatatype.endsWith("[]"))
				{
					isResponseDatatypeArray = true;
					responseDatatypeClass = responseDatatypeClass.substring(0, responseDatatypeClass.lastIndexOf("[]"));
				}
				if(!responseDatatype.equalsIgnoreCase(RETURN_VOID))
					importsBuffer.append("import "+responseDatatypeClass+";\n");
				else
					responseDatatype = "";
				
				HashSet<String> variableNamesActualParametersSet = new HashSet<String>(parameterTypeToActualParameterTypeMap.values());
				for(String parameterType : variableNamesActualParametersSet)
				{
					//if(parameterType.equalsIgnoreCase("PhysicalResourceInfo"))
						//System.out.println(parameterType);
					if(arrayParams.contains(parameterType) && !simpleParams.contains(parameterType))
						continue;
					if(noDefaultConstructorMap.keySet().contains(parameterType))
						bodyBuffer.append("\t\t\t"+parameterType+" a"+parameterType +" = "+noDefaultConstructorMap.get(parameterType)+";\n");
					else
					{
						try{
							Class.forName(parameterTypeToCanonicalParameterTypeMap.get(parameterType)).newInstance();
						}
						catch(ClassNotFoundException cfne)
						{ System.err.println(cfne.toString());}
						catch(InstantiationException inse)
						{ System.err.println(inse.toString());}
						bodyBuffer.append("\t\t\t"+parameterType+" a"+parameterType +" = new "+parameterType+"();\n");
					}
				}
				bodyBuffer.append("\n");
				for(String parameterType : arrayParams)
				{
					if(!abstractParamTypes.contains(parameterType))
					{
						bodyBuffer.append("\t\t\t"+parameterType+"[] a"+parameterType +"Arr = new "+parameterType+"[1];\n");
						if(noDefaultConstructorMap.keySet().contains(parameterType))
							bodyBuffer.append("\t\t\t"+"a"+parameterType +"Arr[0] ="+noDefaultConstructorMap.get(parameterType)+";\n");
						else
						{
							try{
								Class.forName(parameterTypeToCanonicalParameterTypeMap.get(parameterType)).newInstance();
							}
							catch(ClassNotFoundException cfne)
							{ 
								System.out.println();
								System.err.println(cfne.toString());
							}
							/*
							catch(NullPointerException npe)
							{ System.err.println(parameterType);}
							*/
							bodyBuffer.append("\t\t\t"+"a"+parameterType +"Arr[0] = new "+parameterType+"();\n");
						}
					}
				}
				bodyBuffer.append("\n");
				
				/*
				System.out.println("-- 1 --");
				for(String setterName : setterNameToParameterMap.keySet())
					System.out.println(setterName +" ---> "+setterNameToParameterMap.get(setterName));
				
				System.out.println("-- 2 --");
				for(String parameterType : parameterTypeToVariableNameMap.keySet())
					System.out.println(parameterType +" ---> "+parameterTypeToVariableNameMap.get(parameterType));
				
				System.out.println("-- 3 --");
				for(String parameterType : parameterTypeToActualParameterTypeMap.keySet())
					System.out.println(parameterType +" ---> "+parameterTypeToActualParameterTypeMap.get(parameterType));
				*/
				
				HashSet<String> variableNamesSet = new HashSet<String>(parameterTypeToVariableNameMap.values());
				for(String variableName : variableNamesSet)
				{
					//System.out.println("parameterType = "+parameterType.getSimpleName());
					//System.out.println("variableName = "+variableName);
					for(String setterMethod : variableNameToSettersListMap.get(variableName))
					{
						String setterValue = primitiveTypeDefaultValueMap.get(setterNameToParameterMap.get(variableName+"."+setterMethod));
						
						if(setterValue == null)
							setterValue = parameterTypeToVariableNameMap.get(setterNameToParameterMap.get(variableName+"."+setterMethod));
						if(setterValue == null)
							setterValue = parameterTypeToVariableNameMap.get(parameterTypeToActualParameterTypeMap.get(setterNameToParameterMap.get(variableName+"."+setterMethod)));
						
						//if(setterMethod.equalsIgnoreCase("setRelatedOffers"))
						//	System.out.println(setterValue);
						
						try{
							if(setterValue.endsWith("Arr[0]"))
							{
								if(!setterNamesArrayParameterList.contains(variableName+"."+setterMethod))
									setterValue = setterValue.substring(0, setterValue.lastIndexOf("Arr[0]"));
								else
								{
									setterValue = setterValue.substring(0, setterValue.lastIndexOf("["));
									if(!setterValue.endsWith("Arr"))
										setterValue += "Arr";
								}
							}
							else
							{
								if(setterNamesArrayParameterList.contains(variableName+"."+setterMethod))
									setterValue += "Arr";
							}
						}
						//Just for debugging!!
						catch(NullPointerException npe)
						{
							System.err.println(variableName+"."+setterMethod+" --xxx "+setterNameToParameterMap.get(variableName+"."+setterMethod));
							throw npe;
						}
						bodyBuffer.append("\t\t\t"+variableName+"."+setterMethod+"("+setterValue+");\n");
					}
					bodyBuffer.append("\n");
				}
				
				if(isResponseDatatypeArray)
					generateGettersForClass(method.getReturnType(),responseVarName+"[0]");
				else
					generateGettersForClass(method.getReturnType(),responseVarName);
				storage.setResponseDataMap(responseGettersToVariableNameMap);
				businessMethodInvocationBuffer.append(method.getName()+"(");
				int i = 0;
				for(Class parameterType : method.getParameterTypes())
				{
					boolean isArrayType = false;
					String parameterTypeString = parameterType.getSimpleName();
					if(parameterType.getSimpleName().endsWith("[]"))
					{
						isArrayType = true;
						parameterTypeString = parameterTypeString.substring(0, parameterTypeString.lastIndexOf("["));//+"Arr[0]";
					}
					if(primitiveTypeDefaultValueMap.containsKey(parameterType.getSimpleName()))
						businessMethodInvocationBuffer.append(primitiveTypeDefaultValueMap.get(parameterType.getSimpleName()));
					else
						businessMethodInvocationBuffer.append("a"+parameterTypeToActualParameterTypeMap.get(parameterTypeString));
					if(isArrayType)
						businessMethodInvocationBuffer.append("Arr");
					if(i!=method.getParameterTypes().length-1)
						businessMethodInvocationBuffer.append(",");
					i++;
				}
				businessMethodInvocationBuffer.append(");\n");
				
				for(String importClass : staticImportsList)
					importsBuffer.append("import "+importClass+";\n");
				
				storage.setImportsBuffer(importsBuffer);
				storage.setBodyBuffer(bodyBuffer);
				storage.setBusinessMethodBuffer(businessMethodInvocationBuffer);
				storage.setEjbMethodInvocationBuffer(getEjbInvocationMethodBuffer());
				
				CreateGeneratorFile createFile = new CreateGeneratorFile();
				createFile.createServiceFile();
				
				storage.resetStorage();
				
				if(!generateEntireBean)
					break;
			}
		}
		if(!foundBusinessMethod)
		{
			System.out.println("The Remote Class - '"+REMOTE_CLASS+"', does not contain the API - '"+BUSINESS_METHOD+"'");
			return;
		}
	}
	
	private void generateGettersForClass(Class parameterType, String pathSofar) throws Exception 
	{
		boolean isArrayType = false;
		if(parameterType.getSimpleName().endsWith("[]"))
		{
			isArrayType = true;
			String simpleTypeOfArrayType = parameterType.getCanonicalName().substring(0, parameterType.getCanonicalName().indexOf("["));
			parameterType = Class.forName(simpleTypeOfArrayType);
		}
		for(Method method : parameterType.getDeclaredMethods())
		{
			if(method.getName().startsWith("get") && (method.getParameterTypes().length==0))
			{
				if(getterMethodsSet.contains(method.getName()))
					continue;
				else
					getterMethodsSet.add(method.getName());
				isArrayType = false;
				pathSofar += "."+method.getName();
				parameterType = method.getReturnType();
				if(parameterType.getSimpleName().endsWith("[]"))
				{
					isArrayType = true;
					String simpleTypeOfArrayType = parameterType.getCanonicalName().substring(0, parameterType.getCanonicalName().indexOf("["));
					if(!primitiveTypeDefaultValueMap.containsKey(simpleTypeOfArrayType))
						parameterType = Class.forName(simpleTypeOfArrayType);
				}
				pathSofar += "()";
				if(isArrayType)
					pathSofar+="[0]";
				 
				Class getMethodReturnType = method.getReturnType();
				if(primitiveTypeDefaultValueMap.containsKey(getMethodReturnType.getSimpleName()))
				{
					responseGettersToVariableNameMap.put(pathSofar, pathSofar.substring(pathSofar.lastIndexOf(".")+4,pathSofar.length()-2));
					//System.out.println(pathSofar.substring(pathSofar.lastIndexOf(".")+4,pathSofar.length()-2)+" == "+pathSofar);
					pathSofar = pathSofar.substring(0,pathSofar.lastIndexOf("."));
					//System.err.println("IFFF--pathSofar=="+pathSofar);
				}
				else
				{
					//System.err.println("ESLEE (before)--pathSofar=="+pathSofar);
					generateGettersForClass(getMethodReturnType,pathSofar);
					pathSofar = pathSofar.substring(0,pathSofar.lastIndexOf("."));
					//System.err.println("ESLEE(after) --pathSofar=="+pathSofar);
				}
			}
		}
	}

	public void generateSettersForClass(Class parameterType) throws Exception
	{
		/*
		if(parameterType.getSimpleName().contains("PhysicalResourceInfo"))
		{
			System.out.println(parameterType.getSimpleName());
			System.out.print("");
		}
		*/
		
		String variableName = "";
		boolean isArray = false;
		
		if(parameterType.getSimpleName().endsWith("[]"))
		{
			isArray = true;
			String simpleTypeOfArrayType = parameterType.getCanonicalName().substring(0, parameterType.getCanonicalName().lastIndexOf("["));
			arrayParams.add(parameterType.getSimpleName().substring(0, parameterType.getSimpleName().lastIndexOf("[")));
			parameterType = Class.forName(simpleTypeOfArrayType);
		}
		else
			simpleParams.add(parameterType.getSimpleName());
		
		if(primitiveTypeDefaultValueMap.containsKey(parameterType.getSimpleName()))
			return;
		else
		{
			if(Modifier.isAbstract(parameterType.getModifiers()))
			{
				abstractParamTypes.add(parameterType.getSimpleName());
				Class assignableClass = findAssignableClassInClasspath(parameterType);
				if(isArray)
					assignableClass =  Class.forName("[L" + assignableClass.getCanonicalName()+";");
				generateSettersForClass(assignableClass);
				variableName = "a"+assignableClass.getSimpleName();
				if(isArray)
				{
					String simpleTypeOfArrayType = assignableClass.getCanonicalName().substring(0, assignableClass.getCanonicalName().lastIndexOf("["));
					assignableClass = Class.forName(simpleTypeOfArrayType);
					variableName = "a"+assignableClass.getSimpleName()+"Arr[0]";
				}
				parameterTypeToActualParameterTypeMap.put(parameterType.getSimpleName(), assignableClass.getSimpleName());
				importsList.add(assignableClass.getCanonicalName());
				parameterTypeToCanonicalParameterTypeMap.put(assignableClass.getSimpleName(), assignableClass.getCanonicalName());
				parameterTypeToVariableNameMap.put(parameterType.getSimpleName(), variableName);
			}
			else
			{
				variableName = "a"+parameterType.getSimpleName();
				if(isArray)
					variableName = "a"+parameterType.getSimpleName()+"Arr[0]";
				importsList.add(parameterType.getCanonicalName());
				parameterTypeToCanonicalParameterTypeMap.put(parameterType.getSimpleName(), parameterType.getCanonicalName());
				parameterTypeToActualParameterTypeMap.put(parameterType.getSimpleName(), parameterType.getSimpleName());
				parameterTypeToVariableNameMap.put(parameterType.getSimpleName(), variableName);
			}
		}
		ArrayList<String> settersList = new ArrayList<>();
		for(Method method : parameterType.getMethods())
		{
			if(method.getName().startsWith("set") && !method.getName().endsWith("All")
					&& !method.getName().endsWith("UnAssigned") && !ignoreSetMethodsList.contains(method.getName())
						&& (method.getParameterTypes().length==1))
			{
				for(Class setMethodParameter : method.getParameterTypes())
				{
					if(primitiveTypeDefaultValueMap.containsKey(setMethodParameter.getSimpleName()))
						setterNameToParameterMap.put(variableName+"."+method.getName(), setMethodParameter.getSimpleName());
					else
					{
						Class originalSetMethodParmeter = setMethodParameter;
						boolean isSetMethodParamArray = false;
						
						if(setMethodParameter.getSimpleName().endsWith("[]"))
						{
							isSetMethodParamArray = true;
							String setMethodParameterName = setMethodParameter.getCanonicalName().substring(0,setMethodParameter.getCanonicalName().lastIndexOf("["));
							setMethodParameter = Class.forName(setMethodParameterName);
							arrayParams.add(setMethodParameter.getSimpleName());
							setterNamesArrayParameterList.add(variableName+"."+method.getName());
						}
						else
							simpleParams.add(setMethodParameter.getSimpleName());

						setterNameToParameterMap.put(variableName+"."+method.getName(), setMethodParameter.getSimpleName());
						
						if(!parameterTypeToVariableNameMap.containsKey(setMethodParameter.getSimpleName())
								&& !primitiveTypeDefaultValueMap.containsKey(setMethodParameter.getSimpleName()))
						{
							if(isSetMethodParamArray)
								setMethodParameter = originalSetMethodParmeter;
							generateSettersForClass(setMethodParameter);
						}
					}
					settersList.add(method.getName());
				}
			}
		}
		if(!variableNameToSettersListMap.containsKey(variableName))
			variableNameToSettersListMap.put(variableName, settersList);
	}
	
	public Class findAssignableClassInClasspath(Class parameterType) throws Exception
	{
		if(abstractToConcreteMap.containsKey(parameterType.getCanonicalName()))
		{
			Class assignableClass = Class.forName(abstractToConcreteMap.get(parameterType.getCanonicalName()));
			if(parameterType.isAssignableFrom(assignableClass) && !Modifier.isAbstract(assignableClass.getModifiers()))
    			return assignableClass;
			else
				System.err.println(parameterType.getSimpleName()+" is either assignable from "+assignableClass.getSimpleName()+" or is an abstract class");
		}
		
		String classpath = System.getProperty(JAVA_CLASS_PATH);
		String[] jarFiles = classpath.split(System.getProperty(JAVA_PATH_SEPERATOR));
		for(String jarFileName : jarFiles)
		{
			if(jarFileName.endsWith(JAR_FILE_EXTENSION))
			{
				JarFile jarFile = new JarFile(jarFileName);
		        final Enumeration<JarEntry> entries = jarFile.entries();
		        while (entries.hasMoreElements()) 
		        {
		            final JarEntry entry = entries.nextElement();
		            String entryName = entry.getName();
		            if(entryName.endsWith(CLASS_FILE_EXTENSION))
		            {
		            	if(entryName.startsWith(parameterType.getCanonicalName().substring(0,parameterType.getCanonicalName().lastIndexOf(".")).replace('.', '/')))
		            	{
		            		if(entryName.endsWith(CLASS_FILE_EXTENSION))
		            			entryName = entryName.substring(0, entryName.lastIndexOf("."));
		            		Class assignableClass = Class.forName(entryName.replace('/', '.'));
		            		if(parameterType.isAssignableFrom(assignableClass) && !Modifier.isAbstract(assignableClass.getModifiers()))
		            			return assignableClass;
		            	}
		            }
		        }
			 }
		}
		return null;
	}
	
	
	public Class findRemoteInterface(String apiName) throws Exception
	{
		String classpath = System.getProperty(JAVA_CLASS_PATH);
		String[] jarFiles = classpath.split(System.getProperty(JAVA_PATH_SEPERATOR));
		for(String jarFileName : jarFiles)
		{
			if(jarFileName.endsWith(JAR_FILE_EXTENSION))
			{
				JarFile jarFile = new JarFile(jarFileName);
		        final Enumeration<JarEntry> entries = jarFile.entries();
		        while (entries.hasMoreElements()) 
		        {
		            final JarEntry entry = entries.nextElement();
		            String entryName = entry.getName();
		            if(entryName.endsWith(CLASS_FILE_EXTENSION) && entryName.contains(MODULE_PACKAGE))
		            {
		            	int lastIdxOfClass = entryName.lastIndexOf(".");
		            	String className = entryName.substring(0, lastIdxOfClass);
		            	className = className.replaceAll("/", ".");
		            	try
		            	{
			            	if(doesExtendsEJBRemote(className))
			            		if(doesContainRequiredApi(className,apiName))
			            		{
			            			jarFile.close();
			            			return Class.forName(className);
			            		}
		            	}
		            	catch(NoClassDefFoundError cnfe)
		            	{}
		            	catch(ExceptionInInitializerError ini)
		            	{}
		            }
		        }
		        jarFile.close();
			 }
		}
		return null;
	}

	public StringBuffer getEjbInvocationMethodBuffer() throws Exception
	{
		StringBuffer sbuff = new StringBuffer();
		if(!(doesExtendsEJBRemote(REMOTE_CLASS) || doesExistsAndExtendsEJBHome(HOME_CLASS)))
		{
			staticImportsList.remove("java.util.Properties");
			staticImportsList.remove("javax.naming.Context");
			staticImportsList.remove("javax.naming.InitialContext");
			staticImportsList.remove("javax.rmi.PortableRemoteObject");
			staticImportsList.remove(HOME_CLASS);
			return sbuff;
		}
		
		String homeObject = HOME_CLASS.substring(HOME_CLASS.lastIndexOf(".")+1);
		//sbuff.append(("\t\tpublic void getEjbInvocationMethod() throws Exception\n\t\t{\n"));
		if(ServiceGeneratorConstants.withMain)
		{
			sbuff.append("\t\t\tProperties props = new Properties();\n");
			sbuff.append("\t\t\tprops.put(Context.INITIAL_CONTEXT_FACTORY, \"weblogic.jndi.WLInitialContextFactory\");\n");
			sbuff.append("\t\t\tprops.put(Context.SECURITY_PRINCIPAL, \"ABPBatchUser\");\n");
			sbuff.append("\t\t\tprops.put(Context.SECURITY_CREDENTIALS, \"Unix11\");\n");
			sbuff.append("\t\t\tprops.put(Context.PROVIDER_URL, \"t3://"+HOST_PORT+"\");\n");
			sbuff.append("\t\t\tInitialContext context = new InitialContext(props);\n");
		}
		if(ServiceGeneratorConstants.generationMode == ServiceGeneratorConstants.GEN_MODE.EJB)
		{
			sbuff.append("\t\t\t"+homeObject+" homeObject = ("+homeObject+")PortableRemoteObject.narrow(context.lookup(\"");
			sbuff.append(JNDI_NAME+"\"), "+homeObject+".class);\n");
			if(responseDatatype.length() == 0)
				sbuff.append("\t\t\thomeObject.create()."+ServiceGeneratorStorage.getInstance().getBusinessMethodBuffer().toString());
			else
				sbuff.append("\t\t\t"+responseDatatype+" "+responseVarName+" = homeObject.create()."+ServiceGeneratorStorage.getInstance().getBusinessMethodBuffer().toString());
		}
		else
		{
			sbuff.append("\t\t\t//LocalInterface aLI = ProxyFactory.getInstance.create(mAppCtx);\n");
			if(responseDatatype.length() == 0)
				sbuff.append("\t\t\t//aLI."+ServiceGeneratorStorage.getInstance().getBusinessMethodBuffer().toString());
			else
				sbuff.append("\t\t\t//"+responseDatatype+" "+responseVarName+" = aLI."+ServiceGeneratorStorage.getInstance().getBusinessMethodBuffer().toString());
			
			sbuff.append("\t\t\t//aLI.close();\n");
		}
		//sbuff.append("\t\t}\n");
		//System.out.println(sbuff.toString());
		return sbuff;
	}

	private boolean doesExtendsEJBRemote(String className) throws ClassNotFoundException
	{
		Class actualClass = null;
		try
		{
			actualClass = Class.forName(className);
		}
		catch(Exception arch)
		{
			System.err.println("Exception on initilaizing -- "+className);
			return false;
		}
		
		if(!Class.forName("javax.ejb.EJBObject").isAssignableFrom(actualClass))
			return false;
		return true;
	}

	private boolean doesExistsAndExtendsEJBHome(String className) throws ClassNotFoundException
	{
		if(!Class.forName("javax.ejb.EJBHome").isAssignableFrom(Class.forName(className)))
			return false;
		return true;
	}
	
	private boolean doesContainRequiredApi(String remoteInterface, String apiName) throws ClassNotFoundException
	{
		Method[] methodArr = Class.forName(remoteInterface).getDeclaredMethods();
		for(Method method : methodArr)
		{
			if(method.getName().equalsIgnoreCase(apiName))
				return true;
		}
		return false;
	}
	
	private String getHomeClassFromRemoteInteface(String remoteInterface) throws ClassNotFoundException
	{
		String homeClass = remoteInterface.replaceAll(".api.", ".home.");
		homeClass+="Home";
		return homeClass;
	}
	
	private String getJndiNameFromHomeClass(String homeClass) throws ClassNotFoundException
	{
		String jndiName = homeClass.substring(homeClass.lastIndexOf("."));
		jndiName = "amdocsBeans"+jndiName;
		return jndiName;
	}
}
