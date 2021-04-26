package com.juno.logs;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class ErrorLogger {
	static {
		PropertyConfigurator.configure(Logging.class.getResourceAsStream("/log4j.properties"));
	}
	
	private static final Logger log = Logger.getLogger("com.juno.logs.ErrorLogger");

	public static Logger getLogger() {
		return log;
	}
	
	public void debug(String message) {
		log.debug(message);
	}

	public void info(String message) {
		log.info(message);
	}

	public void warn(String message) {
		log.warn(message);
	}

	public void error(String message) {
		log.error(message);
	}
	
	public void exception(String message,Exception e) {
		log.error(message,e);
	}
}
