package anl.verdi.plot.gui;

import javax.swing.SpinnerNumberModel;

public class DoubleSpinnerModel extends SpinnerNumberModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4548420004247519107L;
	
	public DoubleSpinnerModel() {
		this(Double.valueOf(0), null, null, Double.valueOf(1));
	}
	
    @SuppressWarnings("rawtypes")
	public DoubleSpinnerModel(Number value, Comparable minimum, Comparable maximum, Number stepSize) {
    	super(value, minimum, maximum, stepSize);
    	setMinimum(minimum);
    	setMaximum(maximum);
    }
    
	public DoubleSpinnerModel(int value, int minimum, int maximum, int stepSize) {
    	super(Double.valueOf(value), Double.valueOf(minimum), Double.valueOf(maximum), Double.valueOf(stepSize));
    }
    
    
	/*public void setValue(Object value) {
		super.setValue(value);
	}
	
	public Object getValue() {
		return super.getValue();
	}*/
	

	/*double value = 0;
	
	double min = Double.MIN_VALUE;
	double max = Double.MAX_VALUE;
	
	public DoubleSpinnerModel(double initialValue, double minValue, double maxValue) {
		value = initialValue;
		min = minValue;
		max = maxValue;
	}
	
	@Override
	public Object getValue() {
		return value;
	}

	@Override
	public void setValue(Object v) {
		if (v instanceof String) {
			try {
				value = Double.parseDouble((String)v);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Illegal value " + v);
			}
		} else if (v instanceof Number) {
			value = ((Number)v).doubleValue();
		}
		
	}
	
	public void setMinimum(Double v) {
		min = v;
	}
	
	public Double getMinimum() {
		return min;
	}	
	
	public void setMaximum(Double v) {
		max = v;
	}
	
	public Double getMaximum() {
		return max;
	}*/
	
    public void setMinimum(Comparable minimum) {
    	if (minimum instanceof Number) {
    		super.setMinimum(((Number)minimum).doubleValue());
    	} else
    		super.setMinimum(minimum);
    }
    
    public void setMaximum(Comparable maximum) {
    	if (maximum instanceof Number) {
    		super.setMaximum(((Number)maximum).doubleValue());
    	} else
    		super.setMaximum(maximum);
    }

	@Override
	public Object getNextValue() {
		
		Double prev = ((Number)getValue()).doubleValue();
		
		if (getMaximum().compareTo(prev + 1) <= 0)
			return null;
		
		double next = prev + 1;
		return next;
	}

	@Override
	public Object getPreviousValue() {
		Double prev = ((Number)getValue()).doubleValue();
		
		if (getMinimum().compareTo(prev - 1) >= 0)
				return null;

		double next = prev - 1;
		return next;
	}

}
