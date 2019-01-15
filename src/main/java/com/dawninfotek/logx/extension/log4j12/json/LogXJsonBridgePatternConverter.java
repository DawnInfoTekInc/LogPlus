package com.dawninfotek.logx.extension.log4j12.json;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.pattern.DatePatternConverter;
import org.apache.log4j.pattern.LoggingEventPatternConverter;
import org.apache.log4j.spi.LoggingEvent;

import com.dawninfotek.logx.config.JsonField;
import com.dawninfotek.logx.config.JsonFieldsConstants;
import com.dawninfotek.logx.config.LogXField;
import com.dawninfotek.logx.core.LogXContext;
import com.dawninfotek.logx.extension.log4j12.LogXBridgePatternConverter;
import com.dawninfotek.logx.util.LogXUtils;

public final class LogXJsonBridgePatternConverter extends LogXBridgePatternConverter {
	
	private Map<String, LoggingEventPatternConverter> logxConverters = new HashMap<String, LoggingEventPatternConverter>();
	
	public static final String C = ",";
	
	public static final String[] LOGX_RESERVED_FIELD_NAMES = {
			"TIMESTAMP:Date",
			"THREAD:Thread",
			"LOGGER:Logger",
			"LEVEL:Level",
			"MESSAGE:Message",
			"EXCEPTION:Throwable",
			"METHOD:Method",
			"LINE:Line"
	};

	/**
	 * Create a new instance.
	 * 
	 * @param pattern
	 *            pattern, may not be null.
	 */
	public LogXJsonBridgePatternConverter(final String pattern) {
		
		super(pattern);	
		
		System.out.println("LogXJsonBridgePatternConverter(final String pattern) is called ..." );
		//Create the Converter for LogX logging Patterns

		String logXName = null;
		String name = null;
		
		JsonField dateField = null;
		
		for(JsonField af:LogXContext.configuration().getJsonFields()) {
			if(af.getName().equals(JsonFieldsConstants.TIMESTAMP)) {
				dateField = af;
			}
		}
		
		String dateFormat = dateField.getFormat();
		
		for(String mapping:LOGX_RESERVED_FIELD_NAMES) {
			
			String[] ls = mapping.split(":");
			logXName = ls[0];
			name = ls[1];
			
			//find the converters
			for(LoggingEventPatternConverter p:patternConverters) {				
				//System.out.println(p.getName() + "::" + p);				
				if(name.equals(p.getName())){
					
					if(dateFormat != null && p.getName().equals("Date")) {
						//need to grate a new instance of dateConverter for special format
						logxConverters.put(logXName, DatePatternConverter.newInstance(new String[] {dateFormat}));
					}else {					
						logxConverters.put(logXName, p);
					}
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
	public void format(final StringBuffer sbuf, final LoggingEvent event) {			

		String value = null;
		String dsp = null;
		boolean begin = true;
		
		//resolve all reserved values
		Map<String, String> rsrd = resolveReservedValues(event);
		
		sbuf.append("{");
		
		for(JsonField field:LogXContext.configuration().getJsonFields()) {
			
			if(rsrd.keySet().contains(field.getName())){
				
				value = rsrd.get(field.getName());
				
			}else {			
				//not a reserved field
				//get from logX field
				LogXField logXfield = LogXContext.getLogXField(field.getName());
				if(logXfield != null && logXfield.getScope() == LogXField.SCOPE_LINE) {					
					//this is a log line scope field, generate value here					
					if(LogXUtils.resolveFieldValueRequired(logXfield, rsrd.get("LEVEL"))) {
						//need to resolve the value for this log level for this field
						value = LogXUtils.resolveFieldValue(logXfield, rsrd.get("LOGGER"), rsrd.get("MESSAGE"));
						if(StringUtils.isEmpty(value)) {
							//try resolve from exception body
							value = LogXUtils.resolveFieldValue(logXfield, rsrd.get("LOGGER"), rsrd.get("EXCEPTION"));
						}
					}else {
						value = null;
					}
					
				}else {
					value = LogXUtils.getLogXFieldValue(field.getName(), false);
				}
			}
			
			dsp = field.toDisplayText(value);
			
			if(StringUtils.isEmpty(dsp)) {
				continue;
			}
			
			if(!begin) {
				sbuf.append(C);				
			}else {
				begin = false;
			}
			
			sbuf.append(dsp);
			
		}
		
		sbuf.append("}\n");	

	}
	
	private Map<String, String> resolveReservedValues(LoggingEvent event){
		
		Map<String, String> result = new HashMap<String, String>(logxConverters.size());
		
		StringBuffer sb = new StringBuffer();
		
		for(String converter:logxConverters.keySet()) {
			
			logxConverters.get(converter).format(event, sb);
			result.put(converter, sb.toString());
			sb.setLength(0);
		}
		
		return result;
	}

}
