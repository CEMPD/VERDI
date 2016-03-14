package anl.verdi.plot.data;

public interface MinMaxLevelListener {

	public void layerUpdated(int level, double min, double max, double percentComplete, boolean isLog);
	
	public void datasetUpdated(double min, double max, double percentComplete, boolean isLog);
		
}
