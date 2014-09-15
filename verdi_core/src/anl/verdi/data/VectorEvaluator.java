package anl.verdi.data;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import anl.verdi.plot.data.DFVectorXYDataset;

/**
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class VectorEvaluator {

	private DataFrame uComp, vComp;
	private DFVectorXYDataset data;


	public VectorEvaluator(DataFrame uComp, DataFrame vComp) {
		this.uComp = uComp;
		this.vComp = vComp;
		data = new DFVectorXYDataset();
	}

	public DFVectorXYDataset getData(int timeStep, int layer) {
		data.addSeries(uComp, vComp, timeStep, layer);
		return data;
	}

	public List<VectorData> evaluate(int timeStep) {
		data.addSeries(uComp, vComp, timeStep, 0);
		BoundingBoxer boxer = uComp.getAxes().getBoundingBoxer();
		List<VectorData> list = new ArrayList<VectorData>();
		int count = data.getItemCount(0);
		for (int i = 0; i < count; i++) {
			double dx = data.getDeltaXValue(0, i);
			double dy = data.getDeltaYValue(0, i);
			double x = data.getXValue(0, i);
			double y = data.getYValue(0, i);

			Point2D latLon = boxer.axisPointToLatLonPoint((int)x, (int)y);
			VectorData data = new VectorData(latLon.getY(), latLon.getX(), dx, dy);
			list.add(data);
		}

		return list;
	}
}
