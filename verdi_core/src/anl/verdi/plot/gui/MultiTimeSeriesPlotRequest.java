package anl.verdi.plot.gui;

import anl.verdi.data.Axes;
import anl.verdi.data.DataFrame;
import anl.verdi.data.DataFrameAxis;
import anl.verdi.data.TimeStepAverager;
import anl.verdi.formula.Formula;
import anl.verdi.plot.config.PlotConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class MultiTimeSeriesPlotRequest extends AbstractPlotRequest {

	private List<DataFrame> frames = new ArrayList<DataFrame>();
	private String name;
	private PlotConfiguration config = new PlotConfiguration();

	public MultiTimeSeriesPlotRequest(String title) {
		this.name = title;
	}

	public void addItem(DataFrame frame, boolean takeAvg) {
		DataFrame avgFrame;
		if (takeAvg) {
			TimeStepAverager averager = new TimeStepAverager();
			avgFrame = averager.transform(frame);
		} else {
			avgFrame = frame;
		}
		frames.add(avgFrame);
		config.putObject(avgFrame, getXY(frame));
	}

	/**
	 * Adds the data frame as item to show and
	 * does a time series average over this frame.
	 *
	 * @param frame the frame to add
	 */
	public void addItem(DataFrame frame) {
		addItem(frame, true);
	}


	/**
	 * Perform the actual plot creation.
	 */
	public Plot doCreatePlot() {
		config.setProperty(PlotFactory.TITLE_PREFIX, name);
		PlotFactory factory = new PlotFactory();
		PlotPanel panel = factory.getPlot(Formula.Type.TIME_SERIES_LINE, name, frames, config);
		app.getGui().addPlot(panel);
		panel.addPlotListener(app);
		return panel.getPlot();
	}

	private String getXY(DataFrame frame) {
		Axes<DataFrameAxis> axes = frame.getAxes();
		StringBuilder builder = new StringBuilder("(");
		builder.append(axes.getXAxis().getOrigin() + 1);
		builder.append(", ");
		builder.append(axes.getYAxis().getOrigin() + 1);
		builder.append(")");
		return builder.toString();
	}
}
