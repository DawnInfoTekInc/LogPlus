package com.dawninfotek.logplus.resolver;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractResolver implements Resolver {
	
	public static Logger logger = LoggerFactory.getLogger(AbstractResolver.class);
	
	public String resolveValue(HttpServletRequest httpRequest, Map<String, Object> parameters) {
		
		String result = resolveValueInternal(httpRequest, parameters);
		
		if(logger.isDebugEnabled()) {
			logger.debug("resolved value as:" + result + " by Resolver:" + this.getClass() + " with parameters" + parameters);
		}
		
		return result;
		
	}

	protected abstract String resolveValueInternal(HttpServletRequest httpRequest, Map<String, Object> parameters);
	
}
