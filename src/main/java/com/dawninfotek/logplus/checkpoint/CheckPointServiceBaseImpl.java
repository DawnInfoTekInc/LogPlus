package com.dawninfotek.logplus.checkpoint;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.dawninfotek.logplus.core.LogPlusConstants;
import com.dawninfotek.logplus.util.LogPlusUtils;

public class CheckPointServiceBaseImpl implements CheckPointService {
	
	public static final Logger logger = LoggerFactory.getLogger(CheckPointServiceBaseImpl.class);
	
	private static final boolean logCheckPointEvent = Boolean.valueOf(LogPlusUtils.getLogProperty("log.checkpoint.event", "false"));

	@Override
	public void startCheckPoint(String checkName) {
		String checkPointName = MDC.get(LogPlusConstants.CURR_CHECKPOINT);
		if (checkPointName == null || checkPointName.length() == 0) {
			checkPointName = checkName;
		} else {
			checkPointName = checkPointName + "::" + checkName;
		}
		
		if(logger.isDebugEnabled()) {
			logger.debug("CheckPoint as name '" + checkPointName + "' is created ...");
		}
		
		MDC.put(LogPlusConstants.CURR_CHECKPOINT, checkPointName);
		MDC.put(checkPointName, String.valueOf(System.currentTimeMillis()));
		if(logCheckPointEvent) {			
			logger.info("CheckPoint as name '" + checkPointName + "' started.");
		}
	}

	@Override
	public void endCheckPoint(Object aLogger) {
		long executionTime = 0;
		long end = System.currentTimeMillis();
		String checkPointName = MDC.get(LogPlusConstants.CURR_CHECKPOINT);
		String checkName = checkPointName;
		String start = MDC.get(checkPointName);
		if (start != null) {
			executionTime = end - Long.parseLong(start);
		}
		
		String transPath = MDC.get(LogPlusConstants.TRANSACTION_PATH);
		String path = MDC.get(LogPlusConstants.PATH);

		String p = "";

		if (!StringUtils.isEmpty(transPath)) {
			p = transPath;
		} else if (path != null) {
			p = path;
		}	


		String message = String.format(LogPlusUtils.getLogProperty(LogPlusConstants.LOG_MSG_PFM_METRIC, ""), checkName, executionTime, p);
		
		MDC.put(LogPlusConstants.CHECKPOINT_DSP, checkName);
		MDC.put(LogPlusConstants.ELAPSED_TIME, String.valueOf(executionTime));		
		
		LogPlusUtils.logTextMessage(aLogger, message);
		
		MDC.remove(LogPlusConstants.CHECKPOINT_DSP);
		MDC.remove(LogPlusConstants.ELAPSED_TIME);		
		
		if(logCheckPointEvent) {
			logger.info("CheckPoint as name '" + checkPointName + "' ended.");
		}
		
		if(StringUtils.isEmpty(checkPointName) || checkPointName.indexOf("::") < 0) {			
			checkPointName = "";			
		}else {			
			checkPointName = checkPointName.substring(0, checkPointName.lastIndexOf("::"));			
		}
		
		MDC.put(LogPlusConstants.CURR_CHECKPOINT, checkPointName);
		
		if(logger.isTraceEnabled()) {
			logger.trace("Current CheckPoint is set to {}", checkPointName);
		}

		MDC.remove(checkName);

	}

	@Override
	public String getCurrentCheckPoint() {
		return MDC.get(CURR_CHECKPOINT);
	}

	@Override
	public void endCheckPoint(String checkName, Object logger) {
		
	}


}