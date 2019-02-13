package com.dawninfotek.logplus.security;

import com.dawninfotek.logplus.core.Component;

public interface HashService extends Component {
	
	/**
	 * hash a given text
	 * @param source
	 * @param salt
	 * @return hashed String
	 */
	String hash(String source, String salt);

}
