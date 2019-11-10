package com.dawninfotek.logplus.resolver.impl;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dawninfotek.logplus.resolver.AbstractResolver;
import com.dawninfotek.logplus.util.StringUtils;

public class RequestHeaderResolver extends AbstractResolver {

	public static Logger logger = LoggerFactory.getLogger(RequestHeaderResolver.class);

	@Override
	protected String resolveValueInternal(HttpServletRequest httpRequest, Map<String, Object> parameters) {

		String result = "";

		String value = (String) parameters.get(PARAMETERS);
		if (logger.isTraceEnabled()) {
			logger.trace("header retrieve value:" + value);
		}

		try {

			String headerName = value;

			String attribute = StringUtils.substringBetween(headerName, "[", "]");

			if (attribute != null) {

				headerName = headerName.substring(0, headerName.indexOf("["));

				String headerValue = httpRequest.getHeader(headerName);

				if (headerValue != null) {
					// multiple header is separated by ';'
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
			logger.warn("value not exist, " + e.getMessage());
		}

		return result;
	}

}
