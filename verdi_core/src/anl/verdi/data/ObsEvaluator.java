package anl.verdi.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
//import javax.measure.units.Unit;		// JScience changed its hierarchy
//import javax.measure.unit.Unit;
import org.unitsofmeasurement.unit.Unit;

import ucar.ma2.Array;
import ucar.ma2.IndexIterator;
import anl.verdi.util.VUnits;

/**
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class ObsEvaluator {

	static final Logger Logger = LogManager.getLogger(ObsEvaluator.class.getName());
	private DataManager manager;
	private Variable var, lat, lon;

	private static class ObsIterator implements Iterator<ObsData>, Iterable<ObsData> {

		private Array lat, lon, values;
		private IndexIterator latIndex, lonIndex, valuesIndex;
		private Unit unit;

		public ObsIterator(Array lat, Array lon, Array values, Unit unit) {
			this.lat = lat;
			this.lon = lon;
			this.values = values;
//			latIndex = lat.getIndexIteratorFast();	// NetCDF library replaced getIndexIteratorFast
			latIndex = lat.getIndexIterator();
			lonIndex = lon.getIndexIterator();
			valuesIndex = values.getIndexIterator();
			this.unit = unit;
			Logger.debug("in ObsEvaluator constructor, unit = " + this.unit);
		}

		public boolean hasNext() {
			return latIndex.hasNext();
		}

		public ObsData next() {
			return new ObsData(latIndex.getDoubleNext(),
							lonIndex.getDoubleNext(), unit,
							valuesIndex.getDoubleNext());
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}

		public Iterator<ObsData> iterator() {
			return this;
		}
	}

	public ObsEvaluator(DataManager manager, Variable var) {
		this.manager = manager;
		this.var = var;
		Unit deg = VUnits.createUnit("degrees");
		lat = new DefaultVariable("LAT", "LAT", deg, var.getDataset());
		lon = new DefaultVariable("LON", "LON", deg, var.getDataset());
	}

	public List<ObsData> evaluate(int timestep) {
		Dataset dataset = var.getDataset();
		DataReader reader = manager.getDataReader(dataset);
		return getObsDataList(dataset, reader, timestep);
	}
	
	public List<ObsData> evaluate(Date date) {
		Dataset dataset = var.getDataset();
		DataReader reader = manager.getDataReader(dataset);
		TimeCoordAxis timeAxis = (TimeCoordAxis)dataset.getCoordAxes().getTimeAxis();
		int timestep = timeAxis.getTimeStep(date);

		return getObsDataList(dataset, reader, timestep);
	}

	private List<ObsData> getObsDataList(Dataset dataset, DataReader reader,
			int timestep) {
		List<AxisRange> range = new ArrayList<AxisRange>();
		range.add(new AxisRange(dataset.getCoordAxes().getTimeAxis(), 0, timestep));
		range.add(new AxisRange(dataset.getCoordAxes().getZAxis(), 0, 1)); //could add layer if observation data is layered

		DataFrame obsFrame = reader.getValues(dataset, range, var);
		DataFrame latFrame = reader.getValues(dataset, range, lat);
		DataFrame lonFrame = reader.getValues(dataset, range, lon);
		Iterable<ObsData> iter = new ObsIterator(latFrame.getArray(), lonFrame.getArray(),
						obsFrame.getArray(), var.getUnit());
		List<ObsData> list = new ArrayList<ObsData>();
		
		for (ObsData data : iter)
			list.add(data);

		return list;
	}
	
	public Variable getVariable() {
		return this.var;
	}
}
