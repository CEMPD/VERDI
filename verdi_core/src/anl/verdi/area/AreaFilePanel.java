/*
 * Created by JFormDesigner on Tue Feb 03 13:34:45 CST 2009
 */

package anl.verdi.area;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
//import org.geotools.map.MapContext;			// replaced for GeoTools v10
import org.geotools.map.MapContent;

import anl.verdi.area.target.OpenTargetWindow;
import anl.verdi.area.target.Target;
import anl.verdi.core.Project;
import anl.verdi.core.VerdiApplication;
import anl.verdi.plot.gui.FastAreaTilePlot;
import anl.verdi.plot.gui.Plot;
import anl.verdi.util.FocusClickFix;
import anl.verdi.util.Tools;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.Sizes;

/**
 * @author Mary Ann Bitz
 * @author Argonne National Lab
 */
public class AreaFilePanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2092173824541825554L;
	static final Logger Logger = LogManager.getLogger(AreaFilePanel.class.getName());
	private Project project;
	private MapContent context;	// replaced MapContext, DefaultMapContext with MapContent for GeoTools v10

	private VerdiApplication verdiApp;
	public AreaFilePanel() {
		Logger.debug("in AreaFilePanel default constructor");
		initComponents();
	}
	public AreaFilePanel(Project project, MapContent context, VerdiApplication app) // replaced MapContext, DefaultMapContext with MapContent for GeoTools v10
	{
		Logger.debug("in AreaFilePanel constructor with Project, MapContent, VerdiApplication");
		this.project = project;
		this.context = context;
		this.verdiApp=app;
		initComponents();
		areaList.setModel(new AreasModel());
		areaFileList.setModel(project.getAreaFiles());
		areaFileList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		initListeners();
		initializeButtons();
		initializePopups();
		addOpenAreaFileAction(openAreaFileAction);
	}
	private void initializePopups() {
		Logger.debug("in AreaFilePanel.initializePopups");
		areaFileList.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent evt) {
				if (evt.isPopupTrigger()) {
					showAreaFileListPopup(evt);
				}
			}

			public void mouseReleased(MouseEvent evt) {
				if (evt.isPopupTrigger()) {
					showAreaFileListPopup(evt);
				}
			}
		});
	}

	private Action openAreaFileAction = new AbstractAction() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 5389738453732436847L;

		public void actionPerformed(ActionEvent e) {
			OpenTargetWindow win = new OpenTargetWindow(verdiApp.getGui().getFrame(),AreaFilePanel.this,"Open Area File",false);

		}
	};
	private Action addAreaFileAction = new AbstractAction("Add Area File") {
		/**
		 * 
		 */
		private static final long serialVersionUID = -5718833501923207816L;

		public void actionPerformed(ActionEvent e) {
			btnAdd.doClick();
		}
	};

	private Action deleteAreaFileAction = new AbstractAction("Delete Area File") {
		/**
		 * 
		 */
		private static final long serialVersionUID = 3609491332807172053L;

		public void actionPerformed(ActionEvent e) {
			btnDelete.doClick();
		}
	};

	private Action areaFilePropertiesAction = new AbstractAction("Properties") {
		/**
		 * 
		 */
		private static final long serialVersionUID = -2185780103182292124L;

		public void actionPerformed(ActionEvent e) {
			Logger.debug("properties");
		}
	};

	private void showAreaFileListPopup(MouseEvent evt) {
		Logger.debug("in AreaFilePanel showAreaFileListPopup");
		boolean enable = areaFileList.getSelectedIndex() != -1;
		JPopupMenu menu = new JPopupMenu();
		menu.add(addAreaFileAction);
		menu.add(deleteAreaFileAction).setEnabled(enable);
		menu.add(areaFilePropertiesAction).setEnabled(false);

		menu.show(evt.getComponent(), evt.getX(), evt.getY());
	}
	private void initListeners() {
		Logger.debug("in AreaFilePanel initListeners");
		ListSelectionListener listSelectionListener = new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent arg0) {
				int index = areaFileList.getSelectedIndex();
				if (index >= 0) {
					AreaFileListElement element = (AreaFileListElement) areaFileList.getModel().getElementAt(index);
					// update other values on screen
					//((AreaFileListModel) areaList.getModel()).addVariablesForDataset(element.getDataset());
					//					setTimeValues(element);
					//					setLayerValues(element);
					//					setDomainValues(element);
				} 
				//				else {
				//					((VariablesModel) variableList.getModel()).addVariablesForDataset(null);
				//					setTimeValues(null);
				//					setLayerValues(null);
				//					setDomainValues(null);
				//				}
			}
		};

		addPanelListeners();
		areaFileList.addListSelectionListener(listSelectionListener);
	}
	public void updateTilePlots(boolean newAreas){
		Logger.debug("in AreaFilePanel updateTilePlots");
		// redraw any AreaTilePlots to show selected ones
		java.util.List<Plot> plots=verdiApp.getGui().getDisplayedPlots();
		for(Plot plot:plots){
			if(plot instanceof FastAreaTilePlot){
				if(newAreas)((FastAreaTilePlot)plot).recalculateAreas();
				((FastAreaTilePlot)plot).validate();
				((FastAreaTilePlot)plot).repaint();
			}
		}
	}
	private void addPanelListeners() {
		Logger.debug("in AreaFilePanel addPanelListeners");
		ListSelectionListener listSelectionListener = new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent arg0) {
				// mark the selected targets
				if( arg0.getValueIsAdjusting() ) {
					return;
				}

				//				Object[] values = areaList.getSelectedValues();	// replacing getSelectedValues()
				List values = areaList.getSelectedValuesList();
				Target.setCurrentSelectedTargets(values);

				// redraw any AreaTilePlots to show selected ones
				updateTilePlots(false);

			}
		};
		areaList.addListSelectionListener(listSelectionListener );
	}

	private void initializeButtons() {
		Logger.debug("in AreaFilePanel.initializeButtons");
		Icon icon = btnAdd.getIcon();
		btnAdd.setMaximumSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
		btnAdd.setPreferredSize(btnAdd.getMaximumSize());
		btnAdd.setMinimumSize(btnAdd.getMaximumSize());

		icon = btnDelete.getIcon();
		btnDelete.setMaximumSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
		btnDelete.setPreferredSize(btnDelete.getMaximumSize());
		btnDelete.setMinimumSize(btnDelete.getMaximumSize());

		btnDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				//				Object[] objs = areaFileList.getSelectedValues();	// replacing getSelectedValues()

				List objs = areaFileList.getSelectedValuesList();
				//				if (objs.length > 0) {
				if (objs.size() > 0) {
					for (Object obj : objs) {
						AreaFileListModel listModel = ((AreaFileListModel) areaFileList.getModel());
						AreaFileListElement item = (AreaFileListElement) obj;

						listModel.removeAreaFile(item);
						removeAreasFor(item.getAreaFile());

						//project.removeFormulas(formulas);
					}
				}
			}
		});

		btnAdd.addMouseListener(new FocusClickFix());
		btnDelete.addMouseListener(new FocusClickFix());
	}
	void removeAreasFor(AreaFile areaFile){
		Logger.debug("in AreaFilePanel.removeAreasFor");
		// remove Targets for that source
		ArrayList list = Target.getTargetsForSource(areaFile);
		// remove from the lower panel
		if(list!=null){
			Target.getTargets().removeAll(list);
			addAreas(Target.getTargets());
		}
		Target.removeSource(areaFile);

	}
	private void initComponents() {
		Logger.debug("in AreaFilePanel.initComponents");
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner non-commercial license
		areaFilesPanel = new JPanel();
		areaFiles = new JPanel();
		btnAdd = new JButton();
		btnDelete = new JButton();
		scrollPaneAreaFiles = new JScrollPane();
		areaFileList = new JList();
		areasPanel = new JPanel();
		scrollPane6 = new JScrollPane();
		areaList = new JList();
		CellConstraints cc = new CellConstraints();

		//======== this ========
		setLayout(new FormLayout(
				"pref:grow",
				"fill:default:grow"));

		//======== areaFilesPanel ========
		{
			areaFilesPanel.setBorder(null);
			// 2014 - underlying jgoodies class changed
			ColumnSpec[] aColumnSpec = ColumnSpec.decodeSpecs("pref:grow");
			RowSpec[] aRowSpec = RowSpec.decodeSpecs("fill:max(pref;125dlu):grow");
			areaFilesPanel.setLayout(new FormLayout(
					aColumnSpec,
					new RowSpec[] {
							new RowSpec(Sizes.dluY(108)),
							new RowSpec(Sizes.dluY(92)),
							FormFactory.PREF_ROWSPEC,
							FormFactory.LINE_GAP_ROWSPEC,
							FormFactory.PREF_ROWSPEC,
							FormFactory.LINE_GAP_ROWSPEC,
							aRowSpec[0]
					}));

//			areaFilesPanel.setLayout(new FormLayout(
//					ColumnSpec.decodeSpecs("pref:grow"),
//					new RowSpec[] {
//						new RowSpec(Sizes.dluY(108)),
//						new RowSpec(Sizes.dluY(92)),
//						FormFactory.PREF_ROWSPEC,
//						FormFactory.LINE_GAP_ROWSPEC,
//						FormFactory.PREF_ROWSPEC,
//						FormFactory.LINE_GAP_ROWSPEC,
//						new RowSpec("fill:max(pref;125dlu):grow")
//					}));

			//======== areaFiles ========
			{
				areaFiles.setBorder(new TitledBorder("Area Files"));
				// 2014
				ColumnSpec bColumnSpec = new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW);
				RowSpec bRowSpec = new RowSpec(RowSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW);
				areaFiles.setLayout(new FormLayout(
						new ColumnSpec[] {
								FormFactory.DEFAULT_COLSPEC,
								FormFactory.RELATED_GAP_COLSPEC,
								FormFactory.DEFAULT_COLSPEC,
								bColumnSpec
						},
						new RowSpec[] {
								FormFactory.DEFAULT_ROWSPEC,
								FormFactory.RELATED_GAP_ROWSPEC,
								bRowSpec
						}));
//				areaFiles.setLayout(new FormLayout(
//						new ColumnSpec[] {
//								FormFactory.DEFAULT_COLSPEC,
//								FormFactory.RELATED_GAP_COLSPEC,
//								FormFactory.DEFAULT_COLSPEC,
//								new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW)
//						},
//						new RowSpec[] {
//								FormFactory.DEFAULT_ROWSPEC,
//								FormFactory.RELATED_GAP_ROWSPEC,
//								new RowSpec(RowSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW)
//						}));

				// 2014 set up file names
				String pathName = Tools.getIconsDir();

				//---- btnAdd ----
				String filePlus = new String(pathName + "plus.png");
				//				btnAdd.setIcon(new ImageIcon(getClass().getResource("/plus.png")));
				btnAdd.setIcon(new ImageIcon(filePlus));
				btnAdd.setToolTipText("Add Area File");
				areaFiles.add(btnAdd, cc.xy(1, 1));

				//---- btnDelete ----
				String fileMinus = new String(pathName + "Minus.png");
				//				btnDelete.setIcon(new ImageIcon(getClass().getResource("/minus.png")));
				btnDelete.setIcon(new ImageIcon(fileMinus));
				btnDelete.setToolTipText("Delete Area File");
				areaFiles.add(btnDelete, cc.xy(3, 1));

				//======== scrollPaneAreaFiles ========
				{

					//---- areaFileList ----
					areaFileList.setSelectedIndex(0);
					areaFileList.setMaximumSize(new Dimension(300, 100));
					areaFileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					areaFileList.setPrototypeCellValue("RADM_CONC_!");
					scrollPaneAreaFiles.setViewportView(areaFileList);
				}
				areaFiles.add(scrollPaneAreaFiles, cc.xywh(1, 3, 4, 1));
			}
			areaFilesPanel.add(areaFiles, cc.xy(1, 1));

			//======== areasPanel ========
			{
				areasPanel.setBorder(new TitledBorder("Areas"));
				areasPanel.setLayout(new BorderLayout());

				//======== scrollPane6 ========
				{

					//---- areaList ----
					areaList.setPrototypeCellValue("O3[1]");
					scrollPane6.setViewportView(areaList);
				}
				areasPanel.add(scrollPane6, BorderLayout.CENTER);
			}
			areaFilesPanel.add(areasPanel, cc.xywh(1, 2, 1, 6));
		}
		add(areaFilesPanel, cc.xy(1, 1));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner non-commercial license
	private JPanel areaFilesPanel;
	private JPanel areaFiles;
	private JButton btnAdd;
	private JButton btnDelete;
	private JScrollPane scrollPaneAreaFiles;
	private JList areaFileList;
	private JPanel areasPanel;
	private JScrollPane scrollPane6;
	private JList areaList;

	// JFormDesigner - End of variables declaration  //GEN-END:variables
	public void loadAreaFile(AreaFile areaFile) {
		Logger.debug("in AreaFilePanel.loadAreaFile");
		AreaFileListModel model = (AreaFileListModel) areaFileList.getModel();
		int index = model.addAreaFile(areaFile);
		areaFileList.setSelectedIndex(index);
		areaFileList.scrollRectToVisible(areaFileList.getCellBounds(index, index));
	}

	public void addOpenAreaFileAction(Action openAreaFileAction) {
		Logger.debug("in AreaFilePanel addOpenAreaFileAction");
		btnAdd.addActionListener(openAreaFileAction);
	}

	class AreasModel extends AbstractListModel {
		/**
		 * 
		 */
		private static final long serialVersionUID = -6797170699759577892L;
		ArrayList<Area> areas = new ArrayList<Area>();

		public Object getElementAt(int index) {
			Logger.info("in AreaFilePanel AreasModel getElementAt");
			return areas.get(index);
		}

		public Area getAreaAt(int index) {
			Logger.info("in AreaFilePanel AreasModel getAreaAt");
			return areas.get(index);
		}

		public int getSize() {
			Logger.info("in AreaFilePanel AreasModel getSize");
			if (areas == null)
				return 0;
			return areas.size();
		}

		public void addAreas(ArrayList<Area> list) {
			Logger.debug("in AreaFilePanel AreasModel addAreas");
			// create the area file and add it to the model
			int size = areas.size();

			// remove the old ones
			areas.clear();
			fireIntervalRemoved(this, 0, size);

			if (list == null)return;

			for (Area area : list) {
				if (list != null) {
					// add to the model
					int index = areas.size();
					areas.add(area);
					fireIntervalAdded(this, index, index);

				}
			}
		}
	}

	public void addAreas(ArrayList<Area> list) {
		Logger.debug("in AreaFilePanel addAreas");
		((AreasModel)areaList.getModel()).addAreas(list);
	}

	public void areasSelected(ArrayList selections) {
		Logger.debug("in AreaFilePanel areasSelected");
		//change the set to be empty
		areaList.clearSelection();

		//	change the set to highlight the targets
		// make a list of the targets

		ArrayList allTargets=Target.getTargets();
		int[] targets = new int[selections.size()];
		for (int i = 0; i < selections.size(); i++) {
			targets[i] = allTargets.indexOf(selections.get(i));
		}
		areaList.setSelectedIndices(targets);
	}


}
