package simphony.util.messages;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class StackTaskMessageListener extends TaskMessageListener {

	Stack<TaskMessage> messageStack = new Stack<TaskMessage>();

	Set<Object> targetSet = new HashSet<Object>();

	TaskMessageCallback callback;

	public StackTaskMessageListener(TaskMessageCallback callback) {
		this.callback = callback;
	}

	protected synchronized void processTaskMessage(TaskMessage message) {
		if (message.getStatus().equals(TaskMessage.TaskStatus.STARTED)) {
			callback.working(true, message.getMessage());
			targetSet.add(message.getKey());
			messageStack.push(message);
		} else if (message.getStatus().equals(TaskMessage.TaskStatus.WORKING)) {
			callback.working(true, message.getMessage());
		} else {
			targetSet.remove(message.getKey());
			if (targetSet.size() == 0) {
				callback.working(false, "");
			} else {
				callback.working(true, nextMessage(message));
			}
		}
	}

	private String nextMessage(TaskMessage message) {
		Object key = message.getKey();
		if (messageStack.size() > 0) {
			if (messageStack.peek().getKey().equals(key)) {
				messageStack.pop();
			}
			while (messageStack.size() > 0) {
				if (!targetSet.contains(messageStack.peek().getKey())) {
					messageStack.pop();
				} else {
					break;
				}
			}
		}
		if (messageStack.size() > 0) {
			return messageStack.peek().getMessage();
		} else {
			return null;
		}
	}

	public void messageReceived(MessageEvent arg0) {
		if (arg0.getMessage() instanceof TaskMessage) {
			processTaskMessage((TaskMessage) arg0.getMessage());
		}
	}
}
