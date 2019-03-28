package com.dawninfotek.logplus.util;

import java.lang.management.ManagementFactory;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dawninfotek.logplus.config.LogPlusField;
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
			
			String transactionPath = null;
			
			try {
				// need to make sure any error in LogPlus will not impact the application in the
				// run time.
				LogPlusUtils.initThreadContext();
				
				prepareSysFields();
				prepareLogPlusFields(httpRequest);
				transactionPath = LogPlusUtils.getLogPlusFieldValue(LogPlusConstants.TRANSACTION_PATH);

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
		// log service begin
		String transactionPath = LogPlusUtils.getTransactionPath(httpRequest);

		if (logger.isTraceEnabled()) {
			logger.trace("transactionPath is {}.", transactionPath);
		}

		LogPlusUtils.saveFieldValue(LogPlusConstants.TRANSACTION_PATH, transactionPath);
		
		//override the default service name 
		LogPlusUtils.saveFieldValue(LogPlusConstants.SERVICE_NAME, StringUtils.removeStart(transactionPath, "/"));	
		
		String logPlusHeader = httpRequest.getHeader(LogPlusUtils.getLogPlusHeaderName());

		if (StringUtils.isEmpty(logPlusHeader)) {
			processWithoutLogPlusHeader(httpRequest);
		} else {
			processLogPlusHeader(logPlusHeader, httpRequest);
		}

		String path = httpRequest.getPathInfo();
		if (path == null) {
			path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());
			LogPlusUtils.saveFieldValue(LogPlusConstants.PATH, path);
		}
		logger.info("request path: " + path);
	}
	
	/***
	 * process headers with AQAHeader
	 * 
	 * @param logPlusHeader
	 *            passed log header
	 * @return string AQAHeader hash code
	 */
	private void processLogPlusHeader(String logPlusHeader, HttpServletRequest httpRequest) {
		String decodedHeader = LogPlusUtils.decode(logPlusHeader);
		String[] headers = decodedHeader.split(";");
		String key;
		String headerValue;
		for (String header : headers) {

			logger.trace("received AQA header: " + header);

			String[] aqastrings = header.split("=", 2);

			key = aqastrings[0];

			if (aqastrings.length <= 1) {
				headerValue = "";
			} else {
				headerValue = aqastrings[1];
			}

			LogPlusUtils.saveFieldValue(key, headerValue);
			if(key.equals(LogPlusConstants.CHECKPOINT)) {
				LogPlusUtils.saveFieldValue(LogPlusConstants.CURR_CHECKPOINT, headerValue);
			}
		}

		// Need to be able to create new fields which not be defined in the previous tier.
		for (String propertyKey : this.fieldNmaes) {
			
			String values = LogPlusUtils.getLogProperty(propertyKey + ".value", "");
			if(!values.contains(LogPlusConstants.ALIAS)) {
				// deal with non-alias fields first
				// need to be added
				fieldsNameProcess(propertyKey, httpRequest);
			}
		}
		// alias fields process
		for (String propertyKey : this.fieldNmaes) {
			String values = LogPlusUtils.getLogProperty(propertyKey + ".value", "");
			if(values.contains(LogPlusConstants.ALIAS)) {
				// deal with alias fields
				fieldsNameProcess(propertyKey, httpRequest);
			}
		}
	}
	
	private void fieldsNameProcess(String propertyKey, HttpServletRequest httpRequest) {
		String key = LogPlusUtils.getLogProperty(propertyKey + ".key", propertyKey);
		if (LogPlusUtils.containField(key)) {	
			LogPlusField field = LogPlusContext.getLogPlusField(key);				
			if(field == null || field.getScope() != LogPlusField.SCOPE_LINE) {				
				LogPlusUtils.saveFieldValue(key, LogPlusUtils.resolveFieldValue(propertyKey, httpRequest));
			}
		}
	}

	/***
	 * process headers without AQAHeader
	 * 
	 * @param httpRequest
	 *            request
	 * @return string AQAHeader hash code
	 */
	private void processWithoutLogPlusHeader(HttpServletRequest httpRequest) {
		// prepare all fields key value;
		for (String propertyKey : this.fieldNmaes) {
			String values = LogPlusUtils.getLogProperty(propertyKey + ".value", "");
			// deal with non-alias fields first
			if(!values.contains(LogPlusConstants.ALIAS)) {
				fieldsNameProcessWithoutHeader(propertyKey, httpRequest);
			}
		}
		for (String propertyKey : this.fieldNmaes) {
			String values = LogPlusUtils.getLogProperty(propertyKey + ".value", "");
			// deal with alias fields
			if(values.contains(LogPlusConstants.ALIAS)) {
				fieldsNameProcessWithoutHeader(propertyKey, httpRequest);
			}
		}
	}
	
	private void fieldsNameProcessWithoutHeader(String propertyKey, HttpServletRequest httpRequest) {
		LogPlusField field = LogPlusContext.getLogPlusField(propertyKey);
		//will ignored the EVENT scope fields
		if(field == null || field.getScope() != LogPlusField.SCOPE_LINE) {
			String key = LogPlusUtils.getLogProperty(propertyKey + ".key", propertyKey);
			LogPlusUtils.saveFieldValue(key, LogPlusUtils.resolveFieldValue(propertyKey, httpRequest));
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
	
	protected void prepareSysFields() {
		LogPlusUtils.saveFieldValue(LogPlusConstants.PROCESS_ID, processId);
		LogPlusUtils.saveFieldValue(LogPlusConstants.HOST_NAME, LogPlusUtils.getLogPlusFieldValue(LogPlusConstants.HOST_NAME));
		LogPlusUtils.saveFieldValue(LogPlusConstants.SERVICE_NAME, LogPlusUtils.getLogProperty(LogPlusConstants.SERVICE_NAME, ""));
		LogPlusUtils.saveFieldValue(LogPlusConstants.APPLICATION_NAME, LogPlusUtils.getLogProperty(LogPlusConstants.APPLICATION_NAME, ""));
	}

	protected void removeSysFields() {
		LogPlusUtils.removeField(LogPlusConstants.PROCESS_ID);
		LogPlusUtils.removeField(LogPlusConstants.HOST_NAME);
		LogPlusUtils.removeField(LogPlusConstants.SERVICE_NAME);
		LogPlusUtils.removeField(LogPlusConstants.APPLICATION_NAME);
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
