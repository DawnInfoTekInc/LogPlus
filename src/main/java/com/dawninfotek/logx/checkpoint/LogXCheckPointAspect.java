/**************************************************************************
 * Licensed Material - Dawn InfoTek Inc.                                  *
 * Copyright (c) Dawn InfoTek Inc. 1999, 2005, 2018 - All rights reserved.*
 * (<http://www.dawninfotek.com>)                                         *
 *                                                                        *
 **************************************************************************/
package com.dawninfotek.logx.checkpoint;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.aspectj.lang.JoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dawninfotek.logx.core.LogXContext;

@Aspect
public class LogXCheckPointAspect {

    final static Logger logger = LoggerFactory.getLogger(LogXCheckPointAspect.class);

    @Pointcut("@annotation(com.dawninfotek.logx.checkpoint.LogXCheckPoint)")
    public void annotationPointCutDefinition(){
    }

    @Pointcut("execution(* *(..))")
    public void atExecution(){}

    @Around("@annotation(com.dawninfotek.logx.checkpoint.LogXCheckPoint) && execution(* *(..))")
    public Object aroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        Object returnObject;
        try {
        	//For debug only, will be removed
            System.out.println("ExecAspect's aroundAdvice's body is now executed Before yourMethodAround is called.");
            
            LogXContext.checkPointService().startCheckPoint(getCheckPointName(joinPoint));
            
            returnObject = joinPoint.proceed();
            
            if(logger.isDebugEnabled()) {
           
            	logger.debug(joinPoint.getSignature() + " done ");
            
            }

        } catch (Throwable throwable) {
            throw throwable;
        }
        finally {
        	if(logger.isDebugEnabled()) {
        		logger.debug("ExecAspect's aroundAdvice's body is now executed After yourMethodAround is called ==> ending checkpoint.");
        	}
            LogXContext.checkPointService().endCheckPoint(logger);       	
        }
        return returnObject;
    }

    @After("annotationPointCutDefinition() && atExecution()")
    public void printNewLine(JoinPoint pointcut){
    }
    
    private String getCheckPointName(ProceedingJoinPoint joinPoint) {
    	
    	String name = null;
    	
        Method m = ((MethodSignature) joinPoint.getSignature()).getMethod();
        
        LogXCheckPoint exec = null;
        
        for(Annotation ann:m.getAnnotations()) {
        	if(ann.annotationType().equals(LogXCheckPoint.class)) {
        		exec = (LogXCheckPoint)ann;
        	}
        }
        
        if(exec != null) {
        	name = exec.name();
        }
        
        if(name == null || name.isEmpty()) {
        	//use method name as the checkpoint name
        	name = joinPoint.getSignature().getName();
        }
    	
        return name;
    	
    }
    
}
