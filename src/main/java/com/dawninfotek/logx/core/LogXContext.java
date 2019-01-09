package com.dawninfotek.logx.core;

import java.util.HashMap;
import java.util.Map;

import com.dawninfotek.logx.checkpoint.CheckPointService;
import com.dawninfotek.logx.config.Configuration;
import com.dawninfotek.logx.event.EventService;
import com.dawninfotek.logx.resolver.Resolver;
import com.dawninfotek.logx.security.HashService;
import com.dawninfotek.logx.security.MaskService;
import com.dawninfotek.logx.util.LogXUtils;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the Context of the Log Plus Framework, all implementation instances will be help in the instance of Context.
 * The method initialize must be called before using any of the method in this calss.
 * @author John Li
 *
 */
public class LogXContext {
	
	public static Logger logger = LoggerFactory.getLogger(LogXContext.class);
	
	private LogXContext() {
		super();
		components = new HashMap<String, Component>();
	}
	
	private static LogXContext instance;
	
	private Configuration configuration;
	
	private Map<String, Component> components;
	
	private Map<String, String> contextParameters = new HashMap<String, String>();
	
	private static boolean initialized = false;
	
	/**
	 * Initial the Log Plus System
	 * @param configFile
	 */
	public static void initialize(String configFile) {
		
		if(!initialized) synchronized (LogXContext.class) {		
		
			instance = new LogXContext();
			instance.configuration = Configuration.loadFromConfigFile(configFile);
			//indicate the configuration has been initialized.
			initialized = true;			
			//initial other components
			instance.components.put(LogXConstants.C_NAME_CP, getComponent(null,LogXConstants.C_NAME_CP));
			instance.components.put(LogXConstants.C_NAME_EVT, getComponent(null,LogXConstants.C_NAME_EVT));
			instance.components.put(LogXConstants.C_NAME_HASH, getComponent(null,LogXConstants.C_NAME_HASH));
			instance.components.put(LogXConstants.C_NAME_MASK, getComponent(null,LogXConstants.C_NAME_MASK));
			//init Resolvers
			for(String key:instance.configuration.getPropertyMap().keySet()) {			
				String name = null;			
				if(key.startsWith(LogXConstants.RESOLVER_PREFIX)) {
					try {
						name = StringUtils.removeStart(key, LogXConstants.RESOLVER_PREFIX);
						Component c = getComponent(LogXConstants.RESOLVER_PREFIX, name);
						if(c != null) {
							instance.components.put(name, c);
							if(logger.isDebugEnabled()) {
								logger.debug("Resolver:" + c + " is created under name:" + name);
							}
						}
					}catch (Exception e) {
						logger.error("Fail to init Resolver ...", e);
					}
				}		
			
			}
			
			//create LogXContextVariables
			String scope = null;
			
			for(String logXfield:LogXUtils.getLogXFieldNames()) {
				
				scope = LogXUtils.getLogProperty(logXfield + ".scope", null);
				
				if(scope != null && scope.equals("CONTEXT")) {
					//this is a context variable
					instance.contextParameters.put(logXfield, LogXUtils.resolveFieldValue(logXfield, null));
					System.out.println("Context Variable:" + logXfield + " was created ...");
					
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
			initialize(Configuration.logxConfigFile);
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
		return (CheckPointService) instance.components.get(LogXConstants.C_NAME_CP);
	}

	/**
	 * get event service
	 * @return
	 */
	public static EventService eventService() {
		return (EventService) instance.components.get(LogXConstants.C_NAME_EVT);
	}

	/**
	 * get hash service
	 * @return
	 */
	public static HashService hashService() {
		return (HashService) instance.components.get(LogXConstants.C_NAME_HASH);		
	}

	/**
	 * get mask service
	 * @return
	 */
	public static MaskService maskService() {
		return (MaskService) instance.components.get(LogXConstants.C_NAME_MASK);
	}

	private static Component getComponent(String prefix, String componentName) {
		
		String impl = null;	
		
		if(prefix == null) {
			//default
			impl = instance.configuration.getConfigurationValue(LogXConstants.C_NAME_PREFIX + componentName);
		 
		}else {
			impl = instance.configuration.getConfigurationValue(prefix + componentName);
		}
		
		Component result = null;
		
		try {
			
			logger.info("Creating instance for " + componentName + ", using inpl class:" + impl);
			result = (Component) Class.forName(impl).newInstance();
			
		}catch (Exception e) {
			logger.error("Fail to create instance for:" + componentName + ", impl class is:" + impl, e);
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

}
