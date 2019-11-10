package com.dawninfotek.logplus.security;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HashServiceBaseImpl implements HashService {
	
	public static Logger logger = LoggerFactory.getLogger(HashServiceBaseImpl.class);

	@Override
	public String hash(String source, String salt) {
		//TODO with salt
		return SHA256(source);
	}
	
	/***
	 * generate SHA256
	 * 
	 * @param string
	 *            need to hide
	 * @return SHA256 string
	 */
	private String SHA256(String string) {
		try {
			MessageDigest sha = MessageDigest.getInstance("SHA-256");
			sha.update(string.getBytes(Charset.forName("UTF8")));
			return String.format("%064x", new BigInteger(1, sha.digest()));
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
		}
		return "";
	}

}
