package anl.verdi.data;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages

import anl.verdi.plot.data.DFVectorXYDataset;

/**
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class VectorEvaluator {

	private DataFrame uComp, vComp;
	private DFVectorXYDataset data;
	static final Logger Logger = LogManager.getLogger(VectorEvaluator.class.getName());


	public VectorEvaluator(DataFrame uComp, DataFrame vComp) {
		this.uComp = uComp;
		this.vComp = vComp;
		Logger.debug("in VectorEvaluator constructor; ready to call DFVectorXYDataset constructor");
		data = new DFVectorXYDataset();
		Logger.debug("back from DFVectorXYDataset");
	}

	public DFVectorXYDataset getData(int timeStep, int layer) {
		data.addSeries(uComp, vComp, timeStep, layer);
		Logger.debug("done in VectorEvaluator.getData");
		return data;
	}

	public List<VectorData> evaluate(int timeStep) {
		Logger.debug("into VectorEvauator.evaluate for timeStep = " + timeStep);
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
		Logger.debug("done with VectorEvaluator.evaluate");

		return list;
	}
}
