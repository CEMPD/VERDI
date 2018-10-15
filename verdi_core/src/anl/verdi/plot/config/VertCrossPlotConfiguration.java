package anl.verdi.plot.config;

import anl.verdi.plot.types.VerticalCrossSectionPlot;

/**
 * Configuration info for vertical cross section plots.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class VertCrossPlotConfiguration extends TilePlotConfiguration {

	public static final String CROSS_SECTION_TYPE =
					VertCrossPlotConfiguration.class.getName() + ".cross_section_type";
	public static final String CROSS_SECTION_INDEX =
					VertCrossPlotConfiguration.class.getName() + ".cross_section_index";

	public VertCrossPlotConfiguration() {

	}

	public VertCrossPlotConfiguration(PlotConfiguration config) {
		super(config);
	}

	public void setCrossSectionType(VerticalCrossSectionPlot.CrossSectionType type) {
		putObject(CROSS_SECTION_TYPE, type);
	}

	public VerticalCrossSectionPlot.CrossSectionType getCrossSectionType() {
		return (VerticalCrossSectionPlot.CrossSectionType) getObject(CROSS_SECTION_TYPE);
	}

	public void setCrossSectionRowCol(int rowOrCol) {
		putObject(CROSS_SECTION_INDEX, rowOrCol);
	}

	public Integer getCrossSectionRowCol() {
		return (Integer) getObject(CROSS_SECTION_INDEX);
	}
}
