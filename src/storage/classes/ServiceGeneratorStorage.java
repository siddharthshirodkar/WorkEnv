package storage.classes;

import java.util.HashMap;

public class ServiceGeneratorStorage {

	private StringBuffer importsBuffer;
	private StringBuffer bodyBuffer;
	private StringBuffer businessMethodBuffer;
	private StringBuffer ejbMethodInvocationBuffer;
	private StringBuffer responseDatatypeBuffer;
	private HashMap<String, String> responseDataMap;
	private static ServiceGeneratorStorage storage= null;
	
	private ServiceGeneratorStorage()
	{
	}
	
	public static ServiceGeneratorStorage getInstance()
	{
		if(storage == null)
			storage = new ServiceGeneratorStorage();
		
		return storage;
	}
	
	public void resetStorage()
	{
		storage = null;
	}
	
	public StringBuffer getImportsBuffer() {
		return importsBuffer;
	}
	public void setImportsBuffer(StringBuffer importsBuffer) {
		this.importsBuffer = new StringBuffer(importsBuffer);
	}
	public StringBuffer getBodyBuffer() {
		return bodyBuffer;
	}
	public void setBodyBuffer(StringBuffer bodyBuffer) {
		this.bodyBuffer = new StringBuffer(bodyBuffer);
	}
	public StringBuffer getBusinessMethodBuffer() {
		return businessMethodBuffer;
	}
	public void setBusinessMethodBuffer(StringBuffer businessMethodBuffer) {
		this.businessMethodBuffer = businessMethodBuffer;
	}
	public StringBuffer getEjbMethodInvocationBuffer() {
		return ejbMethodInvocationBuffer;
	}
	public void setEjbMethodInvocationBuffer(StringBuffer ejbMethodInvocationBuffer) {
		this.ejbMethodInvocationBuffer = ejbMethodInvocationBuffer;
	}
	public HashMap<String, String> getResponseDataMap() {
		return responseDataMap;
	}
	public void setResponseDataMap(HashMap<String, String> responseDataMap) {
		this.responseDataMap = responseDataMap;
	}
	public StringBuffer getResponseDatatypeBuffer() {
		return responseDatatypeBuffer;
	}
	public void setResponseDatatypeBuffer(StringBuffer responseDatatypeBuffer) {
		this.responseDatatypeBuffer = responseDatatypeBuffer;
	}
}
