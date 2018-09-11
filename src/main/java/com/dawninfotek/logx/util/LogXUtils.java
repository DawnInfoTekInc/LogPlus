/**************************************************************************
 * Licensed Material - Dawn InfoTek Inc.                                  *
 * Copyright (c) Dawn InfoTek Inc. 1999, 2005, 2018 - All rights reserved.*
 * (<http://www.dawninfotek.com>)                                         *
 *                                                                        *
 **************************************************************************/

package com.dawninfotek.logx.util;

import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.dawninfotek.logx.core.LogXConstants;
import com.dawninfotek.logx.core.LogXContext;

public class LogXUtils implements LogXConstants {

	final static Logger utilLogger = LoggerFactory.getLogger(LogXUtils.class);

	/**
	 * Retrieves configuration value from configuration file
	 * 
	 * @param propertyKey
	 * @param defaultValue
	 * @return String
	 */
	public static String getLogProperty(String propertyKey, String defaultValue) {

		String result = LogXContext.configuration().getConfigurationValue(propertyKey);

		if (result == null) {
			result = defaultValue;
		}

		return result;
	}

	/**
	 * Retrieves configuration values from configuration file
	 * 
	 * @param propertyKey
	 * @param defaultValue
	 * @return String[]
	 */
	public static String[] getLogProperties(String propertyKey, String[] defaultValue) {

		String p = LogXContext.configuration().getConfigurationValue(propertyKey);
		if (p != null) {
			return p.split(",");
		}

		return defaultValue;
	}

	public static String[] getLogXFieldNames() {
		return getLogProperties(LogXConstants.FIELDS_NAME, null);
	}

	/**
	 * Answer the value of AQA Trace Header Name
	 * 
	 * @param
	 * @return string AQA header name
	 */
	public static String getLogXHeaderName() {
		return getLogProperty(LogXConstants.HEADER_NAME, "AQALogX");
	}

	/**
	 * Answer the value of AQA Trace Header Name
	 * 
	 * @param
	 * @return string AQA header name
	 */
	public static String getLogXHeaderValue() {
		
		String headerValue = null;
		
		StringBuilder sb = new StringBuilder();
		
		String v = null;
		
		if(getLogXHeaderInclues() != null && getLogXHeaderInclues().length > 0) {
			
			for(String name:getLogXHeaderInclues()) {
				
				sb.append(name).append("=");
				v = MDC.get(name);
				if(v == null) {
					sb.append("");
				}else {
					sb.append(v);
				}
				
				sb.append(";");				
			}
		
		}
		
		v = LogXContext.checkPointService().getCurrentCheckPoint();
		
		if(v != null) {
			sb.append(LogXConstants.CHECKPOINT).append("=").append(v);
		}		
		
		if(sb.length() > 0) {
			headerValue = sb.toString();
			//remove ; at the end
			headerValue = StringUtils.removeEnd(headerValue, ";");
		}else {
			headerValue = "";
		}

		return encode(headerValue);
	}	

	/**
	 * Answer the value of AQA Trace Header
	 * 
	 * @return string AQA header value
	 */
	public static String[] getLogXHeaderInclues() {
		return getLogProperties(LogXConstants.HEADER_INCLUDES, null);
	}

	/**
	 * Answer the transaction path based on the predefined mapping Mapping possible
	 * include combination of request path, header value and form value and more
	 * 
	 * @param httpRequest
	 * @return
	 */
	public static String getTransactionPath(HttpServletRequest httpRequest) {

		return LogXContext.configuration().getTransactionPath(httpRequest);

	}

	/***
	 * private text message format
	 * 
	 * @param givenLogger
	 * @param message
	 */
	public static void logTextMessage(Object givenLogger, String message) {

		if (givenLogger != null) {
			try {

				Method info = givenLogger.getClass().getMethod("info", String.class);
				if (info != null) {
					info.invoke(givenLogger, message);
				}

			} catch (Exception e) {
				utilLogger.info(message);
			}
		} else {
			utilLogger.info(message);
		}

	}

	public static String encode(String source) {

		if (source == null) {
			return null;
		}

		return Base64.encodeBase64String(source.getBytes());

	}

	public static String decode(String source) {

		if (source == null) {
			return null;
		}

		return new String(Base64.decodeBase64(source));

	}

}
