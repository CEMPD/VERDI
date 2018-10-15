package anl.verdi.plot.config;
// 2014 removed old Vector Plot
//import java.awt.Color;
//
//import anl.verdi.gui.FormulaListElement;
//import anl.verdi.plot.types.VectorPlot;
//
///**
// * Configuration info for tile plots.
// *
// * @author Nick Collier
// * @version $Revision$ $Date$
// */
//public class VectorPlotConfiguration extends PlotConfiguration {
//
//	public static final String VECTOR_COLOR = VectorPlot.class.getName() + ".vector_color";
//	public static final String VECTOR_X = VectorPlot.class.getName() + ".vector_x";
//	public static final String VECTOR_Y = VectorPlot.class.getName() + ".vector_y";
//	public static final String VECTOR_TILE = VectorPlot.class.getName() + ".vector_tile";
//
//	public VectorPlotConfiguration() {
//	}
//
//	public VectorPlotConfiguration(PlotConfiguration config) {
//		super(config);
//	}
//
//	/**
//	 * Gets the vector line color.
//	 *
//	 * @return the color.
//	 */
//	public Color getColor() {
//		return getColor(VECTOR_COLOR);
//	}
//
//	/**
//	 * Sets the vector color.
//	 *
//	 * @param color the vector color
//	 */
//	public void setColor(Color color) {
//		putObject(VECTOR_COLOR, color);
//	}
//
//	/**
//	 * Sets the horizontal and vertical vector components.
//	 *
//	 * @param xComp the horizontal component
//	 * @param yComp the vertical component
//	 */
//	public void setVectorsComponents(FormulaListElement xComp, FormulaListElement yComp) {
//		putObject(VECTOR_X, xComp);
//		putObject(VECTOR_Y, yComp);
//	}
//
//	/**
//	 * Sets the horizontal and vertical vector components as well
//	 *  as tile component over which the vectors will be drawn.
//	 *
//	 * @param xComp the horizontal component
//	 * @param yComp the vertical component
//	 * @param tileComp the tile component
//	 */
//	public void setVectorsComponents(FormulaListElement xComp, FormulaListElement yComp,
//	                                 FormulaListElement tileComp) {
//		this.setVectorsComponents(xComp, yComp);
//		putObject(VECTOR_TILE, tileComp);
//	}
//}
