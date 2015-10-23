package saf.core.ui.event;

import saf.core.ui.dock.DockableFrame;

/**
 * Encapsulates an event that occured on DockableFrame.
 *
 * @author Nick Collier
 * @version $Revision: 1.2 $ $Date: 2005/11/21 18:55:17 $
 */
public class DockableFrameEvent {

  public enum Type {MIN, MAX, FLOAT, RESTORE, CLOSE};

  private Type type;
  private DockableFrame dockable;
	private boolean handled = false;

  /**
   * Creates a DockableFrameEvent of the specified type
   * occuring on the specified dockable frame.
   *
   * @param type the event type
   * @param dockable the dockable the event occured on
   */
  public DockableFrameEvent(Type type, DockableFrame dockable) {
    this.type = type;
    this.dockable = dockable;
  }

  /**
   * Gets the type of event.
   *
   * @return the type of event.
   */
  public Type getType() {
    return type;
  }

  /**
   * Gets the dockable that is the subject
   * of this event.
   *
   * @return the dockable that is the subject
   * of this event. 
   */
  public DockableFrame getDockable() {
    return dockable;
  }

	/**
	 * Gets whether or not this event has been handled.
	 *
	 * @return whether or not this event has been handled.
	 */
	public boolean isHandled() {
		return handled;
	}

	/**
	 * Sets whether or not this event has been handled.
	 *
	 * @param handled whether or not this event has been handled.
	 */
	public void setHandled(boolean handled) {
		this.handled = handled;
	}

}
