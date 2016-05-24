/*CopyrightHere*/
package simphony.util.messages;

import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Level;

/**
 * An abstract {@link simphony.util.messages.MessageEventListener} that will filter
 * {@link simphony.util.messages.MessageEventListener} events if they don't have the correct
 * levels set.
 * 
 * @author Jerry Vos
 * @version $Revision: 1.3 $ $Date: 2005/11/07 18:42:32 $
 */
public abstract class AbstractMessageListener implements MessageEventListener {
	private HashSet<Level> listeningLevels;

	/**
	 * Creates this class with no specific Levels to accept, therefore it
	 * accepts all of them.
	 */
	public AbstractMessageListener() {
		this.listeningLevels = new HashSet<Level>();
	}

	/**
	 * Creates this class with the specified Levels to accept. Any message with
	 * one of these levels will be accepted and passed on to the
	 * {@link #handleMessage(MessageEvent)} method.
	 * 
	 * @see #addListeningLevel(Level)
	 * @see #removeListeningLevel(Level)
	 * 
	 * @param listeningLevels
	 *            levels to accept
	 */
	public AbstractMessageListener(Level... listeningLevels) {
		this();
		for (Level level : listeningLevels) {
			this.listeningLevels.add(level);
		}
	}

	/**
	 * Creates this class with the specified Levels to accept. Any message with
	 * one of these levels will be accepted and passed on to the
	 * {@link #handleMessage(MessageEvent)} method.
	 * 
	 * @see #addListeningLevel(Level)
	 * @see #removeListeningLevel(Level)
	 * 
	 * @param listeningLevels
	 *            levels to accept
	 */
	public AbstractMessageListener(Iterable<Level> listeningLevels) {
		this();
		for (Level level : listeningLevels) {
			this.listeningLevels.add(level);
		}
	}

	/**
	 * Recieves a {@link MessageEvent} and if either this class has no Levels
	 * associated with it, or if the event's Level is one of those held in this
	 * class, it will pass the event on to
	 * {@link #handleMessage(MessageEvent, Object)}.
	 * 
	 * @param event
	 *            the message event whose levels will be checked and possibly
	 *            passed on to {@link #handleMessage(MessageEvent, Object)}
	 */
	public void messageReceived(MessageEvent event) {
		// accept if I'm accepting everything (nothing's been set to accept)
		// or it is in one of the levels I'm listening for
		if (listeningLevels.size() == 0
				|| listeningLevels.contains(event.getLevel())) {
			handleMessage(event);
		}
	}

	/**
	 * Adds a level that this class will accept and pass on to the
	 * {@link #handleMessage(MessageEvent, Object)} method.
	 * 
	 * @see #handleMessage(MessageEvent, Object)
	 * 
	 * @param level
	 *            a level that when found in a message event will cause that
	 *            message to be accepted and passed on
	 */
	public void addListeningLevel(Level level) {
		listeningLevels.add(level);
	}

	/**
	 * Removes a level that this class will accept and pass on to the
	 * {@link #handleMessage(MessageEvent, Object)} method.
	 * 
	 * @see #handleMessage(MessageEvent, Object)
	 * 
	 * @param level
	 *            a level that when found in a message event will cause that
	 *            message NOT to be accepted and passed on
	 */
	public void removeListeningLevel(Level level) {
		listeningLevels.remove(level);
	}

	/**
	 * Retrieves the levels that this class accepts. If there are no Levels
	 * returned (ie the size of the collection is 0), then all Levels are
	 * accepted.
	 * 
	 * @return the levels that this class accepts
	 */
	public Collection getListeningLevels() {
		return listeningLevels;
	}

	/**
	 * This is the method subclasses should (have to) override to receive
	 * {@link MessageEvent}s. The events passed to this method were received in
	 * {@link #messageReceived(MessageEvent)} and contained a Level this class
	 * was listening for.
	 * 
	 * @param event
	 *            the event to act on.
	 */
	protected abstract void handleMessage(MessageEvent event);
}
