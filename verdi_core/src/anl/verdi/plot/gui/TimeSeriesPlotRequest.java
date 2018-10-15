package anl.verdi.plot.gui;

import java.util.ArrayList;
import java.util.List;

import anl.verdi.data.Axes;
import anl.verdi.data.DataFrame;
import anl.verdi.data.DataFrameAxis;
import anl.verdi.data.Slice;
import anl.verdi.data.TimeStepAverager;
import anl.verdi.formula.Formula;
import anl.verdi.plot.config.PlotConfiguration;
import anl.verdi.util.VUnits;

/**
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class TimeSeriesPlotRequest extends AbstractPlotRequest {

	private String name;
	private String range;
	private Formula.Type type;
	boolean calcAverage = false;
	private boolean singleValue = false;
	List<DataFrame> frames = new ArrayList<DataFrame>();
	
	public TimeSeriesPlotRequest(DataFrame frame, Slice slice, Formula.Type type) {
		this (frame, slice, type, null);
	}

	public TimeSeriesPlotRequest(DataFrame frame, Slice slice, Formula.Type type, String title) {
		TimeStepAverager averager = new TimeStepAverager();
		if (frame != null) {
			frames.add(averager.transform(frame));
		}

		this.type = type;
		StringBuilder buf = new StringBuilder();
		if (title != null) {
			range = title;
			buf.append(range);
			buf.append(" ");
		}
		else {
			buf.append("(");
			Axes<DataFrameAxis> axes = frame.getAxes();
			buf.append(getRange(axes.getXAxis().getOrigin(), slice.getXRange().getExtent()));
			buf.append(", ");
			buf.append(getRange(axes.getYAxis().getOrigin(), slice.getYRange().getExtent()));
			buf.append(") ");
			range = buf.toString();
			if (axes.getXAxis().getExtent() == 1 && axes.getYAxis().getExtent() == 1) {
				singleValue = true;
			}
			buf.append("from ");
			buf.append(frame.getVariable().getName());
		}

		this.name = buf.toString();
	}
	
	public void addItem(DataFrame frame, boolean takeAvg) {
		calcAverage = takeAvg;
		frames.add(frame);
	}

	private String getRange(int origin, long extent) {
		if (extent == 1) return String.valueOf(origin + 1);
		else {
			StringBuilder builder = new StringBuilder();
			builder.append(origin + 1);
			builder.append(" - ");
			builder.append((origin + extent - 1) + 1);
			return builder.toString();
		}
	}


	/**
	 * Perform the actual plot creation.
	 */
	public Plot doCreatePlot() {
		PlotConfiguration config = new PlotConfiguration();
		config.setProperty(PlotFactory.TITLE_PREFIX, range);
		List<DataFrame> frameList = frames;
		if (frames.size() > 1) {
			singleValue = false;
			
			if (calcAverage) {
				frameList = new ArrayList<DataFrame>();
				TimeStepAverager averager = new TimeStepAverager();
				frameList.add(averager.transform(frames));
			}
		}
		
		if (singleValue) {
			name += ("from " + frames.get(0).getVariable().getName());
			config.setProperty(PlotFactory.SUBTITLE, "Value of " + range + " " +
							VUnits.getFormattedName(frames.get(0).getVariable().getUnit()));
		} else {
			config.setProperty(PlotFactory.SUBTITLE, "Avg. of " + range + " " +
							VUnits.getFormattedName(frames.get(0).getVariable().getUnit()));
		}

		PlotFactory factory = new PlotFactory();
		PlotPanel panel = factory.getPlot(type, name, frameList, config);
		app.getGui().addPlot(panel);
		panel.addPlotListener(app);
		return panel.getPlot();
	}
}
