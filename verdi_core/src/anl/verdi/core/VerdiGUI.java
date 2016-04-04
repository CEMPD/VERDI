package anl.verdi.core;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
import org.geotools.geometry.jts.ReferencedEnvelope;
//import org.geotools.swing.JMapFrame;
import org.geotools.swing.JMapPane;

import saf.core.ui.GUIBarManager;
import saf.core.ui.GUIConstants;
import saf.core.ui.dock.DockableFrame;
import saf.core.ui.dock.DockingManager;
import saf.core.ui.dock.Perspective;
import saf.core.ui.event.DockableFrameEvent;
import saf.core.ui.event.DockableFrameListener;
//import simphony.util.messages.MessageCenter;
import visad.java3d.DisplayImplJ3D;
import anl.verdi.area.AreaFile;
import anl.verdi.area.AreaFilePanel;
import anl.verdi.data.Dataset;
import anl.verdi.formula.Formula;
import anl.verdi.gui.DataSetPanel;
import anl.verdi.gui.FormulasPanel;
import anl.verdi.gui.ScriptPanel;
import anl.verdi.io.TableExporter;
import anl.verdi.plot.gui.Plot;
import anl.verdi.plot.gui.PlotPanel;
import anl.verdi.plot.gui.TitlePanel;

/**
 * Facade for gui related application operations.
 * 
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class VerdiGUI implements WindowListener, DockableFrameListener {
	static final Logger Logger = LogManager.getLogger(VerdiGUI.class.getName());

//	private static final MessageCenter ctr = MessageCenter.getMessageCenter(VerdiGUI.class);

	private DockingManager manager;
	private static int plotCount = 0;
	private JFrame frame;
	private DataSetPanel datasets;
	private AreaFilePanel areaPanel;
	private java.util.List<String> viewList = new ArrayList<String>(); // amw
																		// 02May07
	private java.util.List<JFrame> framesToDisplay = new ArrayList<JFrame>(); // amw	// JEB try JFrame => JMapFrame
																				// 02May07
	private HashMap<String, PlotPanel> plotPanels = new HashMap<String, PlotPanel>();
	private HashMap<String, JPanel> scriptPanels = new HashMap<String, JPanel>();
	private static HashMap<Plot, DockableFrame> views = new HashMap<Plot, DockableFrame>();
	private static boolean windowIsIconified = false;

//	private FormulasPanel formulasPanel;

	private Set<String> contourPlots = new HashSet<String>();

	public VerdiGUI(DockingManager manager, DataSetPanel datasets, FormulasPanel formulas, AreaFilePanel areaPanel) {
		this.manager = manager;
		this.datasets = datasets;
		this.areaPanel = areaPanel;
//		this.formulasPanel = formulas;
	}

	public void windowActivated(WindowEvent unused) {
	}

	public void windowClosed(WindowEvent unused) {
	}

	public void windowClosing(WindowEvent unused) {
	}

	public void windowDeactivated(WindowEvent unused) {
	}

	public void windowDeiconified(WindowEvent unused) {
		windowIsIconified = false;
	}

	public void windowIconified(WindowEvent unused) {
		windowIsIconified = true;
	}

	public void windowOpened(WindowEvent unused) {
	}

	public void undockAllPlots() {
		Perspective perspective = manager.getPerspective(VerdiConstants.PERSPECTIVE_ID);
		java.util.List<DockableFrame> views = manager.getDockableFrames(VerdiConstants.PERSPECTIVE_ID, VerdiConstants.MAIN_GROUP_ID);

		if (perspective.isActive()) {
			for (DockableFrame frame : views) {
				frame.doFloat();
			}
		}
	}

	/**
	 * Gets all the displayed plots of the specified type.
	 * 
	 * @param type
	 *            the type of plot
	 * @return all the displayed plots of the specified type.
	 */
	public java.util.List<Plot> getDisplayedPlots(Formula.Type type) {
		java.util.List<Plot> plots = new ArrayList<Plot>();
		java.util.List<DockableFrame> views = manager.getDockableFrames(VerdiConstants.PERSPECTIVE_ID, VerdiConstants.MAIN_GROUP_ID);
		
		for (DockableFrame view : views) {
			JComponent container = (JComponent)view.getContentPane().getComponent(0);
			if(container instanceof PlotPanel){
				PlotPanel panel = (PlotPanel)container;
				Plot plot = panel.getPlot();
				if (type == null)
					plots.add(plot);
				else if (plot.getType() == type)
					plots.add(plot);
			}
		}
		return plots;
	}

	/**
	 * Gets all the displayed plots.
	 * 
	 * @return all the displayed plots.
	 */
	public java.util.List<Plot> getDisplayedPlots() {
		return getDisplayedPlots(null);
	}

	public void setSaveEnabled(boolean enabled) {
		GUIBarManager barManager = manager.getBarManager();
		barManager.getToolBarComponent(VerdiConstants.SAVE_ID).setEnabled(enabled);
		barManager.getMenuItem(VerdiConstants.SAVE_ID).setEnabled(enabled);
		barManager.getMenuItem(VerdiConstants.SAVE_AS_ID).setEnabled(enabled);
	}

	public void addPlot(PlotPanel plotPanel) {
		setStatusTwoText("");
		String name = plotPanel.getName();
		
		// BEGIN SECTION FOR TESTING
		JMapPane aMapPane = plotPanel.getMapPane();
		if(aMapPane != null)
		{
			Logger.debug("in VerdiGUI.addPlot; existing JMapPane in plotPanel = " + aMapPane.toString());
			ReferencedEnvelope aMPReferencedEnvelope = aMapPane.getDisplayArea();
			double minX = aMPReferencedEnvelope.getMinX();
			double minY = aMPReferencedEnvelope.getMinY();
			double maxX = aMPReferencedEnvelope.getMaxX();
			double maxY = aMPReferencedEnvelope.getMaxY();
			Logger.debug("and its ReferencedEnvelope = (" + minX + ", " + maxX + ", " + minY + ", " + maxY + ")");
			Logger.debug("and its current CRS = " + aMPReferencedEnvelope.getCoordinateReferenceSystem());
		}
		// END SECTION FOR TESTING
		
		String viewId = replaceInvalidChars(name) + plotCount++;

		if (plotPanel.getPlotType() == Formula.Type.CONTOUR) {
			addContourPlot(viewId, name, plotPanel);
		} else {
			DockableFrame view = manager.createDockable(viewId, plotPanel);
			view.setTitle(name);
			Logger.debug("in addPlot: VerdiConstants.PERSPECTIVE_ID = " + VerdiConstants.PERSPECTIVE_ID);
			Logger.debug("VerdiConstants.MAIN_GROUP_ID = " + VerdiConstants.MAIN_GROUP_ID);
			Logger.debug("view title = " + view.getTitle() + ", ID = " + view.getID());
			Logger.debug("ready to call manager.addDockableToGroup");
			manager.addDockableToGroup(VerdiConstants.PERSPECTIVE_ID, VerdiConstants.MAIN_GROUP_ID, view);
			Logger.debug("back again & now ready to call manager.removeDockableListener");
			manager.removeDockableListener(this); //To make sure 'this' listener won't add itself too many times
			Logger.debug("back again & now ready to call manager.addDockableListener");
			manager.addDockableListener(this);
			Logger.debug("back again & now ready to call view.toFront");
			view.toFront();
			Logger.debug("back & now ready to call views.put");
			views.put(plotPanel.getPlot(), view);
			Logger.debug("back from views.put, ending function addPlot");
		}
		viewList.add(viewId); // amw 02May07
		plotPanels.put(viewId, plotPanel);
	}
	
	private String replaceInvalidChars(String name) {
		if (name == null || name.trim().isEmpty())
			return "";
		
		for (int i = 0; i < name.length(); i++) {
			if (!Character.isLetterOrDigit(name.charAt(i))) {
				String random = Math.random() + "";
				name = name.replace(name.charAt(i), random.charAt(random.length() - 1));
			}
		}
		return name;
	}

	// Is the GUI iconified?

	public static boolean isIconified() {
		return windowIsIconified;
	}

	// Is this plot either an unselected tab or is the GUI iconified?

	public static boolean isHidden( Plot plot ) {
		boolean result = windowIsIconified;
		
		if ( ! result ) {
			final DockableFrame view = views.get( plot );
			
			if ( view == null ) {
				result = false;
			} else {
				result = view.isMinimized() || view.isHidden();
			}
		}

		return result;
	}

	public void addScriptPane(JPanel scriptPanel) {
		setStatusTwoText("");
		String name = scriptPanel.getName();
		String viewId = replaceInvalidChars(name) + plotCount++;
		DockableFrame view = manager.createDockable(viewId, scriptPanel);
		view.setTitle(name);
		manager.addDockableToGroup(VerdiConstants.PERSPECTIVE_ID, VerdiConstants.MAIN_GROUP_ID, view);
		manager.removeDockableListener(this); //make sure 'this' listener is not added too many times
		manager.addDockableListener(this);
		view.toFront();

		viewList.add(viewId);
		scriptPanels.put(viewId, scriptPanel);
	}

	private void addContourPlot(String name, String id, PlotPanel plotPanel) {
		setStatusTwoText("");
		JFrame dialog = new JFrame(name);
		dialog.setSize(700, 700);
		dialog.setLayout(new BorderLayout());
		dialog.add(plotPanel, BorderLayout.CENTER);
		if (frame != null)
			dialog.setLocationRelativeTo(frame);
		dialog.addWindowListener(new ContourDialogListener(dialog, id, name));
		if (frame == null) {
			framesToDisplay.add(dialog);
		}
		dialog.setVisible(true);
	}

	private class ContourDialogListener extends WindowAdapter {

		JFrame dialog;
		String id, name;

		public ContourDialogListener(JFrame dialog, String id, String name) {
			this.dialog = dialog;
			this.id = id;
			this.name = name;
		}

		@Override
		public void windowClosing(WindowEvent e) {
			manager.getBarManager().removeMenuItem(id);
			dialog.dispose();
		}

		@Override
		public void windowOpened(WindowEvent e) {
			manager.getBarManager().addMenuItem(id,
					GUIConstants.WINDOW_MENU_ID, new AbstractAction(name) {
						/**
						 * 
						 */
						private static final long serialVersionUID = 1367376620748961571L;

						public void actionPerformed(ActionEvent e) {
							if ((dialog.getExtendedState() & JFrame.ICONIFIED) == JFrame.ICONIFIED) {
								dialog.setState(JFrame.NORMAL);
							}
							dialog.toFront();
						}
					});
		}
	}

	private class ProbeExportAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 6007663326836360586L;
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
			try {
				exporter.run();
			} catch (IOException ex) {
				Logger.error("Error while exporting probed data " + ex.getMessage());
			}
		}
	}

	public void addProbe(final JTable table, String name, String rangeAxisName) {
		String viewId = replaceInvalidChars(name) + plotCount++;
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
		bar.add(menu);
		top.add(bar, BorderLayout.NORTH);
		top.add(title, BorderLayout.CENTER);
		panel.add(top, BorderLayout.NORTH);
		
		DockableFrame view = manager.createDockable(viewId, panel);
		view.setTitle(name);
		manager.addDockableToGroup(VerdiConstants.PERSPECTIVE_ID,
				VerdiConstants.MAIN_GROUP_ID, view);
		view.toFront();
	}

	public void setStatusTwoText(String text) {
		// JEB 2016 altered function to compare new and existing strings and do nothing if they are the same
		String currentText = manager.getBarManager().getStatusBarText("verdi.status.two");
		if(currentText.equalsIgnoreCase(text))
			return;
		manager.getBarManager().setStatusBarText("verdi.status.two", text);
		manager.getBarManager().getStatusBar().repaint();
	}

	public void setStatusOneText(String text) {
		// JEB 2016 altered function to compare new and existing strings and do nothing if they are the same
		String currentText = manager.getBarManager().getStatusBarText("verdi.status.one");
		if(currentText.equalsIgnoreCase(text))
			return;
		manager.getBarManager().setStatusBarText("verdi.status.one", text);
		manager.getBarManager().getStatusBar().repaint();
	}

	public JFrame getFrame() {
		return frame;
	}

	public void setFrame(JFrame frame) {
		this.frame = frame;
		frame.addWindowListener( this );
		// SwingUtilities.invokeLater(new Runnable() {
		// public void run() {
		for (JFrame f : framesToDisplay) {
			f.setLocationRelativeTo(VerdiGUI.this.frame);
			f.setVisible(true);
		}
		framesToDisplay.clear();
		// }
		// });
	}

	public DockingManager getViewManager() {
		return manager;
	}

	public void setDualElementPlotsEnabled(boolean enabled) {
		manager.getBarManager().getToolBarComponent(VerdiConstants.SCATTER_BUTTON_ID).setEnabled(enabled);
//		manager.getBarManager().getToolBarComponent(VerdiConstants.VECTOR_BUTTON_ID).setEnabled(enabled);	// 2014 removed old Vector Plot
	}

	public void setVertCrossPlotEnabled(boolean enabled) {
		manager.getBarManager().getToolBarComponent(VerdiConstants.VERT_CROSS_BUTTON_ID).setEnabled(enabled);
	}

	public void setOtherPlotsEnabled(boolean enabled) {
		Logger.debug("in VerdiGUI.setOtherPlotsEnabled");
		GUIBarManager barManager = manager.getBarManager();
		Logger.debug("just instantiated barManager");
		barManager.getToolBarComponent(VerdiConstants.AREAL_INTERPOLATION_BUTTON_ID).setEnabled(enabled);
//		barManager.getToolBarComponent(VerdiConstants.TILE_BUTTON_ID).setEnabled(enabled);
		barManager.getToolBarComponent(VerdiConstants.TIME_SERIES_LINE_BUTTON_ID).setEnabled(enabled);
		barManager.getToolBarComponent(VerdiConstants.TIME_SERIES_BAR_BUTTON_ID).setEnabled(enabled);
		barManager.getToolBarComponent(VerdiConstants.CONTOUR_BUTTON_ID).setEnabled(enabled);
//		Logger.debug("VerdiConstants.GT_TILE_BUTTON_ID = " + VerdiConstants.GT_TILE_BUTTON_ID);
		barManager.getToolBarComponent(VerdiConstants.FAST_TILE_BUTTON_ID).setEnabled(enabled);
	}

	/**
	 * Loads the specified dataset into the dataset GUI.
	 * 
	 * @param dataset
	 *            the dataset to load
	 */
	public void loadDataset(Dataset dataset) {
		datasets.loadDataset(dataset);
	}
	/**
	 * Loads the specified area file into the area file GUI: NOTE: Not yet implemented.
	 *
	 * @param dataset the area file to load
	 */
	public void loadAreaFile(AreaFile dataset) {
		// TODO mab complete this
	//	areaFiles.loadAreaFile(areaFile);
	}

	/**
	 * Displays a message to the user.
	 * 
	 * @param title
	 *            the title of the message
	 * @param message
	 *            the content of the message
	 */
	public void showMessage(String title, String message) {
		JOptionPane.showMessageDialog(frame, message, title,
				JOptionPane.INFORMATION_MESSAGE);
	}
	
	private Cursor oldCursor = null;
	public void showBusyCursor() {
		setStatusOneText("Loading data. This may take awhile; please be patient...");
		if ( getFrame() != null) {
			oldCursor = getFrame().getCursor();
			getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		}
	}
	public void restoreCursor() {
		if ( oldCursor != null && getFrame() != null) {
			getFrame().setCursor(oldCursor);
		} else {
			if (getFrame() != null) {
//				getFrame().setCursor(Cursor.DEFAULT_CURSOR);	// 2014 deprecated
				getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		}
	}
	
	public void busyCursor() {
		if (getFrame() != null) {
			getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		}
	}
	public void defaultCursor() {
		if (getFrame() != null) {
//			getFrame().setCursor(Cursor.DEFAULT_CURSOR);	// 2014 deprecated
			getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	}

	/**
	 * Sets the current formula label in the toolbar to the specified formula.
	 * 
	 * @param formula
	 *            the formula to show in the toolbar
	 */
	public void setCurrentFormula(String formula) {
		JPanel panel = (JPanel) manager.getBarManager()
				.getToolBarComponent(VerdiConstants.FORMULA_LABEL);
		JLabel label = (JLabel) panel.getComponent(2);
		label.setText(formula);
	}

	/**
	 * Displays a confirmation message and returns true if the user clicks yes.
	 * 
	 * @param title
	 *            the title of the message
	 * @param message
	 *            the message itself
	 * @return true if the user clicks yes, otherwise false.
	 */
	public boolean askMessage(String title, String message) {
		return JOptionPane.showConfirmDialog(frame, message + ". Continue?",
				title, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
	}

	/**
	 * Invoked when a view is closed.
	 * 
	 * @param evt
	 */

	public void dockableClosed(DockableFrameEvent evt) { // TODO: 2011-02-25
		DockableFrame view = evt.getDockable();
		if (contourPlots.contains(view.getID())) {
			contourPlots.remove(view.getID());
			Container contourPanel = view.getContentPane();
			JPanel jPanel = (JPanel) contourPanel.getComponent(1);
			DisplayImplJ3D display = (DisplayImplJ3D) jPanel.getClientProperty("J3D_DISPLAY");
			display.destroyUniverse();
		}
		
		//views.remove( plotPanels.get(view.getPersistentId()).getPlot()); // TODO: 2011-02-25
		viewList.remove(view.getID()); // amw 02May07 // TODO: 2011-02-25
		if (plotPanels.get(view.getID()) != null) {
			if ( plotPanels.get(view.getID()).getPlot() != null) {
				views.remove( plotPanels.get(view.getID()).getPlot()); // TODO: 2011-02-25
			}			
			plotPanels.get(view.getID()).viewClosed();
			plotPanels.remove(view.getID());
			@SuppressWarnings("unused")
			PlotPanel panel = plotPanels.get(view.getID());
			panel = null;
		}
		
		manager.getPerspective().removeDockable(view);
		System.gc();
		
		JPanel sPanel = scriptPanels.get(view.getID());
		
		if (sPanel != null && sPanel instanceof ScriptPanel) {
			ScriptPanel sp = (ScriptPanel) sPanel;
			int option = JOptionPane.NO_OPTION;
			
			if (sp.hasChanges())
				option = JOptionPane.showConfirmDialog(frame, "Current file has unsaved changes. Would you like to save them?", "Script File", JOptionPane.YES_NO_OPTION);
			
			if (option == JOptionPane.YES_OPTION)
				sp.saveChanges();
		}
	}

	/**
	 * Returns the list of open plot view ids
	 * 
	 * @return ArrayList<String>
	 */
	public java.util.List<String> getViewList() {
		return viewList;
	}

	public AreaFilePanel getAreaPanel() {
		return areaPanel;
	}

	@Override
	public void dockableClosing(DockableFrameEvent evt) {
		// TODO Auto-generated method stub
	}

	@Override
	public void dockableFloating(DockableFrameEvent evt) {
		// TODO Auto-generated method stub
	}

	@Override
	public void dockableRestoring(DockableFrameEvent evt) {
		// TODO Auto-generated method stub
	}

	@Override
	public void dockableMinimizing(DockableFrameEvent evt) {
		// TODO Auto-generated method stub
	}

	@Override
	public void dockableMinimized(DockableFrameEvent evt) {
		// TODO Auto-generated method stub
	}

	@Override
	public void dockableFloated(DockableFrameEvent evt) {
		// TODO Auto-generated method stub
	}

	@Override
	public void dockableMaximizing(DockableFrameEvent evt) {
		// TODO Auto-generated method stub
	}

	@Override
	public void dockableMaximized(DockableFrameEvent evt) {
		// TODO Auto-generated method stub
	}

	@Override
	public void dockableRestored(DockableFrameEvent evt) {
		// TODO Auto-generated method stub
	}
}
