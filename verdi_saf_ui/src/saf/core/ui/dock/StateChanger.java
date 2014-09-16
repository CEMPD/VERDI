package saf.core.ui.dock;

import static saf.core.ui.dock.DockingManager.MinimizeLocation.BOTTOM;
import static saf.core.ui.dock.DockingManager.MinimizeLocation.LEFT;
import static saf.core.ui.dock.DockingManager.MinimizeLocation.RIGHT;
import static saf.core.ui.dock.DockingManager.MinimizeLocation.TOP;
import static saf.core.ui.event.DockableFrameEvent.Type.CLOSE;
import static saf.core.ui.event.DockableFrameEvent.Type.FLOAT;
import static saf.core.ui.event.DockableFrameEvent.Type.MAX;
import static saf.core.ui.event.DockableFrameEvent.Type.MIN;
import static saf.core.ui.event.DockableFrameEvent.Type.RESTORE;

import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import javax.swing.LayoutFocusTraversalPolicy;
import javax.swing.SwingUtilities;

import saf.core.ui.event.DockableFrameEvent;
import saf.core.ui.event.DockableFrameListener;
import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CLocation;
import bibliothek.gui.dock.common.action.predefined.CCloseAction;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.common.location.CFlapIndexLocation;
import bibliothek.gui.dock.common.mode.ExtendedMode;
import bibliothek.gui.dock.displayer.DisplayerFocusTraversalPolicy;
import bibliothek.gui.dock.focus.DockFocusTraversalPolicy;
import bibliothek.gui.dock.station.DockableDisplayer;
import bibliothek.gui.dock.themes.basic.BasicDockableDisplayer;

/**
 * Changes a dockable's state -- minimized, normalized, etc.
 * 
 * @author Nick Collier
 */
public class StateChanger {

	// a functor passed to the fire method
	// to call the appropriate listener method
	private static interface Fire {
		void fire(DockableFrameListener listener, DockableFrameEvent evt);
	}

	private CCloseAction closeAction;
	private DefaultDockingManager dockingManager;
	private List<DockableFrameListener> listeners = new ArrayList<DockableFrameListener>();

	/**
	 * Creates a StateChanger that uses the specified control to effect the
	 * state changes.
	 * 
	 * @param control
	 *            the control to use to change the state
	 * @param dockingManager
	 *            the dockingManger used by this StateChanger
	 */
	public StateChanger(CControl control, DefaultDockingManager dockingManager) {
		this.dockingManager = dockingManager;
		closeAction = new CCloseAction(control);
	}

	/**
	 * Adds the specified DockableFrameListener to listener for state changes
	 * fired by this StateChanger.
	 * 
	 * @param listener
	 *            the listener to add
	 */
	public void addDockableFrameListener(DockableFrameListener listener) {
		listeners.add(listener);
	}

	/**
	 * Removes the specified DockableFrameListener.
	 * 
	 * @param listener
	 *            the listener to remove
	 */
	public void removeDockableFrameListener(DockableFrameListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Attempts to close the specified dockable in the specified perspective.
	 * 
	 * @param perspectiveID
	 *            the perspective id
	 * @param dockable
	 *            the dockable to close
	 */
	public void closeDockable(String perspectiveID, CDockable dockable) {
		DockableFrameEvent evt = createEvent(dockable, CLOSE);
		fire(evt, new Fire() {
			public void fire(DockableFrameListener listener,
					DockableFrameEvent evt) {
				listener.dockableClosing(evt);
			}
		});

		if (!evt.isHandled()) {
			// rarely the focus policy get screwed up
			// leading to StackOverflow and hanging
			fixFocusPolicy(evt.getDockable());
			closeAction.close(dockable);
			dockingManager.removeDockable(perspectiveID, dockable);

			fire(evt, new Fire() {
				public void fire(DockableFrameListener listener,
						DockableFrameEvent evt) {
					listener.dockableClosed(evt);
				}
			});
		}

	}

	/**
	 * Attempts to close the specified dockable in the current perspective.
	 * 
	 * @param dockable
	 *            the dockable to close
	 */
	public void closeDockable(CDockable dockable) {

		DockableFrameEvent evt = createEvent(dockable, CLOSE);
		fire(evt, new Fire() {
			public void fire(DockableFrameListener listener,
					DockableFrameEvent evt) {
				listener.dockableClosing(evt);
			}
		});

		if (!evt.isHandled()) {
			// rarely the focus policy get screwed up
			// leading to StackOverflow and hanging
			fixFocusPolicy(evt.getDockable());
			closeAction.close(dockable);
			dockingManager.removeDockable(dockingManager.getPerspective()
					.getID(), dockable);

			fire(evt, new Fire() {
				public void fire(DockableFrameListener listener,
						DockableFrameEvent evt) {
					listener.dockableClosed(evt);
				}
			});
		}

	}

	/**
	 * Fixes the focus policy of any components in the frame's container 
	 * hierarchy, if necessary. This works around an error arising from
	 * Swing's focus policies interacting with the dockable frames library.
	 * 
	 * @param frame the frame that may need fixing
	 */
	public void fixFocusPolicy(DockableFrame frame) {
		Container parent = frame.getContentPane();
		while (parent != null) {
			FocusTraversalPolicy policy = parent.getFocusTraversalPolicy();
			if (policy != null
					&& policy.getClass().getName().equals(
							"javax.swing.LegacyGlueFocusTraversalPolicy")) {

				if (parent instanceof BasicDockableDisplayer) {
					parent.setFocusTraversalPolicy(new DockFocusTraversalPolicy(
									new DisplayerFocusTraversalPolicy((DockableDisplayer)parent),true));
				} else {
					parent.setFocusTraversalPolicy(new LayoutFocusTraversalPolicy());
				}
				break;
			}
			parent = parent.getParent();
		}
	}

	/**
	 * Attempts to restore the specified dockable.
	 * 
	 * @param dockable
	 *            the dockable to restore
	 */
	public void restoreDockable(CDockable dockable) {
		DockableFrameEvent evt = createEvent(dockable, RESTORE);
		fire(evt, new Fire() {
			public void fire(DockableFrameListener listener,
					DockableFrameEvent evt) {
				listener.dockableRestoring(evt);
			}
		});

		if (!evt.isHandled()) {
			// rarely the focus policy get screwed up
			// leading to StackOverflow and hanging
			fixFocusPolicy(evt.getDockable());
			dockable.setExtendedMode(ExtendedMode.NORMALIZED);

			fire(evt, new Fire() {
				public void fire(DockableFrameListener listener,
						DockableFrameEvent evt) {
					listener.dockableRestored(evt);
				}
			});
		}
	}

	/**
	 * Attempts to float the specified dockable.
	 * 
	 * @param dockable
	 *            the dockable to float
	 */
	public void floatDockable(CDockable dockable) {
		DockableFrameEvent evt = createEvent(dockable, FLOAT);
		fire(evt, new Fire() {
			public void fire(DockableFrameListener listener,
					DockableFrameEvent evt) {
				listener.dockableFloating(evt);
			}
		});

		if (!evt.isHandled()) {
			// dockable.setExtendedMode(CDockable.ExtendedMode.EXTERNALIZED);

			// make sure the dockables location is on top of where it was
			// but now externalized
			// rarely the focus policy get screwed up
			// leading to StackOverflow and hanging
			fixFocusPolicy(evt.getDockable());
			Point corner = new Point();
			Component comp = dockable.intern().getComponent();
			SwingUtilities.convertPointToScreen(corner, comp);
			dockable.setLocation(CLocation.external(corner.x, corner.y, comp
					.getWidth(), comp.getHeight()));

			fire(evt, new Fire() {
				public void fire(DockableFrameListener listener,
						DockableFrameEvent evt) {
					listener.dockableFloated(evt);
				}
			});
		}
	}

	/**
	 * Attempts to maximize the specified dockable.
	 * 
	 * @param dockable
	 *            the dockable to maximize
	 */
	public void maximizeDockable(CDockable dockable) {
		DockableFrameEvent evt = createEvent(dockable, MAX);
		fire(evt, new Fire() {
			public void fire(DockableFrameListener listener,
					DockableFrameEvent evt) {
				listener.dockableMaximizing(evt);
			}
		});

		if (!evt.isHandled()) {
			// rarely the focus policy get screwed up
			// leading to StackOverflow and hanging
			fixFocusPolicy(evt.getDockable());
			dockable.setExtendedMode(ExtendedMode.MAXIMIZED);
			fire(evt, new Fire() {
				public void fire(DockableFrameListener listener,
						DockableFrameEvent evt) {
					listener.dockableMaximized(evt);
				}
			});
		}
	}

	/**
	 * Attempts to minimize the specified dockable to the specified location.
	 * 
	 * @param dockable
	 *            the dockable to minimize
	 * @param minLocation
	 *            the location to minimize to
	 */
	public void minimizeDockable(CDockable dockable,
			DockingManager.MinimizeLocation minLocation) {

		DockableFrameEvent evt = createEvent(dockable, MIN);
		fire(evt, new Fire() {
			public void fire(DockableFrameListener listener,
					DockableFrameEvent evt) {
				listener.dockableMinimizing(evt);
			}
		});

		if (!evt.isHandled()) {
			// rarely the focus policy get screwed up
			// leading to StackOverflow and hanging
			fixFocusPolicy(evt.getDockable());

			CFlapIndexLocation loc;
			if (minLocation == TOP)
				loc = CLocation.base().minimalNorth();
			else if (minLocation == BOTTOM)
				loc = CLocation.base().minimalSouth();
			else if (minLocation == LEFT)
				loc = CLocation.base().minimalWest();
			else if (minLocation == RIGHT)
				loc = CLocation.base().minimalEast();
			else
				loc = CLocation.base().minimalSouth();
			dockable.setLocation(loc);

			fire(evt, new Fire() {
				public void fire(DockableFrameListener listener,
						DockableFrameEvent evt) {
					listener.dockableMinimized(evt);
				}
			});
		}

	}

	private DockableFrameEvent createEvent(CDockable dockable,
			DockableFrameEvent.Type type) {
		DockableFrame frame = dockingManager.getDockableFrameFor(dockable);
		return new DockableFrameEvent(type, frame);
	}

	// this is a bit of trick to avoid having to type this loop
	// over and over again
	private void fire(DockableFrameEvent evt, Fire fire) {
		for (DockableFrameListener listener : listeners) {
			fire.fire(listener, evt);
		}
	}
}
