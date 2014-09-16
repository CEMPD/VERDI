package saf.core.ui.event;

import saf.core.ui.dock.DockableFrame;

/**
 * Event fired when a DockableFrame is selected or de-selected.
 *
 * @author Nick Collier
 * @version $Revision: 1.3 $ $Date: 2005/11/21 18:55:17 $
 */
public class DockableSelectionEvent {

  public enum Type {GAINED, LOST}

  private DockableFrame frame;
  private Type type;

  /**
   * Creates a new DockableSelectionEvent for the specified frame
   * and of the specified type.
   *
   * @param frame the frame that is the subject of the event
   * @param type the type of the event
   */
  public DockableSelectionEvent(DockableFrame frame, Type type) {
    this.frame = frame;
    this.type = type;
  }


  /**
   * Get the DockableFrame that is the subject of this
   * event.
   *
   * @return the DockableFrame that is the subject of this
   * event.
   */
  public DockableFrame getDockable() {
    return frame;
  }

  /**
   * Gets the type of this event indicating whether this
   * is a gained or lost selection event.
   *
   * @return the type of this event indicating whether this
   * is a gained or lost selection event.
   */
  public Type getType() {
    return type;
  }

  public String toString() {
    String id = frame != null ? frame.getID() : "null";
    String properties = "frame = " + id +
            " type = " + type + " ";
    return getClass().getName() + "[" + properties + "]";
	}
}
