package simphony.util.messages;

public class NullTaskMessage extends TaskMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1371885063995257369L;

	public NullTaskMessage() {
		super(TaskStatus.NULL, null, null);
	}

	public String toString() {
		return "NULL MESSAGE";
	}
}
