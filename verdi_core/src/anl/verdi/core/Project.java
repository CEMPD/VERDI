package anl.verdi.core;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages

import anl.verdi.area.AreaFileListElement;
import anl.verdi.area.AreaFileListModel;
import anl.verdi.data.Axes;
import anl.verdi.data.AxisRange;
import anl.verdi.data.AxisType;
import anl.verdi.data.CoordAxis;
import anl.verdi.data.Dataset;
import anl.verdi.data.Variable;
import anl.verdi.formula.FormulaVariable;
import anl.verdi.formula.IllegalFormulaException;
import anl.verdi.gui.DatasetListElement;
import anl.verdi.gui.DatasetListModel;
import anl.verdi.gui.FormulaElementCreator;
import anl.verdi.gui.FormulaListElement;
import anl.verdi.gui.FormulaListModel;
import anl.verdi.util.DateRange;

/**
 * Encapsulates the current dataset and formula.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class Project {
	static final Logger Logger = LogManager.getLogger(Project.class.getName());

	private DatasetListModel datasets;
	private FormulaListModel formulas;
	private AreaFileListModel areaFiles;
	private FormulaListElement selectedFormula;
	public Project(DatasetListModel datasets, FormulaListModel formulas, AreaFileListModel areaFiles) {
		Logger.debug("in Project constructor (DatasetListModel, FormulaListModel, AreaFileListModel)");
		this.datasets = datasets;
		this.formulas = formulas;
		this.areaFiles=areaFiles;
	}
	public Project(DatasetListModel datasets, FormulaListModel formulas) {
		Logger.debug("in Project constructor (DatasetListModel, FormulaListModel");
		this.datasets = datasets;
		this.formulas = formulas;
		this.areaFiles=new AreaFileListModel();
	}

	public FormulaListElement getSelectedFormula() {
		Logger.debug("in Project getSelectedFormula");
		return selectedFormula;
	}

	public void setSelectedFormula(FormulaListElement selectedFormula) {
		Logger.debug("in Project setSelectedFormula");
		this.selectedFormula = selectedFormula;
		formulas.setSelectedItem(selectedFormula);
	}

	public DatasetListModel getDatasets() {
		Logger.debug("in Project getDatasets");
		return datasets;
	}

	public FormulaListModel getFormulas() {
		Logger.debug("in Project getFormulas");
		return formulas;
	}
	
	public AreaFileListModel getAreaFiles() {
		Logger.debug("in Project getAreaFiles");
		return areaFiles;
	}
	
	public List<DatasetListElement> getDatasetsAsList() {
		Logger.debug("in Project getDatasetAsList");
		List<DatasetListElement> list = new ArrayList<DatasetListElement>();
		for (DatasetListElement item : datasets.elements()) {
			list.add(item);
		}

		return list;
	}

	/**
	 * Gets the formula list elements as a list.
	 *
	 * @return the formula list elements as a list.
	 */
	public List<FormulaListElement> getFormulasAsList() {
		Logger.debug("in Project getFormulasAsList");
		List<FormulaListElement> list = new ArrayList<FormulaListElement>();
		for (FormulaListElement item : formulas.elements()) {
			list.add(item);
		}

		return list;
	}
	/**
	 * Gets the formula list elements for a particular formula string.
	 *
	 * @return the formula list elements as a list.
	 */
	public FormulaListElement getFormulaFor(String formulaString) {
		Logger.debug("in Project getFormulaFor");
//		List<FormulaListElement> list = new ArrayList<FormulaListElement>();
		for (FormulaListElement item : formulas.elements()) {
			if(item.getFormula().equals(formulaString))return item;
		}

		return null;
	}
	/**
	 * Gets the area file list elements as a list.
	 *
	 * @return the area file list elements as a list.
	 */
	public List<AreaFileListElement> getAreaFilesAsList() {
		Logger.debug("in Project getAreaFilesAsList");
		List<AreaFileListElement> list = new ArrayList<AreaFileListElement>();
		for (AreaFileListElement item : areaFiles.elements()) {
			list.add(item);
		}

		return list;
	}
	/**
	 * Creates the ranges for the currently selected formula given
	 * the current project information.
	 *
	 * @return the ranges for the currently selected formula given
	 *         the current project information.
	 * @throws IllegalFormulaException if the ranges specified by dataset or by formula are invalid
	 */
	public List<AxisRange> createRanges() throws IllegalFormulaException {
		Logger.debug("in Project createRanges()");
		return createRanges(selectedFormula);
	}
	/**
	 * Creates the ranges for the currently selected formula given
	 * the current project information.
	 *
	 * @return the ranges for the currently selected formula given
	 *         the current project information.
	 * @throws IllegalFormulaException if the ranges specified by dataset or by formula are invalid
	 */

	public List<AxisRange> createRanges(FormulaListElement selectedFormula) throws IllegalFormulaException {
		Logger.debug("in Project createRanges (FormulaListElement)");
		if (selectedFormula == null) return new ArrayList<AxisRange>();
		List<AxisRange> ranges = checkDatasetRanges(selectedFormula);
		Logger.debug("in Project createRanges(FormulaListElement): number of ranges: " + ranges.size());

		// 4 is correct here because
		// we always set x and y if have to set either of them
		if (ranges.size() == 4) {
			// all 4 ranges set via the dataset.
			return ranges;
		}

		boolean setTimeRange = true;
		boolean setLayerRange = true;
		boolean setXYRange = true;
		for (AxisRange range : ranges) {
			if (range.getAxisType() == AxisType.TIME) setTimeRange = false;
			else if (range.getAxisType() == AxisType.LAYER) setLayerRange = false;
			else if (range.getAxisType() == AxisType.X_AXIS) setXYRange = false;
			else if (range.getAxisType() == AxisType.Y_AXIS) setXYRange = false;
		}

		// no dataset ranges specified for use, so use the formula ones
		Logger.debug("in Project createRanges(FormulaListElement): setLayerRange = " + setLayerRange +
				" selectedFormula.isLayerUsed = " + selectedFormula.isLayerUsed());

		if (setLayerRange && selectedFormula.isLayerUsed()) {
			Axes<CoordAxis> axes = selectedFormula.getAxes();
			int max = Math.max(selectedFormula.getLayerMin(), selectedFormula.getLayerMax());
			int min = Math.min(selectedFormula.getLayerMin(), selectedFormula.getLayerMax());
			Logger.debug("in Project createRanges(FormulaListElement): min = " + min + ", max = " + max);

			ranges.add(new AxisRange(axes.getZAxis(), min, max - min + 1));
		}

		if (setTimeRange && selectedFormula.isTimeUsed()) {
			Axes<CoordAxis> axes = selectedFormula.getAxes();
			int max = Math.max(selectedFormula.getTimeMin(), selectedFormula.getTimeMax());
			int min = Math.min(selectedFormula.getTimeMin(), selectedFormula.getTimeMax());
			ranges.add(new AxisRange(axes.getTimeAxis(), min, max - min + 1));
		}

		if (setXYRange && selectedFormula.isXYUsed()) {
			Axes<CoordAxis> axes = selectedFormula.getAxes();
			int max = Math.max(selectedFormula.getXMin(), selectedFormula.getXMax());
			int min = Math.min(selectedFormula.getXMin(), selectedFormula.getXMax());
			ranges.add(new AxisRange(axes.getXAxis(), min, max - min + 1));

			max = Math.max(selectedFormula.getYMin(), selectedFormula.getYMax());
			min = Math.min(selectedFormula.getYMin(), selectedFormula.getYMax());
			ranges.add(new AxisRange(axes.getYAxis(), min, max - min + 1));
		}

		return ranges;
	}

	// formula validation will do a more complete range check
	// here we want just want to make sure that the ranges as set
	// per dataset are coherent w/r to each other.
//	private List<AxisRange> checkDatasetRanges() throws IllegalFormulaException {
//		return checkDatasetRanges(selectedFormula);
//	}
	private List<AxisRange> checkDatasetRanges(FormulaListElement selectedFormula) throws IllegalFormulaException {
		Logger.debug("in Project checkDatasetRanges");
		AxisRange layerRange = null;
		AxisRange timeRange = null;
		AxisRange xRange = null;
		AxisRange yRange = null;
		DateRange currRange = null;
		Axes<CoordAxis> tAxes = null;
		for (FormulaVariable var : selectedFormula.variables()) {
			DatasetListElement item = getDatasetListItemFor(var.getDataset().getAlias());

			if (item.isLayerUsed()) {
				Axes<CoordAxis> axes = item.getDataset().getCoordAxes();
				int max = Math.max(item.getLayerMin(), item.getLayerMax());
				int min = Math.min(item.getLayerMin(), item.getLayerMax());
				int extent = max - min + 1;
				if (layerRange == null) layerRange = new AxisRange(item.getZAxisForVariable(var.getName()), min, extent);
				else if (layerRange.getOrigin() != min || layerRange.getExtent() != extent)
					throw new IllegalFormulaException("Dataset layer ranges are incompatible");
			}

			if (item.isXYUsed()) {
				Axes<CoordAxis> axes = item.getDataset().getCoordAxes();
				int max = Math.max(item.getXMin(), item.getXMax());
				int min = Math.min(item.getXMin(), item.getXMax());
				int extent = max - min + 1;
				if (xRange == null) xRange = new AxisRange(axes.getXAxis(), min, extent);
				else if (xRange.getOrigin() != min || xRange.getExtent() != extent)
					throw new IllegalFormulaException("Dataset domain ranges are incompatible");

				max = Math.max(item.getYMin(), item.getYMax());
				min = Math.min(item.getYMin(), item.getYMax());
				extent = max - min + 1;
				if (yRange == null) yRange = new AxisRange(axes.getYAxis(), min, extent);
				else if (yRange.getOrigin() != min || yRange.getExtent() != extent)
					throw new IllegalFormulaException("Dataset domain ranges are incompatible");
			}

			if (item.isTimeUsed()) {
				Axes<CoordAxis> axes = item.getDataset().getCoordAxes();
				int max = Math.max(item.getTimeMin(), item.getTimeMax());
				int min = Math.min(item.getTimeMin(), item.getTimeMax());
				int extent = max - min + 1;
				if (timeRange == null) {
					tAxes = axes;
					timeRange = new AxisRange(item.getTimeAxisForVariable(var.getName()), min, extent);
					long endIndex = timeRange.getOrigin() + (timeRange.getExtent() - 1);
					currRange = new DateRange(axes.getDate(timeRange.getOrigin()), axes.getDate((int) endIndex));
				} else {
					Axes<CoordAxis> otherAxes = var.getDataset().getCoordAxes();
					DateRange otherRange = new DateRange(otherAxes.getDate(min), otherAxes.getDate(max));
					if (!currRange.equals(otherRange)) {
						currRange = currRange.overlap(otherRange);
						if (currRange == null) {
							throw new IllegalFormulaException("Dataset time ranges are incompatible");
						}
					}
				}
			}
		}

		if (currRange != null) {
			Logger.debug("in Project.java, currRange != null so computing GregorianCalendar startDate and endDate");
			// convert the current range into a time axis range
//			Date startDate = new Date(currRange.getStart());
			GregorianCalendar startDate = new GregorianCalendar();	// 2014 changed Date to GregorianCalendar
			startDate.setTimeInMillis(currRange.getStart());
			int timeStart = tAxes.getTimeStep(startDate);
			if (timeStart == Axes.TIME_STEP_NOT_FOUND)
				throw new IllegalFormulaException("Dataset time ranges are incompatible");
//			Date endDate = new Date(currRange.getEnd());
			GregorianCalendar endDate = new GregorianCalendar();
			endDate.setTimeInMillis(currRange.getEnd());
			int timeEnd = tAxes.getTimeStep(endDate);
			if (timeEnd == Axes.TIME_STEP_NOT_FOUND)
				throw new IllegalFormulaException("Dataset time ranges are incompatible");
			timeRange = new AxisRange(tAxes.getTimeAxis(), timeStart, timeEnd - timeStart + 1);
		}

		List<AxisRange> ranges = new ArrayList<AxisRange>();
		if (layerRange != null) ranges.add(layerRange);
		if (timeRange != null) ranges.add(timeRange);
		if (xRange != null) {
			ranges.add(xRange);
			ranges.add(yRange);
		}

		return ranges;
	}

	private DatasetListElement getDatasetListItemFor(String alias) {
		Logger.debug("in Project getDatasetListItemFor");
		for (DatasetListElement item : datasets.elements()) {
			if (item.getDataset().getAlias().equals(alias)) return item;
		}
		return null;
	}

	/**
	 * Gets the set of formulas that reference the specified dataset.
	 *
	 * @param dataset the dataset
	 * @return the set of formulas that reference the specified dataset.
	 */
	public Set<FormulaListElement> getFormulas(Dataset dataset) {
		Logger.debug("in Project getFormulas");
		Set<FormulaListElement> set = new HashSet<FormulaListElement>();
		for (FormulaListElement item : formulas.elements()) {
			for (FormulaVariable var : item.variables()) {
				if (var.getDataset().equals(dataset)) {
					set.add(item);
				}
			}
		}
		return set;
	}

	public void removeFormulas(Set<FormulaListElement> formulas) {
		Logger.debug("in Project removeFormulas");
		for (FormulaListElement item : formulas) {
			this.formulas.removeFormula(item);
		}
	}

	/**
	 * Adds the specified variable as a single variable formula
	 * using the specified formula element creator.
	 *
	 * @param fCreator the creator to use
	 * @param var      the variable to add as a formula.
	 */
	public void addAsFormula(FormulaElementCreator fCreator, Variable var) {
		Logger.debug("in Project addAsFormula");
		String formula = var.getName() + var.getDataset().getAlias();
		FormulaListElement element = fCreator.create(formula);
		if (element != null) {
			int index = formulas.addFormula(element);
			formulas.setSelectedItem(index);
		}
	}
}
