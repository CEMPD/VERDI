package anl.verdi.plot.gui;

import anl.verdi.plot.types.TilePlot;

/**
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class OverlayRequest<T> {

	public enum Type {OBS, VECTOR};

	private Type type;
	private TilePlot plot;
	private FastTilePlot fastPlot;

	public OverlayRequest(Type type, TilePlot plot) {
		this.type = type;
		this.plot = plot;
	}
	
	public OverlayRequest(Type type, FastTilePlot plot) {
		this.type = type;
		this.fastPlot = plot;
	}

	public TilePlot getPlot() {
		return plot;
	}
	
	public FastTilePlot getFastTilePlot() {
		return fastPlot;
	}

	public Type getType() {
		return type;
	}
}
