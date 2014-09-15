package anl.verdi.io;

import anl.verdi.gui.AbstractListElement;

/**
 * Abstract base class for easier to persist form of ListElements.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public abstract class ListElementDescriptor {
	
	protected int timeMin;
	protected int timeMax;
	protected int layerMin;
	protected int layerMax;
	protected boolean timeUsed;
	protected boolean layerUsed;

	public ListElementDescriptor(AbstractListElement element) {
		timeUsed = element.isTimeUsed();
		layerUsed = element.isLayerUsed();
		layerMax = element.getLayerMax();
		timeMin = element.getTimeMin();
		layerMin = element.getLayerMin();
		timeMax = element.getTimeMax();
	}

	public boolean isLayerUsed() {
		return layerUsed;
	}

	public boolean isTimeUsed() {
		return timeUsed;
	}

	public int getLayerMax() {
		return layerMax;
	}

	public int getLayerMin() {
		return layerMin;
	}

	public int getTimeMax() {
		return timeMax;
	}

	public int getTimeMin() {
		return timeMin;
	}
}
