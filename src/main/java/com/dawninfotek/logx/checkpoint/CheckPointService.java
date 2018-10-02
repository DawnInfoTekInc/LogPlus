package com.dawninfotek.logx.checkpoint;

import com.dawninfotek.logx.core.Component;

public interface CheckPointService extends Component {
	
	/**
	 * Start a new check point with given name
	 * 
	 * @param checkName
	 */
	void startCheckPoint(String checkName);

	@Deprecated
	/**
	 * Ending given check point, log the performance data if the given logger
	 * presents
	 * 
	 * @param checkName
	 * @param logger
	 */
	void endCheckPoint(String checkName, Object logger);
	
	/**
	 * Ending given check point, log the performance data if the given logger
	 * presents
	 * 
	 * @param logger
	 */
	void endCheckPoint(Object logger);
	
	/**
	 * Answer the current checkpoint name
	 * @return
	 */
	String getCurrentCheckPoint();

}
