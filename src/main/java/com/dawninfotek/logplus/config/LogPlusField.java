package com.dawninfotek.logplus.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dawninfotek.logplus.util.LogPlusUtils;

public class LogPlusField {
	
	public static final Logger logger = LoggerFactory.getLogger(LogPlusField.class);
	
	public static int SCOPE_CONTEXT = 1;
	
	public static int SCOPE_THREAD = 0;
	
	public static int SCOPE_LINE = 2;
	
	public static int SCOPE_EVENT = 2;
	
	public static int SCOPE_ENTERPRISE = 3;
	
	public static Map<String, Integer> scopeMapping;
	
	static {
		scopeMapping = new HashMap<String, Integer>();
		scopeMapping.put("CONTEXT", SCOPE_CONTEXT);
		scopeMapping.put("THREAD", SCOPE_THREAD);
		scopeMapping.put("LINE", SCOPE_LINE);
		scopeMapping.put("EVENT", SCOPE_EVENT);	
		scopeMapping.put("ENTERPRISE", SCOPE_ENTERPRISE);	
	}
	
	public LogPlusField() {
		super();
	}
	
	public LogPlusField(String fieldName) {
		super();
		this.name = fieldName;
		//this.key = LogPlusUtils.getLogProperty(fieldName + ".key", fieldName);
		this.value = LogPlusUtils.getLogProperty(fieldName + ".value", "");
		if (StringUtils.isEmpty(value)) {
			//the log system may not be fully initialized at this point, use System.out 
			System.out.println("value of LogPlus filed:" + fieldName + " is not define propertly" );			
		}
		
		String s = LogPlusUtils.getLogProperty(fieldName + ".scope", null);
		if(s != null) {
			Integer isc = scopeMapping.get(s);
			if(isc != null) {
				this.scope = isc.intValue();
			}			
		}
		
		String[] sa = LogPlusUtils.getLogProperties(fieldName + ".for.packages", null);
		
		if(sa != null) {
			this.forPackages = new ArrayList<String>();
			for(String p:sa) {
				this.forPackages.add(p);
			}
		}
		
		sa = LogPlusUtils.getLogProperties(fieldName + ".for.logLevel", null);
		
		if(sa != null) {
			this.forLogLevels = new ArrayList<String>();
			for(String p:sa) {
				this.forLogLevels.add(p);
			}
		}		
		
	}
	
	private String name;
	
	//private String key;
	
	private String value;
	
	private int scope;
	
	private List<String> forPackages;
	
	private List<String> forLogLevels;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

//	public String getKey() {
//		return key;
//	}
//
//	public void setKey(String key) {
//		this.key = key;
//	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public int getScope() {
		return scope;
	}

	public void setScope(int scope) {
		this.scope = scope;
	}

	public List<String> getForPackages() {
		return forPackages;
	}

	public void setForPackages(List<String> forPackages) {
		this.forPackages = forPackages;
	}

	public List<String> getForLogLevels() {
		return forLogLevels;
	}

	public void setForLogLevels(List<String> forLogLevels) {
		this.forLogLevels = forLogLevels;
	}	

}
