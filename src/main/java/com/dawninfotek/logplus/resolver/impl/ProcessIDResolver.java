package com.dawninfotek.logplus.resolver.impl;

import java.lang.management.ManagementFactory;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.dawninfotek.logplus.resolver.AbstractResolver;

public class ProcessIDResolver extends AbstractResolver {
	
	@Override
	protected String resolveValueInternal(HttpServletRequest httpRequest, Map<String, Object> parameters) {
		String processId = ManagementFactory.getRuntimeMXBean().getName();
		return processId;
	}

}
