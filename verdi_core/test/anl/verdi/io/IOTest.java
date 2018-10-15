package anl.verdi.io;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.log4j.PropertyConfigurator;
import org.java.plugin.ObjectFactory;
import org.java.plugin.PluginManager;
import org.java.plugin.boot.DefaultPluginsCollector;
import org.java.plugin.util.ExtendedProperties;

import anl.verdi.core.Project;
import anl.verdi.data.DataManager;
import anl.verdi.data.Dataset;
import anl.verdi.gui.DatasetListElement;
import anl.verdi.gui.DatasetListModel;
import anl.verdi.gui.FormulaElementCreator;
import anl.verdi.gui.FormulaListElement;
import anl.verdi.gui.FormulaListModel;

/**
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class IOTest extends TestCase {

	private DataManager manager;

	static {
		Properties props = new Properties();
		// log4j.rootLogger=warn, console
		props.setProperty("log4j.rootLogger", "warn");
		props.setProperty("log4j.logger.MessageCenter.INTERNAL.anl", "warn");
		PropertyConfigurator.configure(props);
	}

	public IOTest() throws Exception {
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

	public void testDatasetSaveLoad() throws IOException {
		DatasetListModel model = new DatasetListModel();
		FormulaListModel fModel = new FormulaListModel();
		Project project = new Project(model, fModel);

		File f = new File("./data/pave_example_data/RADM_CONC_1");
		URL url = f.toURI().toURL();
		 List<Dataset> datasets = manager.createDatasets(url);
		Dataset radm = datasets.get(0);
		String radmAlias = radm.getAlias();
		model.addDataset(radm);
		DatasetListElement element = (DatasetListElement) model.getElementAt(0);
		element.setLayerMax(1);
		element.setLayerMin(2);
		element.setTimeMax(3);
		element.setTimeMin(4);
		element.setLayerUsed(true);
		element.setTimeUsed(true);

		f = new File("e:/VERDI DATA/CCTM_CB05_A.200107.combine.conc");
		url = f.toURI().toURL();
		datasets = manager.createDatasets(url);
		Dataset cctm =datasets.get(0);
		String cctmAlias = cctm.getAlias();
		model.addDataset(cctm);

		element = (DatasetListElement) model.getElementAt(1);
		element.setLayerMax(5);
		element.setLayerMin(6);
		element.setTimeMax(7);
		element.setTimeMin(8);
		element.setLayerUsed(false);
		element.setTimeUsed(false);

		fModel.addFormula("O3[1]");
		FormulaListElement felement = (FormulaListElement) fModel.getElementAt(0);
		felement.setLayerMax(9);
		felement.setLayerMin(10);
		felement.setTimeMax(11);
		felement.setTimeMin(12);
		felement.setLayerUsed(true);
		felement.setTimeUsed(true);

		IO io = new IO();
		io.save(new File("./test/model.xml"), project);
		model.clear();
		fModel.clear();

		io.load( new File("./test/model.xml"), project, manager, new FormulaElementCreator() {
			public FormulaListElement create(String strFormula) {
				return new FormulaListElement(strFormula);
			}
		});
		assertEquals(2, model.getSize());
		element = (DatasetListElement) model.getElementAt(0);
		int radmIndex = 0;
		int cctmIndex = 0;
		if (element.getDataset().getURL().toString().endsWith(".conc")) {
			radmIndex = 1;
		} else {
			cctmIndex = 1;
		}

		element = (DatasetListElement) model.getElementAt(radmIndex);
		assertEquals(radmAlias, element.getDataset().getAlias());
		assertTrue(element.getDataset().getURL().toString().endsWith("RADM_CONC_1"));
		assertEquals(1, element.getLayerMax());
		assertEquals(2, element.getLayerMin());
		assertEquals(3, element.getTimeMax());
		assertEquals(4, element.getTimeMin());
		assertTrue(element.isTimeUsed());
		assertTrue(element.isLayerUsed());

		element = (DatasetListElement) model.getElementAt(cctmIndex);
		assertEquals(cctmAlias, element.getDataset().getAlias());
		assertTrue(element.getDataset().getURL().toString().endsWith("CCTM_CB05_A.200107.combine.conc"));
		assertEquals(5, element.getLayerMax());
		assertEquals(6, element.getLayerMin());
		assertEquals(7, element.getTimeMax());
		assertEquals(8, element.getTimeMin());
		assertTrue(!element.isTimeUsed());
		assertTrue(!element.isLayerUsed());

		felement = (FormulaListElement) fModel.getElementAt(0);
		assertEquals("O3[1]", felement.getFormula());
		assertEquals(9, felement.getLayerMax());
		assertEquals(10, felement.getLayerMin());
		assertEquals(11, felement.getTimeMax());
		assertEquals(12, felement.getTimeMin());
		assertTrue(felement.isTimeUsed());
		assertTrue(felement.isLayerUsed());
	}
}
