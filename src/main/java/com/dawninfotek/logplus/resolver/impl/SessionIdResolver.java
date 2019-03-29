package com.dawninfotek.logplus.resolver.impl;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dawninfotek.logplus.resolver.AbstractResolver;

public class SessionIdResolver extends AbstractResolver {
	
	public static Logger logger = LoggerFactory.getLogger(SessionIdResolver.class);

	@Override
	protected String resolveValueInternal(HttpServletRequest httpRequest, Map<String, Object> parameters) {
		
		String result = "";
		HttpSession session = httpRequest.getSession(false);
		if(session != null) {
			result = session.getId();
		}		

		return result;
	}

}
