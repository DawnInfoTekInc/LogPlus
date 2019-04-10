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
		return MD5(source);
	}
	
	/***
	 * generate MD5
	 * 
	 * @param string
	 *            need to hide
	 * @return md5 string
	 */
	private String MD5(String string) {
		try {
			MessageDigest md5 = MessageDigest.getInstance("SHA-256");
			md5.update(string.getBytes(Charset.forName("UTF8")));
			return String.format("%064x", new BigInteger(1, md5.digest()));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return "";
	}

}
