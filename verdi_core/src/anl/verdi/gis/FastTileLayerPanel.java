package anl.verdi.gis;

import gov.epa.emvl.MapLines;

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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.geotools.map.FeatureLayer;
//import org.geotools.map.MapLayer;	// GeoTools deprecated the MapLayer class; need to use FeatureLayer, GridCoverageLayer, or GridReaderLayer

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
	private List<MapLines> removeLayers = new ArrayList<MapLines>();
	private List<MapLines> addLayers = new ArrayList<MapLines>();
	private List<MapLines> layers;
	private Map<MapLines, LayerMove> layerMoves = new HashMap<MapLines, LayerMove>();
	private FeatureLayer controlLayer;

	enum MoveType {
		UP, DOWN
	}


	// identity is based on the layer,
	// as we only want a single LayerMove in the set
	// per layer.
	private class LayerMove {
		MapLines layer;
		int newIndex;
		MoveType type;		// NOTE: assigned, passed in value, but does not appear to be used

		public LayerMove(MapLines layer, int newIndex, MoveType type) {
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
			JLabel label = (JLabel) super.getListCellRendererComponent(list,
							value, index, isSelected, cellHasFocus);
			MapLines layer = (MapLines) value;
			label.setText(layer.getMapFile());
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
			removeLayers.add((MapLines) select);
			((DefaultListModel) layerList.getModel()).removeElement(select);
		}
	}
	
	private void addBtnActionPerformed(ActionEvent e) {
		FastTileAddLayerWizard wizard = new FastTileAddLayerWizard();
		wizard.setControlLayer(controlLayer);
		JDialog dialog = (JDialog)SwingUtilities.getWindowAncestor(this);
		MapLines layer = wizard.display((JFrame)dialog.getParent(), false);

		if (layer != null) {
			((DefaultListModel) layerList.getModel()).add(0, layer);
			addLayers.add(layer);
			layerList.setSelectedIndex(0);
			layerList.scrollRectToVisible(layerList.getCellBounds(0, 0));
		}
	}
	
	private void editLayerButtonPerformed(ActionEvent e) {
		MapLines selected = (MapLines)layerList.getSelectedValue();
		int index = layerList.getSelectedIndex();
		
		if (selected == null)
			return;
		
		if (controlLayer != null && selected.getStyle() != null)
			controlLayer.setStyle(selected.getStyle());
		
		File mapFile = new File(selected.getMapFile());
		FastTileAddLayerWizard wizard = new FastTileAddLayerWizard(mapFile, controlLayer, selected, false);
		JDialog dialog = (JDialog)SwingUtilities.getWindowAncestor(this);
		MapLines layer = wizard.display((JFrame)dialog.getParent(), true);
		
		if (layer != null && !selected.equals(layer)) {
			((DefaultListModel) layerList.getModel()).remove(index);
			((DefaultListModel) layerList.getModel()).add(index, layer);
			layers.remove(selected);
			addLayers.remove(selected);
			layerMoves.remove(selected);
			removeLayers.add(selected);
			addLayers.add(layer);
			layerList.setSelectedIndex(index);
			layerList.scrollRectToVisible(layerList.getCellBounds(index, index));
			return;
		}
	}

	public void setContext(List<MapLines> layers) {
		this.layers = layers;
		DefaultListModel model = new DefaultListModel();
		
		for (MapLines layer : layers) {
			model.addElement(layer);
		}
		
		layerList.setModel(model);
		layerList.setCellRenderer(new LayerListRenderer());
	}

	private void moveUpButtonActionPerformed(ActionEvent e) {
		Object selected = layerList.getSelectedValue();
		MapLines layer = (MapLines) selected;
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
		MapLines layer = (MapLines) selected;
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
		for (MapLines item : removeLayers) {
			if (layers.contains(item))
				layers.remove(item);
		}

		DefaultListModel model = (DefaultListModel) layerList.getModel();
		int size = model.getSize();
		
		for (int i = 0; i < size; i++) {
			MapLines obj = (MapLines)model.getElementAt(i);
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
		addBtn.setText("Add Layer");
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
		
		//---- removeLayerButton ----
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
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		frame.setSize(500, 500);
		frame.add(new FastTileLayerPanel(), BorderLayout.CENTER);
		frame.setVisible(true);
	}

}
