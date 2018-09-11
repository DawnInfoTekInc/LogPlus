package com.dawninfotek.logx.event;

import com.dawninfotek.logx.util.LogXUtils;

public class EventServiceBaseImpl implements EventService {

	@Override
	public void logEventBegin(String eventName, String eventType, Object logger) {
		
		String message = LogXUtils.getLogProperty(LOG_MSG_EVENT_BEGIN, "");
		LogXUtils.logTextMessage(logger, String.format(message, eventType, eventName));

	}

	@Override
	public void logEventEnd(String eventName, String eventType, Object logger) {
		String message = LogXUtils.getLogProperty(LOG_MSG_EVENT_END, "");
		LogXUtils.logTextMessage(logger, String.format(message, eventType, eventName));

	}

	@Override
	public void logServiceEventBegin(String eventName, Object logger) {		
		String message = LogXUtils.getLogProperty(LOG_MSG_EVENT_BEGIN, "");
		LogXUtils.logTextMessage(logger, String.format(message, LogXUtils.getLogProperty(TYPE_SRV, ""), eventName));

	}

	@Override
	public void logServiceEventEnd(String eventName, Object logger) {
		String message = LogXUtils.getLogProperty(LOG_MSG_EVENT_END, "");
		LogXUtils.logTextMessage(logger, String.format(message, LogXUtils.getLogProperty(TYPE_SRV, ""), eventName));

	}

	@Override
	public void logBusinessEventBegin(String eventName, Object logger) {
		String message = LogXUtils.getLogProperty(LOG_MSG_EVENT_BEGIN, "");
		LogXUtils.logTextMessage(logger, String.format(message, LogXUtils.getLogProperty(TYPE_B, ""), eventName));

	}

	@Override
	public void logBusinessEventEnd(String eventName, Object logger) {
		String message = LogXUtils.getLogProperty(LOG_MSG_EVENT_END, "");
		LogXUtils.logTextMessage(logger, String.format(message, LogXUtils.getLogProperty(TYPE_B, ""), eventName));

	}

	@Override
	public void logApplicationStateChangeEventBegin(String eventName, Object logger) {
		String message = LogXUtils.getLogProperty(LOG_MSG_EVENT_BEGIN, "");
		LogXUtils.logTextMessage(logger, String.format(message, LogXUtils.getLogProperty(TYPE_A_S_C, ""), eventName));

	}

	@Override
	public void logApplicationStateChangeEventEnd(String eventName, Object logger) {
		String message = LogXUtils.getLogProperty(LOG_MSG_EVENT_END, "");
		LogXUtils.logTextMessage(logger, String.format(message, LogXUtils.getLogProperty(TYPE_A_S_C, ""), eventName));

	}

	@Override
	public void logAbnormalEventBegin(String eventName, Object logger) {
		String message = LogXUtils.getLogProperty(LOG_MSG_EVENT_BEGIN, "");
		LogXUtils.logTextMessage(logger, String.format(message, LogXUtils.getLogProperty(TYPE_A_C, ""), eventName));
	}

	@Override
	public void logAbnormalEventEnd(String eventName, Object logger) {
		String message = LogXUtils.getLogProperty(LOG_MSG_EVENT_END, "");
		LogXUtils.logTextMessage(logger, String.format(message, LogXUtils.getLogProperty(TYPE_A_C, ""), eventName));

	}

}
