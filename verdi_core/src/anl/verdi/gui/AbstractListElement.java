package anl.verdi.gui;

import anl.verdi.data.Axes;
import anl.verdi.data.CoordAxis;
import anl.verdi.data.Dataset;

/**
 * Abstract class for formula and dataset list elements.
 * 
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public abstract class AbstractListElement {

	public static int NO_TIME_VALUE = -1;
	public static int NO_LAYER_VALUE = -1;

	protected int timeMin;
	protected int timeMax;
	protected int layerMin, xMin, yMin;
	protected int layerMax, xMax, yMax;
	private boolean isTimeUsed = false;
	private boolean isLayerUsed = false;
	private boolean isXYUsed = false;

	public boolean isLayerUsed() {
		return isLayerUsed;
	}

	public void setLayerUsed(boolean layerUsed) {
		isLayerUsed = layerUsed;
	}

	public boolean isTimeUsed() {
		return isTimeUsed;
	}

	public void setTimeUsed(boolean timeUsed) {
		isTimeUsed = timeUsed;
	}

	public int getLayerMax() {
		return layerMax;
	}

	public void setLayerMax(int layerMax) {
		this.layerMax = layerMax;
	}

	public int getLayerMin() {
		return layerMin;
	}

	public void setLayerMin(int layerMin) {
		this.layerMin = layerMin;
	}

	public int getTimeMax() {
		return timeMax;
	}

	public void setTimeMax(int timeMax) {
		this.timeMax = timeMax;
	}

	public int getTimeMin() {
		return timeMin;
	}

	public void setTimeMin(int timeMin) {
		this.timeMin = timeMin;
	}

	public int getXMax() {
		return xMax;
	}

	public void setXMax(int xMax) {
		this.xMax = xMax;
	}

	public int getXMin() {
		return xMin;
	}

	public void setXMin(int xMin) {
		this.xMin = xMin;
	}

	public int getYMax() {
		return yMax;
	}

	public void setYMax(int yMax) {
		this.yMax = yMax;
	}

	public int getYMin() {
		return yMin;
	}

	public void setYMin(int yMin) {
		this.yMin = yMin;
	}

	public boolean isXYUsed() {
		return isXYUsed;
	}

	public void setXYUsed(boolean xyUsed) {
		isXYUsed = xyUsed;
	}
	public void setDomain(int xMin,int xMax,int yMin, int yMax){
		setXMin(xMin);
		setXMax(xMax);
		setYMin(yMin);
		setYMax(yMax);
		isXYUsed=!(((xMax<xMin))&&((yMax<yMin)));
	}
	
	public abstract Axes<CoordAxis> getAxes();
	
	public abstract Dataset getDataset();
}
