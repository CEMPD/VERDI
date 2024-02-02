package anl.verdi.loaders;

import java.net.URL;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import anl.verdi.data.Dataset;
import ucar.unidata.geoloc.Projection;
import ucar.unidata.geoloc.projection.LatLonProjection;

public class GenericDataWrapper {
	
	public GenericDataWrapper openInstance(URL url) {
		return null;
	}
	
	public void open(URL url) {
		
	}
	
	public boolean canHandle(URL url) {
		return false;
	}
	
	public String[] getTimeVars() {
		return new String[] { "Start_UTC", "Mid_UTC", "UTC", "Stop_UTC" };
	}
	
	public String[] getLonVars() {
		return new String [] { "Longitude", "LONGITUDE", "Lon", "LON" };
	}
	
	public String[] getLatVars() {
		return new String [] { "Latitude", "LATITUDE", "Lat", "LAT" };	
	}
	
	public String[] getReservedVars() {
		return new String[] { "UTC", "Start_UTC", "Stop_UTC", "Mid_UTC", "Longitude", "Latitude"};
	}
	
	public String[] getColumnList() {
		return new String[] {};
	}
	
	public String[] getVarList() {
		return new String[] {};
	}
	
	public GregorianCalendar getStartDate() {
		return null;
	}
	
	public int getNumRows() {
		return 0;
	}
	
	public Double getValue(String variable, int i) {
		return null;
	}
	
	public List<Double> getValues(String variable) {
		return null;
	}
	
	public List<Double> getValues(String variable, int base, int numResults) {
		return null;
	}
	
	public List<Dataset> createDatasets(URL url) {
		return new ArrayList<Dataset>();
	}
	
	public Projection getProjection() {
		return new LatLonProjection();
	}
	
	public boolean isObs() {
		return false;
	}

}
