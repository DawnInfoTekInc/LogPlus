package com.dawninfotek.logx.resolver.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dawninfotek.logx.resolver.AbstractResolver;

public class HostNameResolver extends AbstractResolver {

	public static Logger logger = LoggerFactory.getLogger(HostNameResolver.class);

	@Override
	public String resolveValue(HttpServletRequest httpRequest, Map<String, Object> parameters) {

		String result = "";	

		try {
			result = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException ignored) {
			logger.warn("fail to get hostname", ignored);
		}

		if (logger.isTraceEnabled()) {
			logger.trace("resolved value as:" + result);
		}
		return result;
	}

}
