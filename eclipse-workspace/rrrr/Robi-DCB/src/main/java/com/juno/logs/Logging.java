package com.juno.logs;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Logging {
	static {
		PropertyConfigurator.configure(Logging.class.getResourceAsStream("/log4j.properties"));
	}

	private static final Logger log = Logger.getLogger("com.juno.logs.Logging");
	private static final Logger infolog = Logger.getLogger("com.juno.logs.InfoLogger");

	public static Logger getLogger() {
		return log;
	}
	public static Logger getInfoLogger() {
		return infolog;
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
}
class InfoLogger{
	
}

