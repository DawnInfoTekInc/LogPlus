package com.dawninfotek.logx.extension.logback;

import org.slf4j.MDC;
import java.util.Map;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.contrib.json.classic.JsonLayout;

public class LogXJsonLayout extends JsonLayout {

	protected Map toJsonMap(ILoggingEvent event) {

        Map<String, Object> map = super.toJsonMap(event);
        map.remove("mdc");
        map.remove("context");
        add("logLevel", true, map.get("level").toString(), map);
        map.remove("level");
        add("appName", true, getApplicationName(event), map);
        add("serviceName", true, getFromMDC("transactionPath"), map);
        add("remoteIp", true, getFromMDC("remoteIp"), map);
        add("hostname", true, getFromMDC("hostName"), map);
        add("processId", true, getFromMDC("processId"), map);
        add("corrId", true, getFromMDC("uuid"), map);
        add("sessionId", true, getFromMDC("sessionId"), map);
        add("clientId", true, getFromMDC("userName"), map);
        add("logMethod", true, getMethod(event), map);
        return map;
    }

	protected String getApplicationName(ILoggingEvent event) {
		String applicationName = MDC.get("applicationName");
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


}
