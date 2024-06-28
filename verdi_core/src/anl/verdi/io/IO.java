package anl.verdi.io;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages

import anl.verdi.core.Project;
import anl.verdi.data.DataManager;
import anl.verdi.data.Dataset;
import anl.verdi.gui.DatasetListElement;
import anl.verdi.gui.DatasetListModel;
import anl.verdi.gui.FormulaElementCreator;
import anl.verdi.gui.FormulaListElement;
import anl.verdi.gui.FormulaListModel;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

/**
 * Saves elements of the application to disk so they can be loaded later.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class IO {

	static final Logger Logger = LogManager.getLogger(IO.class.getName());
	private static final String DATASET_ELEMENT_ALIAS = "dataset.element";
	private static final String FORMULA_ELEMENT_ALIAS = "formula.element";
	private static final String PROJECT_ALIAS = "project";

	private static class ElementSet {
		private List<DatasetElementDescriptor> list = new ArrayList<DatasetElementDescriptor>();

		public void addDescriptor(DatasetElementDescriptor desc) {
			list.add(desc);
		}

		public List<DatasetListElement> createElements(List<Dataset> datasets, DataManager manager) throws IOException {
			List<DatasetListElement> elements = new ArrayList<DatasetListElement>();
			Map<Dataset, String> aliasMap = new HashMap<Dataset, String>();
			// find which of the datasets matches descriptor
			for (DatasetElementDescriptor desc : list) {
				int index = desc.getUrlIndex();
				for (Iterator<Dataset> iter = datasets.iterator(); iter.hasNext(); ) {
					Dataset dataset = iter.next();
					if (dataset.getIndexInURL() == index) {
						iter.remove();
						aliasMap.put(dataset, desc.getAlias());
						DatasetListElement element = new DatasetListElement(dataset);
						element.setTimeMin(desc.getTimeMin());
						element.setTimeMax(desc.getTimeMax());
						element.setTimeUsed(desc.isTimeUsed());
						element.setLayerMin(desc.getLayerMin());
						element.setLayerMax(desc.getLayerMax());
						element.setLayerUsed(desc.isLayerUsed());
						elements.add(element);
						break;
					}
				}
			}

			for (Dataset dataset : datasets) {
				// contains the unmatched ones so close those
				manager.closeDataset(dataset.getAlias());
			}

			manager.replaceAliases(aliasMap);
			return elements;
		}
	}

	static class ProjectDescriptor {
		List<DatasetElementDescriptor> datasetElements;
		List<FormulaElementDescriptor> formulaElements;

		public ProjectDescriptor(List<DatasetElementDescriptor> datasetElements, List<FormulaElementDescriptor> formulaElements) {
			this.datasetElements = datasetElements;
			this.formulaElements = formulaElements;
		}
	}

	/**
	 * Saves the specified project to the specified file.
	 *
	 * @param file    the file to save the model to
	 * @param project the project to save
	 * @throws IOException if there is an error duing saving
	 */
	public void save(File file, Project project) throws IOException {

		List<DatasetElementDescriptor> dList = new ArrayList<DatasetElementDescriptor>();
		DatasetListModel datasetModel = project.getDatasets();
		for (DatasetListElement element : datasetModel.elements()) {
			dList.add(new DatasetElementDescriptor(element));
		}

		List<FormulaElementDescriptor> fList = new ArrayList<FormulaElementDescriptor>();
		FormulaListModel formulaModel = project.getFormulas();
		for (FormulaListElement element : formulaModel.elements()) {
			fList.add(new FormulaElementDescriptor(element));
		}
		ProjectDescriptor descriptor = new ProjectDescriptor(dList, fList);

		XStream xstream = new XStream();
		xstream.alias(DATASET_ELEMENT_ALIAS, DatasetElementDescriptor.class);
		xstream.alias(FORMULA_ELEMENT_ALIAS, FormulaElementDescriptor.class);
		xstream.alias(PROJECT_ALIAS, ProjectDescriptor.class);
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(file);
			xstream.toXML(descriptor, fileWriter);
		} finally {
			fileWriter.flush();
			fileWriter.close();
		}
	}

	/**
	 * Loads the project data in specified file into the
	 * specified project.
	 *
	 * @param file    the file containing the data to load
	 * @param project the project to load the data into
	 * @param manager used to create the Datasets
	 * @param creator the creator used to properly create the formula items
	 * @throws IOException if there is an error during loading.
	 */
	public void load(File file, Project project, DataManager manager, FormulaElementCreator creator) throws IOException {
		Logger.debug("in IO.load, ready to instantiate xstream");
		XStream xstream = new XStream(new StaxDriver());
		xstream.setClassLoader(ProjectDescriptor.class.getClassLoader());
		Logger.debug("ready to set alias values in xstream");
		xstream.alias(DATASET_ELEMENT_ALIAS, DatasetElementDescriptor.class);
		xstream.alias(FORMULA_ELEMENT_ALIAS, FormulaElementDescriptor.class);
		xstream.alias(PROJECT_ALIAS, ProjectDescriptor.class);
		xstream.allowTypes(new Class[] { ProjectDescriptor.class, DatasetElementDescriptor.class, FormulaElementDescriptor.class } );
		//

		Logger.debug("ready to insantiate fileReader");
		FileReader fileReader = new FileReader(file);
		Logger.debug("setting descriptor = null");
		ProjectDescriptor descriptor = null;
		Logger.debug("ready to closeAllDatasets");
		manager.closeAllDatasets();
		Logger.debug("going to clear formulas and datasets");
		project.getFormulas().clear();
		project.getDatasets().clear();
		
		try {
			Logger.debug("ready to cast xstream.fromXML to ProjectDescriptor");
			descriptor = (ProjectDescriptor) xstream.fromXML(fileReader);
			Logger.debug("have descriptor, now ready to set up dElements ArrayList");
			List<DatasetListElement> dElements = new ArrayList<DatasetListElement>();
			// group by URL
			Logger.debug("now instantiating map as a new HashMap");
			Map<URL, ElementSet> map = new HashMap<URL, ElementSet>();
			Logger.debug("ready to go into for loop for DatasetElementDescriptor");
			for (DatasetElementDescriptor desc : descriptor.datasetElements) {
				URL url = desc.getDatasetURL();
				ElementSet set = map.get(url);
				if (set == null) {
					set = new ElementSet();
					map.put(url, set);
				}
				set.addDescriptor(desc);
			}
			Logger.debug("done with that loop, now ready to go into for URL loop");
			for (URL url : map.keySet()) {
				List<Dataset> datasets = manager.createDatasets(url);
				if (datasets.equals(DataManager.NULL_DATASETS)) throw new IOException("Cannot find dataset");
				ElementSet set = map.get(url);
				dElements.addAll(set.createElements(datasets, manager));
			}

			Logger.debug("done with that loop, now ready to add all elements to project");
			project.getDatasets().addAll(dElements);

			Logger.debug("now ready to instantiate fElements list");
			List<FormulaListElement> fElements = new ArrayList<FormulaListElement>();
			Logger.debug(" and into the for FormulaElementDescriptor loop");
			for (FormulaElementDescriptor desc : descriptor.formulaElements) {
				FormulaListElement element = creator.create(desc.getFormula());
				element.setTimeMin(desc.getTimeMin());
				element.setTimeMax(desc.getTimeMax());
				element.setTimeUsed(desc.isTimeUsed());
				element.setLayerMin(desc.getLayerMin());
				element.setLayerMax(desc.getLayerMax());
				element.setLayerUsed(desc.isLayerUsed());
				fElements.add(element);
			}
			
			Logger.debug("ready to addAll fElements to project");
			project.getFormulas().addAll(fElements);
			Logger.debug("and done with the load member function in IO");
		} catch (Exception ex) {
			project.getFormulas().clear();
			project.getDatasets().clear();
			throw new IOException("Invalid project file '" + file.getAbsolutePath() + "'", ex);
		} finally {
			fileReader.close();
		}
	}
}
