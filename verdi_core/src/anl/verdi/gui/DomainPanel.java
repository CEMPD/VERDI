package anl.verdi.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.border.TitledBorder;

//import org.piccolo2d.event.PInputEvent;
//import org.piccolo2d.event.PInputEventListener;
//import org.piccolo2d.event.PDragSequenceEventHandler;
//import org.piccolo2d.event.PPanEventHandler;
//import org.piccolo2d.util.PBounds;
//import org.geotools.data.shapefile.indexed.IndexedShapefileDataStoreFactory;	// deprecated, use ShapefileDataStoreFactory
//import org.geotools.styling.StyleFactoryFinder;
import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.shapefile.indexed.IndexedShapefileDataStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.map.MapViewport;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.styling.SLDParser;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
//import org.geotools.data.shapefile.indexed.IndexedShapefileDataStoreFactory;
//import org.geotools.map.DefaultMapContext;
//import org.geotools.map.DefaultMapLayer;
//import org.geotools.map.MapContext;
//import org.geotools.map.MapLayer;

import repast.simphony.gis.display.AbstractMarqueeZoomer;
import repast.simphony.gis.display.PGISCanvas;
import repast.simphony.gis.display.PiccoloMapPanel;
import repast.simphony.gis.tools.MapTool;
import repast.simphony.gis.tools.PGISPanTool;
import repast.simphony.gis.tools.PMarqueeZoomIn;
import repast.simphony.gis.tools.PMarqueeZoomOut;
import repast.simphony.gis.tools.ToolManager;
import anl.verdi.data.Axes;
import anl.verdi.data.CoordAxis;
import anl.verdi.data.Dataset;
import anl.verdi.data.DatasetMetadata;
import anl.verdi.util.Tools;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.Sizes;

import edu.umd.cs.piccolo.event.PInputEvent;			// NOTE: required old piccolo by Repast Simphony
import edu.umd.cs.piccolo.event.PInputEventListener;	// NOTE: required old piccolo by Repast Simphony
import edu.umd.cs.piccolo.util.PBounds;	// NOTE: required old piccolo by Repast Simphony

/**
 * @author User #2
 */
public class DomainPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5720531628640140189L;
	static final Logger Logger = LogManager.getLogger(DomainPanel.class.getName());

//	MapContext context;
	MapContent context;

	PGridNode node;

	PInputEventListener selectionListener;
	AbstractListElement currentElement;
	private Action edit;
	private Action info;
	private JLabel rangeLbl = new JLabel();
	private JLabel infoLb1 = new JLabel();
	private JLabel domainLbl = new JLabel();

	public DomainPanel() {
//		this(new DefaultMapContext(DefaultGeographicCRS.WGS84));
		this(new MapContent());		// 2014
		MapViewport aViewport = new MapViewport();
		try {
			aViewport.setCoordinateReferenceSystem(DefaultGeographicCRS.WGS84);
			
		} catch(Throwable ex){		// Exception - catch and ignore all things Throwable because error coming back is not real
		}
		this.context.setViewport(aViewport);
	}

//	public DomainPanel(MapContext context) {
	public DomainPanel(MapContent context) {	// 2014
		this.context = context;
		initComponents();
		piccoloMapPanel1.setPreferredSize(new Dimension(100, 100));
		piccoloMapPanel1.getCanvas().removeInputEventListener(
				piccoloMapPanel1.getCanvas().getPanEventHandler());

		edit = new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -8986960614344789837L;

			public void actionPerformed(ActionEvent e) {
				if (currentElement != null)
					editDomain();
			}
		};

//		edit.putValue(Action.NAME, "Edit Me");		// Edit button is now throwing error that hangs VERDI completely
//		Logger.debug("in DomainPanel, putValue 'Edit' Me");
//		edit.setEnabled(false);
//		JToolBar toolBar = piccoloMapPanel1.getToolBar();
//		toolBar.add(edit);
//		toolBar.setFloatable(false);
//		toolBar.add(Box.createHorizontalGlue());
//		toolBar.add(rangeLbl);

		info = new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 5772535970664186513L;

			public void actionPerformed(ActionEvent e) {
				if (currentElement != null)
					printInfo();
			}
		};
//		info.putValue(Action.NAME, "Metadata Me");		// Metadata button is now throwing error
//		Logger.debug("in DomainPanel, just putValue Metadata Me");
//		info.setEnabled(false);
//		toolBar.add(info);
//		toolBar.setFloatable(false);
//		toolBar.add(Box.createHorizontalGlue());
//		toolBar.add(infoLb1);
	}

//	public void unsetDomainValues() {	// 2014 appears to not be used
//		getCanvas().getLayer().removeChild((PNode)node);
//		try {
////			context.setAreaOfInterest(context.getLayerBounds(), context.getCoordinateReferenceSystem());	// deprecated functions 2014
//			context.getViewport().s
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//		node = null;
//		piccoloMapPanel1.enableTool(selectionListener);
//	}

	Axes<CoordAxis> axes;

	public void setDomainValues(AbstractListElement element) {
		currentElement = element;
		edit.setEnabled(currentElement != null);
		info.setEnabled(currentElement != null);
		getCanvas().getLayer().removeAllChildren();
		if (currentElement == null)
			return;

		axes = element.getAxes();	// 2014 MapContent.setAreaOfInterest deprecated; replace with MapViewport object connected to the MapContent
		ReferencedEnvelope env = new ReferencedEnvelope(axes.getBoundingBox(currentElement.getDataset().getNetcdfCovn()));
//		context.setAreaOfInterest(axes.getBoundingBox(currentElement.getDataset().getNetcdfCovn()));
		MapViewport aViewport = new MapViewport();
		aViewport.setBounds(env);
		aViewport.setCoordinateReferenceSystem(env.getCoordinateReferenceSystem()); 
		context.setViewport(aViewport);

		node = new PGridNode(axes, currentElement.getDataset().getNetcdfCovn());
		node.setDrawCompleteGrid(false);
		Rectangle2D domain = new Rectangle2D.Float();
		if (element.isXYUsed()) {
			domain.setFrameFromDiagonal(currentElement.getXMin(),
					currentElement.getYMin(), currentElement.getXMax(),
					currentElement.getYMax());
			node.setRangeFromDomain(domain);
		}
		getCanvas().getLayer().addChild(node.getPNode());
		//Rectangle2D rect = getCanvas().getBounds();
		// calculate the scale to use
		piccoloMapPanel1.getCanvas().getLayer().scale(
				1.0 / piccoloMapPanel1.getCanvas().getLayer().getScale());
		piccoloMapPanel1.getCanvas().getLayer().scale(.9);

		if (element.isXYUsed()) {
			final int columnMinimum = (int) element.getXMin() + 1;
			final int columnMaximum = (int) element.getXMax() + 1;
			final int rowMinimum = (int) element.getYMin() + 1;
			final int rowMaximum = (int) element.getYMax() + 1;
			rangeLbl.setText( "(" + columnMinimum + ", " + rowMinimum +
					          ") - (" +
					          columnMaximum + ", " + rowMaximum + ")" );
		} else {
			rangeLbl.setText("");
		}
	}

//	protected void addMapBackground(MapContext context) {
	protected void addMapBackground(MapContent context) {	// 2014
		try {
			StyleBuilder builder = new StyleBuilder();
			Style style = builder.createStyle(builder.createPolygonSymbolizer(
					builder.createStroke(), null));
			URL url = new File("./data/map_na.shp").toURI().toURL();
//			MapLayer layer = createLayer(url, new File("./data/states.sld"));
			FeatureLayer layer = createLayer(url, new File("./data/states.sld"));
			layer.setStyle(style);
			context.addLayer(layer);
			url = new File("./data/map_counties.shp").toURI().toURL();
			layer = createLayer(url, new File("./data/counties.sld"));
			layer.setStyle(style);
			context.addLayer(layer);
			// url = new File("data/majrdnet.shp").toURI().toURL();
			// context.addLayer(createLayer(url, new
			// File("data/majrdnet.sld")));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

//	protected MapLayer createLayer(URL url, File styleFile) throws IOException {
	protected FeatureLayer createLayer(URL url, File styleFile) throws IOException { // 2014
			Map<String, Serializable> params = new HashMap<String, Serializable>();
		params.put(ShapefileDataStoreFactory.URLP.key, url);		// IndexedShapefileDataStoreFactory deprecated, replace with ShapefileDataStoreFactory
		params.put(ShapefileDataStoreFactory.CREATE_SPATIAL_INDEX.key,true);
		ShapefileDataStoreFactory fac = new ShapefileDataStoreFactory();
		ShapefileDataStore ds = (ShapefileDataStore) fac.createDataStore(params);
		SLDParser parser = new SLDParser(CommonFactoryFinder.getStyleFactory());
		parser.setInput(styleFile);
		Style style = parser.readXML()[0];
//		return new DefaultMapLayer(ds.getFeatureSource(), style);
		return new FeatureLayer(ds.getFeatureSource(), style);	// 2014
	}

	class DomainZoomer extends AbstractMarqueeZoomer implements MapTool {
		public void execute(PiccoloMapPanel panel) {
			// TODO Auto-generated method stub
		}

		public Cursor getCursor() {
			// custom cursor
			// return Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
//			ImageIcon icon = new ImageIcon(getClass().getResource(
//					"/addArrow.gif"));
			String verdiHome = Tools.getVerdiHome();		// 2014 new method for reading in an image file
			String pathName = verdiHome + File.separator + "plugins" + File.separator + "core" + File.separator + "icons"
					 + File.separator + "addArrow.gif";
			File imageFile = new File(pathName);
//			return createCursor(icon, new Point(8, 3), "addArrow");
			return createCursor(imageFile, new Point(8,3), "addArrow");
		}

		@Override
		protected void execute(PInputEvent ev, PBounds rect) {
			// TODO Auto-generated method stub

		}
		
		public void activate(PiccoloMapPanel panel)		// 2014 required to implement part of MapTool interface
		{
			panel.requestFocusInWindow();
		}
		
		public void deactivate()	// 2014 required to implement part of MapTool interface
		{
			this.deactivate();
		}

	}

	class DomainZoomOut extends PMarqueeZoomOut implements MapTool {
//		DomainZoomOut(MapContext context) {
		DomainZoomOut(MapContent context) {	// 2014
			super(context);
		}

		@Override
		protected void execute(PInputEvent ev, PBounds rect) {
			// TODO Auto-generated method stub
			super.execute(ev, rect);
		}

		public void execute(PiccoloMapPanel panel) {
		}

		public Cursor getCursor() {
			// custom cursor
			ImageIcon icon = new ImageIcon(PMarqueeZoomOut.class
					.getResource("MagnifyMinus.png"));
			return createCursor(icon, new Point(13, 6), "magnify");
		}
		public void activate(PiccoloMapPanel panel)		// 2014 required to implement part of MapTool interface
		{
			panel.requestFocusInWindow();
		}
		
		public void deactivate()	// 2014 required to implement part of MapTool interface
		{
			this.deactivate();
		}

	}

	class DomainZoomIn extends PMarqueeZoomIn implements MapTool {
//		DomainZoomIn(MapContext context) {
		DomainZoomIn(MapContent context) {
			super(context);
		}

		@Override
		protected void execute(PInputEvent ev, PBounds rect) {
			// TODO Auto-generated method stub
			super.execute(ev, rect);
		}

		public void execute(PiccoloMapPanel panel) {
		}

		public Cursor getCursor() {
			// custom cursor
			URL imageFile = PMarqueeZoomIn.class.getResource("MagnifyPlus.png");
			ImageIcon icon = new ImageIcon(imageFile);
			return createCursor(icon, new Point(13, 6), "magnify");

		}
		public void activate(PiccoloMapPanel panel)		// 2014 required to implement part of MapTool interface
		{
			panel.requestFocusInWindow();
		}
		
		public void deactivate()	// 2014 required to implement part of MapTool interface
		{
			this.deactivate();
		}

	}

	static public Cursor createCursor(ImageIcon icon, Point hotSpot, String name) {
		if (icon != null) {
			Image image = icon.getImage();
			if (image != null) {
				BufferedImage image2 = new BufferedImage(32, 32,
						BufferedImage.TYPE_INT_ARGB);
				Graphics g = image2.getGraphics();
				// paint the icons to it
				g.setColor(new Color(0, 0, 0, 0));
				g.fillRect(0, 0, 32, 32);
				g.drawImage(image, 0, 0, null);
				Cursor cursor = Toolkit.getDefaultToolkit().createCustomCursor(
						image2, hotSpot, name);
				return cursor;
			}
		}
		return Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
	}

	static public Cursor createCursor(File imageInputFile, Point hotSpot, String name) 
	{	// 2014 new version of createCursor to take File instead of ImageIcon
		if (imageInputFile != null) {
			try{
				BufferedImage aBufferedImage = ImageIO.read(imageInputFile);
				BufferedImage image2 = new BufferedImage(32,32,BufferedImage.TYPE_INT_ARGB);
				Graphics g = image2.getGraphics();
				// paint the icons to it
				g.setColor(new Color(0, 0, 0, 0));
				g.fillRect(0, 0, 32, 32);
				g.drawImage(aBufferedImage, 0, 0, null);
				Cursor cursor = Toolkit.getDefaultToolkit().createCustomCursor(
						image2, hotSpot, name);
				return cursor;
			} catch (IOException e)
			{
				System.err.println("Failure reading graphics file: " + imageInputFile.toString());
				e.printStackTrace();
			}
			catch(Exception ex)
			{
				System.err.println("Failure processing graphics file: " + imageInputFile.toString());
				ex.printStackTrace();
			}
		}
		return Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);	// default return for null image file or failure processing graphics input file
	}

	
	class DomainPan extends PGISPanTool implements MapTool {
//		DomainPan(MapContext context, PGISCanvas canvas) {
		DomainPan(MapContent context, PGISCanvas canvas) {	// 2014
			super(context, canvas);
		}

		public void execute(PiccoloMapPanel panel) {
		}

		public Cursor getCursor() {
			// custom cursor
			return Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
		}

	}

	public void editDomain() {
		// 2014 changed to new model of MapContent/MapViewport instead of MapContext
//		MapContext editContext = new DefaultMapContext(context
//		MapContent editContext = new MapContent(context.getCoordinateReferenceSystem());
//		addMapBackground(editContext);
//		PiccoloMapPanel panel = new PiccoloMapPanel(editContext);
//		editContext.setAreaOfInterest(context.getAreaOfInterest());
		MapContent editContext = new MapContent();
		MapViewport aViewport = new MapViewport(context.getViewport().getBounds());
		aViewport.setCoordinateReferenceSystem(context.getViewport().getCoordinateReferenceSystem());
		editContext.setViewport(aViewport);
		addMapBackground(editContext);
		PiccoloMapPanel panel = new PiccoloMapPanel(editContext);
		final PGridNode grid = new PGridNode(axes, currentElement.getDataset().getNetcdfCovn());
		Rectangle2D rect = new Rectangle2D.Float();
		int minX = currentElement.getXMin();
		if (!currentElement.isXYUsed())
			minX = -1;
		rect.setFrameFromDiagonal(minX, currentElement.getYMin(),
				currentElement.getXMax(), currentElement.getYMax());

		grid.setRangeFromDomain(rect);

		panel.getCanvas().getLayer().addChild(grid);

//		for (MapLayer layer : context.getLayers()) {
		List<Layer> contextLayers = context.layers();
		for (Layer layer : contextLayers) {
			editContext.addLayer(layer);
		}
		HashMap<String, Object> toolParams = new HashMap<String, Object>();
		toolParams = new HashMap<String, Object>();
		// toolParams.put(Action.NAME, "Zoom In");
		toolParams.put(ToolManager.TOGGLE, true);
		URL imageFile = PMarqueeZoomIn.class.getResource("MagnifyPlus.png");
		Image image = Toolkit.getDefaultToolkit().getImage(imageFile);
		toolParams.put(Action.SMALL_ICON, new ImageIcon(image));
		toolParams.put(Action.SHORT_DESCRIPTION, "Zoom In");
		panel.addTool(new DomainZoomIn(context), toolParams);

		toolParams = new HashMap<String, Object>();
		// toolParams.put(Action.NAME, "Zoom Out");
		toolParams.put(ToolManager.TOGGLE, true);
		imageFile = PMarqueeZoomOut.class.getResource("MagnifyMinus.png");
		image = Toolkit.getDefaultToolkit().getImage(imageFile);
		toolParams.put(Action.SMALL_ICON, new ImageIcon(image));
		toolParams.put(Action.SHORT_DESCRIPTION, "Zoom Out");
		panel.addTool(new DomainZoomOut(context), toolParams);

		toolParams = new HashMap<String, Object>();
		toolParams.put(ToolManager.TOGGLE, true);
		toolParams.put(ToolManager.SELECTED, true);
		toolParams.put(Action.SHORT_DESCRIPTION, "Pan the map");
		imageFile = PMarqueeZoomOut.class.getResource("Move.png");
		image = Toolkit.getDefaultToolkit().getImage(imageFile);
		toolParams.put(Action.SMALL_ICON, new ImageIcon(image));
		toolParams.put("DEFAULT", new Boolean(true));
		panel.addTool(new DomainPan(context, piccoloMapPanel1.getCanvas()),
				toolParams);
		toolParams = new HashMap<String, Object>();
		toolParams.put(ToolManager.TOGGLE, true);

		toolParams.put(Action.SHORT_DESCRIPTION, "Select Region");
		toolParams.put(Action.NAME, "Select Region");
		selectionListener = new DomainZoomer() {
			public void mouseMoved(PInputEvent event) {
				super.mouseMoved(event);
				Point cell = grid.getGridCellFor(event.getPosition());
				long extent = axes.getYAxis().getRange().getExtent() - 1;
				domainLbl.setText("(" + (cell.x + 1) + ", "
						+ (extent - cell.y + 1) + ")");
			}

			@Override
			protected void execute(PInputEvent ev, PBounds rect) {
				if (rect == null)
					grid.addGridCell(ev.getPosition());
				else
					grid.addEnvelope(rect);
			}

		};
		panel.addTool(selectionListener, toolParams);

		toolParams.put(Action.SHORT_DESCRIPTION, "Clear Region");
		toolParams.put(Action.NAME, "Clear Region");
		JButton clearButton = new JButton("Clear Region");
		clearButton.setToolTipText("Clear the Region");

		clearButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// clear out the selection area
				grid.setRange(new Rectangle2D.Float(0, 0, -1, -1));
				grid.invalidatePaint();
			}

		});
		panel.getToolBar().add(clearButton);

		// piccoloMapPanel1.enableTool(selectionListener);
		String winTitle = "Edit Domain: " + currentElement;
		panel.getToolBar().add(Box.createHorizontalGlue());
		panel.getToolBar().add(domainLbl);
		Logger.debug("in DomainPanel, putting title 'Edit Domain:' before new EditDomainWindow");
		new EditDomainWindow((JFrame) null, grid, currentElement, panel,
				winTitle, true);

	}

	private void printInfo() {
		// Print projection info
		Dataset dataset = currentElement.getDataset();
		DatasetMetadata metaData = dataset.getMetadata();
		List<String> names = metaData.getProjectionNames();
		List<String> dimensionInfo = metaData.getDimensionInfo();
		List<String> attributes = metaData.getGlobalAttributes(names.get(0));

		JPanel panel = new JPanel();
		JTextArea area = new JTextArea(100, 80);

		String projectionInfo = "Projection information:\n\n";
		projectionInfo += getIndent(4) + "Name: "
				+ metaData.getProjectionClassNames().get(0) + "\n";
		projectionInfo += getIndent(4) + "Parameters: "
				+ metaData.paramsToString(names.get(0)) + "\n\n";
		projectionInfo += "Dataset Dimensions: \n";
		
		for (String dim : dimensionInfo) {
			projectionInfo += getIndent(4) + dim + "\n";
		}
		
		projectionInfo += "\n";
		projectionInfo += "Dataset global attributes: " + "\n\n";

		for (String attr : attributes) {
			if (!attr.startsWith("VAR-LIST")) {
				projectionInfo += getIndent(4) + attr + "\n";
			}
		}

		area.setText(projectionInfo);
		area.setEditable(false);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setViewportView(area);
		scrollPane.setPreferredSize(new Dimension(580, 320));

		panel.add(scrollPane);

		new ProjectInfoWindow((JFrame) null, panel,
				"Dataset Metadata");
		Logger.debug("in DomainPanel, just did new ProjectInfoWindow and passed in literal Dataset Metadata");
	}

	class EditDomainWindow extends OKCancelWindow {
		/**
		 * 
		 */
		private static final long serialVersionUID = -6008451308440006381L;
		AbstractListElement list;
		PGridNode grid;

		EditDomainWindow(Frame frame, PGridNode grid, AbstractListElement list,
				PiccoloMapPanel panel, String title, boolean state) {
			super(frame, title, state);
			this.list = list;
			this.grid = grid;
			panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			getContentPane().add("Center", panel);
			pack();
			setLocation(100, 20);
			setSize(700, 700);
			setVisible(true);
		}

		public boolean doAction() {
			// save the domain selected to the original object
			Rectangle2D range = grid.getRange();
			Rectangle2D rect = grid.getDomainFromRange();
			list.setDomain((int) rect.getMinX(), (int) rect.getMaxX(),
					(int) rect.getMinY(), (int) rect.getMaxY());
			if (list.isXYUsed()) {
				final int columnMinimum = (int) rect.getMinX() + 1;
				final int columnMaximum = (int) rect.getMaxX() + 1;
				final int rowMinimum = (int) rect.getMinY() + 1;
				final int rowMaximum = (int) rect.getMaxY() + 1;
				rangeLbl.setText( "(" + columnMinimum + ", " + rowMinimum +
						          ") - (" +
						          columnMaximum + ", " + rowMaximum + ")" );
			} else {
				rangeLbl.setText("");
			}

			if (currentElement == list) {
				node.getRange().setRect(range);
				DomainPanel.this.update();
			}
			return true;
		}
	}

	class ProjectInfoWindow extends OKCancelWindow {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		ProjectInfoWindow(Frame frame, JPanel panel, String title) {
			super(frame, title, true);
			panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			getContentPane().add("Center", panel);
			pack();
			setLocation(100, 100);
			setSize(600, 400);
			setVisible(true);
		}
	}

	public PGISCanvas getCanvas() {
		return piccoloMapPanel1.getCanvas();
	}
	
	private String getIndent(int numOfSpaces) {
		String spaces = "";
		
		for(int i = 0; i < numOfSpaces; i++)
			spaces += " ";
		
		return spaces;
	}

	public void update() {
		piccoloMapPanel1.getCanvas().repaint();
	}

	public void loadData(URL url) throws Exception {
		IndexedShapefileDataStore ds = new IndexedShapefileDataStore(url);
//		context.setAreaOfInterest(ds.getFeatureSource().getBounds(), 
//				ds.getFeatureSource().getSchema().getDefaultGeometry().getCoordinateSystem());
		context.getViewport().setBounds(ds.getFeatureSource().getBounds()); 
		context.getViewport().setCoordinateReferenceSystem(ds.getFeatureSource().getSchema().getCoordinateReferenceSystem());	//.getCRS());	
		StyleBuilder builder = new StyleBuilder();
		Style style = builder.createStyle(builder.createPolygonSymbolizer(builder.createStroke(), null));
		FeatureLayer newLayer = new FeatureLayer(ds.getFeatureSource(), style);
		
		context.addLayer(newLayer);
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		// Generated using JFormDesigner non-commercial license
		piccoloMapPanel1 = new PiccoloMapPanel(context);
		CellConstraints cc = new CellConstraints();

		// ======== this ========
		setBorder(new TitledBorder("Domain"));
		// 2014
		RowSpec[] aRowSpec = RowSpec.decodeSpecs("fill:default:grow");
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
				new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT,
						FormSpec.DEFAULT_GROW),
				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC }, aRowSpec));
//		setLayout(new FormLayout(new ColumnSpec[] {
//				FormFactory.DEFAULT_COLSPEC,
//				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
//				new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT,
//						FormSpec.DEFAULT_GROW),
//				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
//				FormFactory.DEFAULT_COLSPEC }, RowSpec
//				.decodeSpecs("fill:default:grow")));
		add(piccoloMapPanel1, cc.xywh(1, 1, 5, 1));
		// //GEN-END:initComponents
		piccoloMapPanel1.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				// resize the grid
				setDomainValues(currentElement);
			}
		});
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	// Generated using JFormDesigner non-commercial license
	private PiccoloMapPanel piccoloMapPanel1;

	// JFormDesigner - End of variables declaration //GEN-END:variables

	public static void main(String[] args) throws Exception {
		JFrame frame = new JFrame();
		frame.setSize(300, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		DomainPanel panel = new DomainPanel();
		frame.add(panel, BorderLayout.CENTER);
		frame.setVisible(true);
		panel.loadData(new File("./data/map_na.shp").toURI().toURL());
	}

}
