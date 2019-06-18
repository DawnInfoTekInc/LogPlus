/**************************************************************************
 * Licensed Material - Dawn InfoTek Inc.                                  *
 * Copyright (c) Dawn InfoTek Inc. 1999, 2005, 2018 - All rights reserved.*
 * (<http://www.dawninfotek.com>)                                         *
 *                                                                        *
 **************************************************************************/

package com.dawninfotek.logplus.extension.log4j12;

import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.helpers.Loader;
import org.apache.log4j.xml.DOMConfigurator;

public class Log4jConfigChangeWatcherService implements Runnable {
	
	public static final Logger logger = Logger.getLogger(Log4jConfigChangeWatcherService.class);
	
	private String configFileName = null;
	private String fullFilePath = null;
	private int initRequestCounter = 0;
	public boolean shouldStop = false;
	
	private static Log4jConfigChangeWatcherService instance;
	
	public static synchronized Log4jConfigChangeWatcherService getInstance() {		
		if(instance == null) {
			instance = new Log4jConfigChangeWatcherService();
		}
		
		return instance;
	}
	
	public int startMonitoring() {
		if(initRequestCounter == 0) {
			Thread t = new Thread(this);
			t.start();
			logger.info("Log4j Monitoring Thread with name:" + t.getName() + " Started ...");
		}
		
		initRequestCounter ++;
		return initRequestCounter;
	}
	
	public int stopMnitoring() {
		initRequestCounter--;
		if(initRequestCounter <= 0) {
			//stop he thread
			shouldStop = true;
		}
		return initRequestCounter;
	}

	private Log4jConfigChangeWatcherService() {
		super();
		init();
	}
	
	private void init() {
		//initialize the configuration path and file
		String configFile = null;

		try {

			URL url = Loader.getResource("log4j.xml");
			if (url == null) {
				url = Loader.getResource("log4j.properties");
			}

			if (url != null) {
				configFile = url.getFile();
				//remove something likes '/C:' 
				if(configFile.indexOf(":") > 0) {
					configFile = configFile.substring(configFile.indexOf(":") + 1);
				}
				
			}
			
			int lastIndex = configFile.lastIndexOf("/");			
			this.fullFilePath = configFile.substring(0, lastIndex + 1);
			this.configFileName = configFile.substring(lastIndex + 1);

		} catch (Exception e) {
			logger.error("Something went wrong", e);
		}
		
	}

	// This method will be called each time the log4j configuration is changed
	public void configurationChanged(final String file) {
		System.out.println("Log4j configuration file changed. Reloading ... File:" + file);
		
		if(file.endsWith(".xml")) {
			DOMConfigurator.configure(file);
		}else {
			PropertyConfigurator.configure(file);
		}
	}

	public void run() {
		try {			
			register();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void register() throws IOException {
		//configurationChanged(file);
		startWatcher(fullFilePath, configFileName);
	}

	private void startWatcher(String filePath, String fileName) throws IOException {
		final WatchService watchService = FileSystems.getDefault().newWatchService();
		
		shouldStop = false;

		// Define the file and type of events which the watch service should handle
		Path path = Paths.get(filePath);
		path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					watchService.close();
				} catch (IOException e) {
					System.out.println("Fail to close watch service");
					e.printStackTrace();
				}
			}
		});

		WatchKey key = null;
		logger.info("Starting to watch the change of:" + fullFilePath + configFileName);
		while (!shouldStop) {
			try {
				key = watchService.take();
				for (WatchEvent<?> event : key.pollEvents()) {
					if (event.context().toString().equals(fileName)) {

						// From here the configuration change callback is triggered
						configurationChanged(filePath + fileName);
					}
				}
				boolean reset = key.reset();
				if (!reset) {
					System.out.println("Could not reset the watch key.");
					break;
				}
			} catch (Exception e) {
				System.out.println("InterruptedException: " + e.getMessage());
			}
		}
	}

}
