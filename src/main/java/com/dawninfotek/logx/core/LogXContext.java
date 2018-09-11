package com.dawninfotek.logx.core;

import java.util.HashMap;
import java.util.Map;

import com.dawninfotek.logx.checkpoint.CheckPointService;
import com.dawninfotek.logx.config.Configuration;
import com.dawninfotek.logx.event.EventService;
import com.dawninfotek.logx.security.HashService;
import com.dawninfotek.logx.security.MaskService;

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
	
	/**
	 * Initial the Log Plus System
	 * @param configFile
	 */
	public static void initialize(String configFile) {
		
		instance = new LogXContext();
		instance.configuration = Configuration.loadFromConfigFile(configFile);
		//init all components
		instance.components.put(LogXConstants.C_NAME_CP, getComponent(LogXConstants.C_NAME_CP));
		instance.components.put(LogXConstants.C_NAME_EVT, getComponent(LogXConstants.C_NAME_EVT));
		instance.components.put(LogXConstants.C_NAME_HASH, getComponent(LogXConstants.C_NAME_HASH));
		instance.components.put(LogXConstants.C_NAME_MASK, getComponent(LogXConstants.C_NAME_MASK));		
	}
	
	public static Configuration configuration() {
		return instance.configuration;
	}
	
	public static CheckPointService checkPointService() {
		return (CheckPointService) instance.components.get(LogXConstants.C_NAME_CP);
	}
	
	public static EventService eventService() {
		return (EventService) instance.components.get(LogXConstants.C_NAME_EVT);
	}
	
	public static HashService hashService() {
		return (HashService) instance.components.get(LogXConstants.C_NAME_HASH);		
	}
	
	public static MaskService maskService() {
		return (MaskService) instance.components.get(LogXConstants.C_NAME_MASK);
	}
	
	private static Component getComponent(String componentName) {
		
		String impl = instance.configuration.getConfigurationValue(LogXConstants.C_NAME_PREFIX + componentName); 		
		
		Component result = null;
		
		try {
			
			logger.info("Creating instance for " + componentName + ", using inpl class:" + impl);
			result = (Component) Class.forName(impl).newInstance();
			
		}catch (Exception e) {
			logger.error("Fail to create instance for:" + componentName + ", impl class is:" + impl, e);
		}
		return result;
	}

}
