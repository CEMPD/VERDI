package anl.verdi.plot.gui;

import gov.epa.emvl.TilePlot;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.DefaultButtonModel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;

//import org.jdesktop.swingx.decorator.AlternateRowHighlighter;	// extension of Highlighter
import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
import org.jdesktop.swingx.JXTable;
//import org.jdesktop.swingx.decorator.Highlighter;

import saf.core.ui.dock.DockableFrame;
import saf.core.ui.dock.DockingManager;
import anl.map.coordinates.Decidegrees;
import anl.verdi.area.AreaDataFrameTableModel;
import anl.verdi.area.AreaTilePlot;
import anl.verdi.area.MapPolygon;
import anl.verdi.area.Units;
import anl.verdi.area.target.DepositionRange;
import anl.verdi.area.target.FormulaDialog;
import anl.verdi.area.target.GridInfo;
//import anl.verdi.area.target.ShapeFileTableExporter;	// 2014 disabling shapefile export in VERDI 1.5.0
import anl.verdi.area.target.Target;
import anl.verdi.area.target.TargetCalculator;
import anl.verdi.area.target.TargetDeposition;
import anl.verdi.core.Project;
import anl.verdi.core.VerdiApplication;
import anl.verdi.core.VerdiConstants;
import anl.verdi.data.Axes;
import anl.verdi.data.DataFrame;
import anl.verdi.data.DataFrameAxis;
import anl.verdi.data.DataFrameIndex;
import anl.verdi.data.DataUtilities.MinMax;
import anl.verdi.data.Variable;
import anl.verdi.formula.Formula;
import anl.verdi.io.TableExporter;
import anl.verdi.plot.color.ColorMap;
import anl.verdi.plot.config.PlotConfiguration;
import anl.verdi.plot.config.TilePlotConfiguration;


public class FastAreaTilePlot extends FastTilePlot {

	private static final long serialVersionUID = 7353202024063347257L;
	static final Logger Logger = LogManager.getLogger(FastAreaTilePlot.class.getName());

	static boolean ADD_SELECT_AREA_TO_CONTROL_MENU = false;

	static boolean showSelectedOnly=false;
	JRadioButtonMenuItem showTotalButton;
	public FastAreaTilePlot(VerdiApplication app,DataFrame dataFrame) {
		super(app,dataFrame);
		//app.getGui().setStatusOneText("Loading data. This may take a while please be patient...");
		this.tilePlot=new AreaTilePlot(this,startDate,startTime,timestepSize,domain,gridBounds,projector);
//		calculateAverageLevels();
//		minMax=null;
	}
	public void showAll() {
		setShowSelectedOnly(false);
		invalidate();
		repaint();
	}
	public void showSelected() {
		setShowSelectedOnly(true);
		invalidate();
		repaint();
	}
	public void showAverages() {
		((AreaTilePlot)tilePlot).showAverages();
		calculateAverageLevels();
//		minMax=null;
		invalidate();
		repaint();
	}
	public void showTotals() {
		((AreaTilePlot)tilePlot).showTotals();
		calculateTotalLevels();
//		minMax=null;
		invalidate();
		repaint();
	}
	public void showGrid() {
		((AreaTilePlot)tilePlot).showGrid();
		calculateGridLevels();
//		minMax=null;
		invalidate();
		repaint();
	}
	public void configure(PlotConfiguration config) {
		super.configure(config);
	}
	public void configure(PlotConfiguration config, Plot.ConfigSoure source) {
		super.configure(config, source);
	}	
	public void init(){

//		if (((AreaTilePlot) tilePlot).mouseOverOK) {
		
		
//			final int count = legendLevels.length;
			
			Logger.debug("calculateAverageLevels ");
			calcGlobalDepositionRange();
			
			// calc range for this set of numbers
			double[] minmax = { 0.0, 0.0 };

//			this.range;
			minmax[0] = range.averageMin;
			minmax[1] = range.averageMax;

			// if(minMax==null || minMax.getMin()>minmax[0] || minMax.getMax()<minmax[1])
			{
				//System.out.println("computing average data minmax...");
				//			double[] minmax = { 0.0, 0.0 };
				//			computeDataRange(minmax);

				// initialize colormap to these min max values
				minMax=new MinMax(minmax[0],minmax[1]);
				// TODO: JIZHEN - need the old map?
				//ColorMap map = new ColorMap(defaultPalette, minmax[0], minmax[1]);
				if ( map == null) {
					map = new ColorMap(defaultPalette, minmax[0], minmax[1]);
				} else {
					map.setPalette(defaultPalette);
					map.setMinMax( minmax[0], minmax[1]);
				}
				map.setPaletteType(ColorMap.PaletteType.SEQUENTIAL);
				config.putObject(TilePlotConfiguration.COLOR_MAP, map);


				Logger.debug("minmax: " + minmax[0] + " " + minmax[1]);
				setLegendLevels(minMax);
			}
			Logger.debug("minMax "+minMax.getMin()+" "+minMax.getMax());
//		}
//			super.draw();
			
	}

	public void calculateAverageLevels(){

		if (((AreaTilePlot) tilePlot).mouseOverOK) {
//			final int count = legendLevels.length;
			
			Logger.debug("calculateAverageLevels ");

			// calc range for this set of numbers
			double[] minmax = { 0.0, 0.0 };

			DepositionRange range = this.getGlobalDepositionRange();
			minmax[0] = range.averageMin;
			minmax[1] = range.averageMax;

			{
				// initialize colormap to these min max values
				minMax=new MinMax(minmax[0],minmax[1]);
				if ( map == null) {
					map = new ColorMap(defaultPalette, minmax[0], minmax[1]);
				} else {
					map.setPalette(defaultPalette);
					map.setMinMax( minmax[0], minmax[1]);
				}
				map.setPaletteType(ColorMap.PaletteType.SEQUENTIAL);
				config.putObject(TilePlotConfiguration.COLOR_MAP, map);


				Logger.debug("minmax: " + minmax[0] + " " + minmax[1]);
				setLegendLevels(minMax);
			}
		}
	}

	public void calculateGridLevels(){

		if (((AreaTilePlot) tilePlot).mouseOverOK) {
//			final int count = legendLevels.length;
			
			Logger.debug("calculateGridLevels ");

			// calc range for this set of numbers
			double[] minmax = { 0.0, 0.0 };
			computeDataRange(minmax, this.log); // grid min max

			{
				// initialize colormap to these min max values
				minMax=new MinMax(minmax[0],minmax[1]);
				if ( map == null) {
					map = new ColorMap(defaultPalette, minmax[0], minmax[1]);
				} else {
					map.setPalette(defaultPalette);
					map.setMinMax( minmax[0], minmax[1]);
				}
				map.setPaletteType(ColorMap.PaletteType.SEQUENTIAL);
				config.putObject(TilePlotConfiguration.COLOR_MAP, map);


				Logger.debug("minmax: " + minmax[0] + " " + minmax[1]);
				setLegendLevels(minMax);
			}
			Logger.debug("minMax "+minMax.getMin()+" "+minMax.getMax());
		}
	}

	public void setLegendLevels(MinMax minMax){
		final int count = legendLevels.length;
		final double delta = (minMax.getMax() - minMax.getMin()) / (count - 1);

		for (int level = 0; level < count; ++level) {
			legendLevels[level] = minMax.getMin() + level * delta;
		}
	}
	public void calculateTotalLevels(){
		if (((AreaTilePlot) tilePlot).mouseOverOK) {
			Logger.debug("calculateTotalLevels");

			// calc range for this set of numbers
			double[] minmax = { 0.0, 0.0 };
			DepositionRange range = this.getGlobalDepositionRange();
			minmax[0] = range.totalMin;
			minmax[1] = range.totalMax;

			// if never set before or if larger range than last set of numbers
			{
				Logger.debug("computing total data minmax...");

				// initialize colormap to these min max values
				minMax=new MinMax(minmax[0],minmax[1]);
				if ( map == null){
					map = new ColorMap(defaultPalette, minmax[0], minmax[1]);
				} else {
					map.setPalette(defaultPalette);
					map.setMinMax( minmax[0], minmax[1]);
				}
				map.setPaletteType(ColorMap.PaletteType.SEQUENTIAL);
				config.putObject(TilePlotConfiguration.COLOR_MAP, map);

				Logger.debug("minmax: " + minmax[0] + " " + minmax[1]);
				setLegendLevels(minMax);
			}
			
		}
	}
	JRadioButtonMenuItem selectionMenuItem=null;
	public boolean selectingAreas(){
		return selectionMenuItem.isSelected();
	}
	public String createTableName(Variable[] vars){
		Axes<DataFrameAxis> axes = getDataFrame().getAxes();
		StringBuilder builder = new StringBuilder("Area Information: ");

		// form the name of the table
		if(vars.length<2)builder.append(vars[0].getName());
		else builder.append("Formulas");
		builder.append(" (");
		int time = axes.getTimeAxis().getOrigin();
		builder.append(time);
		builder.append(", ");
		if (layer != -1) {
			builder.append(layer);
		}
		builder.append(")"); 
		return builder.toString();
	}
	public JTable createTable(String[] formula,boolean selectedOnly){
		// limit it to valid formula only
		DataFrame[] dataFrames=new DataFrame[formula.length];
		Variable[] vars = new Variable[formula.length];
		for(int i=0;i<formula.length;i++){
			DataFrame dataFrame=app.evaluateFormula(formula[i],Formula.Type.TILE, null) ;
			dataFrames[i]=dataFrame;
			vars[i]=dataFrame.getVariable();
		}

		ArrayList targets= selectedOnly ? Target.getSelectedTargets() : Target.getTargets();

		// create the table
		JXTable table = new JXTable(new AreaDataFrameTableModel(dataFrames,targets,vars));
		table.setColumnControlVisible(true);
		table.setHorizontalScrollEnabled(true);
		table.setRolloverEnabled(true);

		// set the column headers on the table
		table.getTableHeader().getColumnModel().getColumn(0).setHeaderValue("Name");
		table.getTableHeader().getColumnModel().getColumn(1).setHeaderValue("Area ("+Target.getDefaultUnits()+")");

		for(int i=0;i<vars.length;i++){
			String massUnit = Units.getTotalVariable(vars[i].getUnit().toString());
			table.getTableHeader().getColumnModel().getColumn(i*2+2).setHeaderValue(vars[i].getName()+" ("+massUnit+")");
			table.getTableHeader().getColumnModel().getColumn(i*2+3).setHeaderValue(vars[i].getName()+"_A"+" ("+vars[i].getUnit()+")");
		}
		String name = createTableName(vars);
		table.setName(name);
		return table;
	}

	public void actionPerformed(ActionEvent event) {
//		final Object source = event.getSource();
		String command = event.getActionCommand();
		if(command.equals(AREA_COMMAND)){
			// show information
			Project project = app.getProject();
			List list = project.getFormulasAsList();
			FormulaDialog dialog = new FormulaDialog(app.getGui().getFrame());
			dialog.setLocationRelativeTo(app.getGui().getFrame());
			dialog.setFormulas(list,getDataFrame().getVariable().toString());
			dialog.pack();

			dialog.setVisible(true);

			String[] formula = dialog.getSelectedFormulas();
			if(formula==null)return ;
			if(formula.length==0)return;

			boolean selectedOnly = dialog.getSelectedAreasOnly();
			JTable table = createTable(formula,selectedOnly);
			if(table!=null)addProbe(table, table.getName(), "Areas");
		}
		super.actionPerformed(event);
	}

	public JMenuBar getMenuBar() {
		JMenuBar bar=super.getMenuBar();
		JMenu menu=null;

		// add in my extra option to control menu
		for(int i=0;i<bar.getMenuCount();i++){
			if(bar.getMenu(i).getText().equals("Controls"))menu=bar.getMenu(i);	
		}
		// get the buttonGroup
		if(menu!=null){
			DefaultButtonModel model=((DefaultButtonModel)((JRadioButtonMenuItem)menu.getMenuComponent(0)).getModel());
			ButtonGroup selectionGroup=model.getGroup(); 
			selectionMenuItem = new JRadioButtonMenuItem(new AbstractAction(
			"Select Areas") {
				private static final long serialVersionUID = 5282480503103839989L;

				public void actionPerformed(ActionEvent e) {
					JRadioButtonMenuItem src = (JRadioButtonMenuItem) e.getSource();
					Logger.debug("selection action "+src.isSelected());
					probe=false;
					zoom=false;
					float xScale = (float)columns / (dataArea.width == 0 ? columns : dataArea.width);
					float yScale = (float)rows / (dataArea.height == 0 ? rows : dataArea.height);

					rubberband.setActive(true);
					rubberband.setDataArea(dataArea, xScale, yScale);
					// change cursor
					setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				}
			});

			selectionGroup.add(selectionMenuItem);
			menu.add(selectionMenuItem,0);

			selectionMenuItem.setEnabled(ADD_SELECT_AREA_TO_CONTROL_MENU);

		}

		// add in my extra option menu
		menu = new JMenu("Options");
		ButtonGroup group = new ButtonGroup();

		JRadioButtonMenuItem radioButton=new JRadioButtonMenuItem(new AbstractAction("Show Area Averages") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 4673389754505180377L;

			public void actionPerformed(ActionEvent e) {
				JRadioButtonMenuItem item = (JRadioButtonMenuItem) e.getSource();
				if(item.isSelected())FastAreaTilePlot.this.showAverages();
				draw();
			}
		});
		radioButton.setSelected(true);
		group.add(radioButton);
		menu.add(radioButton);

		showTotalButton=new JRadioButtonMenuItem(new AbstractAction("Show Area Totals") {
			/**
			 * 
			 */
			private static final long serialVersionUID = -4078259126612838335L;

			public void actionPerformed(ActionEvent e) {
				JRadioButtonMenuItem item = (JRadioButtonMenuItem) e.getSource();
				if(item.isSelected())FastAreaTilePlot.this.showTotals();
				draw();
			}
		});
		group.add(showTotalButton);
		menu.add(showTotalButton);
		// disable the radiobutton if needed
		// see if the current formula type allows this
		if(Units.isConcentration(getDataFrame().getVariable().getUnit().toString())){
			showTotalButton.setEnabled(false);
		}

		radioButton=new JRadioButtonMenuItem(new AbstractAction("Show Gridded Data") {
			/**
			 * 
			 */
			private static final long serialVersionUID = -6206984806970043891L;

			public void actionPerformed(ActionEvent e) {
				JRadioButtonMenuItem item = (JRadioButtonMenuItem) e.getSource();
				if(item.isSelected())FastAreaTilePlot.this.showGrid();
				draw();
			}
		});
		group.add(radioButton);
		menu.add(radioButton);

		menu.addSeparator();

		// make radio buttons for filling options
		ButtonGroup group2 = new ButtonGroup();

		radioButton=new JRadioButtonMenuItem(new AbstractAction("Selected Areas") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 8704295164108322755L;

			public void actionPerformed(ActionEvent e) {
				JRadioButtonMenuItem item = (JRadioButtonMenuItem) e.getSource();
				if(item.isSelected())FastAreaTilePlot.this.showSelected();
				draw();
			}
		});
		radioButton.setSelected(false);
		group2.add(radioButton);
		menu.add(radioButton);

		radioButton=new JRadioButtonMenuItem(new AbstractAction("All Areas") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 841441627768110972L;

			public void actionPerformed(ActionEvent e) {
				JRadioButtonMenuItem item = (JRadioButtonMenuItem) e.getSource();
				if(item.isSelected())FastAreaTilePlot.this.showAll();
				draw();
			}
		});
		radioButton.setSelected(true);
		group2.add(radioButton);
		menu.add(radioButton);

		bar.add(menu);
		return bar;
	}

	public float[][] getAllLayerData() {
		return getAllLayerData(getDataFrame());
	}

	public float[][] getAllLayerData(DataFrame dataFrame) {

		// Reallocate the subsetLayerData[][] only if needed:

		float[][] subsetLayerData;

		subsetLayerData = new float[rows][columns];


		// Copy from dataFrame into subsetLayerData[ rows ][ columns ]:

		final DataFrameIndex dataFrameIndex = dataFrame.getIndex();

		for (int row = 0; row < rows; ++row) {

			for (int column = 0; column < columns; ++column) {
				dataFrameIndex.set(timestep-firstTimestep, layer-firstLayer, column, row);
				final float value = dataFrame.getFloat(dataFrameIndex);
				subsetLayerData[row][column] = value;
			}
		}
		return subsetLayerData;
	}

	public static boolean isShowSelectedOnly() {
		return showSelectedOnly;
	}

	public static void setShowSelectedOnly(boolean showSelectedOnly) {
		FastAreaTilePlot.showSelectedOnly = showSelectedOnly;
	}

	public TilePlot getTilePlot() {
		return tilePlot;
	}
	public void recalculateAreas(){
		// redo the area calculations because something changed with the areas 
		Logger.debug("recalculating areas in FastAreaTilePlot.recalculateAreas");
		TargetCalculator calc = new TargetCalculator();
		if(tilePlot==null||getDataFrame()==null)return;
		calc.calculateIntersections(Target.getTargets(),getDataFrame(),(AreaTilePlot)tilePlot);
	}
	public void repaintAll(){
		validate();
		draw();
		repaint();

	}
	final static int TOLERANCE=2;
	public static final String AREA_COMMAND = "Area Information";
	protected JPopupMenu createPopupMenu(boolean properties, boolean save,
			boolean print, boolean zoomable) {

		JPopupMenu result = super.createPopupMenu(properties, save, print, zoomable);
		// items to the top
		result.setLabel("Areal Interpolation");

		JMenuItem menuItem = new JMenuItem("Area Information...");
		menuItem.setActionCommand(AREA_COMMAND);
		menuItem.addActionListener(this);
		result.add(menuItem,0);
		result.add( new JPopupMenu.Separator(),1 );

		return result;
	}
	public void processMouseEvent(MouseEvent me) {
		int mod = me.getModifiers();
		int mask = MouseEvent.BUTTON3_MASK; 
		//see if they are popping up data on a polygon
		if ((mod & mask) != 0) {
			super.processMouseEvent(me);
			return;
		}

		if(selectingAreas()){

			if(isInDataArea(me)&&me.getID()==MouseEvent.MOUSE_RELEASED){
				Rectangle axisRect = rubberband.getAxisBounds();
				Rectangle rect = rubberband.getBounds();

				super.processMouseEvent(me);

				// assume they meant to pick a point if it was a tiny square
				if(axisRect.getWidth()<TOLERANCE&&axisRect.getHeight()<TOLERANCE){
					// select the area in the list
					Decidegrees gp = getLatLonFor(me.getX(), me.getY());
					Target target=MapPolygon.getTargetWithin(gp.y, gp.x);

					if(target!=null){
						if(!me.isControlDown()){
							//clear all other selections
							Target.setSelectedTargets(new ArrayList());
							// turn this one on
							target.setSelected(true);
						}else{
							// toggle the state
							target.setSelected(!target.isSelectedPolygon());
						}
					}
					app.getGui().getAreaPanel().areasSelected(Target.getSelectedTargets());
					repaintAll();
				}else{
					// pick every polygon within that area
					ArrayList<Target> targets=MapPolygon.getTargetsWithin(rect);
					if(!me.isControlDown()){
						// turn off everything else and turn these on instead
						Target.setSelectedTargets(targets);
					}else{
						// leave other ones alone
						// toggle selected ones
						for(Target target:targets){
							// toggle the state
							target.setSelected(!target.isSelectedPolygon());

						}
					}
					app.getGui().getAreaPanel().areasSelected(Target.getSelectedTargets());
					repaintAll();
				}

			}else super.processMouseEvent(me);
			return;
		}
		super.processMouseEvent(me);
	}

	public void processMouseMotionEvent(MouseEvent me) {
		String units = getDataFrame().getVariable().getUnit().toString();
		if (units==null || units.trim().equals(""))
			units = "none";
		String massUnit = Units.getTotalVariable(units);
		String areaUnit = (Units.getAreaUnit(units) != null ? Units.getAreaUnit(units).toString() : "");
		if(Units.isConcentration(units))massUnit=null;
		if(isInDataArea(me)){
			if(me.getID() == MouseEvent.MOUSE_MOVED){
				Decidegrees gp = getLatLonFor(me.getX(), me.getY());
				Target target=MapPolygon.getTargetWithin(gp.y, gp.x);

				if(target!=null){
					try {
						double areaValue = target.getArea();

						if(target.containsDeposition()){
							float value = target.getDeposition();
							float aveValue = target.getAverageDeposition();

							if(massUnit!=null)app.getGui().setStatusOneText("Area "+target.toString()+": " + areaValue + " " + areaUnit + ", Total: "+value+" "+massUnit+", Average: "+aveValue+" "+units);
							else app.getGui().setStatusOneText("Area "+target.toString()+": " + areaValue + " " + areaUnit + ", Average: "+aveValue+" "+units);
						}else app.getGui().setStatusOneText("Area "+target.toString()+": " + areaValue  + " " + areaUnit + ".");
					} catch ( Exception e) {
						//
					}
				}
				else app.getGui().setStatusOneText(gp.toString());
			}
		}else{
			app.getGui().setStatusOneText("");
		}
		super.processMouseMotionEvent(me);

	}
	// show info on all selected areas
	private class ProbeExportAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 440426181430262840L;
		private JTable table;
		private String title, rangeAxisName;

		public ProbeExportAction(String rangeAxisName, JTable table,
				String title) {
			super("Export");
			this.rangeAxisName = rangeAxisName;
			this.table = table;
			this.title = title;
		}

		public void actionPerformed(ActionEvent e) {
			TableExporter exporter = new TableExporter(table, title,
					rangeAxisName);
			exporter.setExportHeader(true);

			try {
				exporter.run();
			} catch (IOException ex) {
				//ctr.error("Error while exporting probed data", ex);
			}
		}
	}
	// show info on all selected areas
//	private class ProbeExportShapeAction extends AbstractAction {
//
//		/**
//		 * 
//		 */
//		private static final long serialVersionUID = 6184257759928589456L;
//		private JTable table;
//		private String title, rangeAxisName;
//
//		public ProbeExportShapeAction(String rangeAxisName, JTable table,
//				String title) {
//			super("Export Shape Files");
//			this.rangeAxisName = rangeAxisName;
//			this.table = table;
//			this.title = title;
//		}
//
//		public void actionPerformed(ActionEvent e) {	// 2014 disable shapefile export from VERDI 1.5.0
//
//			ShapeFileTableExporter exporter = new ShapeFileTableExporter(table, title,
//					rangeAxisName);
//			exporter.setExportHeader(true);
//
//			try {
//				exporter.run();
//			} catch (IOException ex) {
//				//ctr.error("Error while exporting probed data", ex);
//			}
//		}
//	}
	private int plotCount = 0;
	public void addProbe(final JTable table, String name, String rangeAxisName) {
		String viewId = name + plotCount++;
		JPanel panel = new JPanel(new BorderLayout());
		JScrollPane pane = new JScrollPane(table);
		// if (rowHeader != null) pane.setRowHeaderView(rowHeader);
		panel.add(pane, BorderLayout.CENTER);
		TitlePanel title = new TitlePanel();
		title.setText(name);
		JPanel top = new JPanel(new BorderLayout());
		JMenuBar bar = new JMenuBar();
		JMenu menu = new JMenu("File");
		menu.add(new ProbeExportAction(rangeAxisName, table, name));
//		menu.add(new ProbeExportShapeAction(rangeAxisName, table, name));	// 2014 disable export shapefiles VERDI 1.5.0
		bar.add(menu);
		top.add(bar, BorderLayout.NORTH);
		top.add(title, BorderLayout.CENTER);
		panel.add(top, BorderLayout.NORTH);

		DockingManager viewManager = app.getGui().getViewManager();
		DockableFrame view = viewManager.createDockable(viewId, panel);
		view.setTitle(name);
		viewManager.addDockableToGroup(VerdiConstants.PERSPECTIVE_ID, VerdiConstants.MAIN_GROUP_ID, view);
		view.toFront();
	}

	/*
	 * The following is because the existing code can not calculate total and average deposition for 
	 * all the frames for selected targets.
	 * These methods go over all the time and layer to calculate them, and get the ranges, which are used 
	 * to build the legend on a plot.
	 * 
	 * Author: Jizhen Zhao @ IE UNC
	 * Version: 2012-05-10
	 */

	private DepositionRange range = new DepositionRange();
	private boolean depositionRangeAlreadySet = false;

	private float[][] getSubsetLayerData(DataFrame dataFrame, 
			int timestep, int firstTimestep,
			int layer,    int firstLayer,
			int rows,     int columns) {

		float[][] subsetLayerData;

		subsetLayerData = new float[rows][columns];

		final DataFrameIndex dataFrameIndex = dataFrame.getIndex();

		for (int row = 0; row < rows; ++row) {

			for (int column = 0; column < columns; ++column) {
				dataFrameIndex.set(timestep-firstTimestep, layer-firstLayer, column, row);
				final float value = dataFrame.getFloat(dataFrameIndex);
				subsetLayerData[row][column] = value;
			}
		}
		return subsetLayerData;
	}

	private DepositionRange getGlobalDepositionRange() {

		if ( !depositionRangeAlreadySet ) {

			app.getGui().setStatusOneText("Calculating deposition range.");
			calcGlobalDepositionRange();
			// calc range for this set of numbers
			double[] minmax = { 0.0, 0.0 };

			minmax[0] = range.averageMin;
			minmax[1] = range.averageMax;

			// if(minMax==null || minMax.getMin()>minmax[0] || minMax.getMax()<minmax[1])
			{
				//System.out.println("computing average data minmax...");
				//			double[] minmax = { 0.0, 0.0 };
				//			computeDataRange(minmax);

				// initialize colormap to these min max values
				minMax=new MinMax(minmax[0],minmax[1]);
				if ( map == null) {
					map = new ColorMap(defaultPalette, minmax[0], minmax[1]);
				} else {
					map.setPalette(defaultPalette);
					map.setMinMax( minmax[0], minmax[1]);
				}
				map.setPaletteType(ColorMap.PaletteType.SEQUENTIAL);
				config.putObject(TilePlotConfiguration.COLOR_MAP, map);

				setLegendLevels(minMax);
			}
		}

		return range;
	}

	private void calcGlobalDepositionRange() {
		for (int timestep=0; timestep<this.timesteps; timestep++) {
			for (int layer=0; layer<this.layers; layer++) {
				float[][] subsetLayerData = getSubsetLayerData( this.dataFrame, 
						timestep, this.firstTimestep,
						layer,    this.firstLayer,
						this.rows,     this.columns);
				GridInfo gridInfo = new GridInfo(gridBounds,domain);
				int num=GridInfo.getGridNumber(gridInfo);
				calcFrameDepositionRange(subsetLayerData, num, range);
			}
		}

		depositionRangeAlreadySet = true;
	}

	private void calcFrameDepositionRange(float [][] data, int gridIndex, DepositionRange range) {
		ArrayList polygons=Target.getTargets();
		
		TargetDeposition deposition = new TargetDeposition();
		for(Target polygon:(ArrayList<Target>)polygons){
			Target.setUnitConverters(units);
			polygon.computeAverageDeposition(data,gridIndex, deposition);
			if (deposition.total > range.totalMax) {
				range.totalMax = deposition.total;
			}
			if (deposition.total < range.totalMin) {
				range.totalMin = deposition.total;
			}
			if (deposition.average > range.averageMax) {
				range.averageMax = deposition.average;
			}
			if (deposition.average < range.averageMin) {
				range.averageMin = deposition.average;
			}
		}
	}
}
