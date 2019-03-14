package com.dawninfotek.logplus.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import com.dawninfotek.logplus.config.JsonField;
import com.dawninfotek.logplus.core.Component;
import com.dawninfotek.logplus.core.LogPlusConstants;
import com.dawninfotek.logplus.util.AntPathMatcher;
import com.dawninfotek.logplus.util.LogPlusUtils;
import com.dawninfotek.logplus.util.LogPlusProperties;

public class Configuration implements Component {
	
	//final static Logger logger = LoggerFactory.getLogger(Configuration.class);
	
	public static final String LOGPLUS_CONFIG_FILE_NAME = "logplus-default.properties";
	
	public static final String logplusConfigFile = "CLASS_PATH=logplus.properties";
	
	private String contextName = "";
	
	private Map<Object, Object> sectionMap;
	
	private Map<String, String> propertyMap;
	
	private AntPathMatcher pathMatcher = new AntPathMatcher();
	
	public Map<String, String> getPropertyMap() {
		return propertyMap;
	}
	
	private List<TransactionPathMappingRule> txRules;
	
	private List<JsonField> fieldsMapping;
	
	public static Configuration loadFromConfigFile(String configFile) {
		
		LogPlusProperties override = new LogPlusProperties();	
		
		if (configFile == null) {
			//before LogPlus is initialized, prevents to use logger 
			System.out.println("LogPlus configuration is not provided, use default only ...");
		} else {

			InputStream propFile = null;
			try {
				//logger.info("property file: " + configFile);
				System.out.println("property file: " + configFile);
				
				if (configFile.startsWith("CLASS_PATH") || configFile.startsWith("CLASS-PATH")) {
					String[] ns = configFile.split("=");
					if (ns.length <= 1) {
						ns = configFile.split(":", 2);
					}
					
					//try System Classloader
					propFile = ClassLoader.getSystemClassLoader().getResourceAsStream(ns[1]); 				
					
					if(propFile == null) {
						propFile = Configuration.class.getClassLoader().getResourceAsStream(ns[1]);
					}
					
				} else if (configFile.contains("FILE")) {
					String[] ns = configFile.split("=");
					if (ns.length <= 1) {
						ns = configFile.split(":", 2);
					}
					
					propFile = new FileInputStream(ns[1]);
				}
				
				if(propFile == null) {
					System.out.println("LogPlus configuration file:'" + configFile + "' is not found or invalid, only default configuration is used." );
				}else {				
					override.load(propFile);
					System.out.println("Overide config:" + override);
				}
				
			} catch (FileNotFoundException fnfe) {	
				System.out.println("LogPlus properties not found, ignored:" + fnfe.getMessage());
			} catch (Exception e) {				
				System.out.println("Fail to load LogPlus properties.");			
			} finally {
				try {
					if(propFile != null) {
						propFile.close();
					}
				} catch (Exception ignored) {
					ignored.printStackTrace();
				}
			}

		}
		
		return Configuration.init(override);
		
	}
	
	private Configuration(Map<String, String> propertyMap, List<TransactionPathMappingRule> txRules, List<JsonField> fieldsMapping, Map<Object, Object> cMap) {
		super();
		this.propertyMap = propertyMap;
		this.txRules = txRules;		
		this.fieldsMapping = fieldsMapping;
		this.sectionMap = cMap;
	}

	public String getConfigurationValue(String key) {
		return getConfigurationValueInternal(key);
	}
	
	public String getTransactionPath(HttpServletRequest request) {
		return getTransactionPathInternal(request);
	}
	
	public List<JsonField> getJsonFields(){
		return this.fieldsMapping;
	}
	
	/**
	 * By default, LogPlus configuration will load default configuration in the file CLASS-PATH:logplus.properties. if the given 
	 * overrideConfig is present, the default values will be overridden.
	 * @param overrideConfig
	 */
	private synchronized static Configuration init(LogPlusProperties overrideConfig) {
		
		InputStream propFile = null;
		
		try {
			//load the default properties
			Properties defaultConfig = new Properties();
			
			propFile = ClassLoader.getSystemClassLoader().getResourceAsStream(LOGPLUS_CONFIG_FILE_NAME); 		
			
			if(propFile == null) {				
				propFile = Configuration.class.getClassLoader().getResourceAsStream(LOGPLUS_CONFIG_FILE_NAME);
			}
			
			defaultConfig.load(propFile);		
				
			//merge the override items
			Map<Object, Object> cMap = new HashMap<Object, Object>();
			if(overrideConfig != null && !overrideConfig.isEmpty()) {
				for(Object key:overrideConfig.keySet()) {
					//Merge and override
					Object value = overrideConfig.get(key);
					if(value != null && value instanceof String) {
						defaultConfig.put(key, value);
					}else if(value instanceof Map) {
						cMap.put(key, value);
					}
				}
			}
					
			//crate property Map
			Map<String, String> pm = new HashMap<String, String>();
			for(Object kName:defaultConfig.keySet()) {				
				pm.put((String) kName, defaultConfig.getProperty((String)kName));				
			}		
				
			//create mapping definition objects
			List<TransactionPathMappingRule> rules = new ArrayList<TransactionPathMappingRule>();
			
			TransactionPathMappingRule rule = null;
			
			List<String> jsonFields = new ArrayList<String>();
			
			for(String name:pm.keySet()) {
				
				if(name.startsWith(LogPlusConstants.TX_PATH_PREFIX)) {
					//the line is for defining the rule
					rule = createRule(name, pm.get(name));
					if(rule != null) {
						rules.add(rule);
					}
				}
				if(name.equals(LogPlusConstants.JSON_LAYOUT_DEFAULT) || name.equals(LogPlusConstants.JSON_LAYOUT_CUSTOM)) {
					jsonFields.addAll(Arrays.asList(pm.get(name).split(",")));
				}
			}
			
			if(!rules.isEmpty()) {
				//sort the list
				Collections.sort(rules);
			}
			
			System.out.println("LogPlus system was inittialized successefully, " + pm.size() + " of properties were loaded, " + rules.size() + " of TransactionPath Mapping Rules were creared ...");
			
			List<JsonField> fieldsMapping = new ArrayList<JsonField>();
			
			JsonField fieldObj = null;
			
			if(jsonFields != null && jsonFields.size() > 0) {
				
				for(int i=0; i<jsonFields.size(); i++) {					
					fieldObj = JsonField.fromString(jsonFields.get(i));
					if(fieldObj != null) {
						//Still want to sort the un-position by the original order
						if(fieldObj.getPosition() == JsonField.UN_DEFINED) {
							//set the position
							fieldObj.setPosition(JsonField.UN_DEFINED - jsonFields.size() + i);
						}
						fieldsMapping.add(fieldObj);
					}
				}
			}
			
			//Sort the List
			sortJsonFields(fieldsMapping);			
			//finally, create the Configuration instance and return to caller
			return new Configuration(pm, rules, fieldsMapping, cMap);			
			
		}catch (Exception e) {
			System.out.println("LogPlus system failed to load config from files");
			e.printStackTrace();
			throw new RuntimeException("LogPlus system failed to load config from files", e);
		}finally {
			if(propFile != null) {
				try {
					propFile.close();
					
				} catch (Exception ignored) {
					ignored.printStackTrace();
				}
			}
		}		
	}
	
	private static void sortJsonFields(List<JsonField> jsonFields) {
		if(!jsonFields.isEmpty()) {
			Collections.sort(jsonFields);
		}
	}
	
	private String getConfigurationValueInternal(String key) {
		String value = "";
		if(!getContextName().isEmpty() && !getContextMap().isEmpty()) {
			Object result = getContextMap().get(contextName);
			if(result != null && result instanceof Map) {
				@SuppressWarnings("unchecked")
				Map<String, String> map = (Map<String, String>) result;
				value = map.get(key);
			}
		}
		if(value.isEmpty()) {
			return propertyMap.get(key);
		}
		return value;
	}
	
	private Map<Object, Object> getContextMap(){
		return sectionMap;
	}
	
	private void setContextName(String cname) {
		contextName = cname;
	}
	
	private String getContextName() {
		return contextName;
	}
	
	public String getTransactionPathInternal(HttpServletRequest request) {
		
		//Need to do the rule matching check one by one
		TransactionPathMappingRule lastMatch = null;
		
		for(TransactionPathMappingRule rule:txRules) {
			if(lastMatch != null && lastMatch.length >= rule.length) {
				continue;
			}
			if(isMatch(request, rule)) {
				if(lastMatch == null || lastMatch.getLength() < rule.getLength()) {
					//Always use the better match
					lastMatch = rule;
				}
			}		
		}
		setContextName(request.getContextPath());
		String result = null;
		if(lastMatch != null) {
			result = lastMatch.getTxPathName();
		}else {
			result = request.getRequestURI().substring(request.getContextPath().length());
		}
		return result;
	}
	
	private boolean isMatch(HttpServletRequest request, TransactionPathMappingRule rule) {
		
		boolean result = false;
		//First, the 'method' and path must be match
		if(rule.getMethod() == null || rule.getMethod().length() == 0 || request.getMethod().equalsIgnoreCase(rule.getMethod())) {
			//method match
			String[] uris = rule.getReqPath().split(",");
			for(String uri : uris) {
				//if(request.getRequestURI().substring(request.getContextPath().length()).startsWith(uri)){
				if(isPathMatching(request, uri)){
					//path match
					if(rule.getLength() == 2) {
						result = true;
						break;
					}else {
						//more verification required
						int c = rule.getLength() - 2;
						if(rule.getHeaderName() != null) {
							//checking header
							String hValue = request.getHeader(rule.getHeaderName());
							if(hValue != null && hValue.equals(rule.getHeaderValue())) {
								c--;
							}						
						}
						if(c == 0) {
							result = true;
							break;
						}else {
							if(rule.getParameterName() != null) {
								//checking parameter
								String pValue = request.getParameter(rule.getParameterName());
								if(pValue != null && pValue.equals(rule.getParameterValue())) {
									c--;
									if(c == 0) {
										result = true;
										break;
									}
								}
							}
						}					
					}
				}
			}
		}
		return result;
	}
	
	private boolean isPathMatching(HttpServletRequest request, String urlPattern) {
		
		String path = request.getRequestURI().substring(request.getContextPath().length());
		
		if(Boolean.valueOf(LogPlusUtils.getLogProperty(TX_PATH_PATTERN_MATCHING, "false"))){
			//use url pattern
			return pathMatcher.match(urlPattern, path);
		}else {
			//use start-with
			return path.startsWith(urlPattern);
		}
	}
	
	private static TransactionPathMappingRule createRule(String ruleName, String rule) {
		
		TransactionPathMappingRule result = new TransactionPathMappingRule();
		
		result.setTxPathName(StringUtils.removeStart(ruleName, LogPlusConstants.TX_PATH_PREFIX));
		
		String[] items = StringUtils.split(rule, "::");
		
		//the method and the path must be defined in the rules
		// length equal to 1, accept all methods. length equal to 2, accept specified method.
		if(items.length == 1) {
			result.setMethod("");
			result.setReqPath(items[0]);
			result.setLength(items.length+1);
		} else if(items.length == 2) {
			result.setMethod(items[0]);
			result.setReqPath(items[1]);
			result.setLength(items.length);
		} else if(items.length > 2) {
			for(int i=2; i<items.length; i++) {
				//each one must be key value
				String[] kv = StringUtils.split(items[i], "=");
				if(kv.length == 2) {
					//match
					if(kv[0].startsWith(LogPlusConstants.REQUEST_HEADER)) {
						result.setHeaderName(StringUtils.removeStart(kv[0], LogPlusConstants.REQUEST_HEADER + "."));
						result.setHeaderValue(kv[1]);
					}else if(kv[0].startsWith(LogPlusConstants.REQUST_PARAMETER)) {
						result.setParameterName(StringUtils.removeStart(kv[0], LogPlusConstants.REQUST_PARAMETER + "."));
						result.setParameterValue(kv[1]);												
					}
				}
			}
		} else {
			System.out.println("The rule under key " + ruleName + " is not in well format, please verify ...");
			return null;
		}
		return result;
	}
	
	private static class TransactionPathMappingRule implements Comparable<TransactionPathMappingRule> {
		
		private String txPathName;
		private String method;
		private String reqPath;
		private String parameterName;
		private String parameterValue;
		private String headerName;
		private String headerValue;
		private int length;
		
		public int getLength() {
			return length;
		}
		public void setLength(int length) {
			this.length = length;
		}
		public String getTxPathName() {
			return txPathName;
		}
		public void setTxPathName(String txPathName) {
			this.txPathName = txPathName;
		}
		public String getMethod() {
			return method;
		}
		public void setMethod(String method) {
			this.method = method;
		}
		public String getReqPath() {
			return reqPath;
		}
		public void setReqPath(String reqPath) {
			this.reqPath = reqPath;
		}
		public String getParameterName() {
			return parameterName;
		}
		public void setParameterName(String parameterName) {
			this.parameterName = parameterName;
		}
		public String getParameterValue() {
			return parameterValue;
		}
		public void setParameterValue(String parameterValue) {
			this.parameterValue = parameterValue;
		}
		public String getHeaderName() {
			return headerName;
		}
		public void setHeaderName(String headerName) {
			this.headerName = headerName;
		}
		public String getHeaderValue() {
			return headerValue;
		}
		public void setHeaderValue(String headerValue) {
			this.headerValue = headerValue;
		}
		@Override
		public int compareTo(TransactionPathMappingRule o) {
			
			int s1 = this.reqPath.split("/").length;
			
			int s2 = o.getReqPath().split("/").length;
			
			if(s1 == s2) {			
				return o.getReqPath().compareTo(this.reqPath);			
			}else {
				return s2 - s1;
			}
		}	
	}
}
