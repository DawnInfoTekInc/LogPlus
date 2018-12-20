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
import java.util.HashSet;
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
	private void processWithoutLogXHeader(HttpServletRequest httpRequest) {}

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