package com.dawninfotek.logplus.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dawninfotek.logplus.core.LogPlusConstants;

public class InheritableThreadContext extends InheritableThreadLocal<LogPlusThreadContext> {

	public static final Logger logger = LoggerFactory.getLogger(InheritableThreadContext.class);

	protected LogPlusThreadContext childValue(final LogPlusThreadContext parentValue) {
		
		//for debug, should be removed later
		//logger.debug("InheritableThreadContext.childValue() is called ...");
		
		Map<String, String> childFieldsValue = null;
		
		if(parentValue != null) { 
				
			if(parentValue.getLogPlusFields() != null && parentValue.getLogPlusFields().isEmpty()) {
			
				childFieldsValue = new HashMap<String, String>();
				String key = null;
				for(Entry<String, String> e: parentValue.getLogPlusFields().entrySet()) {				
					//ignore the 'checkpoint' in the new Thread. TODO configurable
					key = e.getKey();
					if(!(key.equals(LogPlusConstants.CHECKPOINT)
							|| key.equals(LogPlusConstants.CURR_CHECKPOINT)
							|| key.equals(LogPlusConstants.CHECKPOINT_DSP)
							|| key.equals(LogPlusConstants.ELAPSED_TIME))
							) {
						childFieldsValue.put(e.getKey(), e.getValue());
					}
				}
				
			}
			
			if(logger.isDebugEnabled() && childFieldsValue != null) {

				logger.debug("" + childFieldsValue.size() + " of fields from parent thread are set ... ");

			}
			
			return new LogPlusThreadContext(parentValue.getHttpRequest(), parentValue.getHttpResponse(), childFieldsValue);
			
		}else {
		
			return null;
			
		}
		
    }

}
