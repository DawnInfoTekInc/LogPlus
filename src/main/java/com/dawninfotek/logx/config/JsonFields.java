package com.dawninfotek.logx.config;

public class JsonFields {
	
	private String Name;
	private String DisplayName;
	private Boolean Display;
	
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
}
