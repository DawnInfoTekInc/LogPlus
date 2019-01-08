package com.dawninfotek.logx.extension.log4j12.json;

import org.apache.log4j.spi.LoggingEvent;

import com.dawninfotek.logx.extension.log4j12.LogXBridgePatternConverter;

public final class LogXJsonBridgePatternConverter extends LogXBridgePatternConverter {

	/**
	 * Create a new instance.
	 * 
	 * @param pattern
	 *            pattern, may not be null.
	 */
	public LogXJsonBridgePatternConverter(final String pattern) {
		
		super(pattern);	
		
		//Create the Converter for LogX logging Patterns
		patternConverters
		
	}


	/**
	 * Format event to string buffer.
	 * 
	 * @param sbuf
	 *            string buffer to receive formatted event, may not be null.
	 * @param e
	 *            event to format, may not be null.
	 */
	public void format(final StringBuffer sbuf, final LoggingEvent e) {
		System.out.println("===============================================================");		
		System.out.println(this);
		System.out.println("===============================================================");
		
		sbuf.append("{\"timestap\": \"2019-01-08 14:22:44.855 EST\", \"logLevel\": \" INFO\", \"hostname\":\"abc\"}\n");
		/**
		for (int i = 0; i < patternConverters.length; i++) {
			int startField = sbuf.length();
			
			System.out.println(patternConverters[i]);
			
			patternConverters[i].format(e, sbuf);
			patternFields[i].format(startField, sbuf);
		}
		
		*/
	}


}
