package gov.epa.emvl;

public interface AxisLabelCreator {
	
	/**
	 * 
	 * @param index - index of the location on the axis to be labeled
	 * @return - label to be used for the given index
	 */
	public String getLabel(int index);

}
