package com.dawninfotek.logplus.resolver.impl;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dawninfotek.logplus.resolver.AbstractResolver;
import com.dawninfotek.logplus.util.BeanUtils;

public class SessionValueResolver extends AbstractResolver {

	public static Logger logger = LoggerFactory.getLogger(SessionValueResolver.class);

	@Override
	protected String resolveValueInternal(HttpServletRequest httpRequest, Map<String, Object> parameters) {
		String result = "";
		if (parameters == null) {
			logger.warn("No Session object is defined.");
		} else {
			String[] keyAndPath = ((String) parameters.get(PARAMETERS)).split("\\.", 2);
			Object sessionObj = null;
			HttpSession session = httpRequest.getSession(false);

			if (session != null) {
				sessionObj = session.getAttribute(keyAndPath[0]);
			}

			if (sessionObj != null) {
				if (keyAndPath.length == 1) {
					result = sessionObj.toString();
				} else {
					try {
						result = (String) BeanUtils.getObject(sessionObj, keyAndPath[1]);
					} catch (Exception e) {
						logger.warn("Fail to resolve session object value", e);
					}
				}

			} else {
				if(logger.isDebugEnabled()) {
					logger.debug("Session object not found under key:" + keyAndPath[0]);
				}

			}
		}

		return result;
	}

}
