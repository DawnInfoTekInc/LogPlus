package com.dawninfotek.logplus.resolver.impl;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.dawninfotek.logplus.resolver.AbstractResolver;

public class UUIDResolver extends AbstractResolver {

	@Override
	protected String resolveValueInternal(HttpServletRequest httpRequest, Map<String, Object> parameters) {		
		String result = java.util.UUID.randomUUID().toString();

		return result;
	}

}
