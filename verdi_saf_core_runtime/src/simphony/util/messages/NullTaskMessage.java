package simphony.util.messages;

public class NullTaskMessage extends TaskMessage {

	public NullTaskMessage() {
		super(TaskStatus.NULL, null, null);
	}

	public String toString() {
		return "NULL MESSAGE";
	}
}
