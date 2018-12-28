package com.dawninfotek.logx.extension.logback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Map;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.contrib.json.classic.JsonLayout;

import com.dawninfotek.logx.core.LogXConstants;
import com.dawninfotek.logx.core.LogXContext;
import com.dawninfotek.logx.config.JsonFields;

public class LogXJsonLayout extends JsonLayout {
	
	public static Logger logger = LoggerFactory.getLogger(LogXJsonLayout.class);
	
	protected static final String LOGMETHOD = "logMethod";

	protected Map toJsonMap(ILoggingEvent event) {
        Map<String, Object> map = super.toJsonMap(event);
        // remove MDC, context from JSON output
        map.remove("mdc");
        map.remove("context");
        
        for(JsonFields field: LogXContext.configuration().getJsonFields()) {
    		String searchName = field.getName();
    		String key = field.getDisplayName();
    		String value = getFromMDC(searchName);
    		if(value.isEmpty()) {
    			// get applicationName from context
    			if(searchName.equals(LogXConstants.APPLICATION_NAME)) {
    				value = getApplicationName(event);
    			}else if(searchName.equals(LOGMETHOD)) {
    				// get log method from logger
    				value = getMethodFromLogger(event);
    			}else if(map.get(searchName) != null && !key.equals(searchName)) {
    				// change default field name
    				value = map.get(searchName).toString();
    				map.remove(searchName);
    			}
    		}
    		// display if mandatory (no [], or [Y or T]) or value exist
        	if(field.getDisplay() || !value.isEmpty()) { 
    			add(key, true, value, map);
        	}
        }
        return map;
    }

	protected String getApplicationName(ILoggingEvent event) {
		String applicationName = event.getLoggerContextVO().getName();
		if(applicationName.equals("default")) {
			return "";
		}
		return applicationName;
	}

	protected String getFromMDC(String value) {
		String result = MDC.get(value);
		if(result == null) {
			return "";
		}
		return result;
	}
	
	protected String getMethodFromLogger(ILoggingEvent event) {
		String value = event.getLoggerName();
		if(value.isEmpty() || value == null) {
			return "";
		}
		String[] result = event.getLoggerName().split("\\.");
		if(result == null) {
			return "";
		}
		return result[result.length-1];
	}

}
