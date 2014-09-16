package simphony.util.messages;

import java.io.Serializable;

/**
 * Message notifying about the start or finish of an executing task. This can be
 * used to notify the GUI when the task is done so that a message can be given
 * or changed.
 * 
 * @author $Author: howe $
 * @version $Revision: 1.1 $
 * 
 */
public class TaskMessage implements Serializable {

	private static final long serialVersionUID = -7988872787862424319L;

	public static enum TaskStatus {
		STARTED, FINISHED, WORKING, NULL
	};

	private TaskStatus status;

	private Object key;

	private String message;

	public TaskMessage() {

	}

	public TaskMessage(TaskStatus status, Object key, String message) {
		super();
		this.status = status;
		this.key = key;
		this.message = message;
	}

	public Object getKey() {
		return key;
	}

	public void setKey(Object key) {
		this.key = key;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public TaskStatus getStatus() {
		return status;
	}

	public void setStatus(TaskStatus status) {
		this.status = status;
	}

	public String toString() {
		return key.toString() + " reports " + message;
	}
}
