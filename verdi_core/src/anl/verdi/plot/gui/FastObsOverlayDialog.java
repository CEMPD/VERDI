package anl.verdi.plot.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import anl.verdi.core.VerdiConstants;
import anl.verdi.data.Variable;
import anl.verdi.gis.OverlayObject;
import anl.verdi.gui.VerdiListPanel;
import anl.verdi.plot.gui.ObsAnnotation.Symbol;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.Sizes;
import anl.verdi.util.Tools;

public class FastObsOverlayDialog extends ObsOverlayDialog implements
		PropertyChangeListener {
	private static final long serialVersionUID = -2908156973292666178L;
	private boolean okButtonOn = false;
	
	public FastObsOverlayDialog(Dialog owner) {
		super(owner);
		// TODO Auto-generated constructor stub
	}
	
	public FastObsOverlayDialog(Frame owner) {
		super(owner);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String name = evt.getPropertyName();
		OverlayObject overlayObj = null;
		
		if (name.equals(VerdiConstants.VERDI_LIST_OBJECT)) {
			overlayObj = (OverlayObject)evt.getNewValue();
			
			if (overlayObj == null)
				return;
			
			varList.setSelectedValue(overlayObj.getVariable(), true);
			strokeSpn.setValue(overlayObj.getStrokeSize());
			shapeSpn.setValue(overlayObj.getShapeSize());
			symList.setSelectedItem(overlayObj.getSymbol());
		}
		
		if (!okButtonOn && name.equals(VerdiConstants.VERDI_LIST_OBJECT_NUMBER)) {
			int num = Integer.parseInt(evt.getNewValue().toString());
			okButtonOn = num > 0;
			okButton.setEnabled(okButtonOn);
		}
	}
	
	public void addListeners() {
		okButton.setEnabled(okButtonOn);
		okButton.addActionListener(new ActionListener() {
		  public void actionPerformed(ActionEvent evt) {
			  canceled = false;
			  overlayListPanel.commit();
			  dispose();
		  }
		});

		cancelButton.addActionListener(new ActionListener() {
		  public void actionPerformed(ActionEvent evt) {
		    dispose();
		  }
		});
		
		varList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					if (varList.getSelectedIndex() != -1) {
						if (checker.isCompatible((Variable)varList.getSelectedValue())) {
							setOverlayObject();
							statusLbl.setText("");
						} else {
							statusLbl.setText("Observation variable must be compatible with tile plot");
						}
					} 
				}
			}
		});
	}
	
	private void setOverlayObject() {
		selectedVar = (Variable)varList.getSelectedValue();
		shapeSize = ((Integer)shapeSpn.getValue()).intValue();
		strokeSize = ((Integer)strokeSpn.getValue()).intValue();
		symbol = getSelectedSymbol();
		overlayListPanel.setAddItem(new OverlayObject(selectedVar, symbol, strokeSize, shapeSize));
	}
	
	private void editOverlayObject() {
		selectedVar = (Variable)varList.getSelectedValue();
		shapeSize = ((Integer)shapeSpn.getValue()).intValue();
		strokeSize = ((Integer)strokeSpn.getValue()).intValue();
		symbol = getSelectedSymbol();
		overlayListPanel.editItem(new OverlayObject(selectedVar, symbol, strokeSize, shapeSize));
	}
	
	@SuppressWarnings("unchecked")
	public void setObservationData(List obsData) {
		overlayListPanel.setList(obsData);
	}
	
	public boolean showLegend() {
		return showLegend.isSelected();
	}
	
	public void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner non-commercial license
		DefaultComponentFactory compFactory = DefaultComponentFactory.getInstance();
		dialogPane = new JPanel();
		contentPanel = new JPanel();
		statusLbl = new JLabel();
		separator1 = compFactory.createSeparator("Observation Details");
		separator2 = compFactory.createSeparator("Selected Observation Variable(s)");
		scrollPane1 = new JScrollPane();
		varList = new JList();
		label1 = new JLabel();
		showLegend = new JCheckBox("Show symbols legend", true);
		strokeSpn = new JSpinner();
		strokeSpn.addChangeListener(new ChangeListener(){

			@Override
			public void stateChanged(ChangeEvent e) {
				editOverlayObject();
			}
			
		});
		label2 = new JLabel();
		shapeSpn = new JSpinner();
		shapeSpn.addChangeListener(new ChangeListener(){

			@Override
			public void stateChanged(ChangeEvent e) {
				editOverlayObject();
			}
			
		});
		buttonBar = new JPanel();
		okButton = new JButton();
		cancelButton = new JButton();
		overlayListPanel = new VerdiListPanel("Variable");
		overlayListPanel.addPropertyChangeListener(VerdiConstants.VERDI_LIST_OBJECT, this);
		overlayListPanel.addPropertyChangeListener(VerdiConstants.VERDI_LIST_OBJECT_NUMBER, this);
		String pathName = Tools.getIconsDir();
		String fileCircle = new String(pathName + "circle.png");
		String fileDiamond = new String(pathName + "diamond.png");
		String fileSquare = new String(pathName + "square.png");
		String fileStar = new String(pathName + "star.png");
		String fileSun = new String(pathName + "sun.png");
		String fileTriangle = new String(pathName + "triangle.png");
		
		names = new Symbol[] {Symbol.CIRCLE, /*Symbol.DIAMOND,*/ Symbol.SQUARE, Symbol.STAR, /*Symbol.SUN,*/ Symbol.TRIANGLE};
		symbols = new Icon[] {
				new ImageIcon(fileCircle),
				//new ImageIcon(fileDiamond),
				new ImageIcon(fileSquare),
				new ImageIcon(fileStar),
				//new ImageIcon(fileSun),
				new ImageIcon(fileTriangle)
//				new ImageIcon(getClass().getResource("/circle.png")),
//				new ImageIcon(getClass().getResource("/diamond.png")),
//				new ImageIcon(getClass().getResource("/square.png")),
//				new ImageIcon(getClass().getResource("/star.png")),
//				new ImageIcon(getClass().getResource("/sun.png")),
//				new ImageIcon(getClass().getResource("/triangle.png"))
		};
		symList = new JComboBox(names);
		
		label3 = new JLabel();
		symPanel = new JPanel(new BorderLayout());
		selectedImg = new JLabel();
		selectedImg.setIcon(symbols[0]);
		
		symPanel.add(selectedImg, BorderLayout.LINE_START);
		symPanel.add(symList, BorderLayout.LINE_END);
		
		symList.addActionListener(new AbstractAction(){
			private static final long serialVersionUID = -5121239000837587642L;

			@Override
			public void actionPerformed(ActionEvent e) {
				int index = ((JComboBox)e.getSource()).getSelectedIndex();
				selectedImg.setIcon(symbols[index]);
				editOverlayObject();
			}
			
		});
		
		CellConstraints cc = new CellConstraints();

		//======== this ========
		setTitle("Observation Dialog");
		setModal(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		//======== dialogPane ========
		{
			dialogPane.setBorder(Borders.DIALOG_BORDER);
			dialogPane.setLayout(new BorderLayout());

			//======== contentPanel ========
			{
				// 2014
				ColumnSpec aColumnSpec = new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, 0.6);
				ColumnSpec bColumnSpec = new ColumnSpec(ColumnSpec.FILL, Sizes.PREFERRED, 0.5);
				contentPanel.setLayout(new FormLayout(
					new ColumnSpec[] {
						aColumnSpec,
						FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
						FormFactory.RELATED_GAP_COLSPEC,
						bColumnSpec
					},
					new RowSpec[] {
						FormFactory.PREF_ROWSPEC,
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
						FormFactory.DEFAULT_ROWSPEC,
						FormFactory.LINE_GAP_ROWSPEC,
						FormFactory.DEFAULT_ROWSPEC,
						FormFactory.LINE_GAP_ROWSPEC,
						FormFactory.DEFAULT_ROWSPEC,
						FormFactory.LINE_GAP_ROWSPEC,
						FormFactory.DEFAULT_ROWSPEC,
						FormFactory.LINE_GAP_ROWSPEC,
						FormFactory.DEFAULT_ROWSPEC
					}));
//				contentPanel.setLayout(new FormLayout(
//						new ColumnSpec[] {
//							new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, 0.6),
//							FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
//							FormFactory.RELATED_GAP_COLSPEC,
//							new ColumnSpec(ColumnSpec.FILL, Sizes.PREFERRED, 0.5)
//						},
//						new RowSpec[] {
//							FormFactory.PREF_ROWSPEC,
//							FormFactory.LINE_GAP_ROWSPEC,
//							FormFactory.DEFAULT_ROWSPEC,
//							FormFactory.LINE_GAP_ROWSPEC,
//							FormFactory.DEFAULT_ROWSPEC,
//							FormFactory.LINE_GAP_ROWSPEC,
//							FormFactory.DEFAULT_ROWSPEC,
//							FormFactory.LINE_GAP_ROWSPEC,
//							FormFactory.DEFAULT_ROWSPEC,
//							FormFactory.LINE_GAP_ROWSPEC,
//							FormFactory.DEFAULT_ROWSPEC,
//							FormFactory.LINE_GAP_ROWSPEC,
//							FormFactory.DEFAULT_ROWSPEC,
//							FormFactory.LINE_GAP_ROWSPEC,
//							FormFactory.DEFAULT_ROWSPEC,
//							FormFactory.LINE_GAP_ROWSPEC,
//							FormFactory.DEFAULT_ROWSPEC,
//							FormFactory.LINE_GAP_ROWSPEC,
//							FormFactory.DEFAULT_ROWSPEC,
//							FormFactory.LINE_GAP_ROWSPEC,
//							FormFactory.DEFAULT_ROWSPEC
//						}));

				//---- statusLbl ----
				statusLbl.setForeground(Color.red);
				contentPanel.add(statusLbl, cc.xywh(1, 1, 4, 1));
				contentPanel.add(separator1, cc.xywh(1, 3, 4, 1));

				//======== scrollPane1 ========
				{

					//---- varList ----
					varList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					scrollPane1.setViewportView(varList);
				}
				contentPanel.add(scrollPane1, cc.xywh(1, 5, 1, 11));

				//---- label1 ----
				label1.setText("Stroke Size:");
				contentPanel.add(label1, cc.xywh(3, 5, 2, 1));

				//---- strokeSpn ----
				strokeSpn.setModel(new SpinnerNumberModel(1, 1, null, 1));
				contentPanel.add(strokeSpn, cc.xy(4, 7));

				//---- label2 ----
				label2.setText("Shape Size:");
				contentPanel.add(label2, cc.xywh(3, 9, 2, 1));
				
				//---- shapeSpn ----
				shapeSpn.setModel(new SpinnerNumberModel(8, 1, null, 1));
				contentPanel.add(shapeSpn, cc.xy(4, 11));
				
				//---- label3 ----
				label3.setText("Symbol:");
				contentPanel.add(label3, cc.xywh(3, 13, 2, 1));

				//---- symPanel ----
				contentPanel.add(symPanel, cc.xy(4, 15));
				
				//---- separator2 ----
				contentPanel.add(separator2, cc.xywh(1, 17, 4, 1));
				
				//---- overlayListPanel ----
				contentPanel.add(overlayListPanel, cc.xywh(1, 19, 4, 1));
				
				//---- showLegend ----
				contentPanel.add(showLegend, cc.xy(1, 21));
			}
			dialogPane.add(contentPanel, BorderLayout.CENTER);

			//======== buttonBar ========
			{
				buttonBar.setBorder(Borders.BUTTON_BAR_GAP_BORDER);
				// 2014
				RowSpec[] aRowSpec = RowSpec.decodeSpecs("pref");
				buttonBar.setLayout(new FormLayout(
					new ColumnSpec[] {
						FormFactory.GLUE_COLSPEC,
						FormFactory.BUTTON_COLSPEC,
						FormFactory.RELATED_GAP_COLSPEC,
						FormFactory.BUTTON_COLSPEC
					},
					aRowSpec));

				//---- okButton ----
				okButton.setText("OK");
				buttonBar.add(okButton, cc.xy(2, 1));

				//---- cancelButton ----
				cancelButton.setText("Cancel");
				buttonBar.add(cancelButton, cc.xy(4, 1));
			}
			dialogPane.add(buttonBar, BorderLayout.SOUTH);
		}
		contentPane.add(dialogPane, BorderLayout.CENTER);
		setSize(520, 520);
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	private VerdiListPanel overlayListPanel;
	protected JComponent separator2;
	private JCheckBox showLegend;

}
