package com.dawninfotek.logx.extension.log4j12;

import org.apache.log4j.pattern.LoggingEventPatternConverter;
import org.apache.log4j.spi.LoggingEvent;

import com.dawninfotek.logx.util.LogXUtils;

/**
 * Return the event's rendered message in a StringBuffer.
 *
 * @author John Li;
 */
public final class LogXMessagePatternConverter extends LoggingEventPatternConverter {
	
	
	private  Boolean VERACODE_REQUIRED = null;
	/**
	 * Singleton.
	 */
	private static final LogXMessagePatternConverter INSTANCE = new LogXMessagePatternConverter();

	/**
	 * Private constructor.
	 */
	private LogXMessagePatternConverter() {
		super("Message", "message");
	}

	/**
	 * Obtains an instance of pattern converter.
	 * 
	 * @param options
	 *            options, may be null.
	 * @return instance of pattern converter.
	 */
	public static LogXMessagePatternConverter newInstance(final String[] options) {
		return INSTANCE;
	}

	/**
	 * {@inheritDoc}
	 */
	public void format(final LoggingEvent event, final StringBuffer toAppendTo) {
		
		if(VERACODE_REQUIRED == null) {
			VERACODE_REQUIRED = Boolean.valueOf(LogXUtils.getLogProperty("veracode.scan.required", "false"));
		}
		
		String msg = event.getRenderedMessage();
		
		//remove all '\n' '\' in the message String
		if(VERACODE_REQUIRED) {			
			//for applications need to pass the Veracode scan
			msg = msg.replaceAll("\\t", "").replaceAll("\\r\n", "").replaceAll("\\n", "    ").replaceAll("\\r|%0d|%0D", "    ");
		}else {
			//normal
			msg = msg.replaceAll("\\t", "").replaceAll("\\r\n", "").replaceAll("\\n", "    ");
		}
		
		toAppendTo.append(msg);
	}

}
