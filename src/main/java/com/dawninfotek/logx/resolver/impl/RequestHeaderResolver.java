package com.dawninfotek.logx.resolver.impl;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dawninfotek.logx.resolver.AbstractResolver;

public class RequestHeaderResolver extends AbstractResolver {
	
	public static Logger logger = LoggerFactory.getLogger(RequestHeaderResolver.class);

	@Override
	public String resolveValue(HttpServletRequest httpRequest, Map<String, Object> parameters) {
		
		String result = "";

		String value = (String) parameters.get(PARAMETERS);
		if(logger.isTraceEnabled()) {
			logger.trace("header retrieve value:" + value);
		}

			try {

				String headerName = value;
	
				String attribute = StringUtils.substringBetween(headerName, "[", "]");

				if (attribute != null) {

					headerName = headerName.substring(0, headerName.indexOf("["));
					
					String headerValue = httpRequest.getHeader(headerName);

					if (headerValue != null) {
						//multiple header is separated by ';'
						String[] keyValues = headerValue.split(";");
						for (String keyValue : keyValues) {
							String[] s = keyValue.split("=", 2);
							if (s.length > 1 && s[0].equals(attribute)) {
								result = s[1];
							}
						}
					}

				} else {
					// single value
					result = httpRequest.getHeader(headerName);

				}

			} catch (Exception e) {
				logger.error("value not exist, " + e.getMessage());
			}

		
		if(logger.isTraceEnabled()) {
			logger.trace("resolved value as:" + result + " under " + parameters.get("parameter"));
		}		
	
		return result;
	}

}
