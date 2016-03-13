package anl.verdi.plot.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.geotools.map.MapContent;
import org.geotools.renderer.GTRenderer;
import org.geotools.swing.AbstractMapPane;
import org.geotools.swing.JMapPane;
import org.geotools.swing.RenderingExecutor;

import saf.core.ui.event.DockableFrameEvent;
import anl.verdi.formula.Formula;
import anl.verdi.plot.gui.GTTilePlotPanel;
import anl.verdi.plot.gui.GTTilePlot;

public class PlotPanel extends JPanel {

	private static final long serialVersionUID = 2937963505375601326L;
	private Plot plot;
	private String name;
	static final Logger Logger = LogManager.getLogger(PlotPanel.class.getName());
	private JPanel topJPanel;		// had been JPanel topPanel
	private JMapPane topMapPanel;	// need the topMapPanel for mapping
	private boolean isAMap = false;	// true == PlotPanel will contain a map; false == no map (JFreeChart)

	public PlotPanel(Plot plot, String name) {
		super(new BorderLayout());
		Logger.debug("in PlotPanel constructor for passed Plot plot, String name");
		isAMap = false;				// defaults to false but want to show it explicitly here in code
		JMenuBar bar = plot.getMenuBar();
		JToolBar toolBar = plot.getToolBar();
		topJPanel = new JPanel();
		topJPanel.setLayout(new BoxLayout(topJPanel, BoxLayout.Y_AXIS));
		if (bar != null) {
			bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
			bar.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			topJPanel.add(bar);
		}
		if (toolBar != null) {
			toolBar.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			if (topJPanel.getComponentCount() == 1) {
				topJPanel.add(Box.createRigidArea(new Dimension(0, 4)));
			}
			topJPanel.add(toolBar);
			topJPanel.add(Box.createRigidArea(new Dimension(0, 4)));
		}
		if (topJPanel.getComponentCount() > 0) add(topJPanel, BorderLayout.NORTH);
		add(plot.getPanel(), BorderLayout.CENTER);
		this.plot = plot;
		this.name = name;
	}

	// use this constructor for a GTTilePlotPanel instead of a JPanel object
	// used for GTTilePlot and AreaGTTilePlot
	public PlotPanel(Plot plot, String name, JMapPane aJMapPane, MapContent content, RenderingExecutor executor,
			GTRenderer renderer) {
		Logger.debug("in PlotPanel constructor for passed Plot plot, String name, MapContent content, RenderingExecutor executor, GTRenderer renderer");
		isAMap = true;
		JMenuBar bar = plot.getMenuBar();		// get the top menu from the plot
		Logger.debug("got bar = " + bar);
		((GTTilePlotPanel)plot).setMenuBar(bar);	// and put it in the GTTilePlotPanel
		JToolBar toolBar = plot.getToolBar();			// get the JToolBar of time step, layer, etc. widgets
		Logger.debug("got toolBar = " + toolBar);
		((GTTilePlotPanel)plot).setToolBar(toolBar);	// and put it in the GTTilePlotPanel
		if(content == null)		// if a MapContent was not passed in arg list, get the one from the plot
			content = topMapPanel.getMapContent();	// current failure point with NullPointerException
		else
			topMapPanel.setMapContent(content);
		Logger.debug("MapContent content = " + content);
		if(executor == null)	// if a RenderingExecutor was not passed in arg list, get the one from the plot
			executor = topMapPanel.getRenderingExecutor();
		else
			topMapPanel.setRenderingExecutor(executor);
		if(renderer == null)	// if a GTRenderer was not passed in arg list, get the one from the plot
			renderer = topMapPanel.getRenderer();
		else 
			topMapPanel.setRenderer(renderer);
		Logger.debug("topMapPanel = " + topMapPanel);
		this.plot = plot;
		Logger.debug("plot = " + plot.toString());
		this.name = name;
		Logger.debug("name = " + name);
		Logger.debug("PlotPanel = " + this.toString());
		Logger.debug("done with PlotPanel constructor");
	}

	public String getName() {
		Logger.debug("returning " + name);
		return name;
	}

	/**
	 * Sets the name this PlotPanel contains.
	 * Use this method instead of as an argument to the constructor.
	 * 
	 * @param aName
	 */
	public void setName(String aName)
	{
		Logger.debug("setting name to " + aName);
		this.name = aName;
	}

	public void addPlotListener(PlotListener listener) {
		Logger.debug("adding PlotListener");
		plot.addPlotListener(listener);
	}

	public void removePlotListener(PlotListener listener) {
		Logger.debug("removing PlotListener");
		plot.removePlotListener(listener);
	}

	/**
	 * Gets the type of the plot that this panel shows.
	 *
	 * @return the type of the plot that this panel shows.
	 */
	public Formula.Type getPlotType() {
		Logger.debug("getting plot type " + plot.getType().toString());
		return plot.getType();
	}

	/**
	 * Gets the plot this PlotPanel contains.
	 *
	 * @return  the plot this PlotPanel contains.
	 */
	public Plot getPlot() {
		Logger.debug("getting Plot");
		return plot;
	}

	/**
	 * Sets the plot this PlotPanel contains.
	 * Can use this method instead of as an argument to the constructor
	 * @param aPlot
	 */
	public void setPlot(Plot aPlot)
	{
		Logger.debug("setting Plot");
		this.plot = aPlot;
	}

	/**
	 * Notifies Plot when its View has been closed. HACK!
	 */
	public void viewClosed() {
		Logger.debug("in viewClosed()");
		if ( plot instanceof GTTilePlot ) {
			( (GTTilePlot) plot).stopThread();
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
	 * Change the view of this window to floating
	 * @param evt
	 */
	public void viewFloated(DockableFrameEvent evt){
		if ( plot instanceof anl.verdi.plot.gui.GTTilePlot ) {
			Logger.debug("Changing a GTTilePlot to viewFloated");
			( (anl.verdi.plot.gui.GTTilePlot) plot).viewFloated(evt);
		}
	}

	/**
	 * Change the view of this window to docked
	 * @param evt
	 */
	public void viewRestored(DockableFrameEvent evt){		
		if ( plot instanceof anl.verdi.plot.gui.GTTilePlot ) {
			Logger.debug("Changing a GTTilePlot to viewRestored");
			( (anl.verdi.plot.gui.GTTilePlot) plot).viewRestored(evt);
		}
	}

	public JMapPane getMapPane()
	{
		if(isAMap)
		{
			Logger.debug("getting ready to return the topMapPanel");
			return topMapPanel;
		}
		else
		{
			return null;
		}
	}

	/**
	 * access to JMapPane function getRenderer()
	 * gets the renderer, creating a default one if required
	 */
	public GTRenderer getRenderer()
	{
		if(isAMap)
		{
			return topMapPanel.getRenderer();
		}
		else
		{
			return null;
		}
	}

	/**
	 * access to JMapPane function setRenderer
	 * sets the renderer to be used by this map pane
	 * 
	 * @param aRenderer
	 */
	public void setRenderer(GTRenderer aRenderer)
	{
		if(isAMap)
			topMapPanel.setRenderer(aRenderer);
	}

	/**
	 * access to JMapPane function getRenderingExecutor
	 * gets the RenderingExecutor being used by this map pane
	 */
	public RenderingExecutor getRenderingExecutor()
	{
		if(isAMap)
		{
			return topMapPanel.getRenderingExecutor();
		}
		else
		{
			return null;
		}
	}

	/**
	 * access to JMapPane function setRenderingExecutor (method inherited from class AbstractMapPane)
	 * sets the RenderingExecutor to be used by this map pane
	 */
	public void setRenderingExecutor(RenderingExecutor aRenderingExecutor)
	{
		if(isAMap)
			topMapPanel.setRenderingExecutor(aRenderingExecutor);
	}

	/**
	 * gets the MapContent instance being displayed by this map pane
	 */
	public MapContent getMapContent()
	{
		if(isAMap)
		{
			return topMapPanel.getMapContent();
		}
		else
		{
			return null;
		}
	}

	/**
	 * access to JMapPane function setMapContent
	 * sets the MapContent instance containing the layers to display
	 */
	public void setMapContent(MapContent aMapContent)
	{
		if(isAMap)
			topMapPanel.setMapContent(aMapContent);
	}

	//	/**
	//	 * access to JMapPane function drawLayers
	//	 * Draws layers into 1 or more images which will then be displayed by the map pane
	//	 */
	//	public void drawLayers(boolean createNewImage)
	//	{
	//		if(isAMap)
	////			topMapPanel.drawLayers(createNewImage);
	//			getMapPane()
	//	}

//	/**
//	 * access to JMapPane function getBaseImage	
//	 */
//	public java.awt.image.RenderedImage getBaseImage()	// not currently used
//	{
//		if(isAMap)
//		{
//			return topMapPanel.getBaseImage();
//		}
//		else
//		{
//			return null;
//		}
//	}

}
