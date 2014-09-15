package anl.verdi.data;

//import javax.measure.units.Unit;		// JScience changed its hierarchy
//import javax.measure.unit.Unit;
import org.unitsofmeasurement.unit.*;

/**
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class ObsData {

	private Unit unit;
	private double value, lat, lon;
	private double x, y;

	public ObsData(double lat, double lon, Unit unit, double value) {
		this.lat = lat;
		this.lon = lon;
		this.unit = unit;
		this.value = value;
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
		return unit;
	}

	public double getValue() {
		return value;
	}
}
