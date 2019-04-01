package com.dawninfotek.logplus.resolver.impl;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dawninfotek.logplus.resolver.AbstractResolver;
import com.dawninfotek.logplus.util.LogPlusUtils;

public class TextSearchResultResolver extends AbstractResolver {

	public static Logger logger = LoggerFactory.getLogger(TextSearchResultResolver.class);

	public static final String LINE_BEGIN = "**LINE_BEGIN**";
	public static final String LINE_END = "**LINE_END**";
	public static final String BETWEEN = "BETWEEN";
	public static final String SUBSTRING = "SUBSTRING";

	@Override
	protected String resolveValueInternal(HttpServletRequest httpRequest, Map<String, Object> parameters) {

		String result = "";

		if (parameters != null) {

			try {

				String searchMode = LogPlusUtils.getLogProperty(parameters.get("fieldName") + "." + parameters.get("package") + ".search",
						null);

				String text = (String) parameters.get("sourceText");

				if (searchMode != null && text != null) {

					String startTag = LogPlusUtils
							.getLogProperty(parameters.get("fieldName") + "." + parameters.get("package") + ".start.tag", null);

					String endTag = LogPlusUtils.getLogProperty(parameters.get("fieldName") + "." + parameters.get("package") + ".end.tag",
							null);

					if (searchMode.equals(BETWEEN)) {
						// between
						if (startTag.equals(LINE_BEGIN) && endTag.equals(LINE_END)) {
							result = text;
						} else if (startTag.equals(LINE_BEGIN)) {
							result = StringUtils.substringBetween(startTag + text, startTag, endTag);
						} else if (endTag.equals(LINE_END)) {
							result = StringUtils.substringBetween(text + endTag, startTag, endTag);
						} else {
							result = StringUtils.substringBetween(text, startTag, endTag);
						}

					} else if (searchMode.equals(SUBSTRING)) {
						// substring
						result = text.substring(Integer.valueOf(startTag).intValue(), Integer.valueOf(endTag).intValue());

					}

				}

			} catch (Exception ignored) {
				logger.info("fail to get value for text search", ignored);
			}
		}

		return result;
	}

}
