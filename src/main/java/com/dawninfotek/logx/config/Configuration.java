package com.dawninfotek.logx.config;

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

import com.dawninfotek.logx.core.Component;
import com.dawninfotek.logx.core.LogXConstants;
import com.dawninfotek.logx.util.AntPathMatcher;
import com.dawninfotek.logx.util.LogXUtils;
import com.dawninfotek.logx.config.JsonField;

public class Configuration implements Component {
	
	//final static Logger logger = LoggerFactory.getLogger(Configuration.class);
	
	public static final String LOGX_CONFIG_FILE_NAME = "logx-default.properties";
	
	public static final String logxConfigFile = "CLASS_PATH=logx.properties";
	
	private Map<String, String> propertyMap;
	
	private AntPathMatcher pathMatcher = new AntPathMatcher();
	
	public Map<String, String> getPropertyMap() {
		return propertyMap;
	}
	
	private List<TransactionPathMappingRule> txRules;
	
	private List<JsonField> fieldsMapping;
	
	public static Configuration loadFromConfigFile(String configFile) {
		
		Properties override = new Properties();	
		
		if (configFile == null) {
			//before logX is initialized, prevents to use logger 
			//logger.warn("logx configuration is not provided, use default only ...");
			System.out.println("logx configuration is not provided, use default only ...");
		} else {

			InputStream propFile = null;
			try {
				//logger.info("property file: " + configFile);
				System.out.println("property file: " + configFile);
				
				if (configFile.contains("CLASS_PATH")) {
					String[] ns = configFile.split("=");
					if (ns.length <= 1) {
						ns = configFile.split(":");
					}
					configFile = ns[1];
					propFile = LogXUtils.class.getClassLoader().getResourceAsStream(configFile);
				} else if (configFile.contains("FILE")) {
					String[] ns = configFile.split("=");
					if (ns.length <= 1) {
						ns = configFile.split(":");
					}
					
					propFile = new FileInputStream(ns[1]);
				}
				
				override.load(propFile);
				
			} catch (FileNotFoundException fnfe) {
				//only say warning here
				//logger.warn("LogX properties not found, ignored:" + fnfe.getMessage());
				System.out.println("LogX properties not found, ignored:" + fnfe.getMessage());
			} catch (Exception e) {
				//logger.error("fail to load LogX properties.", e);
				System.out.println("Fail to load LogX properties.");
				e.printStackTrace();
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
	
	private Configuration(Map<String, String> propertyMap, List<TransactionPathMappingRule> txRules, List<JsonField> fieldsMapping) {
		super();
		this.propertyMap = propertyMap;
		this.txRules = txRules;		
		this.fieldsMapping = fieldsMapping;
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
	 * By default, logx configuration will load default configuration in the file CLASS-PATH:logx.properties. if the given 
	 * overrideConfig is present, the default values will be overridden.
	 * @param overrideConfig
	 */
	private synchronized static Configuration init(Properties overrideConfig) {
		
		InputStream propFile = null;
		
		try {
			//load the default properties
			Properties defaultConfig = new Properties();
			propFile = LogXUtils.class.getClassLoader().getResourceAsStream(LOGX_CONFIG_FILE_NAME);
			defaultConfig.load(propFile);		
				
			//merge the override items
			if(overrideConfig != null && !overrideConfig.isEmpty()) {
				
				for(Object key:overrideConfig.keySet()) {
					//Merge and override
					defaultConfig.put(key, overrideConfig.get(key));
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
				
				if(name.startsWith(LogXConstants.TX_PATH_PREFIX)) {
					//the line is for defining the rule
					rule = createRule(name, pm.get(name));
					if(rule != null) {
						rules.add(rule);
					}
				}
				if(name.equals(LogXConstants.JSON_LAYOUT_DEFAULT) || name.equals(LogXConstants.JSON_LAYOUT_CUSTOM)) {
					jsonFields.addAll(Arrays.asList(pm.get(name).split(",")));
				}
			}
			
			if(!rules.isEmpty()) {
				//sort the list
				Collections.sort(rules);
			}
			
			System.out.println("Logx system was inittialized successefully, " + pm.size() + " of properties were loaded, " + rules.size() + " of TransactionPath Mapping Rules were creared ...");
			
			List<JsonField> fieldsMapping = new ArrayList<JsonField>();
			
			JsonField fieldObj = null;
			
			if(jsonFields != null && jsonFields.size() > 0) {
				for(String field: jsonFields) {
					fieldObj = JsonField.fromString(field);
					if(fieldObj != null) {
						fieldsMapping.add(fieldObj);
					}
				}
			}
			
			//Sort the List
			sortJsonFields(fieldsMapping);			
			//finally, create the Configuration instance and return to caller
			return new Configuration(pm, rules, fieldsMapping);			
			
		}catch (Exception e) {
			System.out.println("LogX system failed to load config from files");
			e.printStackTrace();
			throw new RuntimeException("LogX system failed to load config from files", e);
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
		return propertyMap.get(key);
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
		
		if(Boolean.valueOf(LogXUtils.getLogProperty(TX_PATH_PATTERN_MATCHING, "false"))){
			//use url pattern
			return pathMatcher.match(urlPattern, path);
		}else {
			//use start-with
			return path.startsWith(urlPattern);
		}
	}
	
	private static TransactionPathMappingRule createRule(String ruleName, String rule) {
		
		TransactionPathMappingRule result = new TransactionPathMappingRule();
		
		result.setTxPathName(StringUtils.removeStart(ruleName, LogXConstants.TX_PATH_PREFIX));
		
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
					if(kv[0].startsWith(LogXConstants.REQUEST_HEADER)) {
						result.setHeaderName(StringUtils.removeStart(kv[0], LogXConstants.REQUEST_HEADER + "."));
						result.setHeaderValue(kv[1]);
					}else if(kv[0].startsWith(LogXConstants.REQUST_PARAMETER)) {
						result.setParameterName(StringUtils.removeStart(kv[0], LogXConstants.REQUST_PARAMETER + "."));
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
