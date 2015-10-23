package anl.verdi.gis;

//import gov.epa.emvl.MapLines; // 2015 replaced with anl.verdi.plot.gui.VerdiBoundaries;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
import org.geotools.map.FeatureLayer;
//import org.geotools.map.MapLayer;	// GeoTools deprecated the MapLayer class; need to use FeatureLayer, GridCoverageLayer, or GridReaderLayer
// import javax.swing.JFrame;
import org.geotools.swing.JMapFrame;

import anl.verdi.plot.gui.VerdiBoundaries;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.Sizes;
/**
 * @author IE, UNC at Chapel Hill
 */
public class FastTileLayerPanel extends JPanel {
	private static final long serialVersionUID = -1216281930499819824L;
	static final Logger Logger = LogManager.getLogger(FastTileLayerPanel.class.getName());
	private List<VerdiBoundaries> removeLayers = new ArrayList<VerdiBoundaries>();
	private List<VerdiBoundaries> addLayers = new ArrayList<VerdiBoundaries>();
	private List<VerdiBoundaries> layers;
	private Map<VerdiBoundaries, LayerMove> layerMoves = new HashMap<VerdiBoundaries, LayerMove>();
	private FeatureLayer controlLayer;

	enum MoveType {
		UP, DOWN
	}

	// identity is based on the layer,
	// as we only want a single LayerMove in the set
	// per layer.
	private class LayerMove {
		VerdiBoundaries layer;
		int newIndex;
		MoveType type;

		public LayerMove(VerdiBoundaries layer, int newIndex, MoveType type) {
			this.layer = layer;
			this.newIndex = newIndex;
			this.type = type;
		}

		public boolean equals(Object obj) {
			if (obj instanceof LayerMove) {
				return layer.equals(((LayerMove) obj).layer);
			}
			return false;
		}

		public int hashCode() {
			return layer.hashCode();
		}
	}

	private class LayerListRenderer extends DefaultListCellRenderer {
		private static final long serialVersionUID = -3655313193399647298L;

		@Override
		public Component getListCellRendererComponent(JList list, Object value,
		                                              int index, boolean isSelected, boolean cellHasFocus) {
			// going to call javax.swing.DefaultListCellRenderer
			JLabel label = (JLabel) super.getListCellRendererComponent(list,
							value, index, isSelected, cellHasFocus);
//			MapLines layer = (MapLines) value;
			VerdiBoundaries layer = (VerdiBoundaries) value;	// 2015 no longer MapLines class
			label.setText(layer.getFileName());
			return label;
		}

	}

	private class ButtonClickRepeater extends MouseAdapter implements ActionListener {

		private Timer autoRepeatTimer;
		private JButton button;

		public ButtonClickRepeater(JButton button) {
			this.button = button;
			autoRepeatTimer = new javax.swing.Timer(100, this);
			autoRepeatTimer.setInitialDelay(300);
		}

		@Override
		public void mousePressed(MouseEvent e) {
			autoRepeatTimer.start();
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			autoRepeatTimer.stop();
		}

		public void actionPerformed(ActionEvent e) {
			e.setSource(button);
			if (button.isEnabled()) {
				for (ActionListener listener : button.getActionListeners()) {
					listener.actionPerformed(e);
				}
			}
		}
	}

	public FastTileLayerPanel() {
		initComponents();
		DefaultListModel model = new DefaultListModel();
		layerList.setModel(model);
		layerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		moveUpButton.addMouseListener(new ButtonClickRepeater(moveUpButton));
		moveDownButton.addMouseListener(new ButtonClickRepeater(moveDownButton));
	}


	private void removeLayerButtonActionPerformed(ActionEvent e) {
//		Object[] selected = layerList.getSelectedValues();	// JList getSelectedValues is now GetSelectedValuesList and returns List<>
		List selected = layerList.getSelectedValuesList();
		for (Object select : selected) {
			removeLayers.add((VerdiBoundaries) select);
			((DefaultListModel) layerList.getModel()).removeElement(select);
		}
	}
	
	private void addBtnActionPerformed(ActionEvent e) {	// removed for VERDI 1.5.0, reinstated for VERDI 1.6.0
		Logger.debug("in AddBtnActionPerformed");
		FastTileAddLayerWizard wizard = new FastTileAddLayerWizard();
		wizard.setControlLayer(controlLayer);
		JDialog dialog = (JDialog)SwingUtilities.getWindowAncestor(this);
//		VerdiBoundaries layer = wizard.display((JFrame)dialog.getParent(), false);
		VerdiBoundaries layer = wizard.display((JMapFrame)dialog.getParent(), false);

		if (layer != null) {
			Logger.debug("adding layer to layerList");
			((DefaultListModel) layerList.getModel()).add(0, layer);
			addLayers.add(layer);
			layerList.setSelectedIndex(0);
			layerList.scrollRectToVisible(layerList.getCellBounds(0, 0));
		}
	}
	
	private void editLayerButtonPerformed(ActionEvent e) {
		Logger.debug("in FastTileLayerPlot.editLayerButtonPerformed");
		VerdiBoundaries selected = (VerdiBoundaries)layerList.getSelectedValue();
		int index = layerList.getSelectedIndex();
		Logger.debug("got index = " + index);		
		if (selected == null)
			return;
		Logger.debug("selected is not null");		
		if (controlLayer != null && selected.getVerdiStyle().getStyle() != null)
		{
			controlLayer.setStyle(selected.getVerdiStyle().getStyle());
			Logger.debug("just set selected style");
		}

		Logger.debug("ready to instantiate mapFile");
		File mapFile = new File(selected.getFileName());
		Logger.debug("ready to instantiate FastTileAddLayerWizard");
		FastTileAddLayerWizard wizard = new FastTileAddLayerWizard(mapFile, controlLayer, selected, false);
		Logger.debug("ready to start dialog");
		JDialog dialog = (JDialog)SwingUtilities.getWindowAncestor(this);
		Logger.debug("ready to display wizard");
//		VerdiBoundaries layer = wizard.display((JFrame)dialog.getParent(), true);
		VerdiBoundaries layer = wizard.display((JMapFrame)dialog.getParent(), true);
		Logger.debug("have a layer - ready to check it out");		
		if (layer != null && !selected.equals(layer)) {
			Logger.debug("ready to deal with layers");
			((DefaultListModel) layerList.getModel()).remove(index);
			((DefaultListModel) layerList.getModel()).add(index, layer);
			layers.remove(selected);
			addLayers.remove(selected);
			layerMoves.remove(selected);
			removeLayers.add(selected);
			addLayers.add(layer);
			layerList.setSelectedIndex(index);
			layerList.scrollRectToVisible(layerList.getCellBounds(index, index));
			Logger.debug("done dealing with layers, ready to return from FastTileLayerPlot.editLayerButtonPerformed");
			return;
		}
		Logger.debug("returning without dealing with layers, from FastTileLayerPlot.editLayerButtonPerformed");
	}

	public void setContext(List<VerdiBoundaries> layers) {
		this.layers = layers;
		DefaultListModel model = new DefaultListModel();
		
		for (VerdiBoundaries layer : layers) {
			model.addElement(layer);
		}
		
		layerList.setModel(model);
		layerList.setCellRenderer(new LayerListRenderer());
	}

	private void moveUpButtonActionPerformed(ActionEvent e) {
		Object selected = layerList.getSelectedValue();
		VerdiBoundaries layer = (VerdiBoundaries) selected;
		LayerMove move = layerMoves.get(layer);
		if (move == null) {
			int oldIndex = 0;
			int addIndex = addLayers.indexOf(layer);
			if (addIndex > -1) {
				oldIndex = layers.size() + addIndex;
			} else {
				oldIndex = layers.indexOf(layer);
			}
			move = new LayerMove(layer, oldIndex + 1, MoveType.UP);
			layerMoves.put(layer, move);
		} else {
			move.newIndex++;
		}

		int index = layerList.getSelectedIndex();
		DefaultListModel tModel = (DefaultListModel) layerList.getModel();
		Object obj = tModel.remove(index);
		int index1 = index - 1;
		tModel.add(index1, obj);
		layerList.setSelectedIndex(index1);
		layerList.scrollRectToVisible(layerList.getCellBounds(index1, index1));
	}

	private void moveDownButtonActionPerformed(ActionEvent e) {
		Object selected = layerList.getSelectedValue();
		VerdiBoundaries layer = (VerdiBoundaries) selected;
		LayerMove move = layerMoves.get(layer);
		if (move == null) {
			int oldIndex = 0;
			int addIndex = addLayers.indexOf(layer);
			if (addIndex > -1) {
				oldIndex = layers.size() + addIndex;
			} else {
				oldIndex = layers.indexOf(layer);
			}
			move = new LayerMove(layer, oldIndex - 1, MoveType.UP);
			layerMoves.put(layer, move);
		} else {
			move.newIndex--;
		}

		int index = layerList.getSelectedIndex();
		DefaultListModel tModel = (DefaultListModel) layerList.getModel();
		Object obj = tModel.remove(index);
		int index1 = index + 1;
		tModel.add(index1, obj);
		layerList.setSelectedIndex(index1);
		layerList.scrollRectToVisible(layerList.getCellBounds(index1, index1));
	}

	public void commit() {
		for (VerdiBoundaries item : removeLayers) {
			if (layers.contains(item))
				layers.remove(item);
		}

		DefaultListModel model = (DefaultListModel) layerList.getModel();
		int size = model.getSize();
		
		for (int i = 0; i < size; i++) {
			VerdiBoundaries obj = (VerdiBoundaries)model.getElementAt(i);
			int index = layers.indexOf(obj);
			
			if (index != -1)
				layers.remove(index);
				
			layers.add(i, obj);
		}
	}

	private void layerListValueChanged(ListSelectionEvent e) {
		if (layerList.getSelectedValue() != null)
			editLayerButton.setEnabled(true);
		
		if (layerList.isSelectionEmpty())
			editLayerButton.setEnabled(false);
		
		if (layerList.getSelectedIndex() == 0) {
			moveUpButton.setEnabled(false);
		} else {
			moveUpButton.setEnabled(true);
		}
		if (layerList.getSelectedIndex() == layerList.getModel().getSize() - 1) {
			moveDownButton.setEnabled(false);
		} else {
			moveDownButton.setEnabled(true);
		}
	}

	public void setControlLayer(FeatureLayer layer) {
		this.controlLayer = layer;
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		// Generated using JFormDesigner non-commercial license
		scrollPane1 = new JScrollPane();
		layerList = new JList();
		addBtn = new JButton();
		moveUpButton = new JButton();
		moveDownButton = new JButton();
		removeLayerButton = new JButton();
		editLayerButton = new JButton();
		CellConstraints cc = new CellConstraints();

		//======== this ========
		// 2014
		ColumnSpec aColumnSpec = new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW);
		RowSpec aRowSpec = new RowSpec(RowSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW);
		RowSpec bRowSpec = new RowSpec(RowSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW);
		setLayout(new FormLayout(
						new ColumnSpec[]{
										aColumnSpec,
										FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
										FormFactory.DEFAULT_COLSPEC
						},
						new RowSpec[]{
										aRowSpec,
										FormFactory.LINE_GAP_ROWSPEC,
										FormFactory.DEFAULT_ROWSPEC,
										FormFactory.LINE_GAP_ROWSPEC,
										FormFactory.DEFAULT_ROWSPEC,
										FormFactory.LINE_GAP_ROWSPEC,
										FormFactory.DEFAULT_ROWSPEC,
										FormFactory.LINE_GAP_ROWSPEC,
										FormFactory.DEFAULT_ROWSPEC,
										FormFactory.LINE_GAP_ROWSPEC,
										FormFactory.DEFAULT_ROWSPEC,
										FormFactory.LINE_GAP_ROWSPEC,
										bRowSpec
						}));
//		setLayout(new FormLayout(
//				new ColumnSpec[]{
//								new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW),
//								FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
//								FormFactory.DEFAULT_COLSPEC
//				},
//				new RowSpec[]{
//								new RowSpec(RowSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW),
//								FormFactory.LINE_GAP_ROWSPEC,
//								FormFactory.DEFAULT_ROWSPEC,
//								FormFactory.LINE_GAP_ROWSPEC,
//								FormFactory.DEFAULT_ROWSPEC,
//								FormFactory.LINE_GAP_ROWSPEC,
//								FormFactory.DEFAULT_ROWSPEC,
//								FormFactory.LINE_GAP_ROWSPEC,
//								FormFactory.DEFAULT_ROWSPEC,
//								FormFactory.LINE_GAP_ROWSPEC,
//								FormFactory.DEFAULT_ROWSPEC,
//								FormFactory.LINE_GAP_ROWSPEC,
//								new RowSpec(RowSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW)
//				}));

		//======== scrollPane1 ========
		{

			//---- layerList ----
			layerList.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					layerListValueChanged(e);
				}
			});
			scrollPane1.setViewportView(layerList);
		}
		add(scrollPane1, cc.xywh(1, 1, 1, 13));

		//---- addBtn ----
		addBtn.setText("Add Layer");	// 2014 removed button to avoid problems with Simphony
										// 2015 trying to put it back in or will need to replace it
		addBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addBtnActionPerformed(e);
			}
		});
		add(addBtn, cc.xy(3, 3));

		//---- moveUpButton ----
		moveUpButton.setText("Move Up");
		moveUpButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				moveUpButtonActionPerformed(e);
			}
		});
		add(moveUpButton, cc.xy(3, 5));

		//---- moveDownButton ----
		moveDownButton.setText("Move Down");
		moveDownButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				moveDownButtonActionPerformed(e);
			}
		});
		add(moveDownButton, cc.xy(3, 7));

		//---- removeLayerButton ----
		removeLayerButton.setText("Remove Layer");
		removeLayerButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeLayerButtonActionPerformed(e);
			}
		});
		add(removeLayerButton, cc.xy(3, 9));
		
		//---- editLayerButton ----		// 2014 removed Edit Layer button from interface - causes crash by Simphony
										// 2015 putting back in
		editLayerButton.setText("Edit Layer");
		editLayerButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editLayerButtonPerformed(e);
			}
		});
		add(editLayerButton, cc.xy(3, 11));
		editLayerButton.setEnabled(false);
		
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	// Generated using JFormDesigner non-commercial license
	private JScrollPane scrollPane1;
	private JList layerList;
	private JButton addBtn;
	private JButton moveUpButton;
	private JButton moveDownButton;
	private JButton removeLayerButton;
	private JButton editLayerButton;
	// JFormDesigner - End of variables declaration //GEN-END:variables

	public static void main(String[] args) {
//		JFrame frame = new JFrame();
//		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JMapFrame frame = new JMapFrame();
		frame.setDefaultCloseOperation(JMapFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		frame.setSize(500, 500);
		frame.add(new FastTileLayerPanel(), BorderLayout.CENTER);
		frame.setVisible(true);
	}

}
