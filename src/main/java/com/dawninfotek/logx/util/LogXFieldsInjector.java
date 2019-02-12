package com.dawninfotek.logx.util;

import java.lang.management.ManagementFactory;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.dawninfotek.logx.config.LogXField;
import com.dawninfotek.logx.core.LogXConstants;
import com.dawninfotek.logx.core.LogXContext;

public class LogXFieldsInjector {
	
	private static LogXFieldsInjector instance;
	
	final static Logger logger = LoggerFactory.getLogger(LogXFieldsInjector.class);
	public static String processId = ManagementFactory.getRuntimeMXBean().getName();

	protected String[] fieldNmaes;
	protected String[] logxHeaders;
	
	/**
	 * If annotation is used, the url mapping will be /*, this mapping can be
	 * overridden by the definitions in web.xml or the configuration values in the
	 * logx.properties.
	 */
	protected String[] urlMappingsIncludes;
	protected String[] urlMappingsExcludes;
	
	protected AntPathMatcher antPathMatcher;
	
	static {
		instance = new LogXFieldsInjector();
	}
	
	private LogXFieldsInjector() {
		super();
		init();
	}
	
	private void init() {
		
		this.logxHeaders = LogXUtils.getLogXHeaderInclues();
		this.fieldNmaes = LogXUtils.getLogXFieldNames();
		this.urlMappingsIncludes = LogXUtils.getLogProperties(LogXConstants.URL_MAPPINGS_INCLUDES, null);
		this.urlMappingsExcludes = LogXUtils.getLogProperties(LogXConstants.URL_MAPPINGS_EXCLUDES, null);
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
				// need to make sure any error in logx will not impact the application in the
				// run time.
				prepareSysFields();

				prepareLogXFields(httpRequest);

				transactionPath = MDC.get(LogXConstants.TRANSACTION_PATH);

				if (transactionPath != null) {

					LogXContext.checkPointService().startCheckPoint(transactionPath);

					LogXContext.eventService().logServiceEventBegin(transactionPath, logger);
				}

			} catch (Exception e) {
				logger.error("Error occured during processing logx functions.", e);
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
			
			String transactionPath = MDC.get(LogXConstants.TRANSACTION_PATH);
			
			if (transactionPath != null) {

				LogXContext.eventService().logServiceEventEnd(transactionPath, logger);

				LogXContext.checkPointService().endCheckPoint(logger);
			}

			removeLogXFields(httpRequest);

			removeSysFields();

		} catch (Exception e) {
			logger.error("Error occured during processing logx functions.", e);
		}
		
	}
	
	protected void prepareLogXFields(HttpServletRequest httpRequest) {

		// log service begin
		String transactionPath = LogXUtils.getTransactionPath(httpRequest);

		if (logger.isTraceEnabled()) {
			logger.trace("transactionPath is {}.", transactionPath);
		}

		MDC.put(LogXConstants.TRANSACTION_PATH, transactionPath);
		
		//override the default service name 
		MDC.put(LogXConstants.SERVICE_NAME, StringUtils.removeStart(transactionPath, "/"));		

		String logXHeader = httpRequest.getHeader(LogXUtils.getLogXHeaderName());

		if (StringUtils.isEmpty(logXHeader)) {
			processWithoutLogXHeader(httpRequest);
		} else {
			processLogXHeader(logXHeader, httpRequest);
		}

		String path = httpRequest.getPathInfo();
		if (path == null) {
			path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());
			MDC.put("path", path);
		}
		logger.info("request path: " + path);
	}
	
	/***
	 * process headers with AQAHeader
	 * 
	 * @param logXHeader
	 *            passed log header
	 * @return string AQAHeader hash code
	 */
	private void processLogXHeader(String logXHeader, HttpServletRequest httpRequest) {
		String decodedHeader = LogXUtils.decode(logXHeader);
		// StringBuilder AQAHeaderValue = new StringBuilder();
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

			MDC.put(key, headerValue);
			if(key.equals(LogXConstants.CHECKPOINT)) {
				MDC.put(LogXConstants.CURR_CHECKPOINT, headerValue);
			}
		}

		// Need to be able to create new fields which not be defined in the previous
		// tier.
		for (String propertyKey : this.fieldNmaes) {

			key = LogXUtils.getLogProperty(propertyKey + ".key", propertyKey);

			// need to be added
			if (MDC.get(key) == null) {					
				LogXField field = LogXContext.getLogXField(key);				
				if(field == null || field.getScope() != LogXField.SCOPE_LINE) {				
					MDC.put(key, LogXUtils.resolveFieldValue(propertyKey, httpRequest));
				}

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
	private void processWithoutLogXHeader(HttpServletRequest httpRequest) {

		// prepare all fields key value;

		for (String propertyKey : this.fieldNmaes) {
			
			LogXField field = LogXContext.getLogXField(propertyKey);
			//will ignored the EVENT scope fields
			if(field == null || field.getScope() != LogXField.SCOPE_LINE) {
				String key = LogXUtils.getLogProperty(propertyKey + ".key", propertyKey);
				MDC.put(key, LogXUtils.resolveFieldValue(propertyKey, httpRequest));			
			}
		}
	}

	protected void removeLogXFields(HttpServletRequest httpRequest) {
		for (String key : this.fieldNmaes) {
			MDC.remove(key);
		}

		MDC.remove(LogXUtils.getLogXHeaderName());
		MDC.remove(LogXConstants.TRANSACTION_PATH);
		MDC.remove(LogXConstants.PATH);

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
		MDC.put(LogXConstants.PROCESS_ID, processId);
		MDC.put("hostName", LogXUtils.getLogXFieldValue("hostName", true));
		MDC.put(LogXConstants.SERVICE_NAME, LogXUtils.getLogProperty(LogXConstants.SERVICE_NAME, ""));
		MDC.put(LogXConstants.APPLICATION_NAME, LogXUtils.getLogProperty(LogXConstants.APPLICATION_NAME, ""));
	}

	protected void removeSysFields() {
		MDC.remove(LogXConstants.PROCESS_ID);
		MDC.remove("hostName");
		MDC.remove(LogXConstants.SERVICE_NAME);
		MDC.remove(LogXConstants.APPLICATION_NAME);
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
