package com.dawninfotek.logplus.event;

import com.dawninfotek.logplus.util.LogPlusUtils;

public class EventServiceBaseImpl implements EventService {
	
	public static final String defaultLogLevel = "info";
	
	public static final String beginingMessage = LogPlusUtils.getLogProperty(LOG_MSG_EVENT_BEGIN, "");
	public static final String endingMessage = LogPlusUtils.getLogProperty(LOG_MSG_EVENT_END, "");	

	@Override
	public void logEventBegin(String eventName, String eventType, Object logger) {	
		LogPlusUtils.logTextMessage(logger, String.format(beginingMessage, eventType, eventName), LogPlusUtils.getLogProperty(LOG_MSG_EVENT_BEGIN + ".level", defaultLogLevel));

	}

	@Override
	public void logEventEnd(String eventName, String eventType, Object logger) {
		LogPlusUtils.logTextMessage(logger, String.format(endingMessage, eventType, eventName), LogPlusUtils.getLogProperty(LOG_MSG_EVENT_END + ".level", defaultLogLevel));

	}

	@Override
	public void logServiceEventBegin(String eventName, Object logger) {	
		LogPlusUtils.logTextMessage(logger, String.format(beginingMessage, LogPlusUtils.getLogProperty(TYPE_SRV, ""), eventName), LogPlusUtils.getLogProperty(TYPE_SRV + ".level", defaultLogLevel));

	}

	@Override
	public void logServiceEventEnd(String eventName, Object logger) {	
		LogPlusUtils.logTextMessage(logger, String.format(endingMessage, LogPlusUtils.getLogProperty(TYPE_SRV, ""), eventName), LogPlusUtils.getLogProperty(TYPE_SRV + ".level", defaultLogLevel));

	}

	@Override
	public void logBusinessEventBegin(String eventName, Object logger) {	
		LogPlusUtils.logTextMessage(logger, String.format(beginingMessage, LogPlusUtils.getLogProperty(TYPE_B, ""), eventName), LogPlusUtils.getLogProperty(TYPE_B + ".level", defaultLogLevel));

	}

	@Override
	public void logBusinessEventEnd(String eventName, Object logger) {
		LogPlusUtils.logTextMessage(logger, String.format(endingMessage, LogPlusUtils.getLogProperty(TYPE_B, ""), eventName), LogPlusUtils.getLogProperty(TYPE_B + ".level", defaultLogLevel));

	}

	@Override
	public void logApplicationStateChangeEventBegin(String eventName, Object logger) {
		LogPlusUtils.logTextMessage(logger, String.format(beginingMessage, LogPlusUtils.getLogProperty(TYPE_A_S_C, ""), eventName), LogPlusUtils.getLogProperty(TYPE_A_S_C + ".level", defaultLogLevel));

	}

	@Override
	public void logApplicationStateChangeEventEnd(String eventName, Object logger) {
		LogPlusUtils.logTextMessage(logger, String.format(endingMessage, LogPlusUtils.getLogProperty(TYPE_A_S_C, ""), eventName), LogPlusUtils.getLogProperty(TYPE_A_S_C + ".level", defaultLogLevel));

	}

	@Override
	public void logAbnormalEventBegin(String eventName, Object logger) {
		LogPlusUtils.logTextMessage(logger, String.format(beginingMessage, LogPlusUtils.getLogProperty(TYPE_A_C, ""), eventName), LogPlusUtils.getLogProperty(TYPE_A_C + ".level", defaultLogLevel));
	}

	@Override
	public void logAbnormalEventEnd(String eventName, Object logger) {
		LogPlusUtils.logTextMessage(logger, String.format(endingMessage, LogPlusUtils.getLogProperty(TYPE_A_C, ""), eventName), LogPlusUtils.getLogProperty(TYPE_A_C + ".level", defaultLogLevel));

	}

}
