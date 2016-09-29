package anl.verdi.plot.jfree;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.data.xy.XYDataset;

import anl.verdi.data.DataFrame;
import anl.verdi.plot.gui.MeshPlot;
import anl.verdi.plot.types.VerticalCrossSectionPlot.CrossSectionType;

public class MPASXYBlockRenderer extends XYBlockRenderer {
	
	String axis = "x";
	MeshPlot renderPlot = null;
	DataFrame frame;
	int startDeg;
	int timeStep;
	
	public MPASXYBlockRenderer(CrossSectionType type, DataFrame frame, int step, int constant) {
		if (type == CrossSectionType.Y)
			axis = "y";
		this.frame = frame;
		startDeg = constant;
		timeStep = step;
		renderPlot = new MeshPlot(null, frame, MeshPlot.MODE_CROSS_SECTION);
	}
	
	public void setPlotInfo(int step, int constant) {
		startDeg = constant;
		timeStep = step;
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
		
		renderPlot.initDisplayParameters((int)dataArea.getMinX(), (int)dataArea.getMinY(), (int)Math.round(dataArea.getWidth()), (int)Math.round(dataArea.getHeight()), axis, 1.0, timeStep, (double)startDeg);
		renderPlot.renderVerticalCrossSection(g2);
		
		//g2.translate(-dataArea.getMinX(), -dataArea.getMinY());

		

	
	}

}
