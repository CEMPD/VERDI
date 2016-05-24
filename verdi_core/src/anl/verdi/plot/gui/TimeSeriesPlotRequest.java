package anl.verdi.plot.gui;

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

	private DataFrame frame;
	private String name;
	private String range;
	private Formula.Type type;
	private boolean singleValue = false;

	public TimeSeriesPlotRequest(DataFrame frame, Slice slice, Formula.Type type) {
		TimeStepAverager averager = new TimeStepAverager();
		this.frame = averager.transform(frame);
		this.type = type;
		StringBuilder buf = new StringBuilder("(");
		Axes<DataFrameAxis> axes = frame.getAxes();
		buf.append(getRange(axes.getXAxis().getOrigin(), slice.getXRange().getExtent()));
		buf.append(", ");
		buf.append(getRange(axes.getYAxis().getOrigin(), slice.getYRange().getExtent()));
		buf.append(") ");
		range = buf.toString();
		buf.append("from ");
		buf.append(frame.getVariable().getName());

		this.name = buf.toString();

		if (axes.getXAxis().getExtent() == 1 && axes.getYAxis().getExtent() == 1) {
			singleValue = true;
		}
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
		if (singleValue) {
			config.setProperty(PlotFactory.SUBTITLE, "Value of " + range + " " +
							VUnits.getFormattedName(frame.getVariable().getUnit()));
		} else {
			config.setProperty(PlotFactory.SUBTITLE, "Avg. of " + range + " " +
							VUnits.getFormattedName(frame.getVariable().getUnit()));
		}

		PlotFactory factory = new PlotFactory();
		PlotPanel panel = factory.getPlot(type, name, frame, config);
		app.getGui().addPlot(panel);
		panel.addPlotListener(app);
		return panel.getPlot();
	}
}
