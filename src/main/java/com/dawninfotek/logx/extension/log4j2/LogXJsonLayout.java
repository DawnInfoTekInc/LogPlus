package com.dawninfotek.logx.extension.log4j2;

import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.impl.ThrowableProxy;

import com.dawninfotek.logx.config.JsonFieldsConstants;
import com.dawninfotek.logx.config.JsonField;
import com.dawninfotek.logx.core.LogXContext;


@Plugin(name = "LogXJsonLayout", category = Node.CATEGORY, elementType = Layout.ELEMENT_TYPE)
public class LogXJsonLayout extends AbstractStringLayout {
	
	public static Logger logger = LoggerFactory.getLogger(LogXJsonLayout.class);
    
    protected LogXJsonLayout(Configuration config, Charset aCharset, Serializer headerSerializer, Serializer footerSerializer) {
        super(config, aCharset, headerSerializer, footerSerializer);
    }

    @PluginFactory
    public static LogXJsonLayout createLayout(@PluginConfiguration final Configuration config,
                                                @PluginAttribute(value = "charset", defaultString = "US-ASCII") final Charset charset) {
        return new LogXJsonLayout(config, charset, null, null);
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
        			value = event.getThreadName() + " : " + event.getThreadId();
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
        			value = JsonField.getFromMDC(searchName);
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
            final String stackTrace = thrownProxy.getExtendedStackTraceAsString("");
            
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
