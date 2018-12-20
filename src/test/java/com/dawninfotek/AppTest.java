package com.dawninfotek;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.dawninfotek.logx.core.LogXConstants;
import com.dawninfotek.logx.core.LogXContext;
import com.dawninfotek.logx.util.LogXUtils;

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
        LogXContext.initialize(null);

        LogXContext.checkPointService().startCheckPoint("testCheck");
        System.out.println("currId: "+ LogXUtils.getLogProperty("uuid.key", ""));
        assertTrue(LogXUtils.getLogProperty("uuid.key", "").equals("uuid"));
        LogXContext.checkPointService().startCheckPoint("subTest");
        assertTrue(LogXUtils.getLogXHeaderName().equals("AQALogX"));
        MDC.put("AQALogX", "header");
        //assertTrue(LogXUtils.getLogXHeaderValue().equals("header"));
        System.out.println(LogXUtils.getLogXHeaderValue());
        try {
            Thread.sleep(1000);
        }catch (InterruptedException e){

        }
        LogXContext.checkPointService().endCheckPoint(logger);
        try {
            Thread.sleep(1000);
        }catch (InterruptedException e){

        }
        LogXContext.checkPointService().endCheckPoint(logger);
    }

    @Test
    public void eventTest(){
    	LogXContext.initialize("CLASS_PATH=logx-default.properties");
    	LogXContext.eventService().logEventBegin("pay event", LogXUtils.getLogProperty(LogXConstants.TYPE_A_S_C, ""), logger);
    	LogXContext.eventService().logEventEnd("pay event", LogXUtils.getLogProperty(LogXConstants.TYPE_A_S_C, ""), logger);
    	LogXContext.eventService().logBusinessEventBegin("agreement", logger);
    	LogXContext.eventService().logBusinessEventEnd("end agreement", logger);
    	LogXContext.eventService().logServiceEventBegin("income process", logger);
    	LogXContext.eventService().logServiceEventEnd("finish income process", logger);
    	//LogXContext.eventService().logConnectionStateChangeEventBegin("new connect", logger);
    	//LogXContext.eventService().logConnectionStateChangeEventEnd("connect end", logger);
    	LogXContext.eventService().logApplicationStateChangeEventBegin("change server IP", logger);
    	LogXContext.eventService().logApplicationStateChangeEventEnd("finish server IP change", logger);
    	LogXContext.eventService().logAbnormalEventBegin("server down", logger);
    	LogXContext.eventService().logAbnormalEventEnd("server up", logger);
    }

    @Test
    public void muskTest(){
        String sample = "1234567890123456";
        System.out.println(LogXContext.maskService().mask(null, null));
        System.out.println(LogXContext.maskService().mask(sample, "^{8}[#]"));
        System.out.println(LogXContext.maskService().mask(sample, "^(4-12)[N]"));
        System.out.println(LogXContext.maskService().mask(sample, "${6}"));
        System.out.println(LogXContext.maskService().mask(sample, "^{4}[#]&(8-9)[@]&${2}[*]"));
        System.out.println(LogXContext.maskService().mask(sample, "all"));
        System.out.println(LogXContext.maskService().mask(sample, ""));
    }

    @Test
    public void checkpointTest(){
        LogXContext.initialize(null);
        MDC.put(LogXConstants.CURR_CHECKPOINT, "curr_cp");
        LogXContext.checkPointService().startCheckPoint("testCheck");
        LogXContext.checkPointService().endCheckPoint(logger);
        System.out.println(LogXUtils.getLogXHeaderValue());
    }

    @Test
    public void hashCheck(){
        LogXContext.initialize(null);
        assertTrue(LogXContext.hashService().hash("valid name", null).equals("4277d967dc8434b5b1c59d7e7087760a"));
    }

}
