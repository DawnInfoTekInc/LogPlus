package com.dawninfotek.logx.extension.log4j2;

import java.nio.charset.Charset;

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

import com.google.gson.JsonObject;
import com.dawninfotek.logx.config.JsonFieldsConstants;
import com.dawninfotek.logx.config.JsonField;
import com.dawninfotek.logx.core.LogXContext;
import com.google.gson.Gson;


@Plugin(name = "LogXJsonLayout", category = Node.CATEGORY, elementType = Layout.ELEMENT_TYPE)
public class LogXJsonLayout extends AbstractStringLayout {
	
	public static Logger logger = LoggerFactory.getLogger(LogXJsonLayout.class);
	private final Gson gson = JsonUtils.getGson();
    
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

        JsonObject jsonObject = new JsonObject();
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
        			jsonObject.addProperty(key, value);
            	}
        	}catch (Exception e) {
        		logger.error(e.getMessage());
        	}
        }

        return gson.toJson(jsonObject).concat("\r\n");
    }
    
    protected String getMessage(LogEvent event) {
    	// Message
    	String value = "";
        CustomMessage customMessage = JsonUtils.generateCustomMessage(event.getMessage().getFormattedMessage());
        if (customMessage != null) {
            //value = customMessage.getMessage();
            //jsonObject.addProperty("message", customMessage.getMessage());
            // enable message key value object for JRE1.8 or later
//            customMessage.getNewField().forEach((k, v) -> {
//                if (v instanceof String) {
//                    jsonObject.addProperty(k, (String) v);
//                } else if (v instanceof Number) {
//                    jsonObject.addProperty(k, (Number) v);
//                } else if (v instanceof Character) {
//                    jsonObject.addProperty(k, (Character) v);
//                } else if (v instanceof Boolean) {
//                    jsonObject.addProperty(k, (Boolean) v);
//                } else {
//                    jsonObject.addProperty(k, gson.toJson(v));
//                }
//            });
        } else {
            value = event.getMessage().getFormattedMessage();
        }
        return value;
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
