package com.dawninfotek.logx.extension.log4j12;

import org.apache.log4j.DefaultThrowableRenderer;
import org.apache.log4j.spi.ThrowableRenderer;


public class LogXThrowableRenderer implements ThrowableRenderer {
    private final DefaultThrowableRenderer defaultRenderer = new DefaultThrowableRenderer();
    
    @Override
    public String[] doRender(Throwable t) {
        String[] initialResult = defaultRenderer.doRender(t);
        StringBuilder sb = new StringBuilder(); 
        for(int i = 0; i < initialResult.length; i++) {
        	sb.append(initialResult[i]);
        }
  
        return new String[] {sb.toString()};
    }
}
