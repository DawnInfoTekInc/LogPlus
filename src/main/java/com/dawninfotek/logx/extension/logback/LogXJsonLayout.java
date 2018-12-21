package com.dawninfotek.logx.extension.logback;

import org.slf4j.MDC;
import java.util.Map;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.contrib.json.classic.JsonLayout;

public class LogXJsonLayout extends JsonLayout {

	protected Map toJsonMap(ILoggingEvent event) {

         Map<String, Object> map = super.toJsonMap(event);
         // test
         map.remove("mdc");
         map.remove("context");
         add("appName", true, getApplicationName(event), map);
         add("serviceName", true, MDC.get("transactionPath"), map);
         add("hostname", true, MDC.get("hostName"), map);
         add("processId", true, MDC.get("processId"), map);
         add("corrId", true, MDC.get("uuid"), map);
         add("sessionId", true, MDC.get("sessionId"), map);
         add("userName", true, MDC.get("userName"), map);
         add("logMethod", true, getMethod(event), map);
         return map;
     }

	protected String getApplicationName(ILoggingEvent event) {
		String applicationName = MDC.get("applicationName");
		if(applicationName != null) {
			return applicationName;
		}
		return event.getLoggerContextVO().getName();
	}

	protected String getMethod(ILoggingEvent event) {
		String[] classMethods = event.getLoggerName().split("\\.");
		return classMethods[classMethods.length - 1];
	}

}
