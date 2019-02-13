package com.dawninfotek.logplus.event;

import com.dawninfotek.logplus.util.LogPlusUtils;

public class EventServiceBaseImpl implements EventService {

	@Override
	public void logEventBegin(String eventName, String eventType, Object logger) {
		
		String message = LogPlusUtils.getLogProperty(LOG_MSG_EVENT_BEGIN, "");
		LogPlusUtils.logTextMessage(logger, String.format(message, eventType, eventName));

	}

	@Override
	public void logEventEnd(String eventName, String eventType, Object logger) {
		String message = LogPlusUtils.getLogProperty(LOG_MSG_EVENT_END, "");
		LogPlusUtils.logTextMessage(logger, String.format(message, eventType, eventName));

	}

	@Override
	public void logServiceEventBegin(String eventName, Object logger) {		
		String message = LogPlusUtils.getLogProperty(LOG_MSG_EVENT_BEGIN, "");
		LogPlusUtils.logTextMessage(logger, String.format(message, LogPlusUtils.getLogProperty(TYPE_SRV, ""), eventName));

	}

	@Override
	public void logServiceEventEnd(String eventName, Object logger) {
		String message = LogPlusUtils.getLogProperty(LOG_MSG_EVENT_END, "");
		LogPlusUtils.logTextMessage(logger, String.format(message, LogPlusUtils.getLogProperty(TYPE_SRV, ""), eventName));

	}

	@Override
	public void logBusinessEventBegin(String eventName, Object logger) {
		String message = LogPlusUtils.getLogProperty(LOG_MSG_EVENT_BEGIN, "");
		LogPlusUtils.logTextMessage(logger, String.format(message, LogPlusUtils.getLogProperty(TYPE_B, ""), eventName));

	}

	@Override
	public void logBusinessEventEnd(String eventName, Object logger) {
		String message = LogPlusUtils.getLogProperty(LOG_MSG_EVENT_END, "");
		LogPlusUtils.logTextMessage(logger, String.format(message, LogPlusUtils.getLogProperty(TYPE_B, ""), eventName));

	}

	@Override
	public void logApplicationStateChangeEventBegin(String eventName, Object logger) {
		String message = LogPlusUtils.getLogProperty(LOG_MSG_EVENT_BEGIN, "");
		LogPlusUtils.logTextMessage(logger, String.format(message, LogPlusUtils.getLogProperty(TYPE_A_S_C, ""), eventName));

	}

	@Override
	public void logApplicationStateChangeEventEnd(String eventName, Object logger) {
		String message = LogPlusUtils.getLogProperty(LOG_MSG_EVENT_END, "");
		LogPlusUtils.logTextMessage(logger, String.format(message, LogPlusUtils.getLogProperty(TYPE_A_S_C, ""), eventName));

	}

	@Override
	public void logAbnormalEventBegin(String eventName, Object logger) {
		String message = LogPlusUtils.getLogProperty(LOG_MSG_EVENT_BEGIN, "");
		LogPlusUtils.logTextMessage(logger, String.format(message, LogPlusUtils.getLogProperty(TYPE_A_C, ""), eventName));
	}

	@Override
	public void logAbnormalEventEnd(String eventName, Object logger) {
		String message = LogPlusUtils.getLogProperty(LOG_MSG_EVENT_END, "");
		LogPlusUtils.logTextMessage(logger, String.format(message, LogPlusUtils.getLogProperty(TYPE_A_C, ""), eventName));

	}

}
