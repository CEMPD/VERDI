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
	public static final String CROSS_SECTION_DISPLAY_MODE =
			VertCrossPlotConfiguration.class.getName() + ".cross_display_mode";
	public static final String CROSS_SECTION_SLICE_SIZE =
			VertCrossPlotConfiguration.class.getName() + ".cross_slice_size";
	//public static final String CROSS_SECTION_SLICE_UNITS =
	//		VertCrossPlotConfiguration.class.getName() + ".cross_slice_units";

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

	public void setCrossSectionRowCol(double rowOrCol) {
		putObject(CROSS_SECTION_INDEX, rowOrCol);
	}
	
	/*public void setCrossSectionSliceUnits(String sliceUnits) {
		putObject(CROSS_SECTION_SLICE_UNITS, sliceUnits);
	}
	
	public String getCrossSectionSliceUnits() {
		return (String) getObject(CROSS_SECTION_SLICE_UNITS);
	}*/
	
	public void setCrossSectionSliceSize(double sliceSize) {
		putObject(CROSS_SECTION_SLICE_SIZE, sliceSize);
	}
	
	public Double getCrossSectionSliceSize() {
		return (Double) getObject(CROSS_SECTION_SLICE_SIZE);
	}

	public Double getCrossSectionRowCol() {
		return (Double) getObject(CROSS_SECTION_INDEX);
	}
	
	public void setDisplayMode(int displayMode) {
		putObject(CROSS_SECTION_DISPLAY_MODE, displayMode);
	}

	public Integer getDisplayMode() {
		return (Integer) getObject(CROSS_SECTION_DISPLAY_MODE);
	}
}
