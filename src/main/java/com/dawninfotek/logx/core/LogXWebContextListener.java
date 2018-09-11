package com.dawninfotek.logx.core;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.dawninfotek.logx.util.LogXUtils;

public class LogXWebContextListener implements ServletContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		LogXContext.eventService().logApplicationStateChangeEventEnd(LogXUtils.getLogProperty("applicationName", ""), null);
	}

	@Override
	public void contextInitialized(ServletContextEvent contextEvent) {
		//initialize the logx system after web container loaded		
		LogXContext.initialize(contextEvent.getServletContext().getInitParameter("logx-config-file"));
		//log application startup
		LogXContext.eventService().logApplicationStateChangeEventBegin(LogXUtils.getLogProperty("applicationName", ""), null);
	}

}
