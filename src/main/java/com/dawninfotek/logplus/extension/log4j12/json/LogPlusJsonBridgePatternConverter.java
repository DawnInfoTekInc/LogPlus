package com.dawninfotek.logplus.extension.log4j12.json;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.pattern.DatePatternConverter;
import org.apache.log4j.pattern.LoggingEventPatternConverter;
import org.apache.log4j.spi.LoggingEvent;

import com.dawninfotek.logplus.config.JsonField;
import com.dawninfotek.logplus.config.JsonFieldsConstants;
import com.dawninfotek.logplus.config.LogPlusField;
import com.dawninfotek.logplus.core.LogPlusContext;
import com.dawninfotek.logplus.extension.log4j12.LogPlusBridgePatternConverter;
import com.dawninfotek.logplus.util.LogPlusUtils;
import com.dawninfotek.logplus.util.StringUtils;

public final class LogPlusJsonBridgePatternConverter extends LogPlusBridgePatternConverter {
	
	private Map<String, LoggingEventPatternConverter> logPlusConverters = new HashMap<String, LoggingEventPatternConverter>();

	public static final String C = ",";
	
	public static final String[] LOGPLUS_RESERVED_FIELD_NAMES = {
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
	public LogPlusJsonBridgePatternConverter(final String pattern) {
		
		super(pattern);	
		
		System.out.println("LogPlusJsonBridgePatternConverter(final String pattern) is called ..." );
		//Create the Converter for LogPlus logging Patterns

		String logPlusName = null;
		String name = null;
		
		JsonField dateField = null;
		
		for(JsonField af:LogPlusContext.configuration().getJsonFields()) {
			if(af.getName().equals(JsonFieldsConstants.TIMESTAMP)) {
				dateField = af;
			}
		}
		
		String dateFormat = dateField.getFormat();
		
		for(String mapping:LOGPLUS_RESERVED_FIELD_NAMES) {
			
			String[] ls = mapping.split(":");
			logPlusName = ls[0];
			name = ls[1];
			
			//find the converters
			for(LoggingEventPatternConverter p:patternConverters) {				
				//System.out.println(p.getName() + "::" + p);				
				if(name.equals(p.getName())){
					
					if(dateFormat != null && p.getName().equals("Date")) {
						//need to grate a new instance of dateConverter for special format
						logPlusConverters.put(logPlusName, DatePatternConverter.newInstance(new String[] {dateFormat}));
					}else {					
						logPlusConverters.put(logPlusName, p);
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
		
		for(JsonField field:LogPlusContext.configuration().getJsonFields()) {
			
			if(rsrd.keySet().contains(field.getName())){
				
				value = rsrd.get(field.getName());
				
			}else {			
				//not a reserved field
				//get from logPlus field
				LogPlusField logPlusfield = LogPlusContext.getLogPlusField(field.getName());
				if(logPlusfield != null && logPlusfield.getScope() == LogPlusField.SCOPE_LINE) {					
					//this is a log line scope field, generate value here					
					if(LogPlusUtils.resolveFieldValueRequired(logPlusfield, rsrd.get("LEVEL"))) {
						//need to resolve the value for this log level for this field
						value = LogPlusUtils.resolveFieldValue(logPlusfield, rsrd.get("LOGGER"), rsrd.get("MESSAGE"));
						if(StringUtils.isEmpty(value)) {
							//try resolve from exception body
							value = LogPlusUtils.resolveFieldValue(logPlusfield, rsrd.get("LOGGER"), rsrd.get("EXCEPTION"));
						}
					}else {
						value = null;
					}
					
				}else {
					value = LogPlusUtils.getLogPlusFieldValue(field.getName());
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
		
		Map<String, String> result = new HashMap<String, String>(logPlusConverters.size());
		
		StringBuffer sb = new StringBuffer();
		
		for(String converter:logPlusConverters.keySet()) {
			
			logPlusConverters.get(converter).format(event, sb);
			result.put(converter, sb.toString());
			sb.setLength(0);
		}
		
		return result;
	}

}
