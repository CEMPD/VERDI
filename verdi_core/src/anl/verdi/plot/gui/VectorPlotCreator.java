package anl.verdi.plot.gui;
// 2014 removed old Vector Plot
//import java.util.HashSet;
//import java.util.Set;
//
//import simphony.util.messages.MessageCenter;
//import ucar.ma2.Array;
//import ucar.ma2.IndexIterator;
//import anl.verdi.core.Project;
//import anl.verdi.core.VerdiApplication;
//import anl.verdi.core.VerdiGUI;
//import anl.verdi.data.Axes;
//import anl.verdi.data.DataFrame;
//import anl.verdi.data.DataFrameAxis;
//import anl.verdi.data.DataFrameBuilder;
//import anl.verdi.formula.Formula;
//import anl.verdi.formula.IllegalFormulaException;
//import anl.verdi.gui.FormulaListElement;
//import anl.verdi.plot.config.PlotConfiguration;
//import anl.verdi.plot.config.VectorPlotConfiguration;
//import anl.verdi.util.ArrayFactory;
//import anl.verdi.util.CompatibilityChecker;
//import anl.verdi.util.DateRange;
//
///**
// * Creator for VectorPlots
// *
// * @author Nick Collier
// * @version $Revision$ $Date$
// */
//public class VectorPlotCreator extends AbstractPlotCreator {
//
//	private static final MessageCenter msg = MessageCenter.getMessageCenter(VectorPlotCreator.class);
//
//	/**
//	 * Creates a VectorPlotCreator from the app.
//	 *
//	 * @param app the pave application
//	 */
//	public VectorPlotCreator(VerdiApplication app) {
//		super(Formula.Type.VECTOR, app);
//	}
//
//	/**
//	 * Creates a VectorPlotCreator from the app and
//	 * the specified configuration.
//	 *
//	 * @param app the pave application
//	 * @param config the configuration to use to create the plot
//	 */
//	public VectorPlotCreator(VerdiApplication app, PlotConfiguration config) {
//		super(Formula.Type.VECTOR, app, config);
//	}
//
//	/**
//	 * Perform the actual plot creation.
//	 */
//	public Plot doCreatePlot() {
//		if (config.getObject(VectorPlotConfiguration.VECTOR_X) != null) {
//			// create from config.
//			CompatibilityChecker checker = new CompatibilityChecker(app.getProject().getDatasetsAsList());
//			Set<FormulaListElement> items = new HashSet<FormulaListElement>();
//			FormulaListElement xElement = (FormulaListElement) config.getObject(VectorPlotConfiguration.VECTOR_X);
//			FormulaListElement yElement = (FormulaListElement) config.getObject(VectorPlotConfiguration.VECTOR_Y);
//			FormulaListElement tileElement = (FormulaListElement) config.getObject(VectorPlotConfiguration.VECTOR_TILE);
//
//			items.add(xElement);
//			items.add(yElement);
//			if (tileElement != null) items.add(tileElement);
//			DateRange range = checker.isCompatible(items);
//			if (range != null) return createPlot(xElement, yElement, tileElement, range);
//			else {
//				msg.error("Error creating vector plot: formulas are not compatible",
//								new IllegalFormulaException());
//				return null;
//			}
//		} else {
//			return createFromDialog();
//		}
//	}
//
//	private Plot createFromDialog() {
//		VerdiGUI gui = app.getGui();
//		Project project = app.getProject();
//		VectorDialog dialog = new VectorDialog(gui.getFrame());
//		dialog.setLocationRelativeTo(gui.getFrame());
//		dialog.init(project.getFormulasAsList(), new CompatibilityChecker(project.getDatasetsAsList()));
//		dialog.pack();
//		dialog.setVisible(true);
//		FormulaListElement xElement = dialog.getXElement();
//		if (xElement != null) {
//			return createPlot(xElement, dialog.getYElement(), dialog.getTileElement(),
//							dialog.getResolvedDateRange());
//		}
//		return null;
//	}
//
//	private Plot createPlot(FormulaListElement xElement, FormulaListElement yElement,
//	                        FormulaListElement tileElement, DateRange range) {
//		Project project = app.getProject();
//		VerdiGUI gui = app.getGui();
//		FormulaListElement oldElement = project.getSelectedFormula();
//		project.setSelectedFormula(xElement);
//		DataFrame xFrame = app.evaluateFormula(type, range);
//		project.setSelectedFormula(yElement);
//		DataFrame yFrame = app.evaluateFormula(type, range);
//		DataFrame tileFrame = null;
//		if (tileElement != null) {
//			project.setSelectedFormula(tileElement);
//			tileFrame = app.evaluateFormula(type, range);
//		}
//		project.setSelectedFormula(oldElement);
//		if (xFrame != null && yFrame != null) {
//			PlotFactory factory = new PlotFactory();
//			DataFrame[] unitVecs = unitVectorTransform(xFrame, yFrame);
//			final PlotPanel panel = factory.getVectorPlot(xElement.getFormula(), yElement.getFormula(),
//							unitVecs[0], unitVecs[1], tileFrame, config);
//			gui.addPlot(panel);
//			panel.addPlotListener(app);
//			return panel.getPlot();
//		}
//
//		return null;
//	}
//	
//	/**
//	 * Perform the actual plot creation without adding to the GUI.
//	 */
//	public Plot createPlotInBackground() {
//		if (config.getObject(VectorPlotConfiguration.VECTOR_X) != null) {
//			CompatibilityChecker checker = new CompatibilityChecker(app.getProject().getDatasetsAsList());
//			Set<FormulaListElement> items = new HashSet<FormulaListElement>();
//			FormulaListElement xElement = (FormulaListElement) config.getObject(VectorPlotConfiguration.VECTOR_X);
//			FormulaListElement yElement = (FormulaListElement) config.getObject(VectorPlotConfiguration.VECTOR_Y);
//			FormulaListElement tileElement = (FormulaListElement) config.getObject(VectorPlotConfiguration.VECTOR_TILE);
//
//			items.add(xElement);
//			items.add(yElement);
//			
//			if (tileElement != null) items.add(tileElement);
//			DateRange range = checker.isCompatible(items);
//			
//			if (range != null) return createPlotInBackground(xElement, yElement, tileElement, range);
//		}
//		
//		msg.error("Error creating vector plot: formulas are not compatible", new IllegalFormulaException());
//		
//		return null;
//	}
//	
//	private Plot createPlotInBackground(FormulaListElement xElement, FormulaListElement yElement,
//            FormulaListElement tileElement, DateRange range) {
//		Project project = app.getProject();
//		FormulaListElement oldElement = project.getSelectedFormula();
//		project.setSelectedFormula(xElement);
//		DataFrame xFrame = app.evaluateFormula(type, range);
//		project.setSelectedFormula(yElement);
//		DataFrame yFrame = app.evaluateFormula(type, range);
//		DataFrame tileFrame = null;
//		
//		if (tileElement != null) {
//			project.setSelectedFormula(tileElement);
//			tileFrame = app.evaluateFormula(type, range);
//		}
//		
//		project.setSelectedFormula(oldElement);
//
//		if (xFrame != null && yFrame != null) {
//			PlotFactory factory = new PlotFactory();
//			DataFrame[] unitVecs = unitVectorTransform(xFrame, yFrame);
//			final PlotPanel panel = factory.getVectorPlot(xElement.getFormula(), yElement.getFormula(),
//					unitVecs[0], unitVecs[1], tileFrame, config);
//
//			return panel.getPlot();
//		}
//
//		return null;
//	}
//
//	private DataFrame createDataFrame(DataFrame frame) {
//		DataFrameBuilder builder = new DataFrameBuilder();
//		builder.addDataset(frame.getDataset());
//		builder.setVariable(frame.getVariable());
//		builder.setArray(ArrayFactory.createDoubleArray(frame.getArray().getShape()));
//		Axes<DataFrameAxis> axes = frame.getAxes();
//		if (axes.getTimeAxis() != null)
//			builder.addAxis(DataFrameAxis.createDataFrameAxis(axes.getTimeAxis(), axes.getTimeAxis().getArrayIndex()));
//		if (axes.getZAxis() != null)
//			builder.addAxis(DataFrameAxis.createDataFrameAxis(axes.getZAxis(), axes.getZAxis().getArrayIndex()));
//		if (axes.getXAxis() != null)
//			builder.addAxis(DataFrameAxis.createDataFrameAxis(axes.getXAxis(), axes.getXAxis().getArrayIndex()));
//		if (axes.getYAxis() != null)
//			builder.addAxis(DataFrameAxis.createDataFrameAxis(axes.getYAxis(), axes.getYAxis().getArrayIndex()));
//		return builder.createDataFrame();
//	}
//
//	private DataFrame[] unitVectorTransform(DataFrame xFrame, DataFrame yFrame) {
//		DataFrame newX = createDataFrame(xFrame);
//		DataFrame newY = createDataFrame(yFrame);
////		IndexIterator newXIter = newX.getArray().getIndexIteratorFast();	// deprecated netcdf library
////		IndexIterator newYIter = newY.getArray().getIndexIteratorFast();
//		IndexIterator newXIter = newX.getArray().getIndexIterator();
//		IndexIterator newYIter = newY.getArray().getIndexIterator();
//
//		Array xArray = xFrame.getArray();
//		Array yArray = yFrame.getArray();
////		IndexIterator xIter = xArray.getIndexIteratorFast();	// deprecated netcdf library
////		IndexIterator yIter = yArray.getIndexIteratorFast();
//		IndexIterator xIter = xArray.getIndexIterator();
//		IndexIterator yIter = yArray.getIndexIterator();
//		while (xIter.hasNext()) {
//			double xVal = xIter.getDoubleNext();
//			double yVal = yIter.getDoubleNext();
//			double mag = Math.sqrt(xVal * xVal + yVal * yVal);
//			newXIter.setDoubleNext(xVal / mag * .5);
//			newYIter.setDoubleNext(yVal / mag * .5);
//		}
//
//		return new DataFrame[]{newX, newY};
//	}
//}
