package com.dawninfotek.logplus.resolver.impl;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dawninfotek.logplus.resolver.AbstractResolver;
import com.dawninfotek.logplus.util.LogPlusUtils;

public class ALIASResolver extends AbstractResolver {
	
	public static Logger logger = LoggerFactory.getLogger(ALIASResolver.class);
	
	@Override
	public String resolveValue(HttpServletRequest httpRequest, Map<String, Object> parameters) {
		String value = (String) parameters.get(PARAMETERS);
		String result = LogPlusUtils.getLogPlusFieldValue(value, false);
		if(logger.isTraceEnabled()) {
			logger.trace("resolved value as:" + result);
		}
		return result;
	}

}
