package com.dawninfotek.logx.extension.log4j2;

import lombok.Builder;
import lombok.Value;

import java.io.Serializable;
import java.util.Map;

@Value
@Builder
public class CustomMessage implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String message;
    private Map<String, Object> newField;

    public String toJson() {
        return JsonUtils.getGson().toJson(this);
    }

}