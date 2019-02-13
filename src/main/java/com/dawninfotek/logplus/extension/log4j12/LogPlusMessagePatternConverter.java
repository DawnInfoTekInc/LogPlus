package com.dawninfotek.logplus.extension.log4j12;

import org.apache.log4j.pattern.LoggingEventPatternConverter;
import org.apache.log4j.spi.LoggingEvent;

import com.dawninfotek.logplus.util.LogPlusUtils;

/**
 * Return the event's rendered message in a StringBuffer.
 *
 * @author John Li;
 */
public final class LogPlusMessagePatternConverter extends LoggingEventPatternConverter {
	
	
	private  Boolean VERACODE_REQUIRED = null;
	/**
	 * Singleton.
	 */
	private static final LogPlusMessagePatternConverter INSTANCE = new LogPlusMessagePatternConverter();

	/**
	 * Private constructor.
	 */
	private LogPlusMessagePatternConverter() {
		super("Message", "message");
	}

	/**
	 * Obtains an instance of pattern converter.
	 * 
	 * @param options
	 *            options, may be null.
	 * @return instance of pattern converter.
	 */
	public static LogPlusMessagePatternConverter newInstance(final String[] options) {
		return INSTANCE;
	}

	/**
	 * {@inheritDoc}
	 */
	public void format(final LoggingEvent event, final StringBuffer toAppendTo) {
		
		if(VERACODE_REQUIRED == null) {
			VERACODE_REQUIRED = Boolean.valueOf(LogPlusUtils.getLogProperty("veracode.scan.required", "false"));
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
