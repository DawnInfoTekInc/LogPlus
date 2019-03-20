package com.dawninfotek.logplus.core;

import java.util.HashMap;
import java.util.Map;

public class CurrentContext {
	
	private boolean inintialized;
	
	private Map<String, String> logPlusFields = new HashMap<String, String>();

	public boolean isInintialized() {
		return inintialized;
	}

	public void setInintialized(boolean inintialized) {
		this.inintialized = inintialized;
	}

	public Map<String, String> getLogPlusFields() {
		return logPlusFields;
	}

	public void setLogPlusFields(Map<String, String> logPlusFields) {
		this.logPlusFields = logPlusFields;
	}
	
	public String getFieldValue(String fieldName) {
		
		return logPlusFields.get(fieldName);
		
	}
	
	public void setFieldValue(String fieldName, String fieldValue) {		
		logPlusFields.put(fieldName, fieldValue);
	}
	

}
