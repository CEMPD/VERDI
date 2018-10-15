package anl.verdi.data;

/**
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class VectorData {

	private double uVal, vVal, lat, lon;
	private double tileX, tileY;

	public VectorData(double lat, double lon, double uVal, double vVal) {
		this.lat = lat;
		this.lon = lon;
		this.uVal = uVal;
		this.vVal = vVal;
	}

	public double getLat() {
		return lat;
	}

	public double getLon() {
		return lon;
	}

	public double getUVal() {
		return uVal;
	}

	public double getVVal() {
		return vVal;
	}

	public double getTileX() {
		return tileX;
	}

	public void setTileX(double tileX) {
		this.tileX = tileX;
	}

	public double getTileY() {
		return tileY;
	}

	public void setTileY(double tileY) {
		this.tileY = tileY;
	}
}
