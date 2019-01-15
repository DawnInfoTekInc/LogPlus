/**************************************************************************
 * Licensed Material - Dawn InfoTek Inc.                                  *
 * Copyright (c) Dawn InfoTek Inc. 1999, 2005, 2018 - All rights reserved.*
 * (<http://www.dawninfotek.com>)                                         *
 *                                                                        *
 **************************************************************************/

package com.dawninfotek.logx.util;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.dawninfotek.logx.config.LogXField;
import com.dawninfotek.logx.core.LogXConstants;
import com.dawninfotek.logx.core.LogXContext;
import com.dawninfotek.logx.resolver.Resolver;

public class LogXUtils implements LogXConstants {

	final static Logger utilLogger = LoggerFactory.getLogger(LogXUtils.class);
	
	protected static Set<String> maskNames;
	
	static {
	
		String[] hs = getLogProperties(LogXConstants.MASK_KEYWORD, null);
		
		maskNames = new HashSet<String>();
		if (hs != null) {
			for (String hashName : hs) {
				maskNames.add(hashName);
			}
		}
	}

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

	/**
	 * answer field names
	 * @return
	 */
	public static String[] getLogXFieldNames() {
		return getLogProperties(LogXConstants.FIELDS_NAME, null);
	}

	/**
	 * Answer the Trace Header Name
	 * 
	 * @param
	 * @return string header name
	 */
	public static String getLogXHeaderName() {
		return getLogProperty(LogXConstants.HEADER_NAME, "AQALogX");
	}

	/**
	 * Answer the value of Trace Header Name
	 * 
	 * @param
	 * @return string header value
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
	 * Answer the value of Trace Header
	 * 
	 * @return string header value
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
	 * text message format
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

	/**
	 * encode a string with base64
	 * @param source
	 * @return
	 */
	public static String encode(String source) {

		if (source == null) {
			return null;
		}

		return Base64.encodeBase64String(source.getBytes());

	}

	/**
	 * decode a string with base64 decode
	 * @param source
	 * @return
	 */
	public static String decode(String source) {

		if (source == null) {
			return null;
		}

		return new String(Base64.decodeBase64(source));

	}
	
	public static String getLogXFieldValue(String fieldName, boolean contextScope) {
		
		String result = null;
		
		//first, try MDC first
		if(!contextScope) {
			result = MDC.get(fieldName);
		}
		if(result == null) {
			//try logX Context value
			result = LogXContext.getContextVariable(fieldName);
			
			if(result == null) {
				//try logX properties
				result = getLogProperty(fieldName, null);
			}
		}
		
		return result;
		
	}
	
	/**
	 * Determine if logX system need to resolve the value for given field 
	 * @param fieldName
	 * @param logLevel
	 * @param loggerPackage
	 * @return
	 */
	public static String resolveFieldValue(LogXField field, String loggerPackage, String sourceText) {
		
		String result = null;
		
		String matchingPackage = null;
		
		if(field.getForPackages()!= null) {
			
			for(String p: field.getForPackages()) {
				
				if(loggerPackage.equals(p) || loggerPackage.startsWith(p + ".")){
					//match
					matchingPackage = p;
					break;
				}
				
			}
			
		}
		
		if(matchingPackage != null) {
			
			Map<String, Object> ps = new HashMap<String, Object>();
			
			try {
			
				Resolver resolver = LogXContext.resolver(field.getName());
				
				ps.put("fieldName", field.getName());
			
				ps.put("package", matchingPackage);
			
				ps.put("sourceText", sourceText);
			
				result = resolver.resolveValue(null, ps);
			
			}catch (Exception e) {
				utilLogger.info("Fail to resolve value for:" + field.getName() + " with parameters " + ps , e);
			}
			
		}
		
		return result;
		
	}
	
	/**
	 * Resolve the field value based on the 
	 * @param fieldName
	 * @param logLevel
	 * @param loggerPackage
	 * @return
	 */
	public static boolean resolveFieldValueRequired(LogXField field, String logLevel) {
		
		System.out.println("LogXField:" + field.getName());
		
		List<String> levels = field.getForLogLevels();	
		
		return levels != null && levels.contains(logLevel);
		
	}
	
	public static String resolveFieldValue(String propertyKey, HttpServletRequest httpRequest) {
		
		//String key = LogXUtils.getLogProperty(propertyKey + ".key", propertyKey);
		
		String values = LogXUtils.getLogProperty(propertyKey + ".value", "");

		String fieldValue = null;

		if (StringUtils.isEmpty(values)) {
			fieldValue = "";
			utilLogger.warn("value must not be empty, keyword: " + propertyKey + " value: " + values);
		} else {

			for (String value : values.split(",")) {

				String[] p = value.split("\\.", 2);
				Resolver resolver = LogXContext.resolver(p[0]);

				if (resolver != null) {

					Map<String, Object> parameters = null;
					if (p.length > 1) {
						parameters = new HashMap<String, Object>();
						parameters.put(Resolver.PARAMETERS, p[1]);
					}
					fieldValue = resolver.resolveValue(httpRequest, parameters);
				} else {
					utilLogger.error("unknown property keyword: " + propertyKey + " value: " + value);
				}
				
				if(StringUtils.isNotEmpty(fieldValue)) {
					break;
				}

			}
		}

		// No null value to be returned
		if (fieldValue == null) {
			fieldValue = "";
		}

		// see if hash the value is required
		if (maskNames.contains(propertyKey) && !fieldValue.isEmpty()) {
			fieldValue = LogXContext.hashService().hash(fieldValue, null);
		}

		return fieldValue;

	}
	
}
