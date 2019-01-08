package com.dawninfotek.logx.extension.log4j2;

import java.nio.charset.Charset;

import java.util.Date;
import java.text.SimpleDateFormat;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.impl.ThrowableProxy;

import com.google.gson.JsonObject;
import com.dawninfotek.logx.config.JsonField;
import com.dawninfotek.logx.core.LogXContext;
import com.google.gson.Gson;


@Plugin(name = "LogXJsonLayout", category = Node.CATEGORY, elementType = Layout.ELEMENT_TYPE)
public class LogXJsonLayout extends AbstractStringLayout {
	
	public static Logger logger = LoggerFactory.getLogger(LogXJsonLayout.class);
	private final Gson gson = JsonUtils.getGson();
	
	public String DefaultTimestampFormat = "yyyy-MM-dd HH:mm:ss.SSS zzz";
    
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

        // Log Information
        jsonObject.addProperty("datetime", TimestampToString(event.getTimeMillis()));
        jsonObject.addProperty("level", event.getLevel().name());
        jsonObject.addProperty("thread", event.getThreadName() + " : " + event.getThreadId());

        final StackTraceElement source = event.getSource();
        String logger = source.getClassName() + " " + source.getFileName() + ":" + source.getLineNumber();
        jsonObject.addProperty("method", source.getMethodName());
        jsonObject.addProperty("logger", logger);

        // Message
        CustomMessage customMessage = JsonUtils.generateCustomMessage(event.getMessage().getFormattedMessage());
        if (customMessage != null) {
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
            jsonObject.addProperty("message", event.getMessage().getFormattedMessage());
        }

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
            	jsonObject.addProperty("exception", exception);
            }
        }

        return gson.toJson(jsonObject).concat("\r\n");
    }
    
    protected JsonObject CustomFields(JsonObject jsonObject) {
    	for(JsonField field: LogXContext.configuration().getJsonFields()) {
        	try {
        		String searchName = field.getName();
        		String key = field.getDisplayName();
        		String value = getFromMDC(searchName);
        		if(value.isEmpty()) {
        			// get applicationName from context

        		}
        		// display if mandatory (no [], or [Y or T]) or value exist
            	if(field.getDisplay() || !value.isEmpty()) { 
            		jsonObject.addProperty(key, value);
            	}
        	}catch (Exception e) {
        		logger.error(e.getMessage());
        	}
        }
    	return jsonObject;
    }
    
    protected String getFromMDC(String value) {
		String result = MDC.get(value);
		if(result == null) {
			return "";
		}
		return result;
	}
    
    protected String TimestampToString(long millseconds) {
		SimpleDateFormat formatter = new SimpleDateFormat(DefaultTimestampFormat);
        return formatter.format(new Date(millseconds)).toString();
	}
	
}
