package com.dawninfotek.logx.extension.logback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Map;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.contrib.json.classic.JsonLayout;

import com.dawninfotek.logx.core.LogXConstants;
import com.dawninfotek.logx.util.LogXUtils;

public class LogXJsonLayout extends JsonLayout {
	
	public static Logger logger = LoggerFactory.getLogger(LogXJsonLayout.class);
	
	protected static final String REMOTEIP = "remoteIp";
	protected static final String HOSTNAME = "hostName";
	protected static final String SESSIONID = "sessionId";
	protected static final String LOGMETHOD = "logMethod";

	protected static final String TIMESTAMP = "timestamp";
	protected static final String LEVEL = "level";
	protected static final String LOGGER = "logger";
	protected static final String MESSAGE = "message";
	protected static final String EXCEPTION = "exception";

	protected Map toJsonMap(ILoggingEvent event) {
        Map<String, Object> map = super.toJsonMap(event);
        // remove MDC, context from JSON output
        map.remove("mdc");
        map.remove("context");
        
		String[] jsonFields = LogXUtils.getLogXCustomJsonIncludes();
		if(jsonFields != null && jsonFields.length > 0) {
			for(String field: jsonFields) {
				try {
					if(field.startsWith(LogXConstants.APPLICATION_NAME)) {
						insertMap(field, getApplicationName(event), map);
					}else if(field.startsWith(LogXConstants.SERVICE_NAME)) {
						insertMap(field, getFromMDC(LogXConstants.TRANSACTION_PATH), map);
					}else if(field.startsWith(REMOTEIP)) {
						insertMap(field, getFromMDC(REMOTEIP), map);
					}else if(field.startsWith(HOSTNAME)) {
						insertMap(field, getFromMDC(HOSTNAME), map);
					}else if(field.startsWith(LogXConstants.PROCESS_ID)) {
						insertMap(field, getFromMDC(LogXConstants.PROCESS_ID), map);
					}else if(field.startsWith(LogXConstants.UUID)) {
						insertMap(field, getFromMDC(LogXConstants.UUID), map);
					}else if(field.startsWith(SESSIONID)) {
						insertMap(field, getFromMDC(SESSIONID), map);
					}else if(field.startsWith(LogXConstants.MASK_NAME)) {
						insertMap(field, getFromMDC(LogXConstants.MASK_NAME), map);
					}else if(field.startsWith(LOGMETHOD)) {
						String method = getFromMDC(LOGMETHOD);
						if(method.isEmpty()) {
							method = getMethodFromLogger(map.get(LOGGER).toString());
						}
						insertMap(field, method, map);
					}else if(field.startsWith(TIMESTAMP)) {
						updateMap(field, TIMESTAMP, map.get(TIMESTAMP).toString(), map);
					}else if(field.startsWith(LEVEL)) {
						updateMap(field, LEVEL, map.get(LEVEL).toString(), map);
					}else if(field.startsWith(LOGGER)) {
						updateMap(field, LOGGER, map.get(LOGGER).toString(), map);
					}else if(field.startsWith(MESSAGE)) {
						updateMap(field, MESSAGE, map.get(MESSAGE).toString(), map);
					}else if(field.startsWith(EXCEPTION)) {
						updateMap(field, EXCEPTION, map.get(EXCEPTION).toString(), map);
					}else {
						logger.warn("unexpected field: " + field);
					}
				} catch (Exception ex){
					logger.error("custom json layout config error: " + ex.getMessage());
				}
			}
		}
        return map;
    }

	protected String getApplicationName(ILoggingEvent event) {
		String applicationName = MDC.get(LogXConstants.APPLICATION_NAME);
		if(applicationName != null) {
			return applicationName;
		}
		applicationName = event.getLoggerContextVO().getName();
		if(applicationName.equals("default")) {
			return "";
		}
		return applicationName;
	}

	protected String getMethod(ILoggingEvent event) {
		String[] classMethods = event.getLoggerName().split("\\.");
		return classMethods[classMethods.length - 1];
	}

	protected String getFromMDC(String value) {
		String result = MDC.get(value);
		if(result == null) {
			return "";
		}
		return result;
	}
	
	protected String getMethodFromLogger(String value) {
		if(value.isEmpty() || value == null) {
			return "";
		}
		String[] result = value.split("\\.");
		if(result == null) {
			return "";
		}
		return result[result.length-1];
	}

	protected void insertMap(String field, String value, Map<String, Object> map) {
		if(field.indexOf("[") < 0) {
			add(field, true, value, map);
		} else {
			String[] custom = field.substring(field.indexOf("[") + 1, field.indexOf("]")).split("/");
			if(custom[0].isEmpty() || custom[0].equals("Y") || custom[0].equals("T")) {
				add(custom[1], true, value, map);
			}
		}
	}
	
	protected void updateMap(String field, String key, String value, Map<String, Object> map) {
		if(field.indexOf("[") > 0) {
			String[] custom = field.substring(field.indexOf("[") + 1, field.indexOf("]")).split("/");
			if((custom[0].isEmpty() || custom[0].equals("Y") || custom[0].equals("T")) && !field.equals(custom[1])) {
				add(custom[1], true, value, map);
				map.remove(key);
			} else if(custom[0].equals("N") || custom[0].equals("F")) {
				map.remove(field);
			}
		}
	}
}
