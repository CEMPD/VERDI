package anl.verdi.plot.config;

import java.awt.Color;
import java.awt.Font;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.XYPlot;

/**
 * Configures a JFreeChart using a PlotConfiguration.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class JFreeChartConfigurator {

	private JFreeChart chart;
	private TitleConfigurator titleConfigurator;
	private UnitsConfigurator unitsConfigurator;

	public JFreeChartConfigurator(JFreeChart chart, TitleConfigurator titleConfigurator,
	                              UnitsConfigurator unitsConfigurator) {
		this.chart = chart;
		this.titleConfigurator = titleConfigurator;
		this.unitsConfigurator = unitsConfigurator;
	}

	/**
	 * Configure the chart according to the specified PlotConfiguration.
	 *
	 * @param config the plot configuration
	 */
	public void configure(PlotConfiguration config) {
		configureTitles(config);
		configureAxes(config);
		configureUnits(config);
	}

	private void configureUnits(PlotConfiguration config) {
		if (unitsConfigurator != null) {
			Boolean showLegend = (Boolean) config.getObject(PlotConfiguration.LEGEND_SHOW);
			String label = config.getString(PlotConfiguration.UNITS);
			Color color = config.getColor(PlotConfiguration.UNITS_COLOR);
			Font font = config.getFont(PlotConfiguration.UNITS_FONT);
			unitsConfigurator.configureUnits(showLegend, label, font, color);

			Boolean show = (Boolean) config.getObject(PlotConfiguration.UNITS_SHOW_TICK);
			color = config.getColor(PlotConfiguration.UNITS_TICK_COLOR);
			font = config.getFont(PlotConfiguration.UNITS_TICK_FONT);
			unitsConfigurator.configureUnitsTick(show, font, color);
		}
	}

	private void configureAxes(PlotConfiguration config) {
		Axis domainAxis = null;
		Axis rangeAxis = null;
		if (chart.getPlot() instanceof XYPlot) {
			XYPlot plot = (XYPlot) chart.getPlot();
			domainAxis = plot.getDomainAxis();
			rangeAxis = plot.getRangeAxis();
			configTickLabel(config, domainAxis);
		} else if (chart.getPlot() instanceof CategoryPlot) {
			CategoryPlot plot = (CategoryPlot) chart.getPlot();
			domainAxis = plot.getDomainAxis();
			rangeAxis = plot.getRangeAxis();
			configTickLabel(config, domainAxis);
		}

		String label = config.getString(PlotConfiguration.DOMAIN_LABEL);
		if (label == null) label = "";
		domainAxis.setLabel(label);
		Color color = config.getColor(PlotConfiguration.DOMAIN_COLOR);
		if (color != null) domainAxis.setLabelPaint(color);
		Font font = config.getFont(PlotConfiguration.DOMAIN_FONT);
		if (font != null) domainAxis.setLabelFont(font);

		Boolean show = (Boolean) config.getObject(PlotConfiguration.DOMAIN_SHOW_TICK);
		if (show != null) domainAxis.setTickLabelsVisible(show);
		color = config.getColor(PlotConfiguration.DOMAIN_TICK_COLOR);
		if (color != null) domainAxis.setTickLabelPaint(color);
		font = config.getFont(PlotConfiguration.DOMAIN_TICK_FONT);
		if (font != null) domainAxis.setTickLabelFont(font);

		label = config.getString(PlotConfiguration.RANGE_LABEL);
		if (label == null) label = "";
		rangeAxis.setLabel(label);
		color = config.getColor(PlotConfiguration.RANGE_COLOR);
		if (color != null) rangeAxis.setLabelPaint(color);
		font = config.getFont(PlotConfiguration.RANGE_FONT);
		if (font != null) rangeAxis.setLabelFont(font);

		show = (Boolean) config.getObject(PlotConfiguration.RANGE_SHOW_TICK);
		if (show != null) rangeAxis.setTickLabelsVisible(show);
		color = config.getColor(PlotConfiguration.RANGE_TICK_COLOR);
		if (color != null) rangeAxis.setTickLabelPaint(color);
		font = config.getFont(PlotConfiguration.RANGE_TICK_FONT);
		if (font != null) rangeAxis.setTickLabelFont(font);
	}

	private void configTickLabel(PlotConfiguration config, Axis axis) {
		String format = config.getString(PlotConfiguration.DOMAIN_TICK_LABEL_FORMAT);
		SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, h a");
		String orient = config.getString(PlotConfiguration.DOMAIN_TICK_LABEL_ORIENTATION);
		
		try {
			if (format != null && format.trim().length() != 0) dateFormat = new SimpleDateFormat(format);
		} catch (Exception e) {
			//NOTE: do noting. Better to throw error message to the front. Wait for the infrastructure is ready.
		}

		if (axis instanceof DateAxis) {
			DateAxis dAxis = (DateAxis) axis;
			dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
			dAxis.setDateFormatOverride(dateFormat);
			if (orient != null && "VERTICAL".equals(orient))
				dAxis.setVerticalTickLabels(true);
			
			if (orient != null && !"VERTICAL".equals(orient))
				dAxis.setVerticalTickLabels(false);
		}

		if (axis instanceof CategoryAxis) {
			CategoryAxis cAxis = (CategoryAxis) axis;
			if (orient != null && "VERTICAL".equals(orient)) {
				cAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
			}

			if (orient != null && "LEFTSLANT".equals(orient)) {
				cAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
			}

			if (orient != null && "HORIZONTAL".equals(orient)) {
				cAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(0));
			}

			if (orient != null && "RIGHTSLANT".equals(orient)) {
				cAxis.setCategoryLabelPositions(CategoryLabelPositions.createDownRotationLabelPositions(Math.PI / 4.0));
			}
		}
	}
	
	private void configureTitles(PlotConfiguration config) {
		String text = config.getTitle();
		Color color = (Color) config.getObject(PlotConfiguration.TITLE_COLOR);
		Font font = (Font) config.getObject(PlotConfiguration.TITLE_FONT);
		titleConfigurator.configureTitle(text, font, color);

		text = config.getSubtitle1();
		color = (Color) config.getObject(PlotConfiguration.SUBTITLE_1_COLOR);
		font = (Font) config.getObject(PlotConfiguration.SUBTITLE_1_FONT);
		titleConfigurator.configureSubtitle1(text, font, color);

		text = config.getSubtitle2();
		color = (Color) config.getObject(PlotConfiguration.SUBTITLE_2_COLOR);
		font = (Font) config.getObject(PlotConfiguration.SUBTITLE_2_FONT);
		titleConfigurator.configureSubtitle2(text, font, color);
	}


}
