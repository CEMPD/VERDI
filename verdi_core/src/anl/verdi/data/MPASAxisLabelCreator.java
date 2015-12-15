package anl.verdi.data;

import java.text.DecimalFormat;
import java.util.ArrayList;

import gov.epa.emvl.AxisLabelCreator;

public class MPASAxisLabelCreator implements AxisLabelCreator {

	CoordAxis axis;
	ArrayList<String> labels = new ArrayList<String>();
	
	public MPASAxisLabelCreator(CoordAxis axis) {
		this.axis = axis;
		int length = (int)(axis.getRange().getExtent() - axis.getRange().getOrigin());
		double range = axis.getValue((int)axis.getRange().getUpperBound()) - axis.getValue((int)axis.getRange().getLowerBound());
		// if range is over 90% of possible ranges start and end legends at full values	
		boolean fullRange = false;
		if (axis.getAxisType().equals(AxisType.X_AXIS))
			if (range > Math.PI * 2 * .9) {
				fullRange = true;
			}
		if (axis.getAxisType().equals(AxisType.Y_AXIS))
			if (range > Math.PI * .9)
				fullRange = true;
		
		DecimalFormat df = new DecimalFormat("#.###");
		for (int i = 0; i < length; ++i) {
			double location = axis.getValue(i);// * 57.2958 ;
			if (axis.getAxisType().equals(AxisType.X_AXIS)) //workaround for longitude ranging from 0 - 2pi radians
				location -= 180;	
			labels.add(df.format(location));
		}
		if (fullRange) {
			if (axis.getAxisType().equals(AxisType.X_AXIS)) {
				labels.set(0,  df.format(-180));
				labels.set((int)axis.getRange().getUpperBound(), df.format(180));
			}
			else {
				labels.set(0,  df.format(-90));
				labels.set((int)axis.getRange().getUpperBound(), df.format(90));
			}
		}
	}
	
	@Override
	public String getLabel(int index) {
		return labels.get(index);
	}

}
