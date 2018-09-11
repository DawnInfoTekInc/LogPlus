package com.dawninfotek.logx.checkpoint;

import org.apache.commons.lang.StringUtils;
import org.slf4j.MDC;

import com.dawninfotek.logx.core.LogXConstants;
import com.dawninfotek.logx.util.LogXUtils;

public class CheckPointServiceBaseImpl implements CheckPointService {

	private boolean currCheckpointSet = false;

	@Override
	public void startCheckPoint(String checkName) {
		
		MDC.put(checkName, String.valueOf(System.currentTimeMillis()));
	}

	@Override
	public void endCheckPoint(String checkName, Object logger) {
		long executionTime = 0;
		long end = System.currentTimeMillis();
		String start = MDC.get(checkName);
		if (start != null) {
			executionTime = end - Long.parseLong(start);
		}
		String checkPointName = MDC.get(LogXConstants.CURR_CHECKPOINT);
		if (checkPointName == null) {
			checkPointName = checkName;
		} else {
			checkPointName = checkPointName + "_" + checkName;
		}
		if(!currCheckpointSet){
			MDC.put(LogXConstants.CURR_CHECKPOINT, checkPointName);
			currCheckpointSet = true;
		}
		String transPath = MDC.get(LogXConstants.TRANSACTION_PATH);
		String path = MDC.get(LogXConstants.PATH);

		String p = "";

		if (!StringUtils.isEmpty(transPath)) {
			p = transPath;
		} else if (path != null) {
			p = path;
		}

		String message = String.format(LogXUtils.getLogProperty(LogXConstants.LOG_MSG_PFM_METRIC, ""), checkPointName, executionTime, p);

		LogXUtils.logTextMessage(logger, message);

		MDC.remove(checkName);
		MDC.remove(LogXConstants.TRANSACTION_PATH);
		MDC.remove(LogXConstants.PATH);

	}

	@Override
	public String getCurrentCheckPoint() {
		return MDC.get(CURR_CHECKPOINT);
	}


}
