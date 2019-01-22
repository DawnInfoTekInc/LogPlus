package com.dawninfotek.logx.extension.log4j2;

import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.impl.ThrowableProxy;

import com.dawninfotek.logx.config.JsonFieldsConstants;
import com.dawninfotek.logx.config.LogXField;
import com.dawninfotek.logx.config.JsonField;
import com.dawninfotek.logx.core.LogXContext;
import com.dawninfotek.logx.util.LogXUtils;


@Plugin(name = "LogXJsonLayout", category = "Core", elementType = "layout", printObject = true)
public class LogXJsonLayout extends AbstractStringLayout {
	
	private static final long serialVersionUID = 1L;
	public static Logger logger = LoggerFactory.getLogger(LogXJsonLayout.class);
    
    protected LogXJsonLayout(Configuration config, Charset aCharset) {
        super(aCharset);
    }

    @PluginFactory
    public static LogXJsonLayout createLayout(@PluginConfiguration final Configuration config,
                                                @PluginAttribute(value = "charset", defaultString = "US-ASCII") final Charset charset) {
        return new LogXJsonLayout(config, charset);
    }
    
    @Override
    public String toSerializable(LogEvent event) {

    	Map<String, String> jsonObject = new LinkedHashMap<String, String>();
     // custom all fields
 		for(JsonField field: LogXContext.configuration().getJsonFields()) {
         	try {
         		String searchName = field.getName();
         		String key = field.getLable();
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
        			LogXField logXfield = LogXContext.getLogXField(field.getName());
        			if(logXfield != null && logXfield.getScope() == LogXField.SCOPE_LINE) {					
    					//this is a log line scope field, generate value here					
    					if(LogXUtils.resolveFieldValueRequired(logXfield, event.getLevel().name())) {
    						//need to resolve the value for this log level for this field
    						value = LogXUtils.resolveFieldValue(logXfield, event.getSource().getClassName(), getMessage(event));
    						if(StringUtils.isEmpty(value)) {
    							//try resolve from exception body
    							value = LogXUtils.resolveFieldValue(logXfield, event.getSource().getClassName(), getException(event));
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
            	if(field.getDisplay() || !value.isEmpty()) {
        			jsonObject.put(key, value);
            	}
        	}catch (Exception e) {
        		logger.error(e.getMessage());
        	}
        }

 		return JsonField.convertMapToJsonString(jsonObject);
    }
    
    protected String getMessage(LogEvent event) {
    	// get formatted Message, ignore object as message supported by JRE1.8
        return event.getMessage().getFormattedMessage();
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
            	return exception;
            }
        }
        return "";
    }
}
