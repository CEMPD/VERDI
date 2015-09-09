/*CopyrightHere*/
package simphony.util.messages;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;

/**
 * A class for handling all sorts of messages. This class receives messages of different levels
 * through it's different methods and passes these messages on to it's listeners.<p/> <p/> To
 * retrieve an instance of this class use {@link #getMessageCenter(Class)} or
 * {@link #getMessageCenter(Class, Map)}.
 *
 * @author Jerry Vos
 * @see #getMessageCenter(Class)
 * @see #getMessageCenter(Class, Map)
 * @see #addMessageListener(MessageEventListener)
 * @see #removeMessageListener(MessageEventListener)
 */
public class MessageCenter {

	private static final MessageCenter msgCenter = getMessageCenter(MessageCenter.class);

	/**
	 * Creates a MessageCenter for the specified class.
	 *
	 * @param clazz
	 * @return the created MessageCenter
	 */
	public static MessageCenter getMessageCenter(Class clazz) {
		if (clazz == null) {
			RuntimeException ex = new RuntimeException(
					"When creating a message center clazz must not be null");
			msgCenter
					.warn(
							"MessageCenter.getMessageCenter: Warning, "
									+ "addLogger == true but clazz == null.  Cannot add a logger when the class "
									+ "parameter is null.", ex);
		}
		return getMessageCenter(clazz.getName(), true);
	}

	public static MessageCenter getMessageCenter(String name) {
		return getMessageCenter(name, true);
	}

	/**
	 * TODO: fix this javadoc Creates a MessageCenter.
	 *
	 * @param name
	 *            an id for the message center
	 * @param addLogger
	 *            whether or not to add an automatic logger to the MessageCenter
	 * @return a created MessageCenter
	 * @see Log4jMessageListener
	 */
	public static MessageCenter getMessageCenter(String name, boolean addLogger) {
		MessageCenter newMsgCenter = new MessageCenter();

		if (addLogger) {
			if (name != null) {
				newMsgCenter.logListener = new Log4jMessageListener(name);
			} else {
				RuntimeException ex = new RuntimeException(
						"When adding a logger name must not be null");
				msgCenter
						.warn(
								"MessageCenter.getMessageCenter: Warning, "
										+ "addLogger == true but name == null.  Cannot add a logger when the name "
										+ "parameter is null.", ex);
			}
		}
		return newMsgCenter;
	}

	/**
	 * Currently this method is no different then {@link #getMessageCenter(Class)} but it is here in
	 * case at some point MessageCenter's can be setup with a variety of properties.
	 *
	 * @param clazz
	 *            the class this message center is working for
	 * @param properties
	 *            properties for creation of the MessageCenter (ignored for now)
	 * @return a newly created MessageCenter
	 * @see #getMessageCenter(Class)
	 */
	public static MessageCenter getMessageCenter(Class clazz, Map properties) {
		return getMessageCenter(clazz);
	}

	// TODO: decide about the static business
	private static ArrayList<MessageEventListener> listeners = new ArrayList<MessageEventListener>();

	private Log4jMessageListener logListener;

	/**
	 * Constructs the MessageCenter. See the static getMessageCenter(...) methods for instantiating
	 * MessageCenters.
	 *
	 * @see #getMessageCenter(Class)
	 */
	private MessageCenter() {
	}

	public void info(Object info, Object... metaData) {
		fireMessageEvent(Level.INFO, info, null, metaData);
	}

	public void trace(Object info, Object... metaData) {
		fireMessageEvent(Level.TRACE, info, null, metaData);
	}

	public void debug(Object info, Object... metaData) {
		fireMessageEvent(Level.DEBUG, info, null, metaData);
	}

	public void warn(Object info, Object... metaData) {
		fireMessageEvent(Level.WARN, info, null, metaData);
	}

	public void warn(Object info, Throwable error, Object... metaData) {
		fireMessageEvent(Level.WARN, info, error, metaData);
	}

	public void error(Object info, Throwable error, Object... metaData) {
		fireMessageEvent(Level.ERROR, info, error, metaData);
	}

	public void fatal(Object info, Throwable error, Object... metaData) {
		fireMessageEvent(Level.FATAL, info, error, metaData);
	}

	public void fireMessageEvent(Level level, Object info, Throwable throwable,
	                             Object... metaData) {
		List<MessageEventListener> list;
		synchronized (listeners) {
			list = (List<MessageEventListener>) listeners.clone();
		}
		MessageEvent event = new MessageEvent(this, level, info, throwable, metaData);
		for (MessageEventListener listener : list) {
			listener.messageReceived(event);
		}
		if (logListener != null)
			logListener.messageReceived(event);
	}

	public static void addMessageListener(MessageEventListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	public static void removeMessageListener(MessageEventListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	public static Collection<MessageEventListener> getMessageListeners() {
		return Collections.unmodifiableCollection(listeners);
	}

	/**
	 * Retrieves the default message listener that forwards messages to log4j.
	 *
	 * @return the logging message listener
	 */
	public Log4jMessageListener getLogListener() {
		return logListener;
	}

	/**
	 * Sets the default message listener that forwards messages to log4j.
	 *
	 * @param logListener
	 *            the logging message listener (can be null, in which case no default log listener
	 *            is set)
	 */
	public void setLogListener(Log4jMessageListener logListener) {
		this.logListener = logListener;
	}
}
