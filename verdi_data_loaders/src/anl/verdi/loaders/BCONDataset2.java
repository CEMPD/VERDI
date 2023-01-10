package anl.verdi.loaders;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import anl.verdi.data.ArrayReader;
import anl.verdi.data.Axes;
import anl.verdi.data.AxisType;
import anl.verdi.data.CoordAxis;
import ucar.nc2.Dimension;
import ucar.nc2.dataset.NetcdfDataset;

public class BCONDataset2 extends Models3Dataset {
	
	NetcdfDataset dataset;
	private CoordAxis defaultLayer;

	protected BCONDataset2(URL url, NetcdfDataset ds) {
		super(url);
		this.url = url;
		dataset = ds;
	}
	
	@Override
	public Axes<CoordAxis> getCoordAxes() {
		return coordAxes;
	}
	
	private void initAxes() throws IOException {

		List<CoordAxis> list = new ArrayList<CoordAxis>();
		
		//TODO - fix these
	//	list.add(makeTimeCoordAxis("Time"));
	//	addMonths(list);
		
		MPASBoxer boxer = new MPASBoxer();
							
		
		addLayer("LAY", list);
		
		//Construct axes for latitude and longitude, using average diameter as spacing
		
	//	list.add(new MPASCoordAxis("x", "x", lonMin, AxisType.X_AXIS));
	//	list.add(new MPASCoordAxis("y", "y", latMin, AxisType.Y_AXIS));

		coordAxes = new Axes<CoordAxis>(list, boxer);
		
	}

	private void addLayer(String layerName, List<CoordAxis> axisList) {
		CoordAxis axis = null;
		Dimension dim = dataset.findDimension(layerName);
		if (dim == null)
			return;
		int numLevels = dim.getLength();
		Double[] vertList = new Double[numLevels];
		for (int i = 0; i < vertList.length; ++i)
			vertList[i] = (double)i;
		if (vertList.length > 0) {
			axis =new CSVCoordAxis(vertList, layerName, layerName, AxisType.LAYER);
			axisList.add(axis);
			defaultLayer = axis;
		}
	}
	
}
