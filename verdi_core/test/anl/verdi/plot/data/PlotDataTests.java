package anl.verdi.plot.data;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.log4j.PropertyConfigurator;
import org.java.plugin.ObjectFactory;
import org.java.plugin.PluginManager;
import org.java.plugin.boot.DefaultPluginsCollector;
import org.java.plugin.util.ExtendedProperties;

import anl.verdi.data.AxisRange;
import anl.verdi.data.DataFrame;
import anl.verdi.data.DataFrameIndex;
import anl.verdi.data.DataManager;
import anl.verdi.formula.Formula;
import anl.verdi.formula.FormulaFactory;
import anl.verdi.formula.IllegalFormulaException;
import anl.verdi.formula.ValidationResult;

/**
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class PlotDataTests extends TestCase {

	// RADM_CONC O3(0:1:1, 0:5:1, 34:34:1, 0:3:1)
	private double[][][][] TestData =
	{
	   {
	     {
	       {0.03999655, 0.048183866, 0.05184684, 0.045486894}
	     },
	     {
	       {0.041114684, 0.049221966, 0.052981492, 0.048338357}
	     },
	     {
	       {0.043107543, 0.050416667, 0.054395746, 0.052843202}
	     },
	     {
	       {0.051436752, 0.054904122, 0.05627144, 0.054521676}
	     },
	     {
	       {0.06069959, 0.059118774, 0.057375424, 0.05636552}
	     },
	     {
	       {0.07082665, 0.067097366, 0.062493414, 0.059376683}
	     }
	   },
	   {
	     {
	       {0.033767194, 0.04103644, 0.04470751, 0.038943406}
	     },
	     {
	       {0.036755584, 0.04378156, 0.046483073, 0.043273926}
	     },
	     {
	       {0.038025916, 0.043939807, 0.046688747, 0.046288535}
	     },
	     {
	       {0.049283534, 0.05284342, 0.053528305, 0.052683555}
	     },
	     {
	       {0.060788907, 0.05956651, 0.058242936, 0.05751602}
	     },
	     {
	       {0.07060492, 0.069473624, 0.06755853, 0.06540847}
	     }
	   }
	 };
	
	private DataManager manager;

	static {
		Properties props = new Properties();
		// log4j.rootLogger=warn, console
		props.setProperty("log4j.rootLogger", "warn");
		props.setProperty("log4j.logger.MessageCenter.INTERNAL.anl", "warn");
		PropertyConfigurator.configure(props);
	}

	public PlotDataTests() throws Exception {
		DefaultPluginsCollector collector = new DefaultPluginsCollector();
		ExtendedProperties eprops = new ExtendedProperties();
		eprops.put("org.java.plugin.boot.pluginsRepositories", "./plugins");
		collector.configure(eprops);
		Collection locations = collector.collectPluginLocations();
		PluginManager pluginManager = ObjectFactory.newInstance(eprops).createManager();
		pluginManager.publishPlugins((PluginManager.PluginLocation[])
						locations.toArray(new PluginManager.PluginLocation[locations.size()]));
		manager = new DataManager(pluginManager);
	}

	public void testDataFrameXYZDataset() throws IOException, ParseException {
		File f = new File("./data/pave_example_data/RADM_CONC_1");
		URL url = f.toURI().toURL();
		manager.createDatasets(url);

		FormulaFactory factory = new FormulaFactory();
		Formula formula = factory.createTileFormula("O3[1]", null);
		List<AxisRange> ranges = new ArrayList<AxisRange>();

		ValidationResult validation = formula.validate(manager, new ArrayList<AxisRange>());
		assertEquals(ValidationResult.Status.PASS, validation.getStatus());
		DataFrame result = null;
		try {
			result = formula.evaluate(manager, ranges);
		} catch (IllegalFormulaException e) {
			fail(e.getMessage());
		}

		DataFrameXYZDataset section = new DataFrameXYZDataset();
		section.addSeries(result, 1, 1);
		DataFrameIndex index = result.getIndex();
		for (int x = 0; x < result.getAxes().getXAxis().getExtent(); x++) {
			for (int y = 0; y < result.getAxes().getYAxis().getExtent(); y++) {
				index.set(1, 1, x, y);
				double val = result.getDouble(index);
				assertEquals(val, section.getValue(0, x, y));
			}
		}

		index = result.getIndex();
		for (int i = 0; i < section.getItemCount(0); i++) {
			int x = (int)section.getXValue(0, i);
			int y = (int)section.getYValue(0, i);
			double val = section.getZValue(0, i);
			//System.out.println("x = " + x);
			//System.out.println("y = " + y);
			index.set(1, 1, x, y);
			assertEquals(result.getDouble(index), val);
		}

		manager.closeAllDatasets();
	}

	public void testCrossSectionDataset() throws IOException, ParseException {
		File f = new File("./data/pave_example_data/RADM_CONC_1");
		URL url = f.toURI().toURL();
		manager.createDatasets(url);

		FormulaFactory factory = new FormulaFactory();
		Formula formula = factory.createTileFormula("O3[1]", null);
		List<AxisRange> ranges = new ArrayList<AxisRange>();

		ValidationResult validation = formula.validate(manager, new ArrayList<AxisRange>());
		assertEquals(ValidationResult.Status.PASS, validation.getStatus());
		DataFrame result = null;
		try {
			result = formula.evaluate(manager, ranges);
		} catch (IllegalFormulaException e) {
			fail(e.getMessage());
		}

		CrossSectionXYZDataset section = new CrossSectionXYZDataset();
		section.addColSeries(result, 0, 22);
		DataFrameIndex index = result.getIndex();

		for (int layer = 0; layer < result.getAxes().getZAxis().getExtent(); layer++) {
			for (int y = 0; y < result.getAxes().getYAxis().getExtent(); y++) {
				index.set(0, layer, 22, y);
				double val = result.getDouble(index);
				// y axis of frame is the x axis of the series
				assertEquals(val, section.getValue(0, y, layer));
			}
		}

		index = result.getIndex();
		for (int i = 0; i < section.getItemCount(0); i++) {
			int layer = (int)section.getYValue(0, i);
			int row = (int)section.getXValue(0, i);
			double val = section.getZValue(0, i);
			//System.out.println("layer = " + layer);
			//System.out.println("row = " + row);
			index.set(0, layer, 22, row);
			assertEquals(result.getDouble(index), val);
		}

		// test row constant
		section = new CrossSectionXYZDataset();
		section.addRowSeries(result, 0, 10);
		index = result.getIndex();

		for (int layer = 0; layer < result.getAxes().getZAxis().getExtent(); layer++) {
			for (int x = 0; x < result.getAxes().getXAxis().getExtent(); x++) {
				index.set(0, layer, x, 10);
				double val = result.getDouble(index);
				// y axis of frame is the x axis of the series
				assertEquals(val, section.getValue(0, x, layer));
			}
		}

		index = result.getIndex();
		for (int i = 0; i < section.getItemCount(0); i++) {
			int layer = (int)section.getYValue(0, i);
			int col = (int)section.getXValue(0, i);
			double val = section.getZValue(0, i);
			//System.out.println("layer = " + layer);
			//System.out.println("row = " + row);
			index.set(0, layer, col, 10);
			assertEquals(result.getDouble(index), val);
		}
		
		manager.closeAllDatasets();
	}


}
