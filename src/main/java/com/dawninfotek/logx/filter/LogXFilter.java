/**************************************************************************
 * Licensed Material - Dawn InfoTek Inc.                                  *
 * Copyright (c) Dawn InfoTek Inc. 1999, 2005, 2018 - All rights reserved.*
 * (<http://www.dawninfotek.com>)                                         *
 *                                                                        *
 **************************************************************************/
package com.dawninfotek.logx.filter;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.dawninfotek.logx.core.LogXConstants;
import com.dawninfotek.logx.core.LogXContext;
import com.dawninfotek.logx.resolver.Resolver;
import com.dawninfotek.logx.util.AntPathMatcher;
import com.dawninfotek.logx.util.LogXUtils;

/**
 * This is the Major Filter for LogX, for using the LogX, every application
 * should has to configure this filter in the Web Application.
 * 
 * @author Ryan Wang, John Li
 *
 */
@WebFilter("/*") // default filter mapping, could be overridden by web.xml, the filterName
					// is:com.dawninfotek.logx.filter.LogXFilter
public class LogXFilter implements Filter {

	final static Logger logger = LoggerFactory.getLogger(LogXFilter.class);
	public static String processId = ManagementFactory.getRuntimeMXBean().getName();

	protected String[] fieldNmaes;
	protected String[] logxHeaders;
	protected Set<String> maskNames;
	/**
	 * If annotation is used, the url mapping will be /*, this mapping can be
	 * overridden by the definitions in web.xml or the configuration values in the
	 * logx.properties.
	 */
	protected String[] urlMappings;
	protected AntPathMatcher antPathMatcher;

	@Override
	public void init(FilterConfig filterConfig) {

		this.logxHeaders = LogXUtils.getLogXHeaderInclues();
		this.fieldNmaes = LogXUtils.getLogXFieldNames();
		String[] hs = LogXUtils.getLogProperties(LogXConstants.MASK_KEYWORD, null);
		maskNames = new HashSet<String>();
		if (hs != null) {
			for (String hashName : hs) {
				maskNames.add(hashName);
			}
		}

		urlMappings = LogXUtils.getLogProperties(LogXConstants.URL_MAPPINGS, null);
		if(urlMappings != null) {
			antPathMatcher = new AntPathMatcher();
		}
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		// see if need to verify the url
		if (isUrlMatch(httpRequest)) {

			// to trace all the headers
			if (logger.isTraceEnabled()) {
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

			chain.doFilter(httpRequest, response);

			try {
				if (transactionPath != null) {

					LogXContext.eventService().logServiceEventEnd(transactionPath, logger);

					LogXContext.checkPointService().endCheckPoint(logger);
				}

				removeLogXFields(httpRequest);

				removeSysFields();

			} catch (Exception e) {
				logger.error("Error occured during processing logx functions.", e);
			}

		} else {
			if (logger.isTraceEnabled()) {
				logger.trace("request path:" + httpRequest.getServletPath() + " is not match, do nothing ...");
			}

			chain.doFilter(request, response);
		}

	}

	/***
	 * process headers with AQAHeader
	 * 
	 * @param logXHeader
	 *            passed log header
	 * @return string AQAHeader hash code
	 */
	private void processLogXHeader(String logXHeader) {
		String decodedHeader = LogXUtils.decode(logXHeader);
		// StringBuilder AQAHeaderValue = new StringBuilder();
		String[] headers = decodedHeader.split(";");
		for (String header : headers) {
			String[] aqastrings = header.split("=", 2);
			String key;
			String headerValue;
			logger.trace("received AQA header: " + header);
			if (aqastrings.length <= 1) {
				key = aqastrings[0];
				headerValue = "";
				MDC.put(key, headerValue);
			} else {
				key = aqastrings[0];
				headerValue = aqastrings[1];
				MDC.put(key, headerValue);
				if (key.equals(LogXConstants.CHECKPOINT)) {
					MDC.put(LogXConstants.CURR_CHECKPOINT, headerValue);
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

			String key = LogXUtils.getLogProperty(propertyKey + ".key", propertyKey);
			String value = LogXUtils.getLogProperty(propertyKey + ".value", "");
			String fieldValue;
			
			if(StringUtils.isEmpty(value)) {
				fieldValue = "";
				logger.warn("value must not be empty, keyword: " + key + " value: " + value);				
			}else {	
				
				String[] p = value.split("\\.", 2);
				Resolver resolver = LogXContext.resolver(p[0]);
								
				if(resolver != null) {					
					
					Map<String, Object> parameters = null;
					if(p.length > 1) {
						parameters = new HashMap<String, Object>();
						parameters.put(Resolver.PARAMETERS, p[1]);
					}
					fieldValue = resolver.resolveValue(httpRequest, parameters);
				}else {
					fieldValue = "";
					logger.error("unknown property keyword: " + key + " value: " + value);
				}
			}
			
			/*
			if (value.startsWith(LogXConstants.REQUEST_HEADER)) {
				fieldValue = requestHeader(httpRequest, key, value);
			} else if (value.startsWith(LogXConstants.SESSION_ID)) {
				fieldValue = getSessionId(httpRequest);
			} else if (value.startsWith(LogXConstants.SESSION)) {
				fieldValue = getSession(httpRequest, key, value);
			} else if (value.startsWith(LogXConstants.REMOTEADDR)) {
				fieldValue = getRemoteAddr(httpRequest);
			} else if (value.isEmpty()) {
				fieldValue = "";
				logger.warn("value must not be empty, keyword: " + key + " value: " + value);
			} else {
				fieldValue = "";
				logger.error("unknown property keyword: " + key + " value: " + value);
			}
			*/

			// see if hash the value is required
			if (this.maskNames.contains(key) && StringUtils.isNotEmpty(fieldValue)) {
				fieldValue = LogXContext.hashService().hash(fieldValue, null);
			}

			if (key.equals(LogXConstants.UUID) && StringUtils.isEmpty(fieldValue)) {
				//fieldValue = UUID.randomUUID().toString();
				//use configurable resolver
				fieldValue = LogXContext.resolver(LogXConstants.UUID).resolveValue(httpRequest, null);
			}
			
			if(fieldValue == null) {
				logger.debug("field value under key:" + key + " is null" );
				//make it empty for some MDC does not allow the null value
				fieldValue = "";
			}
			MDC.put(key, fieldValue);
		}

	}

	/***
	 * get value from request header directly
	 * 
	 * @param httpRequest
	 *            http request
	 * @param key
	 *            key from request
	 * @param value
	 *            key matched value
	 * @return value string from header
	 */
	/*
	private String requestHeader(HttpServletRequest httpRequest, String key, String value) {
		logger.trace("header retrieve - key: " + key + " value: " + value);
		String[] values = value.split("\\.", 2);
		if (values.length > 1) {
			try {


				String headerName = values[1];

				String res = null;
				String attribute = StringUtils.substringBetween(values[1], "[", "]");

				if (attribute != null) {

					headerName = headerName.substring(0, headerName.indexOf("["));
					String headerValue = httpRequest.getHeader(headerName);

					if (headerValue != null) {
						String[] keyValues = headerValue.split("\\.");
						for (String keyValue : keyValues) {
							String[] s = keyValue.split("=", 2);
							if (s.length > 1 && s[0].equals(attribute)) {
								res = s[1];
							}
						}
					}

				} else {
					// single value
					res = httpRequest.getHeader(headerName);

				}

				if (res == null) {
					res = "";
				}

				return res;

			} catch (Exception e) {
				logger.error("value not exist, " + e.getMessage());
			}
		}
		return "";
	}
	*/

	/***
	 * get session ID
	 * 
	 * @param httpRequest
	 *            request
	 * @return session id string
	 */
	/*
	private String getSessionId(HttpServletRequest httpRequest) {
		HttpSession session = httpRequest.getSession(false);
		if (session != null) {
			String res = session.getId();
			if (res == null) {
				return "";
			}
			return res;
		} else
			return "";
	}
	*/

	/**
	 * get ip from remote
	 * 
	 * @param httpRequest
	 * @return
	 */
	/*
	private String getRemoteAddr(HttpServletRequest httpRequest) {
		String ip = httpRequest.getHeader("X-Forwarded-For");
		if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {
			ip = httpRequest.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {
			ip = httpRequest.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {
			ip = httpRequest.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {
			ip = httpRequest.getHeader("HTTP_X_FORWARDED");
		}
		if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {
			ip = httpRequest.getHeader("HTTP_X_CLUSTER_CLIENT_IP");
		}
		if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {
			ip = httpRequest.getHeader("HTTP_CLIENT_IP");
		}
		if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {
			ip = httpRequest.getHeader("HTTP_FORWARDED_FOR");
		}
		if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {
			ip = httpRequest.getHeader("HTTP_FORWARDED");
		}
		if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {
			ip = httpRequest.getHeader("HTTP_VIA");
		}
		if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {
			ip = httpRequest.getHeader("REMOTE_ADDR");
		}
		if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {
			ip = httpRequest.getRemoteAddr();
		}
		if (ip == null) {
			ip = "";
		}
		return ip;
	}
	*/

	/***
	 * get key, value from session
	 * 
	 * @param httpRequest
	 *            http request
	 * @param key
	 *            key in session
	 * @param value
	 *            nested key
	 * @return value string
	 */
	/*
	private String getSession(HttpServletRequest httpRequest, String key, String value) {
		
		logger.trace("session retrieve - key: " + key + " value: " + value);
		try {
			String[] headers = value.split("\\.", 3);
			if (headers.length <= 1 || httpRequest.getSession(false) == null) {
				return "";
			}

			// get the session object then lookup the path
			Object sessionObj = httpRequest.getSession().getAttribute(headers[1]);
			String res = null;
			if (sessionObj != null) {
				if (headers.length == 2) {
					// no path
					res = sessionObj.toString();
				} else {
					// has path
					res = (String) PropertyUtils.getProperty(sessionObj, headers[2]);
				}

			}

			if (res != null) {
				return res;
			}
			// if(headers.length == 2){
			// return (String)PropertyUtils.getProperty(httpRequest, headers[1]);
			// }
			// return
			// (String)PropertyUtils.getProperty(httpRequest.getSession(false).getAttribute(headers[1]),
			// headers[2]);
		} catch (Exception e) {
			logger.info("can not get user Id from session", e);
		}
		return "";
	}
	*/

	protected void prepareSysFields() {
		MDC.put(LogXConstants.PROCESS_ID, processId);
		MDC.put(LogXConstants.SERVICE_NAME, LogXUtils.getLogProperty(LogXConstants.SERVICE_NAME, ""));
		MDC.put(LogXConstants.APPLICATION_NAME, LogXUtils.getLogProperty(LogXConstants.APPLICATION_NAME, ""));
	}

	protected void removeSysFields() {
		MDC.remove(LogXConstants.PROCESS_ID);
		MDC.remove(LogXConstants.SERVICE_NAME);
		MDC.remove(LogXConstants.APPLICATION_NAME);
	}

	protected void prepareLogXFields(HttpServletRequest httpRequest) {

		// log service begin
		String transactionPath = LogXUtils.getTransactionPath(httpRequest);

		MDC.put(LogXConstants.TRANSACTION_PATH, transactionPath);

		String logXHeader = httpRequest.getHeader(LogXUtils.getLogXHeaderName());

		if (StringUtils.isEmpty(logXHeader)) {
			processWithoutLogXHeader(httpRequest);
		} else {
			processLogXHeader(logXHeader);
		}

		String path = httpRequest.getPathInfo();
		if (path == null) {
			path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());
			MDC.put("path", path);
		}
		logger.info("request path: " + path);
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
		
		boolean result = false;
		//see if the pattern is defined in properties
		if(urlMappings != null) {
			for(String pattern:urlMappings) {
				if(antPathMatcher.match(pattern, httpRequest.getServletPath())) {
					result = true;
					break;
				}
			}
		}else {
			result = true;
		}
		
		return result;
	}

	@Override
	public void destroy() {
	}
}