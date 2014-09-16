/*CopyrightHere*/
package simphony.util.messages;

import org.apache.log4j.Level;

public class MessageEvent {
	private Object source;
	
	private Level level;
	
	private Object message;
	
	private Object[] metaData;

	private Throwable throwable;
	
	public MessageEvent(Object source, Level level, Object message,
			Throwable throwable, Object... metaData) {
		super();
		this.source = source;
		this.level = level;
		this.message = message;
		this.metaData = metaData;
		this.throwable = throwable;
	}

	public MessageEvent(Object source, Level level, Object message) {
		this(source, level, message, null);
	}
	
	public Level getLevel() {
		return level;
	}

	public Object getMessage() {
		return message;
	}

	public Object[] getMetaData() {
		return metaData;
	}
	
	public Throwable getThrowable() {
		return throwable;
	}
	
	public Object getSource() {
		return source;
	}
}
