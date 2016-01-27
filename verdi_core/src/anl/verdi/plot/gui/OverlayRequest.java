/**
 * Altered to change from FastTilePlot to GTTilePlot January 2016
 */
package anl.verdi.plot.gui;

import anl.verdi.plot.gui.GTTilePlot;
import anl.verdi.plot.types.TilePlot;

/**
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class OverlayRequest<T> {

	public enum Type {OBS, VECTOR};

	private Type type;
	private TilePlot plot;
	private GTTilePlot gtTilePlot;

	public OverlayRequest(Type type, TilePlot plot) {
		this.type = type;
		this.plot = plot;
	}
	
	public OverlayRequest(Type type, GTTilePlot plot) {
		this.type = type;
		this.gtTilePlot = plot;
	}

	public TilePlot getPlot() {
		return plot;
	}
	
	public GTTilePlot getGTTilePlot() {
		return gtTilePlot;
	}

	public Type getType() {
		return type;
	}
}
