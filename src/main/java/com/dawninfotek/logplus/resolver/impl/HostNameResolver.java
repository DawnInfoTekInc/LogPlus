package com.dawninfotek.logplus.resolver.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dawninfotek.logplus.resolver.AbstractResolver;

public class HostNameResolver extends AbstractResolver {

	public static Logger logger = LoggerFactory.getLogger(HostNameResolver.class);

	@Override
	public String resolveValue(HttpServletRequest httpRequest, Map<String, Object> parameters) {

		String result = "";	

		try {
			if(parameters != null && "full".equals(parameters.get(PROCESS_ID))) {
				result = InetAddress.getLocalHost().getCanonicalHostName();
			}else {
				result = InetAddress.getLocalHost().getHostName();
			}
			
		} catch (UnknownHostException ignored) {
			logger.warn("fail to get hostname", ignored);
		}

		if (logger.isTraceEnabled()) {
			logger.trace("resolved value as:" + result);
		}
		return result;
	}

}
