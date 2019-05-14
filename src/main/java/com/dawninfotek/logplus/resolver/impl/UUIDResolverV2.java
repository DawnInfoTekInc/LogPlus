package com.dawninfotek.logplus.resolver.impl;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.dawninfotek.logplus.core.LogPlusConstants;
import com.dawninfotek.logplus.core.LogPlusContext;
import com.dawninfotek.logplus.resolver.AbstractResolver;

public class UUIDResolverV2 extends AbstractResolver {

	@Override
	protected String resolveValueInternal(HttpServletRequest httpRequest, Map<String, Object> parameters) {		
		String prefix = LogPlusContext.configuration().getConfigurationValue(LogPlusConstants.APPLICATION_NAME);
		String result = prefix + java.util.UUID.randomUUID().toString();
		return result;
	}
}
