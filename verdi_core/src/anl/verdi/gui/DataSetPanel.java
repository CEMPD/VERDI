/*
 * Created by JFormDesigner on Tue Mar 06 11:51:27 CST 2007
 */

package anl.verdi.gui;

import gov.epa.emvl.RemoteFileReader;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.map.FeatureLayer;
//import org.geotools.map.MapContext;
import org.geotools.map.MapContent;
import org.geotools.map.MapViewport;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.styling.StyleBuilder;

import anl.verdi.core.Project;
import anl.verdi.core.VerdiApplication;
import anl.verdi.data.Dataset;
import anl.verdi.data.Variable;
import anl.verdi.util.FocusClickFix;
import anl.verdi.util.Tools;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.Sizes;
//import org.geotools.map.DefaultMapContext;

/**
 * @author User #1
 */
public class DataSetPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5160254644729641676L;

	private Project project;

	private FormulaElementCreator fCreator;

	private FormulaEditor fEditor;

//	private MapContext context;
	private MapContent context;
	private final VerdiApplication verdiApp = VerdiApplication.getInstance();

	StyleBuilder builder = new StyleBuilder();

	private final static String remoteHosts = readRemoteHosts();
	private final RemoteFileReader remoteFileReader =
		new RemoteFileReader( remoteHosts );

	private static String readRemoteHosts() {
		System.out.println("in DataSetPanel [static] readRemoteHosts");
	 String result = System.getProperty(Tools.REMOTE_HOSTS);;
		//"amber.nesc.epa.gov,vortex.rtpnc.epa.gov,garnet01.rtpnc.epa.gov,tulip.rtpnc.epa.gov";

	 if (result != null && !result.trim().isEmpty()) return result.trim();
	 
		final String fileName =
			System.getProperty( "user.dir" ) + File.separatorChar +
			"ui.properties";
		try {
			final FileInputStream inputStream = new FileInputStream( fileName );
			final Properties properties = new Properties();
			properties.load( inputStream );
			result = properties.getProperty( "remotehosts" );
		} catch ( Exception unused_ ) {
		}
		return result;
	}
	// dummy constructor for testing
	public DataSetPanel() {
		this(new Project(new DatasetListModel(), new FormulaListModel()),
//				new DefaultMapContext(DefaultGeographicCRS.WGS84));
//				new MapContent(DefaultGeographicCRS.WGS84));	// deprecated changed to (ViewPort.set)
				new MapContent());
		System.out.println("in DataSetPanel default constructor");
		MapViewport aViewport = new MapViewport();
		aViewport.setCoordinateReferenceSystem(DefaultGeographicCRS.WGS84);
		this.context.setViewport(aViewport);
	}

	public DataSetPanel(Project project, MapContent mapContent) {
		System.out.println("in DataSetPanel constructor (Project, MapContent)");
		this.project = project;
		this.context = mapContent;
		initComponents();
		variableList.setModel(new VariablesModel());
		dataList.setModel(project.getDatasets());
		dataList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		initListeners();
		initializeButtons();
		initializePopups();
	}

	public void addFormulaCallbacks(FormulaElementCreator creator,
			FormulaEditor fEditor) {
		System.out.println("in DataSetPanel addFormulaCallbacks");
		this.fCreator = creator;
		this.fEditor = fEditor;
	}

	private void initializePopups() {
		System.out.println("in DataSetPanel.initializePopups");
		dataList.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent evt) {
				if (evt.isPopupTrigger()) {
					showDataListPopup(evt);
				}
			}

			public void mouseReleased(MouseEvent evt) {
				if (evt.isPopupTrigger()) {
					showDataListPopup(evt);
				}
			}
		});

		variableList.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent evt) {
				if (evt.isPopupTrigger()) {
					showVariablesPopup(evt);
				}
			}

			public void mouseReleased(MouseEvent evt) {
				if (evt.isPopupTrigger()) {
					showVariablesPopup(evt);
				}
			}

			public void mouseClicked(MouseEvent evt) {
				if (evt.getClickCount() == 2)
					addVariable();
			}
		});
	}

	private Action addDatasetAction = new AbstractAction("Add Dataset") {
		/**
		 * 
		 */
		private static final long serialVersionUID = -7602621171843094405L;

		public void actionPerformed(ActionEvent e) {
			btnAdd.doClick();
		}
	};

	private Action deleteDatasetAction = new AbstractAction("Delete Dataset") {
		/**
		 * 
		 */
		private static final long serialVersionUID = -316540899797132588L;

		public void actionPerformed(ActionEvent e) {
			btnDelete.doClick();
		}
	};

	private Action addDRemoteDatasetAction = new AbstractAction("Add Remote Dataset") {
		/**
		 * 
		 */
		private static final long serialVersionUID = -927132579168655936L;

		public void actionPerformed(ActionEvent e) {
			btnAdd.doClick();
		}
	};

	private Action datasetPropertiesAction = new AbstractAction("Properties") {
		/**
		 * 
		 */
		private static final long serialVersionUID = 794792777678207306L;

		public void actionPerformed(ActionEvent e) {
			System.out.println("properties");
		}
	};

	private void showDataListPopup(MouseEvent evt) {
		System.out.println("in DataSetPanel showDataListPopup");
		boolean enable = dataList.getSelectedIndex() != -1;
		JPopupMenu menu = new JPopupMenu();
		menu.add(addDatasetAction);
		menu.add(deleteDatasetAction).setEnabled(enable);
		menu.add(datasetPropertiesAction).setEnabled(false);

		menu.show(evt.getComponent(), evt.getX(), evt.getY());
	}

	private Action addVariableAsFormula = new AbstractAction(
			"Add Variable(s) as Formula(s)") {
		private static final long serialVersionUID = 794792777678207306L;

		public void actionPerformed(ActionEvent e) {
			addVariable();
		}
	};

	private void addVariable() {
		System.out.println("in DataSetPanel addVariable");
//		Object[] objs = variableList.getSelectedValues();	// 2014 changed from returning an array to returning a List
		List objs = variableList.getSelectedValuesList();
//		if (objs.length > 0) {
		if (objs.size() > 0) {
			for (Object obj : objs) {
				Variable var = (Variable) obj;
				project.addAsFormula(fCreator, var);
			}
		}
	}

	private Action addVariableToEditor = new AbstractAction(
			"Add Variable(s) to Formula Editor") {
		/**
				 * 
				 */
				private static final long serialVersionUID = -8879713193381817713L;

		public void actionPerformed(ActionEvent e) {
//			Object[] objs = variableList.getSelectedValues();	// 2014 changed from returning an array to returning a List
			List objs = variableList.getSelectedValuesList();
//			if (objs.length > 0) {
			if (objs.size() > 0) {
				StringBuilder builder = new StringBuilder();
				for (Object obj : objs) {
					Variable var = (Variable) obj;
					builder.append(var.getName());
					builder.append(var.getDataset().getAlias());
					builder.append(" ");
				}
				fEditor.add(builder.toString());
			}
		}
	};

	private void showVariablesPopup(MouseEvent evt) {
		System.out.println("in DataSetPanel showVariablesPopup");
		boolean enable = variableList.getSelectedIndex() != -1;
		JPopupMenu menu = new JPopupMenu();
		menu.add(addVariableToEditor).setEnabled(enable);
		menu.add(addVariableAsFormula).setEnabled(enable);

		menu.show(evt.getComponent(), evt.getX(), evt.getY());
	}

	private void initListeners() {
		System.out.println("in DataSetPanel initListeners");
		ListSelectionListener listSelectionListener = new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent arg0) {
				int index = dataList.getSelectedIndex();
				if (index >= 0) {
					DatasetListElement element = (DatasetListElement) dataList
							.getModel().getElementAt(index);
					// update other values on screen
					((VariablesModel) variableList.getModel())
							.addVariablesForDataset(element.getDataset());
					setTimeValues(element);
					setLayerValues(element);
					setDomainValues(element);
				} else {
					((VariablesModel) variableList.getModel())
							.addVariablesForDataset(null);
					setTimeValues(null);
					setLayerValues(null);
					setDomainValues(null);
				}
			}
		};

		addPanelListeners();
		dataList.addListSelectionListener(listSelectionListener);
	}

	private void initializeButtons() {
		System.out.println("in DataSetPanel initializeButtons");
		Icon icon = btnAdd.getIcon();
		btnAdd.setMaximumSize(new Dimension(icon.getIconWidth(), icon
				.getIconHeight()));
		btnAdd.setPreferredSize(btnAdd.getMaximumSize());
		btnAdd.setMinimumSize(btnAdd.getMaximumSize());

		icon = btnDelete.getIcon();
		btnDelete.setMaximumSize(new Dimension(icon.getIconWidth(), icon
				.getIconHeight()));
		btnDelete.setPreferredSize(btnDelete.getMaximumSize());
		btnDelete.setMinimumSize(btnDelete.getMaximumSize());
		
		icon = btnAddRemote.getIcon();
		btnAddRemote.setMaximumSize(new Dimension(icon.getIconWidth(), icon
				.getIconHeight()));
		btnAddRemote.setPreferredSize(btnAddRemote.getMaximumSize());
		btnAddRemote.setMinimumSize(btnAddRemote.getMaximumSize());

		btnDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
//				Object[] objs = dataList.getSelectedValues();	// 2014 changed from returning an array to returning a List
				List objs = dataList.getSelectedValuesList();
//				if (objs.length > 0) {
				if (objs.size() > 0) {
					for (Object obj : objs) {
						DatasetListModel listModel = ((DatasetListModel) dataList
								.getModel());
						DatasetListElement item = (DatasetListElement) obj;
						Set<FormulaListElement> formulas = project
								.getFormulas(item.getDataset());
						if (formulas.size() > 0) {
							int result = JOptionPane
									.showConfirmDialog(
											SwingUtilities
													.getWindowAncestor(DataSetPanel.this),
											"Deleting this dataset will also remove the formulas that reference this dataset. Continue?",
											"Delete Dataset?",
											JOptionPane.YES_NO_OPTION);
							if (result == JOptionPane.NO_OPTION)
								continue;
						}

						listModel.removeDataset(item);
						project.removeFormulas(formulas);
					}
				}
			}
		});

		btnAddRemote.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent unused_) {
				remoteFileReader.setVisible( true );
				final File localCopyFile = remoteFileReader.getLocalCopyFile();

				if (localCopyFile.exists() && localCopyFile.isFile()) {
					try {
						verdiApp.loadDataset( new File[] { localCopyFile } );
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});

		btnAdd.addMouseListener(new FocusClickFix());
		btnAddRemote.addMouseListener(new FocusClickFix());
		btnDelete.addMouseListener(new FocusClickFix());
	}

	private void initComponents() {
		System.out.println("in DataSetPanel initComponents");
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		// Generated using JFormDesigner non-commercial license
		dataSetPanel = new JPanel();
		dataSets = new JPanel();
		btnAdd = new JButton();
		btnDelete = new JButton();
		btnAddRemote = new JButton();
		scrollPaneDataset = new JScrollPane();
		dataList = new JList();
		variablesPanel = new JPanel();
		scrollPane6 = new JScrollPane();
		variableList = new JList();
		timePanel = new TimePanel();
		layerPanel1 = new LayerPanel();
		domainPanel1 = new DomainPanel();
		CellConstraints cc = new CellConstraints();

		// ======== this ========
		setLayout(new FormLayout("pref:grow", "fill:default:grow"));

		// ======== dataSetPanel ========
		{
			dataSetPanel.setBorder(null);
			// 2014
			RowSpec[] aRowSpec = RowSpec.decodeSpecs("fill:max(pref;125dlu):grow");
			ColumnSpec[] aColumnSpec = ColumnSpec.decodeSpecs("pref:grow");
			dataSetPanel.setLayout(new FormLayout(aColumnSpec,
					new RowSpec[] {
					new RowSpec(Sizes.dluY(108)), new RowSpec(Sizes.dluY(92)),
					FormFactory.PREF_ROWSPEC, FormFactory.LINE_GAP_ROWSPEC,
					FormFactory.PREF_ROWSPEC, FormFactory.LINE_GAP_ROWSPEC,
					aRowSpec[0] }));
//			dataSetPanel.setLayout(new FormLayout(ColumnSpec
//					.decodeSpecs("pref:grow"), new RowSpec[] {
//					new RowSpec(Sizes.dluY(108)), new RowSpec(Sizes.dluY(92)),
//					FormFactory.PREF_ROWSPEC, FormFactory.LINE_GAP_ROWSPEC,
//					FormFactory.PREF_ROWSPEC, FormFactory.LINE_GAP_ROWSPEC,
//					new RowSpec("fill:max(pref;125dlu):grow") }));

			// ======== dataSets ========
			{
				dataSets.setBorder(new TitledBorder("Datasets"));
				// 2014
				ColumnSpec cColumnSpec = new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW);
				RowSpec cRowSpec = new RowSpec(RowSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW);
				dataSets.setLayout(new FormLayout(new ColumnSpec[] {
						FormFactory.DEFAULT_COLSPEC,
						FormFactory.RELATED_GAP_COLSPEC,
						FormFactory.DEFAULT_COLSPEC,
						FormFactory.RELATED_GAP_COLSPEC,
						FormFactory.DEFAULT_COLSPEC,
						cColumnSpec }, new RowSpec[] {
						FormFactory.DEFAULT_ROWSPEC,
						FormFactory.RELATED_GAP_ROWSPEC,
						cRowSpec }));
//				dataSets.setLayout(new FormLayout(new ColumnSpec[] {
//						FormFactory.DEFAULT_COLSPEC,
//						FormFactory.RELATED_GAP_COLSPEC,
//						FormFactory.DEFAULT_COLSPEC,
//						FormFactory.RELATED_GAP_COLSPEC,
//						FormFactory.DEFAULT_COLSPEC,
//						new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT,
//								FormSpec.DEFAULT_GROW) }, new RowSpec[] {
//						FormFactory.DEFAULT_ROWSPEC,
//						FormFactory.RELATED_GAP_ROWSPEC,
//						new RowSpec(RowSpec.FILL, Sizes.DEFAULT,
//								FormSpec.DEFAULT_GROW) }));

				String verdiHome = Tools.getVerdiHome();		// 2014 new method for reading in an image file
				String separator = "/";		// use forward slash only for constructor ImageIcon(String filename);
				String pathName = verdiHome + separator + "plugins" + separator + "core" + separator + "icons"
						 + separator;

				// ---- btnAdd ----
				String filePlus = new String(pathName + "plus.png");
//				btnAdd.setIcon(new ImageIcon(getClass().getResource("/plus.png")));
				btnAdd.setIcon(new ImageIcon(filePlus));
				btnAdd.setToolTipText("Add local dataset");
				dataSets.add(btnAdd, cc.xy(1, 1));

				// ---- btnDelete ----
				String fileMinus = new String(pathName + "minus.png");
//				btnDelete.setIcon(new ImageIcon(getClass().getResource("/minus.png")));
				btnDelete.setIcon(new ImageIcon(fileMinus));
				btnDelete.setToolTipText("Remove local dataset");
				dataSets.add(btnDelete, cc.xy(3, 1));

				// ---- btnAddRemote ----
				String filePlusRemote = new String(pathName + "plus-remote.png");
//				btnAddRemote.setIcon(new ImageIcon(getClass().getResource("/plus-remote.png")));
				btnAddRemote.setIcon(new ImageIcon(filePlusRemote));
				btnAddRemote.setToolTipText("Add remote dataset");
				dataSets.add(btnAddRemote, cc.xy(5, 1));

				// ======== scrollPaneDataset ========
				{

					// ---- dataList ----
					dataList.setSelectedIndex(0);
					dataList.setMaximumSize(new Dimension(300, 100));
					dataList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					dataList.setPrototypeCellValue("RADM_CONC_!");
					scrollPaneDataset.setViewportView(dataList);
				}
				dataSets.add(scrollPaneDataset, cc.xywh(1, 3, 6, 1));
			}
			dataSetPanel.add(dataSets, cc.xy(1, 1));

			// ======== variablesPanel ========
			{
				variablesPanel.setBorder(new TitledBorder("Variables"));
				variablesPanel.setLayout(new BorderLayout(2, 5));

				// ======== scrollPane6 ========
				{

					// ---- variableList ----
					variableList.setPrototypeCellValue("O3[1]");
					scrollPane6.setViewportView(variableList);
				}
				
				variablesPanel.add(new JLabel("Double-click to add the variable as a formula."), BorderLayout.NORTH);
				variablesPanel.add(scrollPane6, BorderLayout.CENTER);
			}
			dataSetPanel.add(variablesPanel, cc.xy(1, 2));
			dataSetPanel.add(timePanel, cc.xy(1, 3));
			dataSetPanel.add(layerPanel1, cc.xy(1, 5));
			dataSetPanel.add(domainPanel1, cc.xy(1, 7));
		}
		add(dataSetPanel, cc.xy(1, 1));
		// //GEN-END:initComponents
	}

	private void addPanelListeners() {
		System.out.println("in DataSetPanel addPanelListeners");
		timePanel.addListeners(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Object obj = dataList.getSelectedValue();
				if (obj != null) {
					JSpinner spinner = (JSpinner) e.getSource();
					int val = ((Integer) spinner.getValue()).intValue() - 1;
					((DatasetListElement) obj).setTimeMin(val);
				}
			}
		}, new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Object obj = dataList.getSelectedValue();
				if (obj != null) {
					JSpinner spinner = (JSpinner) e.getSource();
					int val = ((Integer) spinner.getValue()).intValue() - 1;
					((DatasetListElement) obj).setTimeMax(val);
				}
			}
		},

		new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object obj = dataList.getSelectedValue();
				if (obj != null) {
					JCheckBox box = (JCheckBox) e.getSource();
					((DatasetListElement) obj).setTimeUsed(box.isSelected());
				}
			}
		});

		layerPanel1.addListeners(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Object obj = dataList.getSelectedValue();
				if (obj != null) {
					JSpinner spinner = (JSpinner) e.getSource();
					int val = ((Integer) spinner.getValue()).intValue() - 1;
					((DatasetListElement) obj).setLayerMin(val);
				}
			}
		}, new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Object obj = dataList.getSelectedValue();
				if (obj != null) {
					JSpinner spinner = (JSpinner) e.getSource();
					int val = ((Integer) spinner.getValue()).intValue() - 1;
					((DatasetListElement) obj).setLayerMax(val);
				}
			}
		},

		new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				Object obj = dataList.getSelectedValue();
				if (obj != null) {
					JCheckBox box = (JCheckBox) evt.getSource();
					((DatasetListElement) obj).setLayerUsed(box.isSelected());
				}
			}
		});
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	// Generated using JFormDesigner non-commercial license
	private JPanel dataSetPanel;
	private JPanel dataSets;
	private JButton btnAdd;
	private JButton btnDelete;
	private JButton btnAddRemote;
	private JScrollPane scrollPaneDataset;
	private JList dataList;
	private JPanel variablesPanel;
	private JScrollPane scrollPane6;
	private JList variableList;
	private TimePanel timePanel;
	private LayerPanel layerPanel1;
	private DomainPanel domainPanel1;

	// JFormDesigner - End of variables declaration //GEN-END:variables

	public void loadDataset(Dataset dataset) {
		System.out.println("in DataSetPanel loadDataset");
		DatasetListModel model = (DatasetListModel) dataList.getModel();
		int index = model.addDataset(dataset);
		dataList.setSelectedIndex(index);
		dataList.scrollRectToVisible(dataList.getCellBounds(index, index));
	}

	public void addOpenDatasetAction(Action openDatasetAction) {
		System.out.println("in DataSetPanel addOpenDatasetAction");
		btnAdd.addActionListener(openDatasetAction);
	}

	class VariablesModel extends AbstractListModel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 9213900834988423583L;
		ArrayList<Variable> variables = new ArrayList<Variable>();

		public Object getElementAt(int index) {
			return variables.get(index);
		}

		public Variable getVariableAt(int index) {
			return variables.get(index);
		}

		public int getSize() {
			if (variables == null)
				return 0;
			return variables.size();
		}

		public void addVariablesForDataset(Dataset set) {
			// create the dataset and add it to the model
			int size = variables.size();
			if (size > 0) {
				variables.clear();
				fireIntervalRemoved(this, 0, size);
			}
			if (set == null)
				return;
			java.util.List<Variable> list = set.getVariables();
			if (list == null)
				return;
			for (Variable variable : list) {
				if (list != null) {
					// add to the model
					int index = variables.size();
					variables.add(variable);
					fireIntervalAdded(this, index, index);

				}
			}
		}
	}

	private void setTimeValues(DatasetListElement element) {
		System.out.println("in DataSetPanel setTimeValues");
		if (element != null
				&& element.getTimeMin() != DatasetListElement.NO_TIME_VALUE) {
			timePanel.setEnabled(true);
			timePanel.reset(element.getDataset().getCoordAxes(), element
					.getTimeMin(), element.getTimeMax(), element.isTimeUsed());
		} else {
			timePanel.setEnabled(false);
		}
	}

	public void setLayerValues(DatasetListElement element) {
		System.out.println("in DataSetPanel setLayerValues");
		if (element != null
				&& element.getLayerMin() != DatasetListElement.NO_LAYER_VALUE) {
			layerPanel1.setEnabled(true);
			layerPanel1.reset(element.getDataset().getCoordAxes(), element
					.getLayerMin(), element.getLayerMax(), element
					.isLayerUsed());
		} else {
			layerPanel1.setEnabled(false);
		}
	}

	public void setDomainValues(AbstractListElement element) {
		System.out.println("in DataSetPanel setDomainValues");
		if (element != null && element.getAxes().getXAxis() != null
				&& element.getAxes().getYAxis() != null)
			domainPanel1.setDomainValues(element);
		else
			domainPanel1.setDomainValues(null);
	}

	GridCoverage2D coverage;

//	DefaultMapLayer layer;
	FeatureLayer layer;

	float[][] data;
}
