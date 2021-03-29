package anl.verdi.plot.jfree;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.data.Range;
import org.jfree.data.xy.XYDataset;

import anl.verdi.data.CoordAxis;
import anl.verdi.data.DataFrame;
import anl.verdi.plot.color.ColorMap;
import anl.verdi.plot.data.IMPASDataset;
import anl.verdi.plot.gui.MeshPlot;
import anl.verdi.plot.types.VerticalCrossSectionPlot.CrossSectionType;

public class MPASXYBlockRenderer extends XYBlockRenderer {
	
	String axis = "x";
	MeshPlot renderPlot = null;
	DataFrame frame;
	double startDeg;
	int timeStep;
	
	int xOrigin;
	int yOrigin;
	
	double sliceSizeDeg;
	
	public MPASXYBlockRenderer(CrossSectionType type, DataFrame frame, MeshPlot renderPlot, int step, double constant, double sliceSize) {
		if (type == CrossSectionType.Y)
			axis = "y";
		this.frame = frame;
		startDeg = constant;
		timeStep = step;
		sliceSizeDeg = sliceSize;
		this.renderPlot = renderPlot;
	}
	
	public void setPlotInfo(int step, double constant) {
		startDeg = constant;
		timeStep = step;
	}
	
	public void updateColorMap(ColorMap map) {
		renderPlot.updateColorMap(map);
	}
	public void drawItem(Graphics2D g2, XYItemRendererState state,
            Rectangle2D dataArea, PlotRenderingInfo info, XYPlot plot,
            ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset,
            int series, int item, CrosshairState crosshairState, int pass) {


		//g2.translate(dataArea.getMinX(), dataArea.getMinY());
		//g2.translate(dataArea.getMinX(), 0);
		//g2.translate(dataArea.getMinX(), 25);
		//g2.setColor(Color.WHITE);
		//g2.fillRect(0,  0,  (int)dataArea.getWidth(), (int)dataArea.getHeight());	
		//g2.fillRect((int)dataArea.getMinX(),  (int)dataArea.getMinY(),  (int)dataArea.getWidth(), (int)dataArea.getHeight());	
		
		xOrigin = (int)dataArea.getMinX();
		yOrigin = (int)dataArea.getMinY();

		renderPlot.initDisplayParameters(xOrigin, yOrigin, (int)Math.round(dataArea.getWidth()), (int)Math.round(dataArea.getHeight()), axis, sliceSizeDeg, timeStep, (double)startDeg);
		renderPlot.renderVerticalCrossSection(g2);
		
		//g2.translate(-dataArea.getMinX(), -dataArea.getMinY());
	
	}
	
	public void doZoom(Range domainRange, Range rangeRange) {
		renderPlot.zoomCrossSection(domainRange, rangeRange);
	}
	
	public void close() {
		renderPlot.viewClosed();
	}
	
	public Range findDomainBounds(XYDataset dataset) {
		IMPASDataset ds = (IMPASDataset)frame.getDataset().get(0);
		if (axis.equals("y"))
			return new Range(ds.getLonMin() * MeshPlot.RAD_TO_DEG, ds.getLonMax() * MeshPlot.RAD_TO_DEG);
		else
			return new Range(ds.getLatMin() * MeshPlot.RAD_TO_DEG, ds.getLatMax() * MeshPlot.RAD_TO_DEG);
	}
	
	public Range findRangeBounds(XYDataset dataset) {		
		CoordAxis zAxis = ((IMPASDataset)frame.getDataset().get(0)).getZAxis(frame.getVariable().getName());
		if (zAxis != null) {
			//return new Range(zAxis.getRange().getLowerBound() + getYOffset(), zAxis.getRange().getUpperBound() + getBlockHeight() + getYOffset());
			return new Range(zAxis.getRange().getLowerBound(), zAxis.getRange().getUpperBound());
		}
				
		return super.findRangeBounds(dataset);
	}

}
