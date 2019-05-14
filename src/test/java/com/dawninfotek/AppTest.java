package com.dawninfotek;

import static org.junit.Assert.assertTrue;

import java.util.EmptyStackException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.dawninfotek.logplus.config.JsonField;
import com.dawninfotek.logplus.core.LogPlusConstants;
import com.dawninfotek.logplus.core.LogPlusContext;
import com.dawninfotek.logplus.util.LogPlusUtils;
import com.dawninfotek.logplus.util.StringUtils;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    final static Logger logger = LoggerFactory.getLogger(AppTest.class);
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue()
    {
        assertTrue( true );
    }

    @Test
    public void logutilsTest(){
        LogPlusContext.initialize(null);

        LogPlusContext.checkPointService().startCheckPoint("testCheck");
        System.out.println("currId: "+ LogPlusUtils.getLogProperty("uuid.key", ""));
        //There is no 'key' attribute in LogPlusField anymore
        //assertTrue(LogPlusUtils.getLogProperty("uuid.key", "").equals("uuid"));
        LogPlusContext.checkPointService().startCheckPoint("subTest");
        assertTrue(LogPlusUtils.getLogPlusHeaderName().equals("AQALogPlus"));
        LogPlusUtils.saveFieldValue("AQALogPlus", "header"); 
        System.out.println(LogPlusUtils.getLogPlusHeaderValue());
        try {
            Thread.sleep(1000);
        }catch (InterruptedException e){

        }
        LogPlusContext.checkPointService().endCheckPoint(logger);
        try {
            Thread.sleep(1000);
        }catch (InterruptedException e){

        }
        LogPlusContext.checkPointService().endCheckPoint(logger);
    }

    @Test
    public void eventTest(){
    	LogPlusContext.initialize("CLASS_PATH=logplus-default.properties");
    	LogPlusContext.eventService().logEventBegin("pay event", LogPlusUtils.getLogProperty(LogPlusConstants.TYPE_A_S_C, ""), logger);
    	LogPlusContext.eventService().logEventEnd("pay event", LogPlusUtils.getLogProperty(LogPlusConstants.TYPE_A_S_C, ""), logger);
    	LogPlusContext.eventService().logBusinessEventBegin("agreement", logger);
    	LogPlusContext.eventService().logBusinessEventEnd("end agreement", logger);
    	LogPlusContext.eventService().logServiceEventBegin("income process", logger);
    	LogPlusContext.eventService().logServiceEventEnd("finish income process", logger);
    	//LogPlusContext.eventService().logConnectionStateChangeEventBegin("new connect", logger);
    	//LogPlusContext.eventService().logConnectionStateChangeEventEnd("connect end", logger);
    	LogPlusContext.eventService().logApplicationStateChangeEventBegin("change server IP", logger);
    	LogPlusContext.eventService().logApplicationStateChangeEventEnd("finish server IP change", logger);
    	LogPlusContext.eventService().logAbnormalEventBegin("server down", logger);
    	LogPlusContext.eventService().logAbnormalEventEnd("server up", logger);
    }

    @Test
    public void muskTest(){
        String sample = "1234567890123456";
        System.out.println(LogPlusContext.maskService().mask(null, null));
        System.out.println(LogPlusContext.maskService().mask(sample, "^{8}[#]"));
        System.out.println(LogPlusContext.maskService().mask(sample, "^(4-12)[N]"));
        System.out.println(LogPlusContext.maskService().mask(sample, "${6}"));
        System.out.println(LogPlusContext.maskService().mask(sample, "^{4}[#]&(8-9)[@]&${2}[*]"));
        System.out.println(LogPlusContext.maskService().mask(sample, "all"));
        System.out.println(LogPlusContext.maskService().mask(sample, ""));
    }

    @Test
    public void checkpointTest(){
        LogPlusContext.initialize(null);
        LogPlusUtils.saveFieldValue(LogPlusConstants.CURR_CHECKPOINT, "curr_cp");
        LogPlusContext.checkPointService().startCheckPoint("testCheck");
        LogPlusContext.checkPointService().endCheckPoint(logger);
        System.out.println(LogPlusUtils.getLogPlusHeaderValue());
    }

    @Test
    public void hashCheck(){
        LogPlusContext.initialize(null);
        assertTrue(LogPlusContext.hashService().hash("valid name", null).equals("71dc1596838df3bf0083bfe8ff44fa8283e2ecab4f09ff5f57f61ccb2285165f"));
    }
    
    @Test
    public void propertiesLoadTest() {
    	String dir = System.getProperty("user.dir");
    	String propertyPath = "FILE=" + dir + "\\logplus.properties";
    	LogPlusContext.initialize(propertyPath);
    }
    
    @Test
    public void throwableToStringTest() {
    	try {
    		throw new EmptyStackException();
    	}catch(Exception e) {
    		String exception = JsonField.stackTraceToString(e);
    		System.out.println(exception);
    	}
    }
    
    @Test
    public void stringUtilsTests() {
    	System.out.println(StringUtils.removeStart("/api/test", "/"));
    }

}
