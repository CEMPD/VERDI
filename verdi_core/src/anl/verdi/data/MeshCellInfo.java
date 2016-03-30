package anl.verdi.data;

public interface MeshCellInfo {
	
	public int getId();
	
	public double getValue(ArrayReader renderVariable, int timestep, int firstTimestep, int layer, int firstLayer);
	
	public int getNumVertices();
	
	public double getLon();
	
	public double getLat();
	
	public double getLon(int index);
	
	public double getLat(int index);
	
	public double getLonRad(int index);
	
	public double getLatRad(int index);
	
	public double getMinX();
	
	public int getMinXPosition();
	
	public int getMaxXPosition();
	
	public int getMinYPosition();
	
	public int getMaxYPosition();
	
	public double getMaxX();
	
	public double getMinY();
	
	public double getMaxY();
	
	public String getElevation(String axisName, int currentLayer, int currentTimestep);


}
