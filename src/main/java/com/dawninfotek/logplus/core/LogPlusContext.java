package com.dawninfotek.logplus.core;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.dawninfotek.logplus.checkpoint.CheckPointService;
import com.dawninfotek.logplus.config.Configuration;
import com.dawninfotek.logplus.config.LogPlusField;
import com.dawninfotek.logplus.event.EventService;
import com.dawninfotek.logplus.resolver.Resolver;
import com.dawninfotek.logplus.security.HashService;
import com.dawninfotek.logplus.security.MaskService;
import com.dawninfotek.logplus.util.LogPlusUtils;

/**
 * This is the Context of the Log Plus Framework, all implementation instances will be help in the instance of Context.
 * The method initialize must be called before using any of the method in this calss.
 * @author John Li
 *
 */
public class LogPlusContext {
	
	//Use System logger since the methods are called during the Logger initialized.
	
	private LogPlusContext() {
		super();
		components = new HashMap<String, Component>();
	}
	
	private static LogPlusContext instance;
	
	private Configuration configuration;
	
	private Map<String, Component> components;
	
	private Map<String, String> contextParameters = new HashMap<String, String>();
	
	private Map<String, LogPlusField> logPlusFields = new HashMap<String, LogPlusField>();
	
	private static boolean initialized = false;
	
	/**
	 * Initial the Log Plus System
	 * @param configFile
	 */
	public static void initialize(String configFile) {
		
		if(!initialized) synchronized (LogPlusContext.class) {		
		
			instance = new LogPlusContext();
			instance.configuration = Configuration.loadFromConfigFile(configFile);
			//indicate the configuration has been initialized.
			initialized = true;			
			//initial other components
			instance.components.put(LogPlusConstants.C_NAME_CP, getComponent(null,LogPlusConstants.C_NAME_CP));
			instance.components.put(LogPlusConstants.C_NAME_EVT, getComponent(null,LogPlusConstants.C_NAME_EVT));
			instance.components.put(LogPlusConstants.C_NAME_HASH, getComponent(null,LogPlusConstants.C_NAME_HASH));
			instance.components.put(LogPlusConstants.C_NAME_MASK, getComponent(null,LogPlusConstants.C_NAME_MASK));
			//init Resolvers
			for(String key:instance.configuration.getPropertyMap().keySet()) {			
				String name = null;			
				if(key.startsWith(LogPlusConstants.RESOLVER_PREFIX)) {
					try {
						name = StringUtils.removeStart(key, LogPlusConstants.RESOLVER_PREFIX);
						Component c = getComponent(LogPlusConstants.RESOLVER_PREFIX, name);
						if(c != null) {
							instance.components.put(name, c);
							
							System.out.println("Resolver:" + c + " is created under name:" + name);
							
						}
					}catch (Exception e) {
						System.out.println("Fail to init Resolver ..." + e.getMessage());
						e.printStackTrace();
					}
				}		
			
			}
			
			//Create the logPlusFields	
			
			for(String field:LogPlusUtils.getLogPlusFieldNames()) {				
				instance.logPlusFields.put(field, new LogPlusField(field));				
			}
			
			//create logPlusContextVariables
			
			LogPlusField f = null;
			
			for(String logPlusField:LogPlusUtils.getLogPlusFieldNames()) {
				
				f = instance.logPlusFields.get(logPlusField);
				
				if(f != null && f.getScope() == LogPlusField.SCOPE_CONTEXT) {
					//this is a context variable
					instance.contextParameters.put(logPlusField, LogPlusUtils.resolveFieldValue(logPlusField, null));
					System.out.println("Context Variable:" + logPlusField + " was created ...");
					
				}
				
			}		
		
		}
		
	}

	/**
	 * get configuration instance
	 * @return
	 */
	public static Configuration configuration() {
		if(!initialized) {
			initialize(Configuration.logplusConfigFile);
		}
		return instance.configuration;
	}
	
	public static Resolver resolver(String resolverName) {		
		return (Resolver) instance.components.get(resolverName);
	}

	/**
	 * get checkPoint service
	 * @return
	 */
	public static CheckPointService checkPointService() {
		return (CheckPointService) instance.components.get(LogPlusConstants.C_NAME_CP);
	}

	/**
	 * get event service
	 * @return
	 */
	public static EventService eventService() {
		return (EventService) instance.components.get(LogPlusConstants.C_NAME_EVT);
	}

	/**
	 * get hash service
	 * @return
	 */
	public static HashService hashService() {
		return (HashService) instance.components.get(LogPlusConstants.C_NAME_HASH);		
	}

	/**
	 * get mask service
	 * @return
	 */
	public static MaskService maskService() {
		return (MaskService) instance.components.get(LogPlusConstants.C_NAME_MASK);
	}

	private static Component getComponent(String prefix, String componentName) {
		
		String impl = null;	
		
		if(prefix == null) {
			//default
			impl = instance.configuration.getConfigurationValue(LogPlusConstants.C_NAME_PREFIX + componentName);
		 
		}else {
			impl = instance.configuration.getConfigurationValue(prefix + componentName);
		}
		
		Component result = null;
		
		try {
			
			System.out.println("Creating instance for " + componentName + ", using inpl class:" + impl);
			result = (Component) Class.forName(impl).newInstance();
			
		}catch (Exception e) {
			System.out.println("Fail to create instance for:" + componentName + ", impl class is:" + impl + "::" + e.getMessage());
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * Answer the context variable value under given key
	 * @param key
	 * @return
	 */
	public static final String getContextVariable(String key) {
		return instance.contextParameters.get(key);
	}

	/**
	 * Answer the context variable value under given key
	 * @param key
	 * @return
	 */
	public static final LogPlusField getLogPlusField(String fieldName) {
		return instance.logPlusFields.get(fieldName);
	}
}
