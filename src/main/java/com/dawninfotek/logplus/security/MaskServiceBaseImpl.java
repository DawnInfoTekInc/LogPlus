package com.dawninfotek.logplus.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dawninfotek.logplus.util.StringUtils;

public class MaskServiceBaseImpl implements MaskService {
	
	public static Logger logger = LoggerFactory.getLogger(MaskServiceBaseImpl.class);

	@Override
	public String mask(String source, String pattern) {
		
		if(pattern == null || pattern.isEmpty() || source == null || source.isEmpty()){
			return source;
		}
		StringBuilder sourceString = new StringBuilder(source);
		String[] patterns = pattern.split("&");
		for(String one: patterns){
			String muskChar = StringUtils.substringBetween(one, "[", "]");
			String numMusk = StringUtils.substringBetween(one, "{", "}");
			String numRange = StringUtils.substringBetween(one, "(", ")");
			if(one.toLowerCase().startsWith("all")){
				if(muskChar != null){
					return sourceString.replace(0, source.length(), StringUtils.repeat(muskChar, source.length())).toString();
				}
				return sourceString.replace(0, source.length(), StringUtils.repeat("*", source.length())).toString();
			}
			else if(one.startsWith("^")) {
				if(numMusk != null){
					if(muskChar != null){
						sourceString.replace(0, Integer.parseInt(numMusk), StringUtils.repeat(muskChar, Integer.parseInt(numMusk)));
					}
					else sourceString.replace(0, Integer.parseInt(numMusk), StringUtils.repeat("*", Integer.parseInt(numMusk)));
				}
			}else if(one.startsWith("$")){
				if(numMusk != null){
					if(muskChar != null){
						sourceString.replace(source.length()-Integer.parseInt(numMusk), source.length(), StringUtils.repeat(muskChar, Integer.parseInt(numMusk)));
					}
					else sourceString.replace(source.length()-Integer.parseInt(numMusk), source.length(), StringUtils.repeat("*", Integer.parseInt(numMusk)));
				}
			}
			if(numRange != null){
				String[] range = numRange.split("-");
				if(range.length == 2){
					if(muskChar != null){
						sourceString.replace(Integer.parseInt(range[0]), Integer.parseInt(range[1]), StringUtils.repeat(muskChar, Integer.parseInt(range[1]) - Integer.parseInt(range[0])));
					}
					else sourceString.replace(Integer.parseInt(range[0]), Integer.parseInt(range[1]), StringUtils.repeat("*", Integer.parseInt(range[1]) - Integer.parseInt(range[0])));
				}else {
					logger.warn("incorrect range format " + one);
				}
			}
		}
		return sourceString.toString();
	}

}
