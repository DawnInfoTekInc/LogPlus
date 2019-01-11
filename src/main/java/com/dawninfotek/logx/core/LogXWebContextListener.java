package com.dawninfotek.logx.core;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import com.dawninfotek.logx.config.Configuration;
import com.dawninfotek.logx.util.LogXUtils;

@WebListener
public class LogXWebContextListener implements ServletContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		LogXContext.eventService().logApplicationStateChangeEventEnd(LogXUtils.getLogProperty("applicationName", ""), null);
	}

	@Override
	public void contextInitialized(ServletContextEvent contextEvent) {
		//initialize the logx system after web container loaded
		/* before 1.7.2
		String f = contextEvent.getServletContext().getInitParameter("logx-config-file");
		if(f == null) {
			f = Configuration.logxConfigFile;
		}
				
		LogXContext.initialize(f);
		*/
		
		//since 1.7.2
		LogXContext.initialize(Configuration.logxConfigFile);		
		//log application startup
		LogXContext.eventService().logApplicationStateChangeEventBegin(LogXUtils.getLogProperty("applicationName", ""), null);
	}

}
