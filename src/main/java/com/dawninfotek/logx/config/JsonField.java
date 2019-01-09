package com.dawninfotek.logx.config;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.MDC;

public class JsonField {
	
	public static final String EMPT = "";
	public static final String DQ = "\"";
	public static final String C = ":";
	public static final String X = "X";	
	
	public static final String DefaultTimestampFormat = "yyyy-MM-dd HH:mm:ss.SSS zzz";
	
	private String name;
	private String displayName;
	private boolean display;
	private String format;
	private String displayValue;
	
	public JsonField() {
		
	}
	
	public JsonField(String name, String displayname, Boolean display, String format){
		this.name = name;
		this.displayName = displayname;
		this.display = display;
		this.format = format;
	}
	
	public JsonField cloneFromTemplate(String displayValue) {
		JsonField clone = new JsonField();
		clone.setDisplay(this.display);
		clone.setName(this.name);
		clone.setFormat(this.format);
		clone.setDisplayName(displayName);
		clone.setDisplayValue(displayValue);
		
		return clone;
	}
	
	/**
	 * Return the String in json format
	 */
	public String toDisplayText() {
		
		String result = null;
				
		if(StringUtils.isEmpty(displayValue)) {			
			if(display) {
				result = new StringBuilder().append(DQ).append(displayName).append(DQ).append(C).append(DQ).append(DQ).toString();				
			}else {
				result = EMPT;
			}
		}else {
			
			result = new StringBuilder().append(DQ).append(displayName).append(DQ).append(C).append(DQ).append(displayValue).append(DQ).toString();			
		}
		
		return result;
		
	}
	
	public String getDisplayValue() {
		return displayValue;
	}

	public void setDisplayValue(String displayValue) {
		
		if(displayValue != null && format != null && format.startsWith(X) && StringUtils.isNumeric(format.substring(1))){			
			int l = Integer.valueOf(format.substring(1)).intValue();
			if(displayValue.length() > l) {
				this.displayValue = displayValue.substring(0, l);
			}else {
				this.displayValue = displayValue;
			}
		}else {
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
	
	public void setDisplayName(String name) {
		this.displayName = name;
	}
	
	public String getDisplayName() {
		return this.displayName;
	}
	
	public void setDisplay(boolean display) {
		this.display = display;
	}
	
	public boolean getDisplay() {
		return this.display;
	}
	
	public static JsonField createField(String field){
		String key = field;
		if(key.indexOf("[") > 0) {
			key = key.substring(0, key.indexOf("["));
		}
		JsonField newField = new JsonField(key, key, false, "");
		while(field.indexOf("[") >= 0) {
			String custom = field.substring(field.indexOf("[") + 1, field.indexOf("]"));
			field = field.substring(field.indexOf("]")+1);
			if(custom.startsWith("name")) {
				newField.setDisplayName(custom.substring(custom.indexOf("=")+1));
			}
			if(custom.startsWith("mandatory")) {
				if(custom.substring(custom.indexOf("=")+1).equals("true")) {
					newField.setDisplay(true);
				}
			}
			if(custom.startsWith("format")) {
				newField.setFormat(custom.substring(custom.indexOf("=")+1));
			}
		}
		return newField;
	}
	
	public static String getFromMDC(String value) {
		String result = MDC.get(value);
		if(result == null) {
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
		if(format.isEmpty()) {
			value = TimestampToString(millionSeconds, DefaultTimestampFormat);
		} else {
			value = TimestampToString(millionSeconds, format);
		}
		return value;
	}
	

    public static String convertMapToJsonString(Map<String, String> map) {
    	String jsonString = "{";
    	for(Map.Entry<String, String> entry: map.entrySet()) {
    		jsonString += "\"" + entry.getKey() + "\": \"" + entry.getValue() + "\", ";
    	}
    	// remove last comma
    	if(!jsonString.equals("{")) {      
    		jsonString = jsonString.substring(0, jsonString.length() - 2);
    	}
    	jsonString += "}";
    	return jsonString;
    }
}
