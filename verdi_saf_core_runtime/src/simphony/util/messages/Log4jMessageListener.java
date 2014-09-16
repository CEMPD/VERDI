package simphony.util.messages;

import java.io.File;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * A MessageEventListener that forwards messages on to log4j.
 * 
 * @author Jerry Vos
 */
public class Log4jMessageListener implements MessageEventListener {

	public static final String INTERNAL_ANCHOR = "MessageCenter" + "."
			+ "INTERNAL";

	public static final String CONFIG_FILE_NAME = "MessageCenter.log4j.properties";
	
	static {
		if (new File(CONFIG_FILE_NAME).exists()) {
			PropertyConfigurator.configure(CONFIG_FILE_NAME);
		}/* else {
			loadDefaultSettings();
		}*/
		
	}

	private static Logger getLogger(String name) {
		return Logger.getLogger(INTERNAL_ANCHOR + "." + name);
	}

	private Logger logger;

	public Log4jMessageListener(Class clazz) {
		logger = getLogger(clazz.getName());
	}

	public Log4jMessageListener(String name) {
		logger = getLogger(name);
	}

	public Log4jMessageListener() {
		logger = Logger.getLogger(INTERNAL_ANCHOR);
	}

	/**
	 * Sends a message to log4j with the event's level, message, and throwable
	 * (if it is not null).
	 * 
	 * @param event
	 *            a message event
	 */
	public void messageReceived(MessageEvent event) {
		if (event.getThrowable() != null) {
			logger.log(event.getLevel(), getMessage(event), event
					.getThrowable());
		} else {
			logger.log(event.getLevel(), getMessage(event));
		}
	}
	
	protected String getMessage(MessageEvent event) {
		String message = (event.getMessage() == null ? "null" : event.getMessage().toString());
		Object[] metaData = event.getMetaData();
		if (metaData != null && metaData.length > 0) {
			message += " metaData=" + Arrays.toString(metaData);
		}
		return message;
	}
	
	/**
	 * Loads the default logging settings. This sets up a console logger and a rolling file logger, 
	 * both allowing DEBUG messages and higher. The default settings are found in the properties file
	 * in this package with the name {@link #CONFIG_FILE_NAME}.
	 */
	public static void loadDefaultSettings() {
		PropertyConfigurator.configure(Log4jMessageListener.class.getResource(CONFIG_FILE_NAME));
	}
	
	public static void main(String[] args) {
		loadDefaultSettings();
		MessageCenter.getMessageCenter(Log4jMessageListener.class).fatal("blas", null);
	}

}
