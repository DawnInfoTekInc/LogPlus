package com.dawninfotek.logplus.extension.logback;

import java.util.LinkedHashMap;
import java.util.Map;

import com.dawninfotek.logplus.config.JsonField;
import com.dawninfotek.logplus.config.JsonFieldsConstants;
import com.dawninfotek.logplus.config.LogPlusField;
import com.dawninfotek.logplus.core.LogPlusConstants;
import com.dawninfotek.logplus.core.LogPlusContext;
import com.dawninfotek.logplus.util.LogPlusUtils;
import com.dawninfotek.logplus.util.StringUtils;
import com.google.gson.JsonObject;

import ch.qos.logback.classic.pattern.ThrowableHandlingConverter;
import ch.qos.logback.classic.pattern.ThrowableProxyConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.LayoutBase;

public class LogPlusJsonLayout extends LayoutBase<ILoggingEvent> {
	
	private ThrowableHandlingConverter throwableProxyConverter;
	
	public LogPlusJsonLayout() {
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

    @Override
	public String doLayout(ILoggingEvent event) {
    	
		if(Boolean.valueOf(LogPlusUtils.getLogProperty("logplus.use.jsonobject", "false"))) {
    		
        	JsonObject json = new JsonObject();
            for(JsonField field: LogPlusContext.configuration().getJsonFields()) {
            	
            	String value = getFieldValue(field, event);  
            	// display if mandatory or value exist
                if(field.isDisplay() || !value.isEmpty()) {
            		json.addProperty(field.getLabel(), value);
                }
     
            }
            return json.toString() + "\n";    	
    		
    	}else {
    		// custom all fields
    		Map<String, String> map = new LinkedHashMap<String, String>();
    		for(JsonField field: LogPlusContext.configuration().getJsonFields()) {        	

    			String value = getFieldValue(field, event);
    			// display if mandatory or value exist
    			if(field.isDisplay() || !value.isEmpty()) {
    				map.put(field.getLabel(), value);
    			}
  
    		}
    		return JsonField.convertMapToJsonString(map);
    	}
    }
    
    protected String getFieldValue(JsonField field, ILoggingEvent event) {
 
   		String value = "";
       	try {
    		String searchName = field.getName();
  
    		String format = field.getFormat(); 
    		
            // Log default Information
    		if(searchName.equals(JsonFieldsConstants.TIMESTAMP)) {
    			value = JsonField.getTimestampValue(event.getTimeStamp(), format);
    		}else if(searchName.equals(JsonFieldsConstants.LEVEL)) {
    			value = String.valueOf(event.getLevel());
    		}else if(searchName.equals(JsonFieldsConstants.THREAD)) {
    			value = event.getThreadName();
    		}else if(searchName.equals(JsonFieldsConstants.LOGGER)) {
    			value = event.getLoggerName();
    		}else if(searchName.equals(JsonFieldsConstants.MESSAGE)) {
    			value = JsonField.replaceAllNewline(event.getFormattedMessage());
    		}else if(searchName.equals(JsonFieldsConstants.EXCEPTION)) {
    			value = getThrowableException(event);
    		}else if(searchName.equals(JsonFieldsConstants.METHOD)) {
				// get log method from logger
				value = getMethodFromLogger(event);
			}else if(searchName.equals(JsonFieldsConstants.LINE)) {
				value = getLineNumberFromLogger(event);
			}else {
    			// log custom information
    			LogPlusField logPlusField = LogPlusContext.getLogPlusField(field.getName());
    			if(logPlusField != null && logPlusField.getScope() == LogPlusField.SCOPE_LINE) {					
					//this is a log line scope field, generate value here					
					if(LogPlusUtils.resolveFieldValueRequired(logPlusField, String.valueOf(event.getLevel()))) {
						//need to resolve the value for this log level for this field
						value = LogPlusUtils.resolveFieldValue(logPlusField, event.getLoggerName(), event.getFormattedMessage());
						if(StringUtils.isEmpty(value)) {
							//try resolve from exception body
							value = LogPlusUtils.resolveFieldValue(logPlusField, event.getLoggerName(), getThrowableException(event));
						}
					}else {
						value = null;
					}
					
				}
    			if (value == null || value.isEmpty()){
            		value = JsonField.getFromMDC(searchName);
            		if(value == null || value.isEmpty()) {
            			// get applicationName from context
            			if(searchName.equals(LogPlusConstants.APPLICATION_NAME)) {
            				value = getApplicationName(event);
            			}
            		}
				}
    		}
    		
    		if(value == null) {
    			value = "";
    		}
    		// shrink value to format size
    		if(format != null && format.startsWith("X")) {
    			int charNumber = Integer.parseInt(format.substring(1));
    			if(charNumber < value.length()) {
        			value = value.substring(0, charNumber);
    			}
    		}
    		
      	}catch (Exception e) {
    		System.out.println(e.getMessage() + JsonField.stackTraceToString(e));
    	}
    		
    	return value;
    	
    }

	protected String getApplicationName(ILoggingEvent event) {
		String applicationName = event.getLoggerContextVO().getName();
		if(applicationName.equals("default")) {
			return "";
		}
		return applicationName;
	}
	
	protected String getMethodFromLogger(ILoggingEvent event) {		
		
		StackTraceElement[] sts = event.getCallerData();
		
		if(sts != null && sts.length > 0) {
			
			return sts[0].getMethodName();			
			
		}else {
		
			return "";
		
		}
		/*
		String value = event.getLoggerName();
		if(value.isEmpty() || value == null) {
			return "";
		}
		String[] result = event.getLoggerName().split("\\.");
		if(result == null) {
			return "";
		}
		return result[result.length-1];
		*/
	}
	
	protected String getThrowableException(ILoggingEvent event) {
		IThrowableProxy throwableProxy = event.getThrowableProxy();
        if (throwableProxy != null) {
            String ex = this.throwableProxyConverter.convert(event);
            if (ex != null && !ex.equals("")) {
                return JsonField.replaceAllNewline(ex);
            }
        }
        return "";
	}
	
	protected String getLineNumberFromLogger(ILoggingEvent event) {
		
		StackTraceElement[] sts = event.getCallerData();
		
		if(sts != null && sts.length > 0) {
			
			return String.valueOf(sts[0].getLineNumber());			
			
		}else {
		
			return "";
		
		}	
		
	}
}
