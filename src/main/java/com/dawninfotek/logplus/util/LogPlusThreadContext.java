package com.dawninfotek.logplus.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LogPlusThreadContext {
	
	private HttpServletRequest httpRequest;
	
	private HttpServletResponse httpResponse;
	
	private Map<String, String> logPlusFields;

	public LogPlusThreadContext() {
		super();
	}
	
	public LogPlusThreadContext clone() {
		
		Map<String, String> newMap = null;
		if(logPlusFields != null) {
			newMap = new HashMap<String, String>();
			for(Entry<String, String> fieldEntry:logPlusFields.entrySet()) {
				newMap.put(fieldEntry.getKey(), fieldEntry.getValue());
			}
		}
		
		return new LogPlusThreadContext(httpRequest, httpResponse, newMap);
	}
	
	
	public LogPlusThreadContext(HttpServletRequest httpRequest, HttpServletResponse httpResponse, Map<String, String> logPlusFields) {
		super();
		this.httpRequest = httpRequest;
		this.httpResponse = httpResponse;
		this.logPlusFields = logPlusFields;
	}

	public HttpServletRequest getHttpRequest() {
		return httpRequest;
	}

	public HttpServletResponse getHttpResponse() {
		return httpResponse;
	}

	public Map<String, String> getLogPlusFields() {
		return logPlusFields;
	}
	
	public void setHttpRequest(HttpServletRequest httpRequest) {
		this.httpRequest = httpRequest;
	}

	public void setHttpResponse(HttpServletResponse httpResponse) {
		this.httpResponse = httpResponse;
	}

	public void setLogPlusFields(Map<String, String> logPlusFields) {
		this.logPlusFields = logPlusFields;
	}

}
