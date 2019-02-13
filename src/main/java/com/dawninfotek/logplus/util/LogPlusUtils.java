/**************************************************************************
 * Licensed Material - Dawn InfoTek Inc.                                  *
 * Copyright (c) Dawn InfoTek Inc. 1999, 2005, 2018 - All rights reserved.*
 * (<http://www.dawninfotek.com>)                                         *
 *                                                                        *
 **************************************************************************/

package com.dawninfotek.logplus.util;

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

import com.dawninfotek.logplus.config.LogPlusField;
import com.dawninfotek.logplus.core.LogPlusConstants;
import com.dawninfotek.logplus.core.LogPlusContext;
import com.dawninfotek.logplus.resolver.Resolver;

public class LogPlusUtils implements LogPlusConstants {

	final static Logger utilLogger = LoggerFactory.getLogger(LogPlusUtils.class);
	
	protected static Set<String> maskNames;
	
	static {
	
		String[] hs = getLogProperties(LogPlusConstants.MASK_KEYWORD, null);
		
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

		String result = LogPlusContext.configuration().getConfigurationValue(propertyKey);

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

		String p = LogPlusContext.configuration().getConfigurationValue(propertyKey);
		if (p != null) {
			return p.split(",");
		}

		return defaultValue;
	}

	/**
	 * answer field names
	 * @return
	 */
	public static String[] getLogPlusFieldNames() {
		return getLogProperties(LogPlusConstants.FIELDS_NAME, null);
	}

	/**
	 * Answer the Trace Header Name
	 * 
	 * @param
	 * @return string header name
	 */
	public static String getLogPlusHeaderName() {
		return getLogProperty(LogPlusConstants.HEADER_NAME, "AQALogPlus");
	}

	/**
	 * Answer the value of Trace Header Name
	 * 
	 * @param
	 * @return string header value
	 */
	public static String getLogPlusHeaderValue() {
		
		String headerValue = null;
		
		StringBuilder sb = new StringBuilder();
		
		String v = null;
		
		if(getLogPlusHeaderInclues() != null && getLogPlusHeaderInclues().length > 0) {
			
			for(String name:getLogPlusHeaderInclues()) {
				
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
		
		v = LogPlusContext.checkPointService().getCurrentCheckPoint();
		
		if(v != null) {
			sb.append(LogPlusConstants.CHECKPOINT).append("=").append(v);
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
	public static String[] getLogPlusHeaderInclues() {
		return getLogProperties(LogPlusConstants.HEADER_INCLUDES, null);
	}

	/**
	 * Answer the transaction path based on the predefined mapping Mapping possible
	 * include combination of request path, header value and form value and more
	 * 
	 * @param httpRequest
	 * @return
	 */
	public static String getTransactionPath(HttpServletRequest httpRequest) {

		return LogPlusContext.configuration().getTransactionPath(httpRequest);

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
	
	public static String getLogPlusFieldValue(String fieldName, boolean contextScope) {
		
		String result = null;
		
		//first, try MDC first
		if(!contextScope) {
			result = MDC.get(fieldName);
		}
		if(result == null) {
			//try LogPlus Context value
			result = LogPlusContext.getContextVariable(fieldName);
			
			if(result == null) {
				//try LogPlus properties
				result = getLogProperty(fieldName, null);
			}
		}
		
		return result;
		
	}
	
	/**
	 * Determine if LogPlus system need to resolve the value for given field 
	 * @param fieldName
	 * @param logLevel
	 * @param loggerPackage
	 * @return
	 */
	public static String resolveFieldValue(LogPlusField field, String loggerPackage, String sourceText) {
		
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
			
				Resolver resolver = LogPlusContext.resolver(field.getName());
				
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
	public static boolean resolveFieldValueRequired(LogPlusField field, String logLevel) {
		
		List<String> levels = field.getForLogLevels();	
		
		return levels != null && levels.contains(logLevel);
		
	}
	
	public static String resolveFieldValue(String propertyKey, HttpServletRequest httpRequest) {
		
		String values = LogPlusUtils.getLogProperty(propertyKey + ".value", "");

		String fieldValue = null;

		if (StringUtils.isEmpty(values)) {
			fieldValue = "";
			utilLogger.warn("value must not be empty, keyword: " + propertyKey + " value: " + values);
		} else {

			for (String value : values.split(",")) {

				String[] p = value.split("\\.", 2);
				Resolver resolver = LogPlusContext.resolver(p[0]);

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
			fieldValue = LogPlusContext.hashService().hash(fieldValue, null);
		}

		return fieldValue;

	}
	
}
