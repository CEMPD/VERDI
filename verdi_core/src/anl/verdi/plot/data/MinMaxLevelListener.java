package anl.verdi.plot.data;

public interface MinMaxLevelListener {

	public void layerUpdated(int level, double min, int minIndex, double max, int maxIndex, double percentComplete, boolean isLog);
	
	public void datasetUpdated(double min, int minIndex, double max, int maxIndex, double percentComplete, boolean isLog);
	
	public long getRenderTime();
}
