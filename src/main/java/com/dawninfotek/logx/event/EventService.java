package com.dawninfotek.logx.event;

import com.dawninfotek.logx.core.Component;

public interface EventService extends Component {
	
	/**
	 * start event log
	 * @param eventName string
	 * @param eventType five types from constants file, can add more type to LogXConstants config file
	 * @param logger
	 */
	void logEventBegin(String eventName, String eventType, Object logger);
	
	/**
	 * end event log, must match start event type
	 * @param eventName
	 * @param eventType
	 * @param logger
	 */
	void logEventEnd(String eventName, String eventType, Object logger);
	
	/**
	 * start transaction event log
	 * @param eventName event string
	 * @param logger
	 */
	void logServiceEventBegin(String eventName, Object logger);
	
	/**
	 * end transaction event log
	 * @param eventName
	 * @param logger
	 */
	void logServiceEventEnd(String eventName, Object logger);
	
	/**
	 * start business log
	 * @param eventName
	 * @param logger
	 */
	void logBusinessEventBegin(String eventName, Object logger);
	
	/**
	 * end business log
	 * @param eventName
	 * @param logger
	 */
	void logBusinessEventEnd(String eventName, Object logger);
	
	/**
	 * Application Startup Event log
	 * @param eventName
	 * @param logger
	 */
	void logApplicationStateChangeEventBegin(String eventName, Object logger);
	
	/**
	 * Application shutdown Event log
	 * @param eventName
	 * @param logger
	 */
	void logApplicationStateChangeEventEnd(String eventName, Object logger);	

	/**
	 * start abnormal log
	 * @param eventName
	 * @param logger
	 */
	void logAbnormalEventBegin(String eventName, Object logger);
	/**
	 * end abnormal log
	 * @param eventName
	 * @param logger
	 */
	void logAbnormalEventEnd(String eventName, Object logger);

}
