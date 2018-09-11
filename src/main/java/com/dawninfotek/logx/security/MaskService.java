package com.dawninfotek.logx.security;

import com.dawninfotek.logx.core.Component;

public interface MaskService extends Component {
	
	/**
	 * Mask source with specific pattern
	 * @param source source string
	 * @param pattern pattern string
	 * @return masked string
	 */
	String mask(String source, String pattern);

}
