package anl.verdi.core;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIManager;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages

import saf.core.ui.GUIBarManager;
import saf.core.ui.IAppConfigurator;
import saf.core.ui.ISAFDisplay;
import saf.core.ui.IWindowCustomizer;
import saf.core.ui.SplashScreen;
import saf.core.ui.dock.DockableFrame;
import saf.core.ui.dock.DockingManager;
import anl.verdi.area.AreaFilePanel;
import anl.verdi.gui.DataSetPanel;
import anl.verdi.gui.DatasetListModel;
import anl.verdi.gui.FormulaListElement;
import anl.verdi.gui.FormulaListModel;
import anl.verdi.gui.FormulasPanel;

/**
 * Application configurator for Verdi.
 * <p>
 * <p/> The methods in this interface are called by the SAF application
 * initialization mechanism during points in the applications lifecycle. On
 * application start up, the order in which they are called is:
 * <ol>
 * <li> #preWindowOpen </li>
 * <li> #createLayout </li>
 * <li> #fillBars </li>
 * <li> #postWindowOpen </li>
 * </ol>
 * 
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class VerdiAppConfigurator implements IAppConfigurator {
	static final Logger Logger = LogManager.getLogger(VerdiAppConfigurator.class.getName());

	private VerdiApplication verdiApp;
	private DockingManager viewManager;
	private SplashPanel splashPanel = null;
	private SplashScreen screen = null;
//	private JTabbedPane aTabbedPane = null;		// 2014

	private Action openDatasetAction = new AbstractAction() {
		/**
		 * 
		 */
		private static final long serialVersionUID = -3363450744290864524L;

		public void actionPerformed(ActionEvent e) {
			verdiApp.addDataset();
		}
	};

	/**
	 * Creates a VerdiAppConfigurator.
	 * 
	 * @param verdi
	 *            the main verdi application object
	 */
	public VerdiAppConfigurator(VerdiApplication verdi) {
		Logger.debug("Msg #2: in VerdiAppConfigurator constructor");
		System.setProperty("org.geotools.referencing.forceXY","true");
		verdiApp = verdi;
	}


	/**
	 * Creates the initial layout in the main application window. Typically,
	 * implementors would add the initial application views here, setting up the
	 * initial gui layout.
	 * 
	 * @param viewManager
	 *            the ViewManager used to create the initial layout
	 */
	public void createLayout(DockingManager manager) {
		Logger.debug("Msg #4: in VerdiAppConfigurator.createLayout");
//		aTabbedPane = new JTabbedPane();	// 2014 instantiate tabbed pane to try to put 3 panels in 1 tabbed pane
		
		try {
			this.viewManager = manager;

			DatasetListModel datasetModel = new DatasetListModel();
			FormulaListModel formulaModel = new FormulaListModel();
			formulaModel.addListDataListener(verdiApp);
			Project project = new Project(datasetModel, formulaModel);

			// 2014 moved DataSetPanel section to below other sections
			//			AreaFileListModel areaModel = new AreaFileListModel();
			AreaFilePanel areaPanel = new AreaFilePanel(project, verdiApp.getDomainPanelContext(),verdiApp);

			//		//areaModel.addAreaFileModelListener(verdiApp);
			DockableFrame view3 = viewManager.createDockable(VerdiConstants.AREA_VIEW, new JScrollPane(areaPanel));	// 2014 removed LEFT
			view3.setTitle("Areas");
			viewManager.addDockableToGroup(VerdiConstants.PERSPECTIVE_ID, VerdiConstants.FORMULA_DATASET_GROUP, view3);
//			aTabbedPane.addTab("Areas", (Component) view3);


			FormulasPanel formulasPanel = new FormulasPanel(formulaModel);
			formulasPanel.addFormulaSelectionListener(verdiApp);
			formulasPanel.setFormulaCreator(verdiApp);
			// this is necessary to avoid the horizontal scrollbar
			// showing up before it should
			//formulasPanel.setPreferredSize(new Dimension(100, formulasPanel.getMinimumSize().height));
//			formulasPanel.setMinimumSize(new Dimension(100, formulasPanel.getMinimumSize().height));	// 2014 setPreferredSize == setMinimumSize
//			JScrollPane pane = new JScrollPane(formulasPanel);	//  trying same structure as Datasets (which works)


			DockableFrame view1 = viewManager.createDockable(VerdiConstants.FORMULA_VIEW, new JScrollPane(formulasPanel));	// 2014 removed LEFT
			view1.setTitle("Formulas");
			viewManager.addDockableToGroup(VerdiConstants.PERSPECTIVE_ID, VerdiConstants.FORMULA_DATASET_GROUP, view1);
//			aTabbedPane.addTab("Formulas", (Component) view1);
			
			DataSetPanel datasetPanel = new DataSetPanel(project, verdiApp.getDomainPanelContext());
			datasetPanel.addOpenDatasetAction(openDatasetAction);
			datasetModel.addDatasetModelListener(verdiApp);
			DockableFrame view2 = viewManager.createDockable(VerdiConstants.DATASET_VIEW, new JScrollPane(datasetPanel));	// 2014 removed LEFT
			view2.setTitle("Datasets");
			datasetPanel.addFormulaCallbacks(verdiApp, formulasPanel.getFormulaEditor());

			viewManager.addDockableToGroup(VerdiConstants.PERSPECTIVE_ID, VerdiConstants.FORMULA_DATASET_GROUP, view2);
//			aTabbedPane.insertTab("Datasets", null, (Component) view2,"", 1);


			verdiApp.init(new VerdiGUI(viewManager, datasetPanel, formulasPanel,areaPanel), project);
		} catch (Exception e) {
			closeSplash();
		}
	}

	/**
	 * Optionally adds menu items and actions to the menu and tool bars. This
	 * can be used to programmatically add tool bar and menus / menu items for
	 * those that are not described in an xml plugin file.
	 * 
	 * @param guiBarManager
	 *            the GUIBarManager used to configure tool and menu bars.
	 */
	public void fillBars(GUIBarManager guiBarManager) {
		Logger.debug("Msg #9: in VerdiAppConfigurator.fillBars");
		try {
			JTextField fld = new JTextField();
			Font font = fld.getFont().deriveFont(Font.BOLD);
			guiBarManager.setStatusBarFont("verdi.status.two", font);
			guiBarManager.setStatusBarFont("verdi.status.one", font);
			guiBarManager.addToolBarComponent(VerdiConstants.FORMULA_BAR_GROUP, "", Box.createHorizontalGlue());

			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
			panel.add(new JLabel("Selected Formula:"));
			panel.add(Box.createRigidArea(new Dimension(5, 0)));
			JLabel label = new JLabel("    ");
			label.setForeground(Color.BLUE);
			label.setFont(label.getFont().deriveFont(Font.BOLD));
			panel.add(label);
			guiBarManager.addToolBarComponent(VerdiConstants.FORMULA_BAR_GROUP, VerdiConstants.FORMULA_LABEL, panel);
			guiBarManager.addToolBarComponent(VerdiConstants.FORMULA_BAR_GROUP, "", Box
					.createRigidArea(new Dimension(10, 0)));
		} catch (Exception e) {
			closeSplash();
		}
	}

	/**
	 * Performs some arbitrary clean up type actions immediately prior to
	 * closing the main application window.
	 */
	public void postWindowClose() {
		Logger.debug("in VerdiAppConfigurator.postWindowClose does nothing");
	}

	/**
	 * Performs some arbitrary actions immediately after the main application
	 * window has been open.
	 * 
	 * @param display
	 *            the display representing the main application window.
	 */
	public void postWindowOpen(ISAFDisplay display) {
		Logger.debug("Msg #10: in VerdiAppConfigurator.postWindowOpen");
		closeSplash();
		List<FormulaListElement> formList = verdiApp.getProject().getFormulasAsList();
		boolean enabled = formList.size() > 0 && verdiApp.getProject().getSelectedFormula() != null;
		verdiApp.getGui().setFrame(display.getFrame());
		verdiApp.getGui().setOtherPlotsEnabled(enabled);
		verdiApp.getGui().setVertCrossPlotEnabled(enabled);
		verdiApp.getGui().setDualElementPlotsEnabled(formList.size() > 1);
		verdiApp.getGui().setSaveEnabled(verdiApp.getProject().getDatasetsAsList().size() > 0);
		viewManager.getDockable(VerdiConstants.DATASET_VIEW).toFront();
	}

	public void closeSplash() {
		Logger.debug("Msg #11: in VerdiAppConfigurator.closeSplash");
		if (splashPanel != null)
			splashPanel.stop();
		if (screen != null)
			screen.close();
	}

	/**
	 * Performs some arbitrary clean up type actions immediately prior to
	 * closing the main application window. The calls the pave application to
	 * determine whether the app should exit or not.
	 * 
	 * @return true if the window can continue to close, false to veto the
	 *         window close.
	 */
	public boolean preWindowClose() {
		Logger.debug("in VerdiAppConfigurator.preWindowClose");
		return verdiApp.exit();
	}

	/**
	 * Performs some arbitrary actions prior to the main application window
	 * opening. This can be setting the application's look and feel, using the
	 * customizer parameter to set the initial window's size, title and so on.
	 * 
	 * @param customizer
	 *            the customizer used to customize the initial application
	 *            window
	 * @return true if the application should continue to open, or false to
	 *         close stop application initialization. Note that return false can
	 *         be a normal condition, such as a login failing.
	 */
	public boolean preWindowOpen(IWindowCustomizer customizer) {
		Logger.debug("Msg #3: in VerdiAppConfigurator.preWindowOpen");
		try {
			String lf = UIManager.getSystemLookAndFeelClassName();
			if (lf.toLowerCase().contains("gtk"))
				lf = UIManager.getCrossPlatformLookAndFeelClassName();
			UIManager.setLookAndFeel(lf);
			// UIManager.setLookAndFeel();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		//		customizer.useStoredFrameBounds(800, 800);
		//		customizer.useSavedLayout();
		//		customizer.setTitle("VERDI");

		if (!verdiApp.skipSplash()) {
			customizer.useStoredFrameBounds(800, 800);
			customizer.useSavedLayout();
			customizer.setTitle("VERDI");
			splashPanel = new SplashPanel();
			splashPanel.setPreferredSize(new Dimension(480, 300));
			splashPanel.start();
			screen = new SplashScreen(splashPanel, true);
			screen.setMaxProgress(50000000);
			screen.display();

			// this loop simulates some time consuming
			// setup so we can see the splash screen
			// updating.
			for (int i = 0; i < 50000000; i++) {
				if (i % 200 == 0)
					screen.setProgress(i);
			}
		} else {
			customizer.useStoredFrameBounds(0, 0);
			customizer.useSavedLayout();
			customizer.setTitle("VERDI");
		}
		return true;
	}
}
