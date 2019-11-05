package com.dawninfotek.logplus.extension.log4j12.json;

public class LogPlusJsonBridgePatternParser extends org.apache.log4j.helpers.PatternParser {
	
	  /**
	   * Create a new instance.
	   * @param conversionPattern pattern, may not be null.
	   */
	  public LogPlusJsonBridgePatternParser(
	    final String conversionPattern) {
	    super(conversionPattern);
	  }

	  /**
	   * Create new pattern converter.
	   * @return pattern converter.
	   */
	  public org.apache.log4j.helpers.PatternConverter parse() {
		  
		  return new LogPlusJsonBridgePatternConverter(pattern);
	  }


}
