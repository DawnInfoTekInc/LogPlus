package com.dawninfotek.logx.checkpoint;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.dawninfotek.logx.core.LogXConstants;
import com.dawninfotek.logx.util.LogXUtils;

public class CheckPointServiceBaseImpl implements CheckPointService {
	
	public static final Logger logger = LoggerFactory.getLogger(CheckPointServiceBaseImpl.class);

	@Override
	public void startCheckPoint(String checkName) {
		String checkPointName = MDC.get(LogXConstants.CURR_CHECKPOINT);
		if (checkPointName == null || checkPointName.length() == 0) {
			checkPointName = checkName;
		} else {
			checkPointName = checkPointName + "::" + checkName;
		}
		
		if(logger.isDebugEnabled()) {
			logger.debug("CheckPoint as name '" + checkPointName + "' is created ...");
		}
		
		MDC.put(LogXConstants.CURR_CHECKPOINT, checkPointName);
		MDC.put(checkPointName, String.valueOf(System.currentTimeMillis()));
	}

	@Override
	public void endCheckPoint(Object aLogger) {
		long executionTime = 0;
		long end = System.currentTimeMillis();
		String checkPointName = MDC.get(LogXConstants.CURR_CHECKPOINT);
		String checkName = checkPointName;
		String start = MDC.get(checkPointName);
		if (start != null) {
			executionTime = end - Long.parseLong(start);
		}
		
		String transPath = MDC.get(LogXConstants.TRANSACTION_PATH);
		String path = MDC.get(LogXConstants.PATH);

		String p = "";

		if (!StringUtils.isEmpty(transPath)) {
			p = transPath;
		} else if (path != null) {
			p = path;
		}

		String message = String.format(LogXUtils.getLogProperty(LogXConstants.LOG_MSG_PFM_METRIC, ""), checkName, executionTime, p);

		LogXUtils.logTextMessage(aLogger, message);
		
		if(StringUtils.isEmpty(checkPointName) || checkPointName.indexOf("::") < 0) {			
			checkPointName = "";			
		}else {			
			checkPointName = checkPointName.substring(0, checkPointName.lastIndexOf("::"));			
		}
		
		MDC.put(LogXConstants.CURR_CHECKPOINT, checkPointName);
		
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
