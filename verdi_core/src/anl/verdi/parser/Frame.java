package anl.verdi.parser;

import java.util.HashMap;
import java.util.Map;

import anl.verdi.data.DataFrame;

/**
 * Contains the current execution context. This includes the
 * current values of variables and so forth.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class Frame {

	private Map<String, DataFrame> varValues = new HashMap<String, DataFrame>();
	private int timeStep, layer, x, y, nrows, ncols, nlevels;

	/**
	 * Sets the value of the named variable to the specified value.
	 *
	 * @param name the name of the variable
	 * @param val the variables value
	 */
	public void setValue(String name, DataFrame val) {
		varValues.put(name, val);
	}

	/**
	 * Gets the current value of the named variable.
	 * @param name the name of the variable.
	 * @return  the current value of the named variable.
	 */
	public DataFrame getValue(String name) {
		return varValues.get(name);
	}

	/**
	 * Gets the current layer index.
	 *
	 * @return the current layer index.
	 */
	public int getLayer() {
		return layer;
	}

	/**
	 * Gets the current time step index.
	 *
	 * @return the current time step index.
	 */
	public int getTimeStep() {
		return timeStep;
	}

	/**
	 * Sets the coordinate indices for this evaluation context.
	 * The coordinate indices specify which rank or dimension
	 * in the input data refers to which coordinate axsis.
	 *
	 * @param timeStep the new time step index
	 * @param layer the new layer index
	 * @param x the new x index
	 * @param y the new y index
	 */
	public void setCoordinateIndices(int timeStep, int layer, int x, int y) {
		this.timeStep = timeStep;
		this.layer = layer;
		this.x = x;
		this.y = y;
	}

	/**
	 * Gets the current x index.
	 *
	 * @return the current x index
	 */
	public int getX() {
		return x;
	}

	/**
	 * Gets the current y index.
	 *
	 * @return the current y index
	 */
	public int getY() {
		return y;
	}

	/**
	 * Gets the number of columns in the currently selected domain.
	 *
	 * @return the number of columns in the currently selected domain.
	 */
	public int getColumnCount() {
		return ncols;
	}

	/**
	 * Sets the number of columns in the currently selected domain.
	 *
	 * @param ncols the number of columns in the currently selected domain
	 */
	public void setColumnCount(int ncols) {
		this.ncols = ncols;
	}

	/**
	 * Gets the number of level in the currently selected domain.
	 *
	 * @return the number of levels in the currently selected domain.
	 */
	public int getLevelCount() {
		return nlevels;
	}

	/**
	 * Sets the number of levels in the currently selected domain.
	 *
	 * @param nlevels the number of levels in the currently selected domain
	 */
	public void setLevelCount(int nlevels) {
		this.nlevels = nlevels;
	}

	/**
	 * Gets the number of rows in the currently selected domain.
	 *
	 * @return the number of rows in the currently selected domain.
	 */
	public int getRowCount() {
		return nrows;
	}

	/**
	 * Sets the number of rows in the currently selected domain.
	 *
	 * @param nrows the number of rows in the currently selected domain
	 */
	public void setRowCount(int nrows) {
		this.nrows = nrows;
	}

	public int getTimeStepOffset() {
		return 0; 
	}

	public int getXOffset() {
		return 0;
	}

	public int getYOffset() {
		return 0;
	}

	public int getLayerOffset() {
		return 0;
	}
}
