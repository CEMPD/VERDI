package anl.verdi.plot.color;

import java.awt.Color;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages

import anl.verdi.plot.util.PrintfNumberFormat;

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
	static final Logger Logger = LogManager.getLogger(ColorMap.class.getName());
	
	private static final String DEFAULT_NUMBER_FORMAT = "-2.3f";

	public enum IntervalType {
		CUSTOM, AUTOMATIC 
	}
	
	public enum ScaleType {
		LINEAR, LOGARITHM
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
	private String formatString = null;
	private NumberFormat printfFormat = null;
	
	private double logMin, logMax;
	private double[] logIntervals;
	
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
		Logger.debug("in constructor for ColorMap using Palette, min, max");
	}	

	private void calcIntervals(Palette palette, double min, double max) {
		int colorCount = palette.getColorCount();
		this.intervals = new double[colorCount];
		double interval = (max - min) / colorCount;
		for (int i = 0; i < colorCount; i++) {
			intervals[i] = min + (i * interval);
		}
		Logger.debug("finished with calcIntervals using Palette, min, max");
	}
	
	private void calcLogIntervals(Palette palette, double logMin, double logMax) {
		int colorCount = palette.getColorCount();
		this.logIntervals = new double[colorCount];
		double logInterval = (logMax - logMin) / colorCount;
		for (int i = 0; i < colorCount; i++) {
			this.logIntervals[i] = this.logMin + (i * logInterval);
		}			
		Logger.debug("finished with calcLogIntervals using Palette, logMin, logMax");
	}	
	
	public ColorMap(Palette palette, List<Double> steps, List<Double> logSteps, ScaleType scaleType) { // NOT for logarithm
		Logger.debug("in ColorMap constructor including Palette, List steps, List logSteps, and ScaleType");
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
		Logger.debug(" in getStep[index] for FAST_TILE");
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
	}

	public double getMax() throws Exception {	
		if ( this.scaleType == ScaleType.LOGARITHM) {
			return logMax;
		}		
		return max;				
	}

	public double getMin() throws Exception {
		if ( this.scaleType == ScaleType.LOGARITHM) {
			return logMin;
		}		
		return min;		
	}

	public double[] getIntervals() throws Exception {
		if ( this.scaleType == ScaleType.LOGARITHM) {
			return logIntervals;
		}		
		return intervals;
	}

	public PaletteType getPaletteType() {
		Logger.debug("returning PaletteType = " + paletteType);
		return paletteType;
	}

	public void setPaletteType(PaletteType paletteType) {
		this.paletteType = paletteType;
		Logger.debug("just set PaletteType to " + this.paletteType);
	}

	public Palette getPalette() {
		Logger.debug("in getPalette(), returning Palette = " + palette);
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
		if ( this.scaleType == ScaleType.LOGARITHM) {
			return logIntervals[index];
		}
		return intervals[index];			

	}

	public void setIntervalStart(int index, double start)
			throws Exception { // since only for CUSTOM, do not add check for LOGARITHM
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
	
	public String getFormatString() throws Exception {
		return formatString;
	}
	
	/*
	 * 
	 * Verdi uses printf style formatting strings.  However, code using java's NumberFormat.format() will
	 * display incorrectly since it uses a different pattern api.  Instead, expose PrintfNumberFormat which
	 * uses printf style patterns, and provide getInternalNumberFormat for code that needs the java style
	 * formats.
	 * 
	 */
	public NumberFormat getNumberFormat() throws Exception {
		if (formatString == null)
			formatString = DEFAULT_NUMBER_FORMAT;
		if (printfFormat == null) {
			try {
				printfFormat = new PrintfNumberFormat(formatString);
				printfFormat.format(0.0);
			} catch (Throwable e) {
				printfFormat = new DecimalFormat(formatString);
			}
			
		}
		return printfFormat;
	}
	
	/**
	 * setNumberFormat	set the DecimalFormat in the colorMap to what the user entered
	 * @param aNumberFormat
	 */
	public void setFormatString(String pattern)
	{
		if (pattern == null)
			return;
		formatString = pattern;
		printfFormat = null;
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
			if (intervals[intervals.length - 1] == 0)
				intervals[intervals.length - 1]  = max;
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
			if (logIntervals[logIntervals.length - 1] == 0)
				logIntervals[logIntervals.length - 1]  = max;
			return;
		}
		calcLogIntervals(palette, logMin, logMax);
	}
}
