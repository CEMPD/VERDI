package test;

import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class Test {
	public static void main(String[] args) throws Exception {
		CoordinateReferenceSystem crs = CRS.decode("EPSG:9802");
	}
}
