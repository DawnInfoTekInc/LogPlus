package com.dawninfotek.logx.annotation;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.JoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
public class ExecutionAspect {

    final static Logger logger = LoggerFactory.getLogger(ExecutionAspect.class);

    @Pointcut("@annotation(com.dawninfotek.logx.annotation.ExecutionTime)")
    public void annotationPointCutDefinition(){
    }

    @Pointcut("execution(* *(..))")
    public void atExecution(){}

//    @Before("@annotation(com.dawninfotek.logx.annotation.ExecutionTime)")
//    public void printBefore(JoinPoint pointcut){
//        System.out.println(pointcut.getSignature());
//    }

    @Around("@annotation(com.dawninfotek.logx.annotation.ExecutionTime) && execution(* *(..))")
    public Object aroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        Object returnObject;
        try {
            System.out.println("ExecAspect's aroundAdvice's body is now executed Before yourMethodAround is called.");

            final long start = System.currentTimeMillis();
            returnObject = joinPoint.proceed();
            final long executionTime = System.currentTimeMillis() - start;

            System.out.println(joinPoint.getSignature() + " executed in " + executionTime + " ms");
            logger.info(joinPoint.getSignature() + " executed in " + executionTime + " ms");

        } catch (Throwable throwable) {
            throw throwable;
        }
        finally {
            System.out.println("ExecAspect's aroundAdvice's body is now executed After yourMethodAround is called.");
        }
        return returnObject;
    }

    @After("annotationPointCutDefinition() && atExecution()")
    public void printNewLine(JoinPoint pointcut){
        System.out.println("after execution method "+ pointcut.getSignature());
        System.out.print("\n\r");
    }
}
