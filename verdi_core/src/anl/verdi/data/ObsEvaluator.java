package anl.verdi.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
//import javax.measure.units.Unit;		// JScience changed its hierarchy
//import javax.measure.unit.Unit;
import org.unitsofmeasurement.unit.Unit;

import ucar.ma2.Array;
import ucar.ma2.IndexIterator;
import anl.verdi.plot.data.TextDataset;
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
		String latName = "LAT";
		String lonName = "LON";
		
		lat = var.getDataset().getVariable(latName);
		if (lat == null) {
			latName = "Latitude";
			lonName = "Longitude";
		}
		lat = var.getDataset().getVariable(latName); //Models3 hides this - use if Latitude / Longitude not found.  If this gets complicated expose hidden variables and search there too.
		if (lat == null) {
			Axes<CoordAxis> ax = var.getDataset().getCoordAxes();
			if (ax.getXAxis() != null) {
				lonName = ax.getXAxis().getName();
				latName = ax.getYAxis().getName();
				lat = var.getDataset().getVariable(latName);
			}
		}
		if (lat == null) {
			Dataset ds = var.getDataset();
			if (ds instanceof TextDataset) {
				if (!((TextDataset)ds).hasColumn(latName)) {
					latName = "LAT";
					lonName = "LON";						
				}
			}
			if (ds.getClass().getName().endsWith(".Models3ObsDataset")) {
				latName = "LAT";
				lonName = "LON";
			}
		
		}

		lat = new DefaultVariable(latName, latName, deg, var.getDataset());
		lon = new DefaultVariable(lonName, lonName, deg, var.getDataset());
	}

	/* Timestep is ambiguous due to differences ebetween base data and obs data timesteps.  Use 
	 * update(Date, long) instead
	public List<ObsData> evaluate(int timestep) {
		Dataset dataset = var.getDataset();
		DataReader reader = manager.getDataReader(dataset);
		return getObsDataList(dataset, reader, timestep);
	}*/
	
	public boolean dataWithin(Date startDate, Date endDate) {
		TimeCoordAxis timeAxis = (TimeCoordAxis)var.getDataset().getCoordAxes().getTimeAxis();
		Range range = timeAxis.getRange();
		Date obsStart = timeAxis.getDate((int)range.getOrigin()).getTime();
		Date obsEnd = timeAxis.getDate((int)range.getExtent() - 1).getTime();
		return  ( (obsStart.after(startDate) && obsStart.before(endDate)) ||
				obsEnd.after(startDate) && obsEnd.before(endDate));
	}
	
	public List<ObsData> evaluate(Date date, long timestepSize) {
		Dataset dataset = var.getDataset();
		DataReader reader = manager.getDataReader(dataset);
		TimeCoordAxis timeAxis = (TimeCoordAxis)dataset.getCoordAxes().getTimeAxis();
		int firstTimestep = timeAxis.getTimeStep(date);
		int numTimesteps = 0;
		if (firstTimestep >= -1) {
			Date obsDate = timeAxis.getDate((int)timeAxis.getRange().getOrigin()).getTime();
 			if (date.getTime() + timestepSize < obsDate.getTime()) {
				firstTimestep = Axes.TIME_STEP_NOT_FOUND;
				throw new IllegalArgumentException("Date " + date + " not present in observation data");
			}
			else {
				if (timeAxis.getDate(firstTimestep) != null) {
					Date firstTimestepDate = timeAxis.getDate(firstTimestep).getTime();
					//first measurement is after the end of this timestep
					if (date.getTime() + timestepSize < firstTimestepDate.getTime())
						firstTimestep = Axes.TIME_STEP_NOT_FOUND;
				}
				int lastTimestep = timeAxis.getTimeStep(new Date(date.getTime() + timestepSize));
				//if (lastTimestep > 0)
				//	--lastTimestep;
				if (lastTimestep > -1) //TODO - maybe this should only be for first Timestep == last timestep.  Check ICTTimeAxis
					numTimesteps = lastTimestep - firstTimestep;
				else
					numTimesteps = (int)(timeAxis.getRange().getExtent() - firstTimestep);
				if (lastTimestep == firstTimestep)
					++numTimesteps;
			}
		}
			

		return getObsDataList(dataset, reader, firstTimestep, numTimesteps);
	}	
	
	/* Ambuguous - replace with evaluate(Date, long)
	 * public List<ObsData> evaluate(Date date, int timestep) {
		Dataset dataset = var.getDataset(); // date is current timestep
		DataReader reader = manager.getDataReader(dataset);
		TimeCoordAxis timeAxis = (TimeCoordAxis)dataset.getCoordAxes().getTimeAxis();
		int baseTimestep = 0;
		if (date != null)
			baseTimestep = timeAxis.getTimeStep(date);

		return getObsDataList(dataset, reader, baseTimestep + timestep);
	}*/

	private List<ObsData> getObsDataList(Dataset dataset, DataReader reader,
			int timestep, int numTimesteps) {
		if (timestep == Axes.TIME_STEP_NOT_FOUND)
			return new ArrayList<ObsData>();
		List<AxisRange> range = new ArrayList<AxisRange>();
		range.add(new AxisRange(dataset.getCoordAxes().getTimeAxis(), timestep, numTimesteps));
		if (dataset.getCoordAxes().getZAxis() != null) {
			range.add(new AxisRange(dataset.getCoordAxes().getZAxis(), 0, 1)); //could add layer if observation data is layered
		}

		DataFrame obsFrame = reader.getValues(dataset, range, var);
		if (obsFrame == null) {
			for (CoordAxis axis : dataset.getCoordAxes().getAxes()) {	
				if (axis.getAxisType() == AxisType.TIME ) {
					TimeCoordAxis timeAxis = (TimeCoordAxis)dataset.getCoordAxes().getTimeAxis();
					long lower = timeAxis.getRange().getLowerBound();
					long upper = timeAxis.getRange().getUpperBound();
					GregorianCalendar gc1 = timeAxis.getDate((int)lower);
					GregorianCalendar gc2 = timeAxis.getDate((int)upper);
					if (gc1 != null && gc2 != null)
						throw new IllegalArgumentException("OBS data does not contain readings for the times within the data file.  Observational data is from " + gc1.getTime() + " to " + gc2.getTime());
				}
			}
			throw new IllegalArgumentException("OBS data does not contain readings for the times within the data file.");
		}
		DataFrame latFrame = reader.getValues(dataset, range, lat);
		DataFrame lonFrame = reader.getValues(dataset, range, lon);
		if (latFrame == null || lonFrame == null)
			
			throw new IllegalArgumentException("OBS data does not cover the area of the data file.");
		Iterable<ObsData> iter = new ObsIterator(latFrame.getArray(), lonFrame.getArray(),
						obsFrame.getArray(), var.getUnit());
		List<ObsData> list = new ArrayList<ObsData>();
		
		Double invalidValue = dataset.getMissingDataMarker(var);
		for (ObsData data : iter) {
			if (invalidValue == null || !invalidValue.equals(data.getValue()))
				list.add(data);
		}

		return list;
	}
	
	public Variable getVariable() {
		return this.var;
	}
}
