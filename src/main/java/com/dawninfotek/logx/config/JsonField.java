package com.dawninfotek.logx.config;

import org.apache.commons.lang.StringUtils;

public class JsonField {
	
	public static final String EMPT = "";
	public static final String DQ = "\"";
	public static final String C = ":";
	public static final String X = "X";	
	
	private String name;
	private String displayName;
	private boolean display;
	private String format;
	private String displayValue;
	
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
}
