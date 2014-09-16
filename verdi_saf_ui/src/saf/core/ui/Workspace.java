package saf.core.ui;

import saf.core.ui.dock.DockingManager;

/**
 * The workspace contains the main application
 * components: the application mediator and
 * the docking manager.
 *
 * @author Nick Collier
 */
public class Workspace<T> {

	private T app;
	private DockingManager dockingManager;

	public Workspace(T applicationMediator) {
		app = applicationMediator;
	}

	void setViewManager(DockingManager manager) {
		dockingManager = manager;
	}

	public T getApplicationMediator() {
		return app;
	}

	public DockingManager getDockingManager() {
		return dockingManager;
	}
}
