/**
 * 
 */
package anl.verdi.plot.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.JToolBar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.geotools.map.MapContent;
import org.geotools.renderer.GTRenderer;
import org.geotools.swing.JMapPane;
import org.geotools.swing.RenderingExecutor;

import saf.core.ui.event.DockableFrameEvent;
import anl.verdi.formula.Formula;

/**
 * @author Jo Ellen Brandmeyer
 *
 * The purpose of this class is to replace the anl.verdi.plot.gui.PlotPanel for the FastTilePlot.
 * The original PlotPanel will still be used for other plots that were already using it.
 * The reason for the change is to extend the JMapPane in order to use the GeoTools Shapefile functionality.
 * Therefore, some of the member functions in this class are copied/adapted from the PlotPanel class.
 * Although that design is never optimal, both JMapPane and PlotPanel extend JPanel. If MapPlotPanel were
 * created to extend PlotPanel, JMapPane would not be in the inheritance tree.
 */
public class MapPlotPanel extends JMapPane {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static final Logger Logger = LogManager.getLogger(MapPlotPanel.class.getName());
	private Plot plot;
	private String name;
	private BorderLayout aBorderLayout = new BorderLayout();
	private JMenuBar aJMenuBar;
	private JToolBar aJToolBar;
	private JMapPane aJMapPane = new JMapPane();
	private RenderingExecutor aRenderingExecutor;

	/**
	 * 
	 */
	public MapPlotPanel() {
		super();
		// TODO Auto-generated constructor stub
		Logger.debug("in MapPlotPanel default constructor");
	}

	/**
	 * @param content
	 */
	public MapPlotPanel(MapContent content) {
		super(content);
		// TODO Auto-generated constructor stub
		Logger.debug("in MapPlotPanel constructor; MapContent passed in");
	}

	/**
	 * @param content
	 * @param executor
	 * @param renderer
	 */
	public MapPlotPanel(MapContent content, RenderingExecutor executor,
			GTRenderer renderer) {
		super(content, executor, renderer);
		Logger.debug("in MapPlotPanel constructor; MapContent, RenderingExecutor, GTRenderer passed in");
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Constructor to be called from ArealInterpolationPlot
	 * must take a FastAreaTilePlot ???
	 */
//	public MapPlotPanel()

	/**
	 * Gets the Plot this MapPlotPanel contains.
	 * Copied from PlotPanel
	 * 
	 * @return	the plot this MapPlotPanel contains.
	 */
	public Plot getPlot() {
		return plot;
	}

	/**
	 * Sets the Plot this MapPlotPanel contains.
	 * Using this method instead of as an argument to the constructor, which is how PlotPanel does it.
	 * 
	 * @param plot
	 */
	public void setPlot(Plot plot) {
		this.plot = plot;
	}
	
	/**
	 * Gets the Name this MapPlotPanel contains.
	 * Copied from PlotPanel
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the Name this MapPlotPanel contains.
	 * Using this method instead of as an argument to the constructor, which is how Plot Panel does it.
	 * 
	 * @param aName
	 */
	public void setName(String aName)
	{
		this.name = aName;
	}
	
	// 
	// 
	/**
	 * access to JMapPane function getRenderer()
	 * gets the renderer, creating a default one if required
	 */
	public GTRenderer getRenderer()
	{
		return super.getRenderer();
	}
	
	/**
	 * access to JMapPane function setRenderer
	 * sets the renderer to be used by this map pane
	 * 
	 * @param aRenderer
	 */
	public void setRenderer(GTRenderer aRenderer)
	{
		super.setRenderer(aRenderer);
	}
	
	/**
	 * access to JMapPane function getRenderingExecutor
	 * gets the RenderingExecutor being used by this map pane
	 */
	public RenderingExecutor getRenderingExecutor()
	{
		return super.getRenderingExecutor();
	}
	
	/**
	 * access to JMapFrame function setRenderingExecutor
	 * sets the RenderingExecutor to be used by this map pane
	 */
	public void setRenderingExecutor(RenderingExecutor aRenderingExecutor)
	{
		super.setRenderingExecutor(aRenderingExecutor);
	}
	
	/**
	 * gets the MapContent instance being displayed by this map pane
	 */
	public MapContent getMapContent()
	{
		return super.getMapContent();
	}
	
	/**
	 * access to JMapPane function setMapContent
	 * sets the MapContent instance containing the layers to display
	 */
	public void setMapContent(MapContent aMapContent)
	{
		super.setMapContent(aMapContent);
	}
	
	/**
	 * access to JMapPane function drawLayers
	 * Draws layers into 1 or more images which will then be displayed by the map pane
	 */
	public void drawLayers(boolean createNewImage)
	{
		super.drawLayers(createNewImage);
	}

	/**
	 * access to JMapPane function getBaseImage
	 */
	public java.awt.image.RenderedImage getBaseImage()
	{
		return super.getBaseImage();
	}
	
	/**
	 * Call this function after all data members have been set via constructor and/or set function calls
	 * @return boolean success or failure
	 */
	public boolean setupMapPlotPanel()
	{
		boolean retValue = false;	// true for success, false for failure
		
		// check that all required data members have been set
		if(getMapContent() == null)
		{
			Logger.debug("MapContent is not set prior to calling setupMapPlotPanel(). Function fails.");
			return retValue;
		}
		if(getRenderer() == null)
		{
			Logger.debug("GTRenderer is not set prior to calling setupMapPlotPanel(). Function fails.");
			return retValue;
		}
		if(getPlot() == null)
		{
			Logger.debug("Plot is not set prior to calling setupMapPlotPanel(). Function fails.");
			return retValue;
		}
		if(getName() == null)
		{
			Logger.debug("Name is not set prior to calling setupMapPlotPanel(). Function fails.");
			return retValue;
		}
		if(getRenderingExecutor() == null)
		{
			Logger.debug("RenderingExecutor is not set prior to calling setupMapPlotPanel(). Function fails.");
			return retValue;
		}
		
		// now do setup from the anl.verdi.plot.gui.PlotPanel constructor
		aJMenuBar = plot.getMenuBar();
		aJToolBar = plot.getToolBar();
		aJMapPane.setLayout(new BoxLayout(aJMapPane, BoxLayout.Y_AXIS));
		if(aJMenuBar != null)
		{
			aJMenuBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MIN_VALUE));
			aJMenuBar.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			aJMapPane.add(aJMenuBar);
		}
		if(aJToolBar != null)
		{
			aJToolBar.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			if(aJMapPane.getComponentCount() == 1)
			{
				aJMapPane.add(Box.createRigidArea(new Dimension(0,4)));
			}
			aJMapPane.add(aJToolBar);
			aJMapPane.add(Box.createRigidArea(new Dimension(0,4)));
		}
		if(aJMapPane.getComponentCount()> 0)
			add(aJMapPane, BorderLayout.NORTH);
		add(plot.getPanel(), BorderLayout.CENTER);
		
		retValue = true;
		return retValue;
	}
	
	/**
	 * Add a PlotListener to the plot; from PlotPanel
	 * 
	 * @param listener
	 */
	public void addPlotListener(PlotListener listener)
	{
		plot.addPlotListener(listener);
	}
	
	/**
	 * Remove the PlotListener from the plot; from PlotPanel
	 * 
	 * @param listener
	 */
	public void removePlotListener(PlotListener listener)
	{
		plot.removePlotListener(listener);
	}
	
	/**
	 * Gets the type of the plot that this panel shows; from PlotPanel.
	 *
	 * @return the type of the plot that this panel shows.
	 */
	public Formula.Type getPlotType() {
		return plot.getType();
	}

	/**
	 * Notifies Plot when its View has been closed. HACK! from PlotPanel
	 */

	public void viewClosed() {

		if ( plot instanceof anl.verdi.plot.gui.FastTilePlot ) {
			( (anl.verdi.plot.gui.FastTilePlot) plot).stopThread();
			try {
				Thread.sleep(500);
			} catch( Exception e) {

			}
		}
		if ( plot != null) {
			plot.viewClosed();
			plot = null;
		}

	}
	
	/**
	 * Handle undocking the frame from the overall system; from PlotPanel
	 * 
	 * @param evt
	 */
	public void viewFloated(DockableFrameEvent evt){
		if ( plot instanceof anl.verdi.plot.gui.FastTilePlot ) {
			( (anl.verdi.plot.gui.FastTilePlot) plot).viewFloated(evt);
		}
	}
	
	/**
	 * Handle docking the frame to the overall system; from PlotPanel
	 * @param evt
	 */
	public void viewRestored(DockableFrameEvent evt){		
		if ( plot instanceof anl.verdi.plot.gui.FastTilePlot ) {
			( (anl.verdi.plot.gui.FastTilePlot) plot).viewRestored(evt);
		}
	}

}
