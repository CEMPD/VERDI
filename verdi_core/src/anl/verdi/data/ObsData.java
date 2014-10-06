package anl.verdi.data;

//import javax.measure.units.Unit;		// JScience changed its hierarchy
//import javax.measure.unit.Unit;
import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
import org.unitsofmeasurement.unit.Unit;

/**
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class ObsData {
	static final Logger Logger = LogManager.getLogger(ObsData.class.getName());

	private Unit unit;
	private double value, lat, lon;
	private double x, y;

	public ObsData(double lat, double lon, Unit unit, double value) {
		this.lat = lat;
		this.lon = lon;
		this.unit = unit;
		this.value = value;
		Logger.debug("in ObsData constructor, Unit = " + this.unit);
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getLat() {
		return lat;
	}

	public double getLon() {
		return lon;
	}

	public Unit getUnit() {
		Logger.debug("in ObsData.getUnit, unit = " + unit);
		return unit;
	}

	public double getValue() {
		return value;
	}
}
