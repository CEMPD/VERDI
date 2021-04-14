package anl.verdi.core;

import gov.epa.emvl.Mapper;
import gov.epa.emvl.RemoteFileReader;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Level;
import org.apache.logging.log4j.LogManager;	
import org.apache.logging.log4j.Logger;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.MapContent;
import org.jdesktop.swingx.JXTable;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import saf.core.ui.util.FileChooserUtilities;
import simphony.util.messages.MessageCenter;
import simphony.util.messages.MessageEvent;
import simphony.util.messages.MessageEventListener;
import anl.verdi.area.AreaFileListElement;
import anl.verdi.data.Axes;
import anl.verdi.data.AxisRange;
import anl.verdi.data.DataFrame;
import anl.verdi.data.DataFrameAxis;
import anl.verdi.data.DataFrameIndex;
import anl.verdi.data.DataManager;
import anl.verdi.data.DataUtilities;
import anl.verdi.data.Dataset;
import anl.verdi.data.ObsEvaluator;
import anl.verdi.data.Variable;
import anl.verdi.data.VectorEvaluator;
import anl.verdi.formula.Formula;
import anl.verdi.formula.FormulaFactory;
import anl.verdi.formula.FormulaValidator;
import anl.verdi.formula.IllegalFormulaException;
import anl.verdi.formula.ValidationResult;
import anl.verdi.gui.DatasetListElement;
import anl.verdi.gui.DatasetListModel;
import anl.verdi.gui.DatasetModelListener;
import anl.verdi.gui.FormulaElementCreator;
import anl.verdi.gui.FormulaListElement;
import anl.verdi.gui.FormulaListModel;
import anl.verdi.gui.HelpWindowWithContents;
import anl.verdi.gui.ScriptPanel;
import anl.verdi.io.IO;
import anl.verdi.plot.anim.MultPlotAnimationDialog;
import anl.verdi.plot.config.TilePlotConfiguration;
import anl.verdi.plot.gui.AreaSelectionEvent;
import anl.verdi.plot.gui.FastObsOverlayDialog;
import anl.verdi.plot.gui.FastTilePlot;
import anl.verdi.plot.gui.ObsOverlayDialog;
import anl.verdi.plot.gui.OverlayRequest;
import anl.verdi.plot.gui.Plot;
import anl.verdi.plot.gui.PlotListener;
import anl.verdi.plot.gui.PlotRequest;
import anl.verdi.plot.gui.VectorOverlayDialog;
import anl.verdi.plot.probe.ProbeCreator;
import anl.verdi.plot.probe.ProbeCreatorFactory;
import anl.verdi.plot.probe.ProbeEvent;
import anl.verdi.plot.types.TilePlot;
import anl.verdi.util.DateRange;
import anl.verdi.util.ObsTimeChecker;
import anl.verdi.util.Tools;
import anl.verdi.util.VectorOverlayTimeChecker;

/**
 * Main verdi application facade.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class VerdiApplication implements PlotListener, DatasetModelListener, ListSelectionListener,
FormulaElementCreator, ListDataListener {
	static final Logger Logger = LogManager.getLogger(VerdiApplication.class.getName());

//	private static MessageCenter msg = MessageCenter.getMessageCenter(VerdiApplication.class);

	private DataManager manager;

	private VerdiGUI gui;

	private File currentFile = null;

	private File currentDatasetFile = new File("../..");

	private File currentScriptFile = null;

//	private File currentAreaFile = new File("../..");

	private boolean showSplash = false;

	private Project project;

	private MapContent domainPanelContext = new MapContent();
//	MapViewport viewport = new MapViewport();
//	viewport.setCoordinateReferenceSystem((CoordinateReferenceSystem)DefaultGeographicCRS.WGS84);

	private static VerdiApplication singleton = null;
	
	private boolean guiMode = true;

	public boolean skipSplash() {
		return showSplash;
	}

	public void setSkipSplash(boolean skipSplash) {
		showSplash = skipSplash;
	}

	public VerdiApplication(DataManager manager) {
		this.manager = manager;
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		singleton = this;
		Logger.debug("Msg #1: in VerdiApplication DataManager");
	}

	public static VerdiApplication getInstance() {
		Logger.debug("Msg #6: in VerdiApplication.getInstance()");
		return singleton;
	}

	public VerdiGUI getGui() {
		return gui;
	}
	
	//TODO - write me
	//TAH
	Mapper mapper = null;
	public Mapper getLastMapper() {
		return mapper;
	}	
	public void setLastMapper(Mapper mapper) {
		this.mapper = mapper;
	}
	
	double[][] domain = null;
	public double[][] getLastDomain() {
		return domain;
	}	
	public void setLastDomain(double[][] domain) {
		this.domain = domain;
	}
	
	ReferencedEnvelope bounds = null;
	public ReferencedEnvelope getLastBounds() {
		return bounds;
	}
	public void setLastBounds(ReferencedEnvelope bounds) {
		this.bounds = bounds;
	}
	
	CoordinateReferenceSystem crs = null;
	public CoordinateReferenceSystem getLastCoordinateReferenceSystem() {
		return crs;
	}
	public void setLastCoordinateReferenceSystem(CoordinateReferenceSystem crs) {
		this.crs = crs;
	}
	
	public void setProject(Project project) {
		this.project = project;
	}
	
	public void setGuiAvailable(boolean gui) {
		guiMode = gui;
	}
	
	public boolean getGuiAvailable() {
		return guiMode;
	}

	public void init(VerdiGUI gui, Project project) {
		Logger.debug("Msg #8: in VerdiApplication.init");
		this.gui = gui;
		if (project != null)
			this.project = project;
		MessageCenter.addMessageListener(new MessageEventListener() {
			public void messageReceived(MessageEvent messageEvent) {
				if (messageEvent != null) {
					Logger.debug("Msg: " + messageEvent.getMessage());
					if (messageEvent.getThrowable()!=null) {
						Logger.error("Message throwable",  messageEvent.getThrowable());
					}
				}
				if (messageEvent != null && messageEvent.getLevel().equals(Level.ERROR) &&
						VerdiApplication.this != null && VerdiApplication.this.gui != null) {
					VerdiGUI gui = VerdiApplication.this.gui;
					String msg = "" + messageEvent.getMessage();
					if (messageEvent.getThrowable()!=null) {
						msg += ": "
								+ messageEvent.getThrowable().getMessage();
					}
					if ( gui.getFrame() != null && gui.getFrame().isVisible()) {
						gui.showMessage("Error", msg);
					}
				}
			}
		});

		try {
			String datasetHome = System.getProperty(Tools.DATASET_HOME);
			String projectHome = System.getProperty(Tools.PROJECT_HOME);
			String scriptHome = System.getProperty(Tools.SCRIPT_HOME);

			if (datasetHome != null && !datasetHome.isEmpty())
				currentDatasetFile = new File(datasetHome);

			if (projectHome != null && !projectHome.isEmpty())
				currentFile = new File(projectHome);

			if (scriptHome != null && !scriptHome.isEmpty())
				currentScriptFile = new File(scriptHome);
			//}

		} catch (Exception e) {
			Logger.warn("Error while getting configuration properties", e);
		}
	}

	/**
	 * Gets the currently loaded Project.
	 *
	 * @return the currently loaded Project.
	 */
	public Project getProject() {
		Logger.debug("Msg #12 & #13: in VerdiApplication.getProject");
		return project;
	}

	/**
	 * Gets the DataManager.
	 *
	 * @return the DataManager.
	 */
	public DataManager getDataManager() {
		Logger.debug("in VerdiApplication.getDataManager");
		return manager;
	}

	/**
	 * Exits the application.
	 *
	 * @return true if the exit was successful and the application should be
	 *         terminated, otherwise false.
	 */
	public boolean exit() {
		Logger.debug("in VerdiApplication.exit");
		if (RemoteFileReader.TEMP_REMOTE_FILE_LIST.size()>0) {
			int res = JOptionPane.showConfirmDialog(gui.getFrame(), "Do you want to delete the temporary files downloaded remotely?",
					"Delete Temp Files Warning", JOptionPane.YES_NO_OPTION);
			if (res == JOptionPane.YES_OPTION) {
				for (String filePath : RemoteFileReader.TEMP_REMOTE_FILE_LIST) {
					File f = new File(filePath);
					if (f.exists()) {
						boolean ok = f.delete();
						if (!ok) {
							f.deleteOnExit();
						}
					}
				}
			}
		}
		
		try {
			manager.closeAllDatasets();			
		} catch (IOException e) {
			Logger.error("Error closing datasets", e);
		}
		return true;
	}

	/**
	 * Opens a saved project -- a set of datasets and formulas. This opens a file dialog.
	 */
	public void openProject() {
		Logger.debug("in VerdiApplication.openProject");
		boolean abort = false;
		if (manager.getDatasetCount() > 0) {
			int res = JOptionPane.showConfirmDialog(gui.getFrame(), "All currently loaded datasets will be unloaded. Continue?",
					"Open Project Warning", JOptionPane.YES_NO_OPTION);
			abort = res == JOptionPane.NO_OPTION;
		}

		if (!abort) {
			File defaultFile = new File("../..");
			if (currentFile != null)
				defaultFile = currentFile;
			File file = FileChooserUtilities.getOpenFile(defaultFile);
			openProject(file);
		}
	}

	/**
	 * Opens the specified file as a project.
	 *
	 * @param file the project file
	 */
	public void openProject(File file) {
		Logger.debug("in VerdiApplication.openProject for a File");
		if (file != null) {
			IO io = new IO();
			try {
				io.load(file, project, manager, this);
				currentFile = file;
			} catch (IOException e) {
				Logger.error("Error while loading project", e);
			}
		}
	}

	/**
	 * Saves the current project -- the current set of datasets and formulas.
	 */
	public void saveProject() {
		Logger.debug("in VerdiApplication.saveProject");
		if (currentFile == null || currentFile.isDirectory()) {
			saveProjectAs();
		} else if (currentFile.isFile()){
			doSave(currentFile);
		}
	}

	private void doSave(File file) {
		Logger.debug("in VerdiApplication.doSave");
		if (file != null) {
			IO io = new IO();
			try {
				io.save(file, project);
				currentFile = file;
			} catch (IOException e) {
				Logger.error("Error while saving project", e);
			}
		}
	}

	/**
	 * Saves the current project as -- the current set of datasets and formulas .
	 */
	public void saveProjectAs() {
		Logger.debug("in VerdiApplication.saveProjectAs");
		File file = FileChooserUtilities.getSaveFile(currentFile);
		if (file != null) {
			doSave(file);
		}
	}

	/**
	 * Choose the batch script file to run verdi and save the images.
	 */
	public void runBatchScript() {
		File file = FileChooserUtilities.getOpenFile(currentScriptFile);

		if (file != null) {
			currentScriptFile = file;
			ScriptPanel sPanel = new ScriptPanel(currentScriptFile);
			sPanel.observe(this);
			gui.addScriptPane(sPanel);
		}
	}

	public void setCurrentScriptFile(File file) {
		this.currentScriptFile = file;
	}

	/**
	 * Evaluates the current formula and returns the result. If there is no
	 * current formula then returns null. Then will evaluate the formula against
	 * the date range calculated by resolving the various date constraints
	 * and trying to find the largest legal overlapping segment.
	 *
	 * @param type the type of formula (e.g. Tile, TimeSeries etc.) to evaluate
	 * @return the result of the evaluation or null if the evaluation fails.
	 */
	public DataFrame evaluateFormula(Formula.Type type) {
		return evaluateFormula(type, null);
	}

	/**
	 * Evaluates the current formula and returns the result. If there is no
	 * current formula then returns null. The evaluation will ignore the
	 * the date range calculated by resolving the various date constraints
	 * and trying to find the largest legal overlapping segment and use
	 * the passed-in parameter instead.
	 *
	 * @param type      the type of formula (e.g. Tile, TimeSeries etc.) to evaluate
	 * @param dateRange the dateRange to evaluate the formula against
	 * @return the result of the evaluation or null if the evaluation fails
	 */
	public DataFrame evaluateFormula(Formula.Type type, DateRange dateRange) {
		Logger.debug("in VerdiApplication.evaluateFormula 1");
		DataFrame frame = null;
		try {
			if (project == null) 
				throw new IllegalFormulaException("Project is not properly initialized.");

			FormulaListElement listElement = project.getSelectedFormula();

			if (listElement == null)
				throw new IllegalFormulaException("Could not get selected formula.");

			if (listElement != null && listElement.getDataset() != null && listElement.getDataset().isObs())
				throw new IllegalFormulaException("Selected dataset is observational.");

			String strFormula = listElement.getFormula();
			Formula formula = new FormulaFactory().createFormula(type, strFormula, null);
			List<AxisRange> ranges = project.createRanges(listElement);
			ValidationResult result = formula.validate(manager, ranges);
			ValidationResult.Status status = result.getStatus();
			boolean success = true;
			if (status == ValidationResult.Status.FAIL) {
				Exception ex = result.getException();
				if (ex == null)
					ex = new RuntimeException(result.getMessage());
				Logger.error("Formula '" + strFormula + "' is invalid ", ex);
				success = false;
			} else if (status == ValidationResult.Status.WARN) {
				Logger.warn(result.getMessage());
				success = gui.askMessage("Warning", "Warning: " + result.getMessage());
			}

			if (success) {
				boolean convertUnits = result.getProperty(FormulaValidator.UNITS_WARN) == null;
				if (dateRange != null) formula.overrideTimeRange(dateRange);
				frame = formula.evaluate(manager, ranges, convertUnits);
			}

		} catch (Throwable e) {
			Logger.error("Error while evaluating formula", e);
			String errInfo = e.getClass().getName();
			if (e.getMessage() != null && !e.getMessage().equals(""))
				errInfo += ": " + e.getMessage();
			JOptionPane.showMessageDialog(getGui().getFrame(), "An error occured while evaluating the formula:\n" + errInfo + "\nPlease see the log for more details.", "Error", JOptionPane.ERROR_MESSAGE);			
		}

		return frame;
	}
	/**
	 * Evaluates the current formula and returns the result. If there is no
	 * current formula then returns null. The evaluation will ignore the
	 * the date range calculated by resolving the various date constraints
	 * and trying to find the largest legal overlapping segment and use
	 * the passed in parameter instead.
	 *
	 * @param type      the type of formula (e.g. Tile, TimeSeries etc.) to evaluate
	 * @param dateRange the dateRange to evaluate the formula against
	 * @return the result of the evaluation or null if the evaluation fails.
	 */
	public DataFrame evaluateFormula(String strFormula,Formula.Type type, DateRange dateRange) {
		Logger.debug("in VerdiApplication.evaluateFormula 2");
		DataFrame frame = null;
		try {
			if (project == null) 
				throw new IllegalFormulaException("Project is not properly initialized.");


			Formula formula = new FormulaFactory().createFormula(type, strFormula, null);
			FormulaListElement formulaElement=project.getFormulaFor(strFormula);
			List<AxisRange> ranges = project.createRanges(formulaElement);
			ValidationResult result = formula.validate(manager, ranges);
			ValidationResult.Status status = result.getStatus();
			boolean success = true;
			if (status == ValidationResult.Status.FAIL) {
				Exception ex = result.getException();
				if (ex == null)
					ex = new RuntimeException(result.getMessage());
				Logger.error("Formula '" + strFormula + "' is invalid", ex);
				success = false;
			} else if (status == ValidationResult.Status.WARN) {
				Logger.warn(result.getMessage());
				success = gui.askMessage("Warning", "Warning: " + result.getMessage());
			}

			if (success) {
				boolean convertUnits = result.getProperty(FormulaValidator.UNITS_WARN) == null;
				if (dateRange != null) formula.overrideTimeRange(dateRange);
				frame = formula.evaluate(manager, ranges, convertUnits);
			}

		} catch (IllegalFormulaException e) {
			Logger.error("Error while evaluating formula", e);
		}
		return frame;
	}

	/**
	 * Called whenever an area in a plot is being selected.
	 *
	 * @param event contains the details of the area selection
	 */
	public void areaSelected(AreaSelectionEvent event) {
		if (event.isFinished()) {
			gui.setStatusTwoText("");
		} else {
			gui.setStatusTwoText(event.areaToString());
		}
	}

	/**
	 * Notifies this PlotListener when a plot has been probed.
	 */
	public void plotProbed(ProbeEvent event) {
		Logger.debug("in VerdiApplication.plotProbed");
		DataFrame frame = event.getProbedData();
		if (frame.getSize() == 1) {
			probePoint(event.getSource(), frame, event.getIsLog(), event.getLogBase()); // TODO: JIZHEN
		} else {
			ProbeCreator creator = ProbeCreatorFactory.createProbeCreator(event);
			JXTable table = new JXTable(creator.createTableModel());
			table.setColumnControlVisible(true);
			table.setHorizontalScrollEnabled(true);
			table.setRolloverEnabled(true);
			gui.addProbe(table, creator.getName(), creator.getRangeAxisName());
		}
	}

	private void probePoint(Plot plot, DataFrame frame, boolean isLog, double logBase) {
		Logger.debug("in VerdiApplication.probePoint");
		// show in status bar
		Axes<DataFrameAxis> axes = frame.getAxes();
		DataFrameIndex index = frame.getIndex();
		int i = 0;
		int[] point = new int[axes.getAxes().size()];

		if (axes.getTimeAxis() != null) {
			point[i++] = axes.getTimeAxis().getOrigin();
		}

		if (axes.getZAxis() != null) {
			point[i++] = axes.getZAxis().getOrigin();
		}

		if (axes.getXAxis() != null) {
			point[i++] = axes.getXAxis().getOrigin();
		}

		if (axes.getYAxis() != null) {
			point[i++] = axes.getYAxis().getOrigin();
		}

		if (plot.getType() == Formula.Type.TILE) {
			int[] p = point;
			if (point.length == 3) {
				// must be no layer, but we want to add that with the NO_VALUE marker
				p = new int[4];
				p[0] = point[0];
				p[1] = TilePlot.NO_VAL;
				p[2] = point[1];
				p[3] = point[2];
			}

			String status = "";

			if (plot instanceof FastTilePlot)
				status = ((FastTilePlot)plot).createAreaString(p);
			else
				status = ((TilePlot)plot).createAreaString(p);

			NumberFormat format = null;

			try {
				format = ((TilePlotConfiguration) plot.getPlotConfiguration()).getColorMap().getNumberFormat();
			} catch (Exception e) {
				Logger.error("Error getting tile plot color map number format", e);
			}

			StringBuffer buf = new StringBuffer(status);
			buf.append(": ");

			if ( !isLog){
				double doubleValue = frame.getDouble(index) > DataUtilities.BADVAL3 ? frame.getDouble(index) : DataUtilities.BADVAL3;
				//buf.append((format != null) ? format.format(frame.getDouble(index)) : frame.getDouble(index));
				buf.append((format != null) ? format.format(doubleValue) : doubleValue);
			} else {
				double logvalue = Math.log(frame.getDouble(index))/Math.log(logBase);
				logvalue = (logvalue >  DataUtilities.BADVAL3 && logvalue < DataUtilities.NC_FILL_FLOAT) ? logvalue : DataUtilities.BADVAL3;
				buf.append((format != null) ? format.format(logvalue) : logvalue);
			}
			gui.setStatusOneText(buf.toString());
		} else {
			StringBuilder buf = new StringBuilder("(");
			for (int j = 0; j < point.length; j++) {
				if (j > 0)
					buf.append(", ");
				buf.append(point[j]);
			}
			buf.append("): ");
			if ( !isLog){
				double doubleValue = frame.getDouble(index) > DataUtilities.BADVAL3 ? frame.getDouble(index) : DataUtilities.BADVAL3;
				buf.append(doubleValue); //frame.getDouble(index));
			} else {
				double logvalue = Math.log(frame.getDouble(index))/Math.log(logBase);
				logvalue = (logvalue >  DataUtilities.BADVAL3) ? logvalue : DataUtilities.BADVAL3;
				buf.append(logvalue);
			}
			gui.setStatusOneText(buf.toString());
		}
	}

	/**
	 * Loads the specified datasets.
	 *
	 * @param files the datasets to load
	 * @throws IllegalFormulaException 
	 */
	public void loadDataset(File[] files) throws Exception {
		Logger.debug("in VerdiApplication.loadDataset");
		if (project == null) 
			throw new Exception("Project is not properly initialized.");

		Set<URL> urls = new HashSet<URL>();
		for (DatasetListElement set : project.getDatasetsAsList()) {
			urls.add(set.getDataset().getURL());
		}
		for (File file : files) {
			try {
				URL url = file.toURI().toURL();
				if (urls.contains(url)) {
					gui.showMessage("Dataset Loading Error", "'" + file.getAbsolutePath() +
					"' is already loaded");
				} else {
					List<Dataset> datasets = DataManager.NULL_DATASETS;
					try {
						datasets = manager.createDatasets(url); // JIZHEN-JIZHEN
					} catch (Exception e) {
						gui.showMessage("Dataset Loading Error", "No dataset handler registered for '"
								+ file.getAbsolutePath() + "':\n " + e.getMessage());
						Logger.error("Dataset Loading Error", e);
					}
					if (datasets.equals(DataManager.NULL_DATASETS)) {
						//gui.showMessage("Dataset Loading Error", "No dataset handler registered for '"
						//				+ file.getAbsolutePath() + "'");
					} else {
						for (Dataset dataset : datasets) {
							gui.loadDataset(dataset);
						}
					}
				}
			} catch (MalformedURLException e) {
				Logger.error("Error loading dataset", e);
			}
		}
	}
	
	/**
	 * Loads the specified area files.
	 *
	 * @param files the area files to load
	 */
	public void loadAreaFile(File[] files) {
		Logger.debug("in VerdiApplication.loadAreaFile");
		Set<URL> urls = new HashSet<URL>();
		for (AreaFileListElement set : project.getAreaFilesAsList()) {
			urls.add(set.getAreaFile().getURL());
		}
		for (File file : files) {
			try {
				URL url = file.toURI().toURL();
				if (urls.contains(url)) {
					gui.showMessage("Area File Loading Error", "'" + file.getAbsolutePath() +
					"' is already loaded");
				} else {
					// todo mab complete this
					/*
					List<AreaFile> areaFiles = AreaFile.createAreaFiles(url);

					for (AreaFile areaFile : areaFiles) {
						gui.loadAreaFile(areaFile);
					}
					 */
				}
			} catch (MalformedURLException e) {
				Logger.error("Error loading area file", e);
			}
		}
	}
	/**
	 * Opens a dataset.
	 */
	public void addDataset() {
		Logger.debug("in VerdiApplication.addDataset");
		File[] files = FileChooserUtilities.getOpenFiles(currentDatasetFile);
		if (files.length > 0) {
			try {
				getGui().showBusyCursor();
				loadDataset(files);
			} catch (Exception e) {
				Logger.error("Error adding dataset", e);
			}
			getGui().restoreCursor();
			currentDatasetFile = files[0];
		}
	}
//	/**			// 2014 appears to never be used
//	 * Opens area files.
//	 */
//	public void addAreaFile() {
//		Logger.debug("in VerdiApplication.addAreaFile");
//		File[] files = FileChooserUtilities.getOpenFiles(currentAreaFile);
//		if (files.length > 0) {
//			loadAreaFile(files);
//			currentAreaFile = files[0];
//		}
//	}
	/**
	 * Called whenever a formula is selected.
	 *
	 * @param e the event that characterizes the change.
	 */
	public void valueChanged(ListSelectionEvent e) {
		Logger.debug("in VerdiApplication.valueChanged");
		if (!e.getValueIsAdjusting()) {
			FormulaListElement selectedFormula = (FormulaListElement) ((JList) e.getSource()).getSelectedValue();
			project.setSelectedFormula(selectedFormula);
			if (guiMode) {
				if (selectedFormula == null) {
					gui.setCurrentFormula("    ");
					gui.setOtherPlotsEnabled(false);
					gui.setVertCrossPlotEnabled(false);
				} else {
					gui.setCurrentFormula(selectedFormula.getFormula());
					gui.setOtherPlotsEnabled(true);
					boolean enabled = selectedFormula.getAxes().getZAxis() != null;
					gui.setVertCrossPlotEnabled(enabled);
				}
			}
		}
	}

	/**
	 * Called when the specified dataset has been removed. This closes the
	 * dataset.
	 *
	 * @param set   the removed dataset
	 * @param model
	 */
	public void datasetRemoved(Dataset set, DatasetListModel model) {
		Logger.debug("in VerdiApplication.datasetRemoved");
		gui.setSaveEnabled(model.getSize() > 0);
		try {
			manager.closeDataset(set.getAlias());
		} catch (IOException e) {
			Logger.error("Error while closing dataset '" + set.getName() + "'", e);
		}
	}

	/**
	 * Called when the specified dataset has been added.
	 *
	 * @param set   the added dataset
	 * @param model the DatasetListModel from which the set was removed
	 */
	public void datasetAdded(Dataset set, DatasetListModel model) {
		Logger.debug("in VerdiApplication.datasetAdded");
		if (guiMode)
			gui.setSaveEnabled(true);
	}

	/**
	 * Validates the specified formula and displays the appropriate error
	 * messages if necessary.
	 *
	 * @param strFormula the formula to evaluate
	 * @return true if the validation was a success otherwise false.
	 */
	public FormulaListElement create(String strFormula) {
		Logger.debug("in VerdiApplication create");
		// before do this, should validate the formula
		// and if it fails then error message.
		FormulaFactory factory = new FormulaFactory();
		Formula formula = factory.createTileFormula(strFormula, null);
		// don't do any range checking here - that can wait until later
		// when we have an element created
		ValidationResult result = formula.validate(manager, new ArrayList<AxisRange>());
		ValidationResult.Status status = result.getStatus();
		boolean ok = true;
		if (status == ValidationResult.Status.FAIL) {
			Exception ex = result.getException();
			if (ex == null)
				ex = new RuntimeException(result.getMessage());
			Logger.error("Formula '" + strFormula + "' is invalid", ex);
			ok = false;
		} else if (status == ValidationResult.Status.WARN) {
			Logger.warn(result.getMessage());
			ok = gui.askMessage("Warning", "Warning: " + result.getMessage());
		}

		if (ok) {
			return new FormulaListElement(strFormula, result.getVariables());
		} else
			return null;
	}


	/**
	 * Notifies this PlotListener of a request to create
	 * a plot fired from a another plot.
	 *
	 * @param request the request
	 */
	public void plotRequested(PlotRequest request) {
		Logger.debug("in VerdiApplication.plotRequested");
		request.init(this);
		request.createPlot();
	}

	/**
	 * Notifies the PlotListener of a request to create an overlay for the sending plot.
	 * The listener should add the appropriate overlay evaluator to the request
	 *
	 * @param request the overlay request
	 */
	public void overlayRequested(OverlayRequest request) {
		Logger.debug("in VerdiApplication.overlayRequested");
			// 2015 called from FastTilePlot drop-down menu to select obs/vectors
		if (request.getType() == OverlayRequest.Type.OBS) {
			Logger.debug("in VerdiApplication. OverlayRequest.Type.OBS");
			List<Variable> vars = new ArrayList<Variable>();
			for (DatasetListElement element : project.getDatasetsAsList()) {
				if (element.getDataset().isObs()) {
					vars.addAll(element.getDataset().getVariables());
				}
			}

			TilePlot plot = request.getPlot();
			FastTilePlot fastPlot = request.getFastTilePlot();

			ObsOverlayDialog dialog = null;
			DataFrame data = null;

			if (plot != null) {
				dialog = new ObsOverlayDialog(gui.getFrame());
				data = plot.getData().get(0);
			}

			if (fastPlot != null) {
				dialog = new FastObsOverlayDialog(gui.getFrame());
				((FastObsOverlayDialog)dialog).setObservationData(fastPlot.getObservationData());
				data = fastPlot.getData().get(0);
			}

			dialog.init(vars, new ObsTimeChecker(project.getDatasetsAsList(), data));
			dialog.setVisible(true);
			dialog.pack();

			try {
				if (!dialog.isCanceled()) {
					if (plot != null) {
						ObsEvaluator eval = new ObsEvaluator(manager, dialog.getSelectedVar());
						plot.addObsAnnotation(eval, dialog.getShapeSize(), dialog.getStrokeSize(), dialog.getSymbol());
					}

					if (fastPlot != null)
						fastPlot.addObservationData(manager, dialog.showLegend());
				}
			} catch (Exception e) {
				JOptionPane.showMessageDialog(gui.getFrame(), "Please check if the overlay time steps match the underlying data.", "Overlay Error", JOptionPane.ERROR_MESSAGE, null);
				Logger.error("Overlay Error", e);
			}
		} else if (request.getType() == OverlayRequest.Type.VECTOR) {
			Logger.debug("in VerdiApplication. OverlayRequest.Type.VECTOR");
			FastTilePlot fastPlot = request.getFastTilePlot();
			VectorOverlayDialog dialog = new VectorOverlayDialog(gui.getFrame());
			Logger.debug("instantiated VectorOverlayDialog");

			if (fastPlot != null) {
				dialog.init(project.getFormulasAsList(), new VectorOverlayTimeChecker(project.getDatasetsAsList(),
						fastPlot.getData().get(0)));
				Logger.debug("did dialog.init for fastPlot != null");
			} else {
				dialog.init(project.getFormulasAsList(), new VectorOverlayTimeChecker(project.getDatasetsAsList(),
						request.getPlot().getData().get(0)));
				Logger.debug("did dialog.init for fastPlot == null");
			}
			dialog.pack();
			dialog.setVisible(true);
			Logger.debug("dialog is now visible");

			FormulaListElement xElement = dialog.getUElement();
			
			if (xElement != null) {
				FormulaListElement yElement = dialog.getVElement();
				int vectorSamplingInc = dialog.getVectorSamplingInc();	// 2015 get input Vector Sampling Increment
				addVectorOverlay(xElement, yElement, vectorSamplingInc, fastPlot, request);
			}
		}
	}
	
	public void addVectorOverlay(FormulaListElement xElement, FormulaListElement yElement, int vectorSamplingInc, FastTilePlot fastPlot, OverlayRequest request) {
		Logger.debug("xElement != null");
		FormulaListElement oldElement = project.getSelectedFormula();
		project.setSelectedFormula(xElement);
		DataFrame xFrame = evaluateFormula(Formula.Type.VECTOR); //, range);	// domain
		project.setSelectedFormula(yElement);
		DataFrame yFrame = evaluateFormula(Formula.Type.VECTOR); //, range);	// range
		Logger.debug("got vectorSamplingIncrement = " + vectorSamplingInc);
		
		DataFrame[] frames = DataUtilities.unitVectorTransform(xFrame, yFrame, vectorSamplingInc);	// 2015 pass in vector sampling increment
		Logger.debug("back from DataUtilities.unitVectorTransform");
		xFrame = frames[0];
		yFrame = frames[1];
		project.setSelectedFormula(yElement);
		Logger.debug("back from project.setSelectedFormula");

		if (fastPlot != null) {
			fastPlot.addVectorAnnotation(new VectorEvaluator(xFrame, yFrame));
			Logger.debug("did addVectorAnnotation for new VectorEvaluator (fastPlot)");
		} else{
			request.getPlot().addVectorAnnotation(new VectorEvaluator(xFrame, yFrame));
			Logger.debug("did addVectorAnnotation for new VectorEvaluator (NOT fastPlot)");
		}

		project.setSelectedFormula(oldElement);

	}

	public MapContent getDomainPanelContext() {
		Logger.debug("Logger #5 & #7: in VerdiApplication.getDomainPanelContext");
		return domainPanelContext;
	}

	public void setDomainPanelContext(MapContent domainPanelContext) {
		Logger.debug("in VerdiApplication setDomainPlanelContext");
		this.domainPanelContext = domainPanelContext;
	}

	// listeners for the formula model
	public void contentsChanged(ListDataEvent e) {
		Logger.debug("in VerdiApplication.contentsChanged (empty function)");
	}

	public void intervalAdded(ListDataEvent e) {
		Logger.debug("in VerdiApplication intervalAdded");
		FormulaListModel model = (FormulaListModel) e.getSource();
		if (guiMode)
			gui.setDualElementPlotsEnabled(model.getSize() > 0);
	}

	public void intervalRemoved(ListDataEvent e) {
		Logger.debug("in VerdiApplication IntervalRemoved");
		FormulaListModel model = (FormulaListModel) e.getSource();
		gui.setDualElementPlotsEnabled(model.getSize() > 0);
	}

	public void animateTilePlots() {
		Logger.debug("in VerdiApplication animateTilePlots");
		MultPlotAnimationDialog dialog = new MultPlotAnimationDialog(gui.getFrame());
		dialog.init(gui.getDisplayedPlots(Formula.Type.TILE));
		dialog.pack();
		dialog.setLocationRelativeTo(gui.getFrame());
		dialog.setVisible(true);
	}

	/**
	 * Shows help.
	 */
	public void showHelp() {
		Logger.debug("in VerdiApplication showHelp");
		String helpDir = System.getProperty("user.dir");
		Logger.debug("helpdir = user.dir == " + helpDir);
		if (helpDir == null)
			return;
		helpDir = helpDir + "/help";

		String helpName1 = helpDir + "/verdiUserManualIndex.htm";
		String helpName2 = helpDir + "/verdiUserManual.htm";
		HelpWindowWithContents.showContents(null, "VERDI Help", helpName1, helpName2);

		Logger.warn("help: " + helpName1);
		Logger.warn("help: " + helpName2);
	}
}
