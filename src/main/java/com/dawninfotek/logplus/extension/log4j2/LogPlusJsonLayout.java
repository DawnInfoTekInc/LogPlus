package com.dawninfotek.logplus.extension.log4j2;

import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.impl.ThrowableProxy;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;

import com.dawninfotek.logplus.config.JsonField;
import com.dawninfotek.logplus.config.JsonFieldsConstants;
import com.dawninfotek.logplus.config.LogPlusField;
import com.dawninfotek.logplus.core.LogPlusContext;
import com.dawninfotek.logplus.util.LogPlusUtils;
import com.dawninfotek.logplus.util.StringUtils;

// currently support log4j2.3

@Plugin(name = "LogPlusJsonLayout", category = "Core", elementType = "layout", printObject = true)
public class LogPlusJsonLayout extends AbstractStringLayout {
	
	private static final long serialVersionUID = 1L;
    
    protected LogPlusJsonLayout(Configuration config, Charset aCharset) {
        super(aCharset);
    }

    @PluginFactory
    public static LogPlusJsonLayout createLayout(@PluginConfiguration final Configuration config,
                                                @PluginAttribute(value = "charset", defaultString = "US-ASCII") final Charset charset) {
        return new LogPlusJsonLayout(config, charset);
    }
    
    @Override
    public String toSerializable(LogEvent event) {

    	Map<String, String> jsonObject = new LinkedHashMap<String, String>();
     // custom all fields
 		for(JsonField field: LogPlusContext.configuration().getJsonFields()) {
         	try {
         		String searchName = field.getName(); 
         		String format = field.getFormat();
         		String value = "";

                // Log default Information
         		if(searchName.equals(JsonFieldsConstants.TIMESTAMP)) {
        			value = JsonField.getTimestampValue(event.getTimeMillis(), format);
        		}else if(searchName.equals(JsonFieldsConstants.LEVEL)) {
        			value = event.getLevel().name();
        		}else if(searchName.equals(JsonFieldsConstants.THREAD)) {
        			value = event.getThreadName();
        		}else if(searchName.equals(JsonFieldsConstants.LOGGER)) {
        			final StackTraceElement source = event.getSource();
        			value = source.getClassName() + " " + source.getFileName() + ":" + source.getLineNumber();
        		}else if(searchName.equals(JsonFieldsConstants.MESSAGE)) {
        			value = getMessage(event);
        		}else if(searchName.equals(JsonFieldsConstants.EXCEPTION)) {
        			value = getException(event);
        		}else if(searchName.equals(JsonFieldsConstants.METHOD)) {
    				value = event.getSource().getMethodName();
    			}
         		// log custom information
        		else {
        			LogPlusField logPlusField = LogPlusContext.getLogPlusField(field.getName());
        			if(logPlusField != null && logPlusField.getScope() == LogPlusField.SCOPE_LINE) {					
    					//this is a log line scope field, generate value here					
    					if(LogPlusUtils.resolveFieldValueRequired(logPlusField, event.getLevel().name())) {
    						//need to resolve the value for this log level for this field
    						value = LogPlusUtils.resolveFieldValue(logPlusField, event.getSource().getClassName(), getMessage(event));
    						if(StringUtils.isEmpty(value)) {
    							//try resolve from exception body
    							value = LogPlusUtils.resolveFieldValue(logPlusField, event.getSource().getClassName(), getException(event));
    						}
    					}else {
    						value = null;
    					}
    				}
        			if (value.isEmpty()) {
            			value = JsonField.getFromMDC(searchName);
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
            	if(field.isDisplay() || !value.isEmpty()) {
        			jsonObject.put(field.getLabel(), value);
            	}
        	}catch (Exception e) {
        		System.out.println(e.getMessage() + JsonField.stackTraceToString(e));
        	}
        }

 		return JsonField.convertMapToJsonString(jsonObject);
    }
    
    protected String getMessage(LogEvent event) {
    	// get formatted Message, ignore object as message supported by JRE1.8
        return JsonField.replaceAllNewline(event.getMessage().getFormattedMessage());
    }
    
    protected String getException(LogEvent event) {
    	// Exceptions
        if (event.getThrownProxy() != null) {
            final ThrowableProxy thrownProxy = event.getThrownProxy();
            final Throwable throwable = thrownProxy.getThrowable();

            String exception = "";
            final String exceptionsClass = throwable.getClass().getCanonicalName();
            final String exceptionsMessage = throwable.getMessage();
            final String stackTrace = thrownProxy.getExtendedStackTraceAsString();
            
            if (exceptionsClass != null) {
                exception += exceptionsClass;
            }
            if (exceptionsMessage != null) {
                exception += exceptionsMessage;
            }
            if (stackTrace != null) {
                exception += stackTrace;
            }
            if(!exception.isEmpty()) {
            	return JsonField.replaceAllNewline(exception);
            }
        }
        return "";
    }
}
