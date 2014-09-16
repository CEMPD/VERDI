/*CopyrightHere*/
package simphony.util.messages;

/**
 * This is used by {@link simphony.util.messages.MessageCenter} as a sink for
 * messages it receives. When a MessageCenter recieves a message it will call
 * the methods in this interface with information on the message.
 * 
 * @author Jerry Vos
 * @version $Revision: 1.2 $ $Date: 2005/11/10 23:24:25 $
 */
public interface MessageEventListener {
	/**
	 * Called when a message was received in the MessageCenter.
	 * 
	 * @param event
	 *            the message event
	 */
	void messageReceived(MessageEvent event);
}
