package anl.verdi.plot.types;

import java.awt.Color;
import java.awt.Font;
import java.text.SimpleDateFormat;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.ui.RectangleEdge;

import anl.verdi.plot.config.PlotConfiguration;
import anl.verdi.plot.config.TitleConfigurator;

/**
 * Encapsulates the titles in a JFreeChart.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class JChartTitlesLabels {

	private JFreeChart chart;
	protected int subTitle1Index, subTitle2Index, bottomTitle1Index, bottomTitle2Index;

	public JChartTitlesLabels(JFreeChart chart) {
		this.chart = chart;
		TextTitle title = new TextTitle();
		title.setPosition(RectangleEdge.BOTTOM);
		int index = chart.getSubtitleCount();
		chart.addSubtitle(title);
		// 2 is first because titles are pushed "down" as they are added
		bottomTitle2Index = index++;

		title = new TextTitle();
		title.setPosition(RectangleEdge.BOTTOM);
		chart.addSubtitle(title);
		bottomTitle1Index = index++;

		title = new TextTitle();
		title.setPosition(RectangleEdge.TOP);
		chart.addSubtitle(title);
		subTitle1Index = index++;
		
		title = new TextTitle();
		title.setPosition(RectangleEdge.TOP);
		chart.addSubtitle(title);
		subTitle2Index = index++;
	}

	public void setTitle(String text) {
		chart.setTitle(text);
	}

	public PlotConfiguration getConfiguration(PlotConfiguration config) {
		config.setTitle(chart.getTitle().getText());
		config.putObject(PlotConfiguration.TITLE_FONT, chart.getTitle().getFont());
		config.putObject(PlotConfiguration.TITLE_COLOR, (Color) chart.getTitle().getPaint());

		TextTitle title = (TextTitle) chart.getSubtitle(subTitle1Index);
		config.setSubtitle1(title.getText());
		config.putObject(PlotConfiguration.SUBTITLE_1_FONT, title.getFont());
		config.putObject(PlotConfiguration.SUBTITLE_1_COLOR, (Color) title.getPaint());

		title = (TextTitle) chart.getSubtitle(subTitle2Index);
		config.setSubtitle2(title.getText());
		config.putObject(PlotConfiguration.SUBTITLE_2_FONT, title.getFont());
		config.putObject(PlotConfiguration.SUBTITLE_2_COLOR, (Color) title.getPaint());

		if (chart.getPlot() instanceof XYPlot) {
			XYPlot plot = (XYPlot) chart.getPlot();
			ValueAxis axis = plot.getDomainAxis();
			config.putObject(PlotConfiguration.DOMAIN_LABEL, axis.getLabel());
			config.putObject(PlotConfiguration.DOMAIN_FONT, axis.getLabelFont());
			config.putObject(PlotConfiguration.DOMAIN_COLOR, axis.getLabelPaint());

			config.putObject(PlotConfiguration.DOMAIN_SHOW_TICK, axis.isTickLabelsVisible());
			config.putObject(PlotConfiguration.DOMAIN_TICK_COLOR, (Color) axis.getTickLabelPaint());
			config.putObject(PlotConfiguration.DOMAIN_TICK_FONT, axis.getTickLabelFont());
			
			if (axis instanceof DateAxis) {
				DateAxis dAxis = (DateAxis) axis;
				config.putObject(PlotConfiguration.DOMAIN_TICK_LABEL_FORMAT, ((SimpleDateFormat) dAxis.getDateFormatOverride()).toPattern());
				config.putObject(PlotConfiguration.DOMAIN_TICK_LABEL_ORIENTATION,(dAxis.isVerticalTickLabels()) ? "VERTICAL" : "HORIZONTAL");
			}

			axis = plot.getRangeAxis();
			config.putObject(PlotConfiguration.RANGE_LABEL, axis.getLabel());
			config.putObject(PlotConfiguration.RANGE_FONT, axis.getLabelFont());
			config.putObject(PlotConfiguration.RANGE_COLOR, axis.getLabelPaint());

			config.putObject(PlotConfiguration.RANGE_SHOW_TICK, axis.isTickLabelsVisible());
			config.putObject(PlotConfiguration.RANGE_TICK_COLOR, (Color) axis.getTickLabelPaint());
			config.putObject(PlotConfiguration.RANGE_TICK_FONT, axis.getTickLabelFont());
		} else if (chart.getPlot() instanceof CategoryPlot) {
			CategoryPlot plot = (CategoryPlot) chart.getPlot();
			Axis axis = plot.getDomainAxis();
			config.putObject(PlotConfiguration.DOMAIN_LABEL, axis.getLabel());
			config.putObject(PlotConfiguration.DOMAIN_FONT, axis.getLabelFont());
			config.putObject(PlotConfiguration.DOMAIN_COLOR, axis.getLabelPaint());

			config.putObject(PlotConfiguration.DOMAIN_SHOW_TICK, axis.isTickLabelsVisible());
			config.putObject(PlotConfiguration.DOMAIN_TICK_COLOR, (Color) axis.getTickLabelPaint());
			config.putObject(PlotConfiguration.DOMAIN_TICK_FONT, axis.getTickLabelFont());

			CategoryAxis cAxis = (CategoryAxis) axis;
			CategoryLabelPositions positions = cAxis.getCategoryLabelPositions();

			if (positions != null && positions.equals(CategoryLabelPositions.UP_90)) {
				config.putObject(PlotConfiguration.DOMAIN_TICK_LABEL_ORIENTATION, "VERTICAL");
			}

			if (positions != null && positions.equals(CategoryLabelPositions.UP_45)) {
				config.putObject(PlotConfiguration.DOMAIN_TICK_LABEL_ORIENTATION, "LEFTSLANT");
			}

			if (positions != null && positions.equals(CategoryLabelPositions.createUpRotationLabelPositions(0))) {
				config.putObject(PlotConfiguration.DOMAIN_TICK_LABEL_ORIENTATION, "HORIZONTAL");
			}

			if (positions != null
					&& positions.equals(CategoryLabelPositions.createDownRotationLabelPositions(Math.PI / 4.0))) {
				config.putObject(PlotConfiguration.DOMAIN_TICK_LABEL_ORIENTATION, "RIGHTSLANT");
			}
			
			ValueAxis vAxis = plot.getRangeAxis();
			config.putObject(PlotConfiguration.RANGE_LABEL, vAxis.getLabel());
			config.putObject(PlotConfiguration.RANGE_FONT, vAxis.getLabelFont());
			config.putObject(PlotConfiguration.RANGE_COLOR, vAxis.getLabelPaint());

			config.putObject(PlotConfiguration.RANGE_SHOW_TICK, vAxis.isTickLabelsVisible());
			config.putObject(PlotConfiguration.RANGE_TICK_COLOR, (Color) vAxis.getTickLabelPaint());
			config.putObject(PlotConfiguration.RANGE_TICK_FONT, vAxis.getTickLabelFont());
		}

		return config;
	}

	public TitleConfigurator getTitleConfigurator() {
		return new TitleConfigurator() {
			public void configureSubtitle1(String text, Font font, Color color) {
				TextTitle title = (TextTitle) chart.getSubtitle(subTitle1Index);
				updateTextTitle(title, text, color, font);
			}

			public void configureSubtitle2(String text, Font font, Color color) {
				TextTitle title = (TextTitle) chart.getSubtitle(subTitle2Index);
				updateTextTitle(title, text, color, font);
			}

			public void configureTitle(String text, Font font, Color color) {
				TextTitle title = chart.getTitle();
				updateTextTitle(title, text, color, font);
			}
		};
	}

	private void updateTextTitle(TextTitle title, String text, Color color, Font font) {
		if (title != null) {
			if (text != null && text.length() > 0) {
				if (text != null) title.setText(text);
				if (color != null) title.setPaint(color);
				if (font != null) title.setFont(font);
			} else {
				title.setText("");
			}
		}
	}

}
