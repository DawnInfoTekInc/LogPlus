package com.dawninfotek.logx.extension.log4j12;

import org.apache.log4j.DefaultThrowableRenderer;
import org.apache.log4j.spi.ThrowableRenderer;


public class LogXThrowableRenderer implements ThrowableRenderer {
    private final DefaultThrowableRenderer defaultRenderer = new DefaultThrowableRenderer();
    
    @Override
    public String[] doRender(Throwable t) {
        String[] initialResult = defaultRenderer.doRender(t);
        String line = "";
        for(int i = 0; i < initialResult.length; i++) {
        	line += initialResult[i];
        }
        line.trim();

    	String[] Result = new String[] {line};
  
        return Result;
    }
}
