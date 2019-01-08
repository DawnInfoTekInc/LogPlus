package com.dawninfotek.logx.config;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.MDC;


public class JsonFields {
	
	private String Name;
	private String DisplayName;
	private Boolean Display;
	private String Format;
	
	public static String DefaultTimestampFormat = "yyyy-MM-dd HH:mm:ss.SSS zzz";
	
	public JsonFields(String name, String displayname, Boolean display, String format){
		this.Name = name;
		this.DisplayName = displayname;
		this.Display = display;
		this.Format = format;
	}
	
	public void setName(String name) {
		this.Name = name;
	}
	
	public String getName() {
		return this.Name;
	}
	
	public void setDisplayName(String name) {
		this.DisplayName = name;
	}
	
	public String getDisplayName() {
		return this.DisplayName;
	}
	
	public void setDisplay(Boolean display) {
		this.Display = display;
	}
	
	public Boolean getDisplay() {
		return this.Display;
	}
	
	public void setFormat(String format) {
		this.Format = format;
	}
	
	public String getFormat() {
		return this.Format;
	}
	
	public static JsonFields createField(String field){
		String key = field;
		if(key.indexOf("[") > 0) {
			key = key.substring(0, key.indexOf("["));
		}
		JsonFields newField = new JsonFields(key, key, false, "");
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
			value = JsonFields.TimestampToString(millionSeconds, DefaultTimestampFormat);
		} else {
			value = JsonFields.TimestampToString(millionSeconds, format);
		}
		return value;
	}
}
