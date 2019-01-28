package com.dawninfotek.logx.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dawninfotek.logx.util.LogXUtils;

public class LogXField {
	
	public static final Logger logger = LoggerFactory.getLogger(LogXField.class);
	
	public static int SCOPE_CONTEXT = 1;
	
	public static int SCOPE_THREAD = 0;
	
	public static int SCOPE_LINE = 2;
	
	public static int SCOPE_EVENT = 2;
	
	public static Map<String, Integer> scopeMapping;
	
	static {
		scopeMapping = new HashMap<String, Integer>();
		scopeMapping.put("CONTEXT", SCOPE_CONTEXT);
		scopeMapping.put("THREAD", SCOPE_THREAD);
		scopeMapping.put("LINE", SCOPE_LINE);
		scopeMapping.put("EVENT", SCOPE_EVENT);		
	}
	
	public LogXField() {
		super();
	}
	
	public LogXField(String fieldName) {
		super();
		this.name = fieldName;
		this.key = LogXUtils.getLogProperty(fieldName + ".key", fieldName);
		this.value = LogXUtils.getLogProperty(fieldName + ".value", "");
		if (StringUtils.isEmpty(value)) {
			//the log system may not be fully initialized at this point, use System.out 
			System.out.println("value of logx filed:" + fieldName + " is not define propertly" );			
		}
		
		String s = LogXUtils.getLogProperty(fieldName + ".scope", null);
		if(s != null) {
			Integer isc = scopeMapping.get(s);
			if(isc != null) {
				this.scope = isc.intValue();
			}			
		}
		
		String[] sa = LogXUtils.getLogProperties(fieldName + ".for.packages", null);
		
		if(sa != null) {
			this.forPackages = new ArrayList<String>();
			for(String p:sa) {
				this.forPackages.add(p);
			}
		}
		
		sa = LogXUtils.getLogProperties(fieldName + ".for.logLevel", null);
		
		if(sa != null) {
			this.forLogLevels = new ArrayList<String>();
			for(String p:sa) {
				this.forLogLevels.add(p);
			}
		}		
		
	}
	
	private String name;
	
	private String key;
	
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

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

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
