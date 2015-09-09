/*
 * Created by JFormDesigner on Wed Apr 25 09:21:24 CDT 2007
 */

package anl.verdi.gis;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

//import org.geotools.map.MapLayer;	// GeoTools deprecated the MapLayer class; need to use FeatureLayer, GridCoverageLayer, or GridReaderLayer
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
//import org.geotools.map.MapContext;	// GeoTools replaced MapContext with MapContent
import org.geotools.map.MapContent;
// import javax.swing.JFrame;
import org.geotools.swing.JMapFrame;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.Sizes;

/**
 * @author User #2
 */
public class LayerEditorPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2863275464550102150L;
	private List<FeatureLayer> removeLayers = new ArrayList<FeatureLayer>();
	private List<FeatureLayer> addLayers = new ArrayList<FeatureLayer>();

	private Map<FeatureLayer, LayerMove> layerMoves = new HashMap<FeatureLayer, LayerMove>();

	private MapContent context;

	enum MoveType {
		UP, DOWN
	}


	// identity is based on the layer,
	// as we only want a single LayerMove in the set
	// per layer.
	private class LayerMove {
		FeatureLayer layer;
		int newIndex;
		MoveType type;

		public LayerMove(FeatureLayer layer, int newIndex, MoveType type) {
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

		/**
		 * 
		 */
		private static final long serialVersionUID = -83963190498745795L;

		@Override
		public Component getListCellRendererComponent(JList list, Object value,
		                                              int index, boolean isSelected, boolean cellHasFocus) {
			JLabel label = (JLabel) super.getListCellRendererComponent(list,
							value, index, isSelected, cellHasFocus);
			FeatureLayer layer = (FeatureLayer) value;
			label.setText(layer.getFeatureSource().getSchema().getName().toString());
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

	public LayerEditorPanel() {
		initComponents();
		DefaultListModel model = new DefaultListModel();
		layerList.setModel(model);
		layerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		moveUpButton.addMouseListener(new ButtonClickRepeater(moveUpButton));
		moveDownButton.addMouseListener(new ButtonClickRepeater(moveDownButton));
	}


	private void removeLayerButtonActionPerformed(ActionEvent e) {
//		Object[] selected = layerList.getSelectedValues();
		List selected = layerList.getSelectedValuesList();
		for (Object select : selected) {
			removeLayers.add((FeatureLayer) select);
			((DefaultListModel) layerList.getModel()).removeElement(select);
		}
	}

	public void setContext(MapContent context) {
		this.context = context;
		DefaultListModel model = new DefaultListModel();
		List<FeatureLayer> layers = new ArrayList<FeatureLayer>();
//		for (FeatureLayer layer : context.getLayers()) {
		for (Layer layer : context.layers()) {
			layers.add((FeatureLayer)layer);
		}
//		Collections.sort(layers, new Comparator<FeatureLayer>() {	// 2014 appears to not be called
//			public int compare(FeatureLayer o1, FeatureLayer o2) {
//				int index1 = LayerEditorPanel.this.context.indexOf(o1);	// 2014 no equivalent to .indexOf found
//				int index2 = LayerEditorPanel.this.context.indexOf(o2);
//				// do reverse sort because higher context index means drawn
//				// on top
//				return index2 < index1 ? -1 : index2 == index1 ? 0 : 1;
//			}
//		});
		for (FeatureLayer layer : layers) {
			model.addElement(layer);
		}
		layerList.setModel(model);
		layerList.setCellRenderer(new LayerListRenderer());
	}

	private void moveUpButtonActionPerformed(ActionEvent e) {
		Object selected = layerList.getSelectedValue();
		FeatureLayer layer = (FeatureLayer) selected;
		LayerMove move = layerMoves.get(layer);
		if (move == null) {
			int oldIndex = 0;
			int addIndex = addLayers.indexOf(layer);
			if (addIndex > -1) {
//				oldIndex = context.getLayers().length + addIndex;
				oldIndex = context.layers().size() + addIndex;
			} else {
//				oldIndex = context.indexOf(layer);
				oldIndex = context.layers().indexOf(layer);
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
		FeatureLayer layer = (FeatureLayer) selected;
		LayerMove move = layerMoves.get(layer);
		if (move == null) {
			int oldIndex = 0;
			int addIndex = addLayers.indexOf(layer);
			if (addIndex > -1) {
//				oldIndex = context.getLayers().length + addIndex;
				oldIndex = context.layers().size() + addIndex;
			} else {
//				oldIndex = context.indexOf(layer);
				oldIndex = context.layers().indexOf(layer);
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
		for (FeatureLayer layer : addLayers) {
			context.addLayer(layer);
			/*
			LayerMove move = layerMoves.remove(layer);
			if (move != null) {
				move.layer = layer;
				layerMoves.put(layer, move);
			}
			*/
		}

		for (LayerMove move : layerMoves.values()) 
		{
//			int index = context.indexOf(move.layer);
			int index = context.layers().indexOf(move.layer);
			if (index != move.newIndex) 
			{
				// remove it from wherever it is now
//				context.removeLayer(index);
				context.layers().remove(index);
//				context.addLayer(move.newIndex, move.layer);
				context.layers().add(move.newIndex, move.layer);
			}
		}

		for (FeatureLayer layer : removeLayers) {
			context.removeLayer(layer);
		}
	}

	private void layerListValueChanged(ListSelectionEvent e) {
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

	private void addBtnActionPerformed(ActionEvent e) {

		AddLayerWizard wizard = new AddLayerWizard();
		JDialog dialog = (JDialog)SwingUtilities.getWindowAncestor(this);
//		FeatureLayer layer = wizard.display((JFrame)dialog.getParent());
		FeatureLayer layer = wizard.display((JMapFrame)dialog.getParent());

		if (layer != null) {
			((DefaultListModel) layerList.getModel()).add(0, layer);
			addLayers.add(layer);
			layerList.setSelectedIndex(0);
			layerList.scrollRectToVisible(layerList.getCellBounds(0, 0));
		}
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
		add(scrollPane1, cc.xywh(1, 1, 1, 11));

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
	// JFormDesigner - End of variables declaration //GEN-END:variables

	public static void main(String[] args) {
//		JFrame frame = new JFrame();
//		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JMapFrame frame = new JMapFrame();
		frame.setDefaultCloseOperation(JMapFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		frame.setSize(500, 500);
		frame.add(new LayerEditorPanel(), BorderLayout.CENTER);
		frame.setVisible(true);
	}

}
