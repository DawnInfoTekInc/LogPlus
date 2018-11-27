package com.dawninfotek.logx.extension.log4j12;

import org.apache.log4j.pattern.LoggingEventPatternConverter;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Return the event's rendered message in a StringBuffer.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public final class LogXMessagePatternConverter extends LoggingEventPatternConverter {
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
		
		String msg = event.getRenderedMessage();
		
		//remove all '\n' '\' in the message String
		msg = msg.replaceAll("\\t", "");
		msg = msg.replaceAll("\\n", "    ");
		
		toAppendTo.append(msg);
	}

}