/**************************************************************************
 * Licensed Material - Dawn InfoTek Inc.                                  *
 * Copyright (c) Dawn InfoTek Inc. 1999, 2005, 2018 - All rights reserved.*
 * (<http://www.dawninfotek.com>)                                         *
 *                                                                        *
 **************************************************************************/

package com.dawninfotek.logplus.util;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//import org.apache.commons.codec.binary.Base64;
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
	
	public static ThreadLocal<LogPlusThreadContext> threadContext;
	
	static {
		
		if(Boolean.valueOf(LogPlusContext.configuration().getConfigurationValue(LogPlusConstants.INHB_FIELD_VALUE))) {
			threadContext = new InheritableThreadContext();
		}else {
			threadContext = new ThreadContext();
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
//				v = MDC.get(name);
				v = getLogPlusFieldValue(name);
				if(StringUtils.isEmpty(v)) {
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
	
	public static Map<String, String> getLogPlusEnterpriseHeaders() {
		
		Map<String, String> map = new HashMap<String, String>();
		String v = null;
		map.put(getLogPlusHeaderName(), getLogPlusHeaderValue());
		for(String name: getLogPlusFieldNames()) {
			LogPlusField field = LogPlusContext.getLogPlusField(name);
			if(field != null && field.getScope() == LogPlusField.SCOPE_ENTERPRISE) {
				if(LogPlusUtils.containField(name)) {
					v = getLogPlusFieldValue(name);
					map.put(name, v);
				}
			}
		}
		return map;
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
		//To prevent the new version of 'common-codec.jar' conflict with some old system. 
		try {
			return Base64Util.encode(source.getBytes("UTF-8"));
		}catch(UnsupportedEncodingException ignored) {
			utilLogger.error("Failed to encode...",ignored);
		}		
		return null; 
		//return Base64.encodeBase64String(source.getBytes());

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
		
		try {
			return new String(Base64Util.decode(source), "UTF-8");
		}catch (Exception ignored) {
			utilLogger.error("Failed to decode ...",ignored);
		}
		
		return null;

		//return new String(Base64.decodeBase64(source));

	}
	
	public static String getLogPlusFieldValue(String fieldName) {
		
		String result = null;
		
		LogPlusThreadContext lc = threadContext.get();
		
		if(lc != null && lc.getLogPlusFields() != null) {
			
			result = lc.getLogPlusFields().get(fieldName);
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
	
	static String ALS = LogPlusConstants.ALIAS + ".";
	
	/**
	 * Resolve the fieldValue, the name/value will be stored into the given Map
	 * @param propertyKey
	 * @param httpRequest
	 * @param fieldsMap
	 * @return
	 */
	public static String resolveFieldValue(String fieldName, HttpServletRequest httpRequest, Map<String, String> fieldsMap, boolean lookForContextParameters) {
		
		LogPlusField field = LogPlusContext.getLogPlusField(fieldName);
		
		if(field == null) {
			utilLogger.error("The field named as:" + fieldName + " is not defined." );
			return null;
		}
		
		String result = fieldsMap.get(fieldName);		
		//see if it is already resolved
		if(result == null) {
					
			if(field.getScope() == LogPlusField.SCOPE_CONTEXT && lookForContextParameters) {
				
				result = LogPlusContext.getContextVariable(fieldName);
				
			}
			
			if(result == null) {
				//still not found, resolve value based on the configuration
				String values = field.getValue();
				
				if (StringUtils.isEmpty(values)) {
					result = "";
					utilLogger.warn("field value must not be empty, fieldName: " + fieldName);
				} else {
					
					if(values.startsWith(ALS)) {
						//alias
						String aliasName = StringUtils.removeStart(values, ALS);
						result = resolveFieldValue(aliasName, httpRequest, fieldsMap, lookForContextParameters);
					}else {
						//resolve regular value
						result = resolveFieldValue(values, httpRequest);

					}
				}
				
			}
			
			if(maskNames == null) {
				
				String[] hs = getLogProperties(LogPlusConstants.MASK_KEYWORD, null);
				maskNames = new HashSet<String>();
				if (hs != null) {
					for (String hashName : hs) {
						maskNames.add(hashName);
					}
				}
			}
			
			// see if hash the value is required
			if (maskNames.contains(fieldName) && !fieldName.isEmpty()) {
				result = LogPlusContext.hashService().hash(result, null);
			}
			
			fieldsMap.put(fieldName, result);
			
			if(utilLogger.isTraceEnabled()) {
				utilLogger.trace("field value resolved result:" + fieldName + "=" + result);
			}
		}
		
		return result;
	}
	
	private static String resolveFieldValue(String values, HttpServletRequest httpRequest) {
		String result = null;
		for (String value : values.split(",")) {
			String[] p = value.split("\\.", 2);
		
			Resolver resolver = LogPlusContext.resolver(p[0]);

			if (resolver != null) {
				Map<String, Object> parameters = null;
				if (p.length > 1) {
					parameters = new HashMap<String, Object>();
					parameters.put(Resolver.PARAMETERS, p[1]);
				}
				result = resolver.resolveValue(httpRequest, parameters);
			} else {
				utilLogger.error("unknown resolver keyword: " + value);
			}
		
			if(StringUtils.isNotEmpty(result)) {
				break;
			}
		}
		
		if(result == null) {
			//no 'null' will be returned. 
			result = "";
		}
		return result;
	}
	

	
	/**
	 * Clear the LogPlusFields in the MDC and Copy the new values from the given Map
	 * @param logplusFilesds
	 */
	public static void clearAndCopyMDC(Map<String, String> logplusFilesds) {
		
		//Since MDC might be used by the application, so only remove the LogPlus fields one by one instead of clear() method by MDC
		for(String fieldMightInMDC:LogPlusContext.notEventScopeFields()) {
			MDC.remove(fieldMightInMDC);
		}
		
		copyToMDC(logplusFilesds);
	}
	
	/**
	 * Copy the new values to MDC from the given Map
	 * @param logplusFilesds
	 */
	public static void copyToMDC(Map<String, String> logplusFilesds) {
		
		if(logplusFilesds != null) {
			for(String name:logplusFilesds.keySet()) {
				MDC.put(name, logplusFilesds.get(name));
			}
		}
	}
	
	public static void saveFieldValue(String fieldName, String fieldValue) {
		
		LogPlusThreadContext lc = threadContext.get();
		
		if(lc == null) {
			threadContext.set(new LogPlusThreadContext(null, null, new HashMap<String, String>()));
		}
		
		Map<String, String> sc = getLogPlusThreadContextFields();

		sc.put(fieldName, fieldValue);
		
		if(LogPlusContext.isUseMDC()) {
			//Update the MDC values as well
			MDC.put(fieldName, fieldValue);
		}
	}
	
	public static void removeField(String fieldName) {

		Map<String, String> sc = getLogPlusThreadContextFields();
		if(sc != null) {
			sc.remove(fieldName);
		}
		
		if(LogPlusContext.isUseMDC()) {
			MDC.remove(fieldName);
		}
	}
	
	public static boolean containField(String fieldName) {

		boolean result = false;
		Map<String, String> sc = getLogPlusThreadContextFields();
		if(sc !=null) {
			result = sc.keySet().contains(fieldName);
		}
		
		return result;
	}
	
	public static void initThreadContext(HttpServletRequest request, HttpServletResponse response) {
		initThreadContext(request, response, new HashMap<String, String>());
	}
	
	public static void initThreadContext(HttpServletRequest request, HttpServletResponse response, Map<String, String> map) {

		threadContext.set(new LogPlusThreadContext(request, response, map));

		if(LogPlusContext.isUseMDC() && map != null) {
			for(String fieldName:map.keySet()) {
				MDC.put(fieldName, map.get(fieldName));
			}
		}
	}


	public static LogPlusThreadContext getCopyOfThreadContext() {
		
		if(threadContext.get() != null) {
			return threadContext.get().clone();
		}		

		return null;
		
	}
	
	public static void clearThreadContext() {
		Map<String, String> sc = getLogPlusThreadContextFields();
		if(sc !=null) {			
			if(LogPlusContext.isUseMDC()) {
				for(String key:sc.keySet()) {
					MDC.remove(key);
				}
			}
			sc.clear();
		}	
	}
	
	public static final LogPlusThreadContext getLogPlusThreadContext() {
		return threadContext.get();
	}
	
	public static final Map<String, String> getLogPlusThreadContextFields() {
		LogPlusThreadContext lc = threadContext.get();
		if(lc != null) {
			return lc.getLogPlusFields();
		}else {
			return null;
		}
	}
}
