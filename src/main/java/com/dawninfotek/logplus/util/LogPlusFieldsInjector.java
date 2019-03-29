package com.dawninfotek.logplus.util;

import java.lang.management.ManagementFactory;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dawninfotek.logplus.core.LogPlusConstants;
import com.dawninfotek.logplus.core.LogPlusContext;

public class LogPlusFieldsInjector {
	
	private static LogPlusFieldsInjector instance;
	
	final static Logger logger = LoggerFactory.getLogger(LogPlusFieldsInjector.class);
	public static String processId = ManagementFactory.getRuntimeMXBean().getName();

	protected String[] fieldNmaes;
	protected String[] logPlusHeaders;
	
	/**
	 * If annotation is used, the url mapping will be /*, this mapping can be
	 * overridden by the definitions in web.xml or the configuration values in the
	 * logplus.properties.
	 */
	protected String[] urlMappingsIncludes;
	protected String[] urlMappingsExcludes;
	
	protected AntPathMatcher antPathMatcher;
	
	static {
		instance = new LogPlusFieldsInjector();
	}
	
	private LogPlusFieldsInjector() {
		super();
		init();
	}
	
	private void init() {
		this.logPlusHeaders = LogPlusUtils.getLogPlusHeaderInclues();
		this.fieldNmaes = LogPlusUtils.getLogPlusFieldNames();
		this.urlMappingsIncludes = LogPlusUtils.getLogProperties(LogPlusConstants.URL_MAPPINGS_INCLUDES, null);
		this.urlMappingsExcludes = LogPlusUtils.getLogProperties(LogPlusConstants.URL_MAPPINGS_EXCLUDES, null);
		if (this.urlMappingsIncludes != null || this.urlMappingsExcludes != null) {
			this.antPathMatcher = new AntPathMatcher();
		}
	}
	
	public static boolean preProcessHttp(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		return instance.preProcessHttpInternal(httpRequest, httpResponse);
	}
	
	public static void postProcessHttp(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		instance.postProcessHttpInternal(httpRequest, httpResponse);
	}
	
	protected boolean preProcessHttpInternal(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		// see if need to verify the url
		if (isUrlMatch(httpRequest)) {
			// to trace all the headers
			if (logger.isTraceEnabled()) {
				traceRequest(httpRequest);
			}			
			
			try {
				// need to make sure any error in LogPlus will not impact the application in the run time.	
				prepareLogPlusFields(httpRequest);
				String transactionPath = LogPlusUtils.getLogPlusFieldValue(LogPlusConstants.TRANSACTION_PATH);
				if (transactionPath != null) {
					LogPlusContext.checkPointService().startCheckPoint(transactionPath);
					LogPlusContext.eventService().logServiceEventBegin(transactionPath, logger);
				}
			} catch (Exception e) {
				logger.error("Error occured during processing LogPlus functions.", e);
			}
			return true;
		} else {
			if (logger.isTraceEnabled()) {
				logger.trace("request path:" + httpRequest.getServletPath() + " is not match, do nothing ...");
			}
			return false;
		}
	}
	
	protected void postProcessHttpInternal(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		try {
			String transactionPath = LogPlusUtils.getLogPlusFieldValue(LogPlusConstants.TRANSACTION_PATH);
			
			if (transactionPath != null) {
				LogPlusContext.eventService().logServiceEventEnd(transactionPath, logger);
				LogPlusContext.checkPointService().endCheckPoint(logger);
			}
		} catch (Exception e) {
			logger.error("Error occured during processing LogPlus functions.", e);
		}finally {				
			LogPlusUtils.clearThreadContext();
		}
	}
	
	protected void prepareLogPlusFields(HttpServletRequest httpRequest) {
		
		Map<String, String> fieldsMap = new HashMap<String, String>();
		
		String transactionPath = LogPlusUtils.getTransactionPath(httpRequest);

		if (logger.isTraceEnabled()) {
			logger.trace("transactionPath is {}.", transactionPath);
		}
		
		fieldsMap.put(LogPlusConstants.TRANSACTION_PATH, transactionPath);
		
		fieldsMap.put(LogPlusConstants.SERVICE_NAME, StringUtils.removeStart(transactionPath, "/"));
		//override the default service name 
		//LogPlusUtils.saveFieldValue(LogPlusConstants.SERVICE_NAME, StringUtils.removeStart(transactionPath, "/"));	
		
		String logPlusHeader = httpRequest.getHeader(LogPlusUtils.getLogPlusHeaderName());
		
		if(StringUtils.isNotEmpty(logPlusHeader)) {
			//Extract the headers passed from parent tier
			extractLogPlusHeader(logPlusHeader, fieldsMap);
		}	

		// Need to be able to create new fields which not be defined in the previous tier.
		for (String fieldName : this.fieldNmaes) {
			
			LogPlusUtils.resolveFieldValue(fieldName, httpRequest, fieldsMap, true);			

		}

		String path = httpRequest.getPathInfo();
		if (path == null) {
			path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());
			fieldsMap.put(LogPlusConstants.PATH, path);
		}
		logger.info("request path: " + path);
		
		//store the values into thread local. ready to use after this point ...
		LogPlusUtils.initThreadContext(fieldsMap);
		
	}
	
	private void extractLogPlusHeader(String logPlusHeader, Map<String, String> fieldsMap) {
		
		String decodedHeader = LogPlusUtils.decode(logPlusHeader);
		String[] headers = decodedHeader.split(";");
		String key;
		String headerValue;
		for (String header : headers) {
			
			if(logger.isTraceEnabled()) {
				logger.trace("received AQA header: " + header);
			}

			String[] aqastrings = header.split("=", 2);

			key = aqastrings[0];

			if (aqastrings.length <= 1) {
				headerValue = "";
			} else {
				headerValue = aqastrings[1];
			}
			
			fieldsMap.put(key, headerValue);

			if(key.equals(LogPlusConstants.CHECKPOINT)) {
				fieldsMap.put(LogPlusConstants.CURR_CHECKPOINT, headerValue);
			}
		}
	}

	protected void removeLogPlusFields(HttpServletRequest httpRequest) {
		for (String key : LogPlusContext.notEventScopeFields()) {
			LogPlusUtils.removeField(key);
		}
		LogPlusUtils.removeField(LogPlusUtils.getLogPlusHeaderName());
	}
	
	/**
	 * Check if the url mappings matching
	 * 
	 * @param httpRequest
	 * @return
	 */
	protected boolean isUrlMatch(HttpServletRequest httpRequest) {
		if(urlMappingsExcludes != null) {			
			for (String pattern : urlMappingsExcludes) {
				if (antPathMatcher.match(pattern, httpRequest.getServletPath())) {
					return false;
				}
			}
		}
		
		boolean result = false;
		
		// see if the pattern is defined in properties
		if (urlMappingsIncludes != null) {
			for (String pattern : urlMappingsIncludes) {
				if (antPathMatcher.match(pattern, httpRequest.getServletPath())) {
					result = true;
					break;
				}
			}
		} else {
			result = true;
		}
		return result;
	}
	
	/**
	 * Trace HTTP request
	 * 
	 * @param httpRequest
	 */
	private void traceRequest(HttpServletRequest httpRequest) {
		logger.trace("request servlet path: " + httpRequest.getServletPath());
		logger.trace("request context path: " + httpRequest.getContextPath());
		logger.trace("request path info: " + httpRequest.getPathInfo());
		@SuppressWarnings("rawtypes")
		Enumeration names = httpRequest.getHeaderNames();
		StringBuilder sb = new StringBuilder();
		String name;
		while (names.hasMoreElements()) {
			name = (String) names.nextElement();
			sb.append("[").append(name).append("=").append(httpRequest.getHeader(name)).append("]");
		}
		logger.trace("request headers:" + sb.toString());
	}
}
