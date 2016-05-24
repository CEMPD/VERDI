package anl.verdi.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import anl.verdi.core.VerdiConstants;

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
public class VerdiListPanel extends JPanel {
	private static final long serialVersionUID = 3993278178349959927L;
	private List<Object> removeItems = new ArrayList<Object>();
	private List<Object> addItems = new ArrayList<Object>();
	private List<Object> items;
	private String objLabel;
	private List<Object> addItem = new ArrayList<Object>();

	public VerdiListPanel(String label) {
		objLabel = label;
		initComponents();
		DefaultListModel model = new DefaultListModel();
		objectList.setModel(model);
		objectList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		moveUpButton.addMouseListener(new ButtonClickRepeater(moveUpButton));
		moveDownButton.addMouseListener(new ButtonClickRepeater(moveDownButton));
	}
	
	public void setAddItem(Object toAdd) {
		int selected = -1;
		DefaultListModel model = (DefaultListModel) objectList.getModel();
		
		for (int i = 0; i < model.getSize(); i++) {
			Object obj = model.getElementAt(i);
			
			if (toAdd.equals(obj))
				selected = i;
		}
		
		updateButtons(toAdd, selected);
	}

	public void editItem(Object toAdd) {
		int selected = objectList.getSelectedIndex();
		DefaultListModel model = (DefaultListModel) objectList.getModel();
		
		for (int i = 0; i < model.getSize(); i++) {
			Object obj = model.getElementAt(i);
			
			if (toAdd.equals(obj)) {
				model.remove(i); //some other fields get updated even though they are 'equal'
				model.add(i, toAdd);
			}
		}
		
		updateButtons(toAdd, selected);
	}
	
	private void updateButtons(Object toAdd, int selected) {
		if (selected >= 0) {
			objectList.setSelectedIndex(selected);
			objectList.scrollRectToVisible(objectList.getCellBounds(selected, selected));
		}
		
		if (toAdd != null && addItems.contains(toAdd)) {
			toAdd = null;
		}
		
		if (addItem.size() == 1)
			addItem.remove(0);
		
		if (toAdd != null) 
			addItem.add(toAdd);
			
		layerListValueChanged(null);
	}
	
	public void setList(List<Object> listItems) {
		items = listItems;
		addItems.addAll(listItems);
		DefaultListModel model = new DefaultListModel();
		
		for (Object item : listItems) {
			model.addElement(item);
		}
		
		objectList.setModel(model);
		objectList.setCellRenderer(new LayerListRenderer());
		firePropertyChange(VerdiConstants.VERDI_LIST_OBJECT_NUMBER, null, model.getSize());
	}
	
	public void commit() {
		for (Object item : removeItems) {
			if (items.contains(item))
				items.remove(item);
		}

		DefaultListModel model = (DefaultListModel) objectList.getModel();
		int size = model.getSize();
		
		for (int i = 0; i < size; i++) {
			Object obj = model.getElementAt(i);
			int index = items.indexOf(obj);
			
			if (index != -1)
				items.remove(index);
				
			items.add(i, obj);
		}

	}

	private class LayerListRenderer extends DefaultListCellRenderer {
		private static final long serialVersionUID = 8701689203846051085L;

		@Override
		public Component getListCellRendererComponent(JList list, Object value,
		                                              int index, boolean isSelected, boolean cellHasFocus) {
			JLabel label = (JLabel) super.getListCellRendererComponent(list,
							value, index, isSelected, cellHasFocus);
			label.setText(value.toString());
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

//	private void removeLayerButtonActionPerformed(ActionEvent e) {
//		Object[] selected = objectList.getSelectedValues();
//		for (Object select : selected) {
//			removeItems.add(select);
//			addItems.remove(select);
//			((DefaultListModel) objectList.getModel()).removeElement(select);
//			layerListValueChanged(null);
//			firePropertyChange(VerdiConstants.VERDI_LIST_OBJECT_NUMBER, null, objectList.getModel().getSize());
//		}
//	}
	private void removeLayerButtonActionPerformed(ActionEvent e) {
		List selected = objectList.getSelectedValuesList();
		for (Object select : selected) {
			removeItems.add(select);
			addItems.remove(select);
			((DefaultListModel) objectList.getModel()).removeElement(select);
			layerListValueChanged(null);
			firePropertyChange(VerdiConstants.VERDI_LIST_OBJECT_NUMBER, null, objectList.getModel().getSize());
		}
	}
	
	private void addBtnActionPerformed(ActionEvent e) {
		if (addItem.size() == 1) {
			Object toAdd = addItem.get(0);
			((DefaultListModel) objectList.getModel()).add(0, toAdd);
			addItems.add(0, toAdd);
			
			if (addItem.size() == 1)
				addItem.remove(0);
			
			objectList.setSelectedIndex(0);
			objectList.scrollRectToVisible(objectList.getCellBounds(0, 0));
			layerListValueChanged(null);
			firePropertyChange(VerdiConstants.VERDI_LIST_OBJECT_NUMBER, null, objectList.getModel().getSize());
		}
	}
	
	private void moveUpButtonActionPerformed(ActionEvent e) {
		int index = objectList.getSelectedIndex();
		DefaultListModel tModel = (DefaultListModel) objectList.getModel();
		Object obj = tModel.remove(index);
		int index1 = index - 1;
		tModel.add(index1, obj);
		objectList.setSelectedIndex(index1);
		objectList.scrollRectToVisible(objectList.getCellBounds(index1, index1));
	}

	private void moveDownButtonActionPerformed(ActionEvent e) {
		int index = objectList.getSelectedIndex();
		DefaultListModel tModel = (DefaultListModel) objectList.getModel();
		Object obj = tModel.remove(index);
		int index1 = index + 1;
		tModel.add(index1, obj);
		objectList.setSelectedIndex(index1);
		objectList.scrollRectToVisible(objectList.getCellBounds(index1, index1));
	}

	private void layerListValueChanged(ListSelectionEvent e) {
		if (addItem.size() ==  1)
			addBtn.setEnabled(true);
		
		if (addItem.size() == 0)
			addBtn.setEnabled(false);
		
		if (objectList.getSelectedIndex() >= 0)
			removeLayerButton.setEnabled(true);
		
		if (objectList.getSelectedIndex() < 0)
			removeLayerButton.setEnabled(false);
		
		if (objectList.getSelectedIndex() <= 0) {
			moveUpButton.setEnabled(false);
		} else {
			moveUpButton.setEnabled(true);
		}
		if (objectList.getSelectedIndex() == objectList.getModel().getSize() - 1
				|| objectList.getSelectedIndex() < 0) {
			moveDownButton.setEnabled(false);
		} else {
			moveDownButton.setEnabled(true);
		}
	}

	private void initComponents() {
		scrollPane1 = new JScrollPane();
		objectList = new JList();
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
			objectList.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					layerListValueChanged(e);
					
					if (!e.getValueIsAdjusting()) 
						if (objectList.getSelectedIndex() != -1)
							VerdiListPanel.this.firePropertyChange(VerdiConstants.VERDI_LIST_OBJECT, null, objectList.getSelectedValue());
				}
			});
			
			scrollPane1.setViewportView(objectList);
		}
		add(scrollPane1, cc.xywh(1, 1, 1, 11));

		//---- addBtn ----
		addBtn.setText("Add " + objLabel);
		addBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addBtnActionPerformed(e);
			}
		});
		addBtn.setEnabled(false);
		add(addBtn, cc.xy(3, 3));

		//---- moveUpButton ----
		moveUpButton.setText("Move Up");
		moveUpButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				moveUpButtonActionPerformed(e);
			}
		});
		moveUpButton.setEnabled(false);
		add(moveUpButton, cc.xy(3, 5));

		//---- moveDownButton ----
		moveDownButton.setText("Move Down");
		moveDownButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				moveDownButtonActionPerformed(e);
			}
		});
		moveDownButton.setEnabled(false);
		add(moveDownButton, cc.xy(3, 7));

		//---- removeLayerButton ----
		removeLayerButton.setText("Remove " + objLabel);
		removeLayerButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeLayerButtonActionPerformed(e);
			}
		});
		removeLayerButton.setEnabled(false);
		add(removeLayerButton, cc.xy(3, 9));
		
		// //GEN-END:initComponents
	}

	private JScrollPane scrollPane1;
	private JList objectList;
	private JButton addBtn;
	private JButton moveUpButton;
	private JButton moveDownButton;
	private JButton removeLayerButton;

	public static void main(String[] args) {
		VerdiListPanel test = new VerdiListPanel("Variable");
		List<Object> vars = Arrays.asList(new Object[]{"O3[1]", "O38[1]"});
		test.setList(vars);
		test.setAddItem("New Variable");
		
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		frame.setSize(500, 500);
		frame.add(test, BorderLayout.CENTER);
		frame.setVisible(true);
	}

}
