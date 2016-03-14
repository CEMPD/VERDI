package anl.verdi.plot.data;

import anl.verdi.data.CoordAxis;
import anl.verdi.data.DataFrame;
import anl.verdi.data.Dataset;

public interface IMPASDataset extends Dataset {
		
	/**
	 * Returns min / max values for the given variable over the entire dataset.  Initially, values are only accurate for the
	 *  first timestep of the first layer, with remaining values calculated as quickly as possible.
	 *  
	 * @param listener - listener to be notified as min/max is updated over the entire dataset
	 * @return - min max values for the given variable
	 */
	public MinMaxInfo getPlotMinMax(DataFrame variable, MinMaxLevelListener listener);
	
	/**
	 * Returns min / max values for the given variable within the given layer.  Initially, values are only accurate for the
	 * first timestep, with remaining values calculated as quickly as possible.
	 *  
	 * @param listener - listener to be notified as min/max is updated over the entire layer
	 * @return - min max values for the given variable within the layer
	 */
	public MinMaxInfo getLayerMinMax(DataFrame variable, int layer, MinMaxLevelListener listener);


}
