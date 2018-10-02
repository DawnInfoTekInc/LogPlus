package com.dawninfotek.logx.checkpoint;

import org.apache.commons.lang.StringUtils;
import org.slf4j.MDC;

import com.dawninfotek.logx.core.LogXConstants;
import com.dawninfotek.logx.util.LogXUtils;

public class CheckPointServiceBaseImpl implements CheckPointService {

	@Override
	public void startCheckPoint(String checkName) {
		String checkPointName = MDC.get(LogXConstants.CURR_CHECKPOINT);
		if (checkPointName == null || checkPointName.length() == 0) {
			checkPointName = checkName;
		} else {
			checkPointName = checkPointName + "\"" + checkName;
		}
		MDC.put(LogXConstants.CURR_CHECKPOINT, checkPointName);
		MDC.put(checkPointName, String.valueOf(System.currentTimeMillis()));
	}

	@Override
	public void endCheckPoint(Object logger) {
		long executionTime = 0;
		long end = System.currentTimeMillis();
		String checkPointName = MDC.get(LogXConstants.CURR_CHECKPOINT);
		String checkName = checkPointName;
		String start = MDC.get(checkPointName);
		if (start != null) {
			executionTime = end - Long.parseLong(start);
		}
		if (checkPointName != null && checkPointName.contains("\"")) {
			checkPointName = "";
		} else {
			int endIndex = checkPointName.lastIndexOf("\"");
			if(endIndex != -1) {
				checkPointName = checkPointName.substring(0, endIndex-1);
			}
		}

		MDC.put(LogXConstants.CURR_CHECKPOINT, checkPointName);
		
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

	@Override
	public void endCheckPoint(String checkName, Object logger) {
		// TODO Auto-generated method stub
		
	}


}
