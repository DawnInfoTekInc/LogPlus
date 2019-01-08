package com.dawninfotek.logx.extension.logback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import ch.qos.logback.classic.pattern.ThrowableHandlingConverter;
import ch.qos.logback.classic.pattern.ThrowableProxyConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.contrib.json.classic.JsonLayout;

import com.dawninfotek.logx.core.LogXConstants;
import com.dawninfotek.logx.core.LogXContext;
import com.dawninfotek.logx.config.JsonFields;
import com.dawninfotek.logx.config.JsonFieldsConstants;

public class LogXJsonLayout extends JsonLayout {
	
	public static Logger logger = LoggerFactory.getLogger(LogXJsonLayout.class);
	
	private ThrowableHandlingConverter throwableProxyConverter;
	
	public LogXJsonLayout() {
	    this.includeMessage = false;
        this.includeLevel = false;
        this.includeThreadName = false;
        this.includeMDC = false;
        this.includeLoggerName = false;
        this.includeFormattedMessage = false;
        this.includeException = false;
        this.includeContextName = false;
        this.throwableProxyConverter = new ThrowableProxyConverter();
	}

    @Override
    public void start() {
        this.throwableProxyConverter.start();
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
        this.throwableProxyConverter.stop();
    }

	protected void addCustomDataToJsonMap(Map<String, Object> map, ILoggingEvent event) {
        // custom all fields
		for(JsonFields field: LogXContext.configuration().getJsonFields()) {
        	try {
        		String searchName = field.getName();
        		String key = field.getDisplayName();
        		String format = field.getFormat();
        		String value = "";
        		
                // Log default Information
        		if(searchName.equals(JsonFieldsConstants.TIMESTAMP)) {
        			value = JsonFields.getTimestampValue(event.getTimeStamp(), format);
        		}else if(searchName.equals(JsonFieldsConstants.LEVEL)) {
        			value = String.valueOf(event.getLevel());
        		}else if(searchName.equals(JsonFieldsConstants.THREAD)) {
        			value = event.getThreadName();
        		}else if(searchName.equals(JsonFieldsConstants.LOGGER)) {
        			value = event.getLoggerName();
        		}else if(searchName.equals(JsonFieldsConstants.MESSAGE)) {
        			value = event.getFormattedMessage();
        		}else if(searchName.equals(JsonFieldsConstants.EXCEPTION)) {
        			value = getThrowableException(event);
        		}else if(searchName.equals(JsonFieldsConstants.METHOD)) {
    				// get log method from logger
    				value = getMethodFromLogger(event);
    			}
        		// log custom information
        		else {
            		value = JsonFields.getFromMDC(searchName);
            		if(value.isEmpty()) {
            			// get applicationName from context
            			if(searchName.equals(LogXConstants.APPLICATION_NAME)) {
            				value = getApplicationName(event);
            			}
            		}
        		}
        		// shrink value to format size
        		if(!format.isEmpty() && format.startsWith("X")) {
        			int charNumber = Integer.parseInt(format.substring(1));
        			if(charNumber < value.length()) {
            			value = value.substring(0, charNumber);
        			}
        		}
        		// display if mandatory or value exist
            	if(field.getDisplay() || !value.isEmpty()) { 
        			add(key, true, value, map);
            	}
        	}catch (Exception e) {
        		logger.error(e.getMessage());
        	}
        }
    }

	protected String getApplicationName(ILoggingEvent event) {
		String applicationName = event.getLoggerContextVO().getName();
		if(applicationName.equals("default")) {
			return "";
		}
		return applicationName;
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
	
	protected String getThrowableException(ILoggingEvent event) {
		IThrowableProxy throwableProxy = event.getThrowableProxy();
        if (throwableProxy != null) {
            String ex = this.throwableProxyConverter.convert(event);
            if (ex != null && !ex.equals("")) {
                return ex;
            }
        }
        return "";
	}
}
