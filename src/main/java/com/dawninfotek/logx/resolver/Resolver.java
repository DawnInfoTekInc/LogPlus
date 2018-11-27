package com.dawninfotek.logx.resolver;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.dawninfotek.logx.core.Component;

public interface Resolver extends Component {
	
	public static final String PARAMETERS = "parameter";
	
	String resolveValue(HttpServletRequest httpRequest, Map<String, Object> parameters);

}

