package com.dawninfotek.logx.resolver.impl;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.dawninfotek.logx.resolver.AbstractResolver;

public class UUIDResolver extends AbstractResolver {
	
	public static Logger logger = LoggerFactory.getLogger(UUIDResolver.class);

	@Override
	public String resolveValue(HttpServletRequest httpRequest, Map<String, Object> parameters) {		
		String result = java.util.UUID.randomUUID().toString();
		if(logger.isTraceEnabled()) {
			logger.trace("resolved value as:" + result);
		}
		return result;
	}

}
