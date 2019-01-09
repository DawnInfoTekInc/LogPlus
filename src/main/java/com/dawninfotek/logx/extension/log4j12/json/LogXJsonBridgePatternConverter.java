package com.dawninfotek.logx.extension.log4j12.json;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.pattern.LoggingEventPatternConverter;
import org.apache.log4j.spi.LoggingEvent;

import com.dawninfotek.logx.config.JsonField;
import com.dawninfotek.logx.core.LogXContext;
import com.dawninfotek.logx.extension.log4j12.LogXBridgePatternConverter;
import com.dawninfotek.logx.util.LogXUtils;

public final class LogXJsonBridgePatternConverter extends LogXBridgePatternConverter {
	
	private Map<String, LoggingEventPatternConverter> logxConverters = new HashMap<String, LoggingEventPatternConverter>();
	
	public static final String[] LOGX_RESERVED_FIELD_NAMES = {
			"TIMESTAMP:Date",
			"THREAD:Thread,",
			"LOGGER:Logger",
			"LEVEL:Level",
			"MESSAGE:Message",
			"EXCEPTION:Throwable"		
	};

	/**
	 * Create a new instance.
	 * 
	 * @param pattern
	 *            pattern, may not be null.
	 */
	public LogXJsonBridgePatternConverter(final String pattern) {
		
		super(pattern);	
		
		//Create the Converter for LogX logging Patterns

		String logXName = null;
		String name = null;
		for(String mapping:LOGX_RESERVED_FIELD_NAMES) {
			
			String[] ls = mapping.split(":");
			logXName = ls[0];
			name = ls[1];
			
			//find the converters
			for(LoggingEventPatternConverter p:patternConverters) {				
				if(name.equals(p.getName())){
					logxConverters.put(logXName, p);
					break;
				}
			}
			
		}
		
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
		
		JsonField[] logFields = new JsonField[LogXContext.configuration().getJsonFields().size()];
		
		JsonField field = null;
		LoggingEventPatternConverter converter = null;
		StringBuffer sb = new StringBuffer();
		
		for(int i=0; i<logFields.length; i++) {
			
			field = LogXContext.configuration().getJsonFields().get(i);
			converter = logxConverters.get(field.getName()); 
			
			if(converter != null){
				//reserved field
				//clear the buffer
				sb.setLength(0);
				converter.format(e, sb);				
				logFields[i] = field.cloneFromTemplate(sb.toString());
				
			}else {
				//not the reserved field
				//get from logX field
				logFields[i] = field.cloneFromTemplate(LogXUtils.getLogXFieldValue(field.getName()));
			}
			
		}		
		
		sbuf.append("{");
		
		boolean begin = true;
		String dsp = null;
		
		for(JsonField logfield:logFields) {
			
			dsp = logfield.toDisplayText();
			if(StringUtils.isEmpty(dsp)) {
				continue;
			}
			if(!begin) {
				sbuf.append(",");				
			}else {
				begin = false;
			}
			sbuf.append(dsp);
			
		}
		
		sbuf.append("}\n");		

	}


}
