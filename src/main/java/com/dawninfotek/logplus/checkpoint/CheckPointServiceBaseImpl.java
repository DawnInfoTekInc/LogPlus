package com.dawninfotek.logplus.checkpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dawninfotek.logplus.core.LogPlusConstants;
import com.dawninfotek.logplus.util.LogPlusUtils;
import com.dawninfotek.logplus.util.StringUtils;

public class CheckPointServiceBaseImpl implements CheckPointService {
	
	public static final Logger logger = LoggerFactory.getLogger(CheckPointServiceBaseImpl.class);
	
	private static final boolean logCheckPointEvent = Boolean.valueOf(LogPlusUtils.getLogProperty("log.checkpoint.event", "false"));

	@Override
	public void startCheckPoint(String checkName) {

		String checkPointName = LogPlusUtils.getLogPlusFieldValue(LogPlusConstants.CURR_CHECKPOINT);
		if (checkPointName == null || checkPointName.length() == 0) {
			checkPointName = checkName;
		} else {
			checkPointName = checkPointName + "::" + checkName;
		}
		
		if(logger.isDebugEnabled()) {
			logger.debug("CheckPoint as name '" + checkPointName + "' is created ...");
		}		
	
		LogPlusUtils.saveFieldValue(LogPlusConstants.CURR_CHECKPOINT, checkPointName);
		LogPlusUtils.saveFieldValue(checkPointName, String.valueOf(System.currentTimeMillis()));
		if(logCheckPointEvent) {			
			logger.info("CheckPoint as name '" + checkPointName + "' started.");
		}
	}

	@Override
	public void endCheckPoint(Object aLogger) {
		long executionTime = 0;
		long end = System.currentTimeMillis();
		String checkPointName = LogPlusUtils.getLogPlusFieldValue(LogPlusConstants.CURR_CHECKPOINT);
		String checkName = checkPointName;
		
		try {
			String start = LogPlusUtils.getLogPlusFieldValue(checkPointName);
			if (start != null) {
				executionTime = end - Long.parseLong(start);
			}		

			String transPath = LogPlusUtils.getLogPlusFieldValue(LogPlusConstants.TRANSACTION_PATH);
			String path = LogPlusUtils.getLogPlusFieldValue(LogPlusConstants.PATH);
			
			String p = "";

			if (!StringUtils.isEmpty(transPath)) {
				p = transPath;
			} else if (path != null) {
				p = path;
			}	


			String message = String.format(LogPlusUtils.getLogProperty(LogPlusConstants.LOG_MSG_PFM_METRIC, ""), checkName, executionTime, p);
			LogPlusUtils.saveFieldValue(LogPlusConstants.CHECKPOINT_DSP, checkName);
			LogPlusUtils.saveFieldValue(LogPlusConstants.ELAPSED_TIME, String.valueOf(executionTime));	
			LogPlusUtils.logTextMessage(aLogger, message, "info");
		
		}finally {	
			LogPlusUtils.removeField(LogPlusConstants.CHECKPOINT_DSP);	
			LogPlusUtils.removeField(LogPlusConstants.ELAPSED_TIME);
			LogPlusUtils.removeField(checkName);
		}
		
		if(logCheckPointEvent) {
			logger.info("CheckPoint as name '" + checkPointName + "' ended.");
		}
		
		if(StringUtils.isEmpty(checkPointName) || checkPointName.indexOf("::") < 0) {			
			checkPointName = "";			
		}else {			
			checkPointName = checkPointName.substring(0, checkPointName.lastIndexOf("::"));			
		}		
	
		LogPlusUtils.saveFieldValue(LogPlusConstants.CURR_CHECKPOINT, checkPointName);
		
		if(logger.isTraceEnabled()) {
			logger.trace("Current CheckPoint is set to {}", checkPointName);
		}
		
	}

	@Override
	public String getCurrentCheckPoint() {
		return LogPlusUtils.getLogPlusFieldValue(CURR_CHECKPOINT);
	}

	@Override
	public void endCheckPoint(String checkName, Object logger) {
		
	}
}
