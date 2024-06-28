package anl.verdi.loaders;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import ucar.ma2.Array;
import anl.verdi.data.AbstractDataReader;
import anl.verdi.data.AxisRange;
import anl.verdi.data.AxisType;
import anl.verdi.data.CoordAxis;
import anl.verdi.data.DataFrame;
import anl.verdi.data.DataFrameAxis;
import anl.verdi.data.DataFrameBuilder;
import anl.verdi.data.Range;
import anl.verdi.data.TimeCoordAxis;
import anl.verdi.data.Variable;

/**
 * Interface for classes that handle reading data from
 * datasets.  This data could be originally from a file
 * or from a model or other source.
 *
 * @author Mary Ann Bitz
 * @author Eric Tatara
 * @version $Revision$ $Date$
 * @see anl.verdi.data.Dataset , DataLoader
 */
public class GenericReader extends AbstractDataReader<GenericDataset> {

	public GenericReader(GenericDataset set) {
		super(set);
	}

	private void createDefaultAxes(DataFrameBuilder builder, GenericDataset set) {
		int index = 0;
		for (CoordAxis axis : set.getCoordAxes().getAxes()) {			
			builder.addAxis(DataFrameAxis.createDataFrameAxis(axis, index));
			index++;
		}
	}

	/**
	 * get the values for the given data parameters
	 */
	public DataFrame getValues(GenericDataset set, List<AxisRange> ranges, Variable variable) {

		DataFrameBuilder builder = new DataFrameBuilder();

		builder.addDataset(set).setVariable(variable);

		createDefaultAxes(builder, set);

		for (AxisRange axis : ranges) {
			if (axis.getExtent() < 0)
				return null;
			int dimIndex = 0;

			DataFrameAxis frameAxis = DataFrameAxis.createDataFrameAxis(axis.getAxis(), axis.getOrigin(), axis.getExtent(), dimIndex);

			builder.addAxis(frameAxis);
		}
		
		Date dataStart = null;
		Date dataEnd = null;
		TimeCoordAxis timeAxis = (TimeCoordAxis)set.getCoordAxes().getTimeAxis();
		if (timeAxis != null) {
			for (AxisRange range : ranges) {
				if (AxisType.TIME.equals(range.getAxisType())) {
					if (range.getOrigin() < 0)
						return null;
					GregorianCalendar startCal = timeAxis.getDate(range.getOrigin());
					if (startCal == null)
						continue;
					dataStart = startCal.getTime();
					GregorianCalendar endCal = timeAxis.getDate((int)(range.getRange().getOrigin() + range.getRange().getExtent()));
					if (endCal != null)
					dataEnd = endCal.getTime();
				}
			}
		}


		builder.setArray(set.getArray());

		return builder.createDataFrame();

	}
}
