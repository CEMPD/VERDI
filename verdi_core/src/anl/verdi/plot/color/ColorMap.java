package anl.verdi.plot.color;

import java.awt.Color;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;

/**
 * Maps colors to a range of values.
 * 
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class ColorMap implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public enum IntervalType {
		CUSTOM, AUTOMATIC 
	}
	
	public enum ScaleType {
		LINEAR, LOGARITHM
	}
	
	public enum PlotType {
		FAST_TILE, OTHER
	}

	public enum PaletteType {
		SEQUENTIAL, QUALITATIVE, DIVERGING
	}

	public static IntervalType getIntervalType(String type) {
		for (IntervalType iType : IntervalType.values()) {
			if (iType.toString().equalsIgnoreCase(type))
				return iType;
		}

		return null;
	}

	public static PaletteType getPaletteType(String type) {
		for (PaletteType pType : PaletteType.values()) {
			if (pType.toString().equalsIgnoreCase(type))
				return pType;
		}

		return null;
	}
	
	public static ScaleType getScaleType(String type) {
		for (ScaleType iType : ScaleType.values()) {
			if (iType.toString().equalsIgnoreCase(type))
				return iType;
		}

		return null;
	}	

	private Palette palette;
	private double min, max;
	private double[] intervals;
	
	private double logBase = 10.0; //Math.E;
	private PaletteType paletteType = PaletteType.QUALITATIVE;
	private DecimalFormat format;
	
	private double logMin, logMax;
	private double[] logIntervals;
	
	private PlotType plotType = PlotType.FAST_TILE;  
	private IntervalType intervalType = IntervalType.AUTOMATIC;
	private ScaleType scaleType = ScaleType.LINEAR;

	public ColorMap() {
		palette = new Palette(new Color[0], "", false);
	}
	
	public ColorMap(Palette palette, double min, double max) { // NOT for logarithm
// 2014 if this function is NOT for logarithm, why does it call logIntervals?
		this.min = min;
		this.max = max;
		this.palette = new Palette(palette);
		calcIntervals(palette, this.min, this.max);
		//default to something...
		this.logIntervals = new double[palette.getColorCount() + 1];
	}	

	private void calcIntervals(Palette palette, double min, double max) {
		int colorCount = palette.getColorCount();
		this.intervals = new double[colorCount];
		double interval = (max - min) / colorCount;
		for (int i = 0; i < colorCount; i++) {
			intervals[i] = min + (i * interval);
		}
	}
	
	private void calcLogIntervals(Palette palette, double logMin, double logMax) {
		int colorCount = palette.getColorCount();
		this.logIntervals = new double[colorCount];
		double logInterval = (logMax - logMin) / colorCount;
		for (int i = 0; i < colorCount; i++) {
			this.logIntervals[i] = this.logMin + (i * logInterval);
		}			
	}	
	
	public ColorMap(Palette palette, List<Double> steps, List<Double> logSteps, ScaleType scaleType) { // NOT for logarithm
		this.scaleType = scaleType;
		//make sure and sort both of these in ascending fashion...could be defined incorrectly by config file
		Collections.sort(steps);
		Collections.sort(logSteps);
		int logStepSize = logSteps.size();
		int stepSize = steps.size();
		if ( this.scaleType != ScaleType.LOGARITHM ) {
			this.min = steps.get(0);
			this.max = steps.get(stepSize - 1);
		} else {
			if (logStepSize >= 1) {
				this.logMin = logSteps.get(0);
				this.logMax = logSteps.get(logStepSize - 1);
			}
		}
		this.palette = new Palette(palette);
		int colorCount = palette.getColorCount();
		
		//setup default color map step/range intervals
		this.intervals = new double[colorCount + 1];
		for (int i = 0; i < stepSize; i++) {
			this.intervals[i] = steps.get(i);
		}
		this.logIntervals = new double[colorCount + 1];
		for (int i = 0; i < logStepSize; i++) {
			this.logIntervals[i] = logSteps.get(i);
		}
	}

	public int getMaxIndex() {
		if ( this.scaleType == ScaleType.LOGARITHM ) {
			return logIntervals.length - 1;
		}
		
		return intervals.length - 1;
	}

	public double getStep(int index) throws Exception {
		if ( this.plotType == PlotType.FAST_TILE) {
			if ( this.scaleType == ScaleType.LOGARITHM) {
				if (index > logIntervals.length - 1)
					return logIntervals[logIntervals.length - 1];

				if (index < 0)
					return logIntervals[0];

				return logIntervals[index];			
			} else {
				if (index > intervals.length - 1)
					return intervals[intervals.length - 1];

				if (index < 0)
					return intervals[0];

				return intervals[index];
			}			
		} else {
			if ( this.scaleType == ScaleType.LOGARITHM) {
				
				throw new Exception("Logarithm is not supported for PlotType " + plotType);
		
			} else {
				if (index > intervals.length - 1)
					return intervals[intervals.length - 1];

				if (index < 0)
					return intervals[0];

				return intervals[index];
			}			
		}
	}

	public double getMax() throws Exception {
		
		if ( this.plotType == PlotType.FAST_TILE) {
			if ( this.scaleType == ScaleType.LOGARITHM) {
				return logMax;
			}		
			return max;		
		} else {
			if ( this.scaleType == ScaleType.LOGARITHM) {
				throw new Exception("Logarithm is not supported for PlotType " + plotType);
			}		
			return max;
		}		
	}

	public double getMin() throws Exception {
		if ( plotType == PlotType.FAST_TILE) {
			if ( this.scaleType == ScaleType.LOGARITHM) {
				return logMin;
			}		
			return min;		
		} else {
			if ( this.scaleType == ScaleType.LOGARITHM) {
				throw new Exception("Logarithm is not supported for PlotType " + plotType);
			}		
			return min;
		}
	}

	public double[] getIntervals() throws Exception {
		if ( plotType == PlotType.FAST_TILE) {
			if ( this.scaleType == ScaleType.LOGARITHM) {
				return logIntervals;
			}		
			return intervals;			
		} else {
			if ( this.scaleType == ScaleType.LOGARITHM) {
				throw new Exception("Logarithm is not supported for PlotType " + plotType);
			}		
			return intervals;
		}
	}

	public PaletteType getPaletteType() {
		return paletteType;
	}

	public void setPaletteType(PaletteType paletteType) {
		this.paletteType = paletteType;
	}

	public Palette getPalette() {
		return palette;
	}

	public void setPalette(Palette palette) {
		int prevIntervals = this.palette.getColorCount();
		int curIntervals = palette.getColorCount();
		this.palette = new Palette(palette); //palette;

		if (prevIntervals != curIntervals) {
			calcIntervals(palette, min, max);
			calcLogIntervals(palette, logMin, logMax);
		}
	}

	public Color getColor(int index) {
		return palette.getColor(index);
	}

	public void setColor(int index, Color color) {
		palette.setColor(index, color);
	}

	public int getColorCount() {
		return palette.getColorCount();
	}

	public double getIntervalStart(int index) throws Exception {
		if ( plotType == PlotType.FAST_TILE) {
			if ( this.scaleType == ScaleType.LOGARITHM) {
				return logIntervals[index];
			}
			return intervals[index];			
		} else {
			if ( this.scaleType == ScaleType.LOGARITHM) {
				throw new Exception("Logarithm is not supported for PlotType " + plotType);
			}
			return intervals[index];			
		}
	}

	public void setIntervalStart(int index, double start)
			throws Exception { // since only for CUSTOM, do not add check for LOGARITHM
		
		if ( this.plotType == PlotType.FAST_TILE) {
			if ( this.scaleType != ScaleType.LOGARITHM) {
				int last = intervals.length - 1;

				if (paletteType == PaletteType.SEQUENTIAL && index > 0) {
					if (index < last && start <= intervals[index - 1])
						return;

					if (index < last && start >= intervals[index + 1])
						return;

					if (index == 0)
						min = start;

					if (index == last)
						max = start + (start - intervals[index - 1]);
				}

				intervals[index] = start;
			} else {
				int last = logIntervals.length - 1;

				if (paletteType == PaletteType.SEQUENTIAL && index > 0) {
					if (index < last && start <= logIntervals[index - 1])
						return;

					if (index < last && start >= logIntervals[index + 1])
						return;

					if (index == 0)
						min = start;

					if (index == last)
						max = start + (start - logIntervals[index - 1]);
				}

				logIntervals[index] = start;			
			}			
		} else {
			if ( this.scaleType == ScaleType.LOGARITHM) {
				
				throw new Exception("Logarithm is not supported for PlotType " + plotType);

			} else {
				int last = intervals.length - 1;

				if (paletteType == PaletteType.SEQUENTIAL && index > 0) {
					if (index < last && start <= intervals[index - 1])
						return;

					if (index < last && start >= intervals[index + 1])
						return;

					if (index == 0)
						min = start;

					if (index == last)
						max = start + (start - intervals[index - 1]);
				}

				intervals[index] = start;
			}
		}
	}

	public IntervalType getIntervalType() {
		return intervalType;
	}

	public void setIntervalType(IntervalType intervalType) {
		this.intervalType = intervalType;
	}
	
	public ScaleType getScaleType() {
		return scaleType;
	}

	public void setScaleType(ScaleType scaleType) {
		this.scaleType = scaleType;
	}	
	
	public double getLogBase() {
		return logBase;
	}
	
	public void setLogBase(double base) {
		this.logBase = base;
//		calcLogIntervals(palette, logMin, logMax);
	}	

	public NumberFormat getNumberFormat() throws Exception {
		
		if (format != null)
			return format;
		
		if ( this.plotType == PlotType.FAST_TILE) {
			if ( this.scaleType == ScaleType.LOGARITHM) {
				String strMin = String.valueOf(logMin);
				String strMax = String.valueOf(logMax);

				if (strMin.contains("E-") || strMax.contains("E-"))
					return new DecimalFormat("0.000E0");

				return new DecimalFormat("0.000");
			}
			
			String strMin = String.valueOf(min);
			String strMax = String.valueOf(max);

			if (strMin.contains("E-") || strMax.contains("E-"))
				return new DecimalFormat("0.000E0");

			return new DecimalFormat("0.000");			
		} else {
			if ( this.scaleType == ScaleType.LOGARITHM) {
				throw new Exception("Logarithm is not supported for PlotType " + plotType);
			}
			
			String strMin = String.valueOf(min);
			String strMax = String.valueOf(max);

			if (strMin.contains("E-") || strMax.contains("E-"))
				return new DecimalFormat("0.000E0");

			return new DecimalFormat("0.000");				
		}

	}

	public void setNumberFormat(NumberFormat format) throws Exception {
		if (!(format instanceof DecimalFormat))
			throw new Exception("Number format: " + format.toString()
					+ " is not supported.");

		this.format = (DecimalFormat) format;
	}

	public void setPlotType(PlotType plotType) {
		this.plotType = plotType;
	}

	public PlotType getPlotType() {
		return plotType;
	}
	
	public void setMinMax(double min, double max) {
		this.min = min;
		this.max = max;
		calcIntervals(palette, min, max);
	}

	public void setMinMax(double min, double max, boolean keepOverridenIntervals) {
		this.min = min;
		this.max = max;
		if (keepOverridenIntervals) {
			return;
		}
		calcIntervals(palette, min, max);
	}

	public void setLogMinMax(double min, double max) {
		this.logMin = min;
		this.logMax = max;	
		calcLogIntervals(palette, logMin, logMax);
	}

	public void setLogMinMax(double min, double max, boolean keepOverridenIntervals) {
		this.logMin = min;
		this.logMax = max;	
		if (keepOverridenIntervals) {
			return;
		}
		calcLogIntervals(palette, logMin, logMax);
	}
}
