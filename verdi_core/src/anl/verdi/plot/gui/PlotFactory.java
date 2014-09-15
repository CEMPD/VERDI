package anl.verdi.plot.gui;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import simphony.util.messages.MessageCenter;
import visad.VisADException;
import anl.verdi.data.DataFrame;
import anl.verdi.formula.Formula;
import anl.verdi.formula.Formula.Type;
import anl.verdi.plot.config.PlotConfiguration;
import anl.verdi.plot.config.VertCrossPlotConfiguration;
import anl.verdi.plot.types.Contour3D;
import anl.verdi.plot.types.LinePlot;
import anl.verdi.plot.types.ScatterPlot;
import anl.verdi.plot.types.TilePlot;
import anl.verdi.plot.types.TimeSeriesBarPlot;
import anl.verdi.plot.types.VectorPlot;
import anl.verdi.plot.types.VerticalCrossSectionPlot;
import anl.verdi.plot.types.VerticalCrossSectionPlot.CrossSectionType;

/**
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class PlotFactory {

	private static final MessageCenter msg = MessageCenter.getMessageCenter(PlotFactory.class);

	public static final String TITLE_PREFIX = PlotFactory.class.getName() + ".TITLE_PREFIX";
	public static final String SUBTITLE = PlotFactory.class.getName() + ".SUBTITLE";
	public static final String LEGEND_MAP = PlotFactory.class.getName() + ".LEGEND_MAP";

	private static Map<Type, String> typeNameMap = new HashMap<Type, String>();

	static {
		typeNameMap.put(Type.TILE, "Tile");
		typeNameMap.put(Type.SCATTER_PLOT, "Scatter");
		typeNameMap.put(Type.CONTOUR, "Contour");
		typeNameMap.put(Type.TIME_SERIES_BAR, "Time Series Bar");
		typeNameMap.put(Type.TIME_SERIES_LINE, "Time Series");
		typeNameMap.put(Type.VERTICAL_CROSS_SECTION, "Vertical Cross Section");
		typeNameMap.put(Type.AREAL_INTERPOLATION, "Areal Interpolation");
	}

	public PlotPanel getScatterPlot(String xFormula, String yFormula, DataFrame xFrame, DataFrame yFrame) {
		Plot plot = new ScatterPlot(xFrame, yFrame);
		return new PlotPanel(plot, xFormula + " x " + yFormula + " Scatter");
	}

	public PlotPanel getVectorPlot(String xFormula, String yFormula, DataFrame xFrame, DataFrame yFrame,
	                               DataFrame tileFrame, PlotConfiguration config) {
		Plot plot = new VectorPlot(xFrame, yFrame, tileFrame, config);
		return new PlotPanel(plot, xFormula + " x " + yFormula + " Vector");
	}

	private String getTypeName(Type type) {
		String name = typeNameMap.get(type);
		if (name == null) return type.toString();
		return name;
	}


	public PlotPanel getPlot(Formula.Type type, String formula, DataFrame frame) {
		return this.getPlot(type, formula, frame, new PlotConfiguration());
	}

	public PlotPanel getPlot(Formula.Type type, String formula, DataFrame frame, PlotConfiguration config) {
		List<DataFrame> frames = new ArrayList<DataFrame>();
		frames.add(frame);
		return getPlot(type, formula, frames, config);
	}

	public PlotPanel getPlot(Formula.Type type, String formula, List<DataFrame> frames, PlotConfiguration config) {
		String name = getTypeName(type) + ": " + formula;
		if (type == Type.TILE) {
			Plot plot = new TilePlot(frames.get(0), config);
			return new PlotPanel(plot, name);
		} else if (type == Type.TIME_SERIES_LINE) {
			Plot plot = new LinePlot(frames, config);
			return new PlotPanel(plot, name);
		} else if (type == Type.TIME_SERIES_BAR) {
			Plot plot = new TimeSeriesBarPlot(frames.get(0), config);
			return new PlotPanel(plot, name);
		} else if (type == Type.CONTOUR) {
			Plot plot = null;
			try {
				plot = new Contour3D(frames.get(0), config);
			} catch (RemoteException e) {
				msg.error("Error creating contour 3D plot", e);
			} catch (VisADException e) {
				msg.error("Error creating contour 3D plot", e);
			}
			return new PlotPanel(plot, name);
		}

		return null;
	}

	public PlotPanel getVerticalCrossPlot(String formula, DataFrame frame, VertCrossPlotConfiguration config) {
		String name = formula + " " + getTypeName(Type.VERTICAL_CROSS_SECTION);
		Plot plot = new VerticalCrossSectionPlot(frame, config);
		return new PlotPanel(plot, name);
	}

	public PlotPanel getVerticalCrossPlot(String formula, DataFrame frame,
	                                      CrossSectionType type, int constAxisIndex) {
		String name = formula + " " + getTypeName(Type.VERTICAL_CROSS_SECTION);
		Plot plot = new VerticalCrossSectionPlot(frame, type, constAxisIndex);
		return new PlotPanel(plot, name);
	}
}
