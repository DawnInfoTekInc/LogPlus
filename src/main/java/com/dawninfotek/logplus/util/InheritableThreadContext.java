package com.dawninfotek.logplus.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dawninfotek.logplus.core.LogPlusConstants;

public class InheritableThreadContext extends InheritableThreadLocal<Map<String, String>> {
	
	public static final Logger logger = LoggerFactory.getLogger(InheritableThreadContext.class);
	
	protected Map<String, String> childValue(final Map<String, String> parentValue) {
		
		//for debug, should be removed later
		//logger.debug("InheritableThreadContext.childValue() is called ...");
		
		Map<String, String> childValue = null;
		
		if(parentValue != null && !parentValue.isEmpty()) {
			
			childValue = new HashMap<String, String>();
			String key = null;
			for(Entry<String, String> e: parentValue.entrySet()) {				
				//ignore the 'checkpoint' in the new Thread. TODO configurable
				key = e.getKey();
				if(!(key.equals(LogPlusConstants.CHECKPOINT)
						|| key.equals(LogPlusConstants.CURR_CHECKPOINT)
						|| key.equals(LogPlusConstants.CHECKPOINT_DSP)
						|| key.equals(LogPlusConstants.ELAPSED_TIME))
						) {
					childValue.put(e.getKey(), e.getValue());
				}
			}
			
		}
		
		if(logger.isDebugEnabled() && childValue != null) {

			logger.debug("" + childValue.size() + " of fields from parent thread are set ... ");

		}
		
		return childValue;
		
    }

}
