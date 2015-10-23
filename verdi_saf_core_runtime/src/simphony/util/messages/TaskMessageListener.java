package simphony.util.messages;

/**
 * Abstract implementation of a MessageEventListener that listens for task
 * messages.
 */
public abstract class TaskMessageListener implements MessageEventListener {

	public TaskMessageListener() {

	}

	protected abstract void processTaskMessage(TaskMessage message);

	public void messageReceived(MessageEvent arg0) {
		if (arg0.getMessage() instanceof TaskMessage) {
			processTaskMessage((TaskMessage) arg0.getMessage());
		}
	}
}
