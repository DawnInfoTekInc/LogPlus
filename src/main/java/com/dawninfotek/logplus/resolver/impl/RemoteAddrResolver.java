package com.dawninfotek.logplus.resolver.impl;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dawninfotek.logplus.resolver.AbstractResolver;

public class RemoteAddrResolver extends AbstractResolver {
	
	public static Logger logger = LoggerFactory.getLogger(RemoteAddrResolver.class);

	@Override
	protected String resolveValueInternal(HttpServletRequest httpRequest, Map<String, Object> parameters) {
		
		String result = httpRequest.getHeader("X-Forwarded-For");
		
		if (result == null || result.length() == 0 || result.equalsIgnoreCase("unknown")) {
			result = httpRequest.getHeader("Proxy-Client-IP");
		}
		if (result == null || result.length() == 0 || result.equalsIgnoreCase("unknown")) {
			result = httpRequest.getHeader("WL-Proxy-Client-IP");
		}
		if (result == null || result.length() == 0 || result.equalsIgnoreCase("unknown")) {
			result = httpRequest.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (result == null || result.length() == 0 || result.equalsIgnoreCase("unknown")) {
			result = httpRequest.getHeader("HTTP_X_FORWARDED");
		}
		if (result == null || result.length() == 0 || result.equalsIgnoreCase("unknown")) {
			result = httpRequest.getHeader("HTTP_X_CLUSTER_CLIENT_IP");
		}
		if (result == null || result.length() == 0 || result.equalsIgnoreCase("unknown")) {
			result = httpRequest.getHeader("HTTP_CLIENT_IP");
		}
		if (result == null || result.length() == 0 || result.equalsIgnoreCase("unknown")) {
			result = httpRequest.getHeader("HTTP_FORWARDED_FOR");
		}
		if (result == null || result.length() == 0 || result.equalsIgnoreCase("unknown")) {
			result = httpRequest.getHeader("HTTP_FORWARDED");
		}
		if (result == null || result.length() == 0 || result.equalsIgnoreCase("unknown")) {
			result = httpRequest.getHeader("HTTP_VIA");
		}
		if (result == null || result.length() == 0 || result.equalsIgnoreCase("unknown")) {
			result = httpRequest.getHeader("REMOTE_ADDR");
		}
		if (result == null || result.length() == 0 || result.equalsIgnoreCase("unknown")) {
			result = httpRequest.getRemoteAddr();
		}
		if (result == null) {
			result = "";
		}

		return result;
	}

}
