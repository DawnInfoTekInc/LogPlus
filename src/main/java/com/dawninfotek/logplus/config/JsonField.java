package com.dawninfotek.logplus.config;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import com.dawninfotek.logplus.util.LogPlusUtils;
import com.dawninfotek.logplus.util.StringUtils;

public class JsonField implements Comparable<JsonField> {

	public static final String EMPT = "";
	public static final String DQ = "\"";
	public static final String C = ":";
	public static final String X = "X";

	public static final String DefaultTimestampFormat = "yyyy-MM-dd HH:mm:ss.SSS zzz";

	private String name;
	private String label;
	private boolean display;
	private String format;
	private String displayValue;
	private boolean enabled;

	public static final int UN_DEFINED = Integer.MAX_VALUE - 1000;
	/**
	 * default value means any position is good for this field.
	 */
	private int position = UN_DEFINED;

	public JsonField() {
	}

	public JsonField(String name, String label, boolean display, String format) {
		this.name = name;
		this.label = label;
		this.display = display;
		this.format = format;
	}

	public JsonField cloneFromTemplate(String displayValue) {
		JsonField clone = new JsonField(this.name, this.label, this.display, this.format);
		clone.setDisplayValue(displayValue);
		return clone;
	}

	/**
	 * Return the String in json format
	 */
	public String toDisplayText(String displayValue) {

		String result = null;

		if (StringUtils.isEmpty(displayValue)) {
			if (display) {
				result = new StringBuilder().append(DQ).append(label == null ? name : label).append(DQ).append(C)
						.append(DQ).append(DQ).toString();
			} else {
				result = EMPT;
			}
		} else {

			String toDisplay = null;
			if (displayValue != null && format != null && format.startsWith(X)
					&& StringUtils.isNumeric(format.substring(1))) {
				int l = Integer.valueOf(format.substring(1)).intValue();

				if (displayValue.length() > l) {
					toDisplay = displayValue.substring(0, l);
				} else {
					toDisplay = displayValue;
				}
			} else {
				toDisplay = displayValue;
			}

			// need to replace all '"' to '\"'
			toDisplay = StringUtils.replace(toDisplay, "\"", "\\\"");

			result = new StringBuilder().append(DQ).append(label == null ? name : label).append(DQ).append(C).append(DQ)
					.append(toDisplay).append(DQ).toString();
		}
		return result;
	}

	/**
	 * Return the String in json format
	 */
	public String toDisplayText() {

		return toDisplayText(this.displayValue);
	}

	public String getDisplayValue() {
		return displayValue;
	}

	public void setDisplayValue(String displayValue) {

		if (displayValue != null && format != null && format.startsWith(X)
				&& StringUtils.isNumeric(format.substring(1))) {
			int l = Integer.valueOf(format.substring(1)).intValue();
			if (displayValue.length() > l) {
				this.displayValue = displayValue.substring(0, l);
			} else {
				this.displayValue = displayValue;
			}
		} else {
			this.displayValue = displayValue;
		}
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public void setDisplay(boolean display) {
		this.display = display;
	}

	public boolean isDisplay() {
		return display;
	}


	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	
	public static JsonField fromString(String field) {
		String key = field;
		if (key.indexOf("[") > 0) {
			key = key.substring(0, key.indexOf("["));
		}
		JsonField newField = new JsonField(key, key, false, null);
	
		String[] defs = StringUtils.substringsBetween(field, "[", "]");

		if (defs != null && defs.length > 0) {
			String defKey = null;
			String value = null;

			for (String def : defs) {
				String[] nv = def.split("=");
				if (nv.length == 2) {
					defKey = nv[0];
					value = nv[1];
					if (defKey.equals("name") || defKey.equals("n")) {
						newField.setName(value);
					} else if (defKey.equals("mandatory") || defKey.equals("m")) {
						if (value.equalsIgnoreCase("true")) {
							newField.setDisplay(true);
						}
					}else if(defKey.equals("label") || defKey.equals("l")) {
						newField.setLabel(value);
					} else if (defKey.equals("format") || defKey.equals("f")) {
						newField.setFormat(value);
					} else if (defKey.equals("position") || defKey.equals("p")) {
						if (value.equalsIgnoreCase("first")) {
							newField.setPosition(-1);
						} else if (value.equalsIgnoreCase("last")) {
							newField.setPosition(Integer.MAX_VALUE-1);
						} else {
							try {
								newField.setPosition(Integer.valueOf(value).intValue());
							} catch (Exception ignored) {
								// do nothing
							}
						}
					}
				}
			}
		}
		return newField;
	}

	public static String getFromMDC(String value) {
		String result = LogPlusUtils.getLogPlusFieldValue(value);
		if (result == null) {
			return "";
		}
		return result;
	}

	public static String TimestampToString(long millseconds, String timeFormat) {
		SimpleDateFormat formatter = new SimpleDateFormat(timeFormat);
		return formatter.format(new Date(millseconds)).toString();
	}

	public static String getTimestampValue(long millionSeconds, String format) {
		String value = "";
		if (format == null || format.isEmpty()) {
			value = TimestampToString(millionSeconds, DefaultTimestampFormat);
		} else {
			value = TimestampToString(millionSeconds, format);
		}
		return value;
	}

	public static String convertMapToJsonString(Map<String, String> map) {
		String jsonString = "{";
		for (Map.Entry<String, String> entry : map.entrySet()) {
			jsonString += "\"" + entry.getKey() + "\": \"" + entry.getValue() + "\", ";
		}
		// remove last comma
		if (!jsonString.equals("{")) {
			jsonString = jsonString.substring(0, jsonString.length() - 2);
		}
		jsonString += "}\n";
		return jsonString;
	}

	public static String replaceAllNewline(String msg) {
		return msg.replaceAll("(\\r|\\n|\\t)", "");
	}

	public static String stackTraceToString(Exception e) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		e.printStackTrace(printWriter);
		return stringWriter.toString();
	}

	@Override
	public int compareTo(JsonField arg0) {
		return this.position - arg0.getPosition();
	}

	public String toString() {
		StringBuffer sb = new StringBuffer("{");
		sb.append("\"name\":\"").append(getName()).append("\"").append(",");
		sb.append("\"lable\":\"").append(getLabel()).append("\"").append(",");
		sb.append("\"display\":").append(isDisplay()).append(",");
		sb.append("\"format\":\"").append(getFormat()).append("\"").append(",");
		sb.append("\"position\":").append(getPosition()).append("}");

		return sb.toString();
	}
}
