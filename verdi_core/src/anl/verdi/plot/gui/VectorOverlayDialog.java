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
import java.text.NumberFormat;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.NumberFormatter;

import org.apache.logging.log4j.LogManager;		// 2015
import org.apache.logging.log4j.Logger;			// 2015 replacing System.out.println with logger messages

import anl.verdi.core.VerdiApplication;
import anl.verdi.gui.FormulaListElement;
import anl.verdi.util.VectorOverlayTimeChecker;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;


/**
 * @author User #2
 */
public class VectorOverlayDialog extends JDialog implements PropertyChangeListener {

	/**
	 * 
	 */
	static final Logger Logger = LogManager.getLogger(VerdiApplication.class.getName());
	private static final long serialVersionUID = 3023231401696996595L;
	private FormulaListElement xElement, yElement;
	private VectorOverlayTimeChecker checker;

	public VectorOverlayDialog(Frame owner) {
		super(owner);
		Logger.debug("in VectorOverlayDialog for Frame owner");	// 2015 called as part of vector overlay on fast tile plot
		initComponents();
		addListeners();
	}

	public VectorOverlayDialog(Dialog owner) {
		super(owner);
		Logger.debug("in VectorOverlayDialog for Dialog owner");	// 2015 NOT called as part of vector overlay on fast tile plot
		initComponents();
		addListeners();
	}

	private void addListeners() {

		formulaList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					btnX.setEnabled(formulaList.getSelectedIndex() != -1);
					btnY.setEnabled(formulaList.getSelectedIndex() != -1);
				}
			}
		});

		btnX.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				FormulaListElement item = (FormulaListElement) formulaList.getSelectedValue();
				if(item != null)	// 2015 next line caused NullPointerException
				{
					String val = item.toString();
					fldX.setText(val);
					if (fldY.getText().length() > 0) {
						if (checkCompatibility()) {
							okButton.setEnabled(true);
							statusLbl.setText(" ");
						}
						else {
							statusLbl.setText("Components time and layer range must be compatible with the tile plot.");
						}
					}
					
				}
			}
		});

		btnY.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				String val = formulaList.getSelectedValue().toString();
				fldY.setText(val);
				if (fldX.getText().length() > 0) {
					if (checkCompatibility()) {
						okButton.setEnabled(true);
						statusLbl.setText("");
					}
					else {
						statusLbl.setText("Components time and layer range must be compatible with the tile plot.");
					}
				}
			}
		});

		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (fldX.getText().length() > 0 && fldY.getText().length() > 0) {
					findAxisFormulas();
					dispose();
				}
			}
		});

		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				xElement = null;
				yElement = null;
				dispose();
			}
		});
	}

	private boolean checkCompatibility() {
		findAxisFormulas();
		java.util.List<FormulaListElement> list = new ArrayList<FormulaListElement>();
		list.add(xElement);
		list.add(yElement);

		//return checker.isCompatible(list);
		return true;
	}

	private void findAxisFormulas() {
		//items.clear();
		DefaultListModel model = (DefaultListModel) formulaList.getModel();
		xElement = null;
		yElement = null;
		for (int i = 0; i < model.size(); i++) {
			FormulaListElement element = (FormulaListElement) model.getElementAt(i);
			if (element.getFormula().equals(fldX.getText())) {
				xElement = element;
			}

			if (element.getFormula().equals(fldY.getText())) {
				yElement = element;
			}
		}
	}

	public FormulaListElement getUElement() {
		return xElement;
	}

	public FormulaListElement getVElement() {
		return yElement;
	}
	
	public int getVectorSamplingInc()
	{
		// 2015 return value of Vector Sampling Increment
		// verify value is 1 - 100
		NumberFormatter formatter = (NumberFormatter) fldV.getFormatter();
		if(formatter != null)
		{
			Number value = (Number)fldV.getValue();
			try {
				int inputInt = value.intValue();
				if(inputInt < 1)
				{
					fldV.setValue(new Integer(1));
					return 1;
				}
				else if(inputInt > 100)
				{
					fldV.setValue(new Integer(100));
					return 100;
				}
				else
					return inputInt;
			} catch (Exception e) {
				Logger.error("Invalid entry for Vector Sampling Increment; resetting value to 1.");
				fldV.setValue(new Integer(1));
				return 1;
			}
		}
		else
		{
			// formatter == null so reset value to 1
			fldV.setValue(new Integer(1));
			return 1;
		}
	}

	public void init(java.util.List<FormulaListElement> elements, VectorOverlayTimeChecker checker) {
		this.checker = checker;
		DefaultListModel model = new DefaultListModel();
		for (FormulaListElement item : elements) {
			model.addElement(item);
		}
		formulaList.setModel(model);
		formulaList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		okButton.setEnabled(false);
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner non-commercial license
		DefaultComponentFactory compFactory = DefaultComponentFactory.getInstance();
		dialogPane = new JPanel();
		contentPanel = new JPanel();
		statusLbl = new JLabel();
		separator2 = compFactory.createSeparator("Select Components");
		scrollPane1 = new JScrollPane();
		formulaList = new JList();
		label1 = new JLabel();
		labelV = new JLabel();	// 2015 text "Vector Sampling Increment" 
		fldV = new JFormattedTextField(NumberFormat.getIntegerInstance());	// 2015 field for user-entered input
		fldV.setColumns(3);
		fldX = new JTextField();
		label2 = new JLabel();
		fldY = new JTextField();
//		panel1 = new JPanel();
		btnX = new JButton();
		btnY = new JButton();
		separator1 = compFactory.createSeparator("");
		separatorV = compFactory.createSeparator("");
		buttonBar = new JPanel();
		okButton = new JButton();
		cancelButton = new JButton();
		CellConstraints cc = new CellConstraints();

		//======== this ========
		setModal(true);
		setTitle("Vector Overlay");
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		//======== dialogPane ========
		{
			dialogPane.setBorder(Borders.DIALOG_BORDER);
			dialogPane.setLayout(new BorderLayout());

			//======== contentPanel ========
			{
				// 2014
				ColumnSpec[] aColumnSpec = ColumnSpec.decodeSpecs("min(min;5dlu):grow");
				ColumnSpec[] bColumnSpec = ColumnSpec.decodeSpecs("max(min;100dlu)");	// CHANGED 75 TO 100
				contentPanel.setLayout(new FormLayout(
						new ColumnSpec[]{
								FormFactory.PREF_COLSPEC,
								FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
								FormFactory.PREF_COLSPEC,
								FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
								aColumnSpec[0],
								FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
								FormFactory.RELATED_GAP_COLSPEC,
								FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
								bColumnSpec[0],
								FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
								FormFactory.DEFAULT_COLSPEC
						},
						new RowSpec[]{
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

				//---- statusLbl ----
				statusLbl.setForeground(Color.red);
				statusLbl.setText(" ");
				contentPanel.add(statusLbl, cc.xywh(1, 1, 9, 1));
				contentPanel.add(separator2, cc.xywh(1, 3, 9, 1));

				//======== scrollPane1 ========
				{
					scrollPane1.setViewportView(formulaList);
				}
				contentPanel.add(scrollPane1, cc.xywh(1, 5, 5, 8));

				//---- label1 ----
				label1.setText("Horizontal (X) Component:");
				contentPanel.add(label1, cc.xywh(8, 5, 2, 1));

				//---- fldX ----
				fldX.setEditable(false);
				contentPanel.add(fldX, cc.xywh(9, 7, 3, 1));

				//---- label2 ----
				label2.setText("Vertical (Y) Component:");
				contentPanel.add(label2, cc.xywh(8, 9, 2, 1));

				//---- fldY ----
				fldY.setEditable(false);
				contentPanel.add(fldY, cc.xywh(9, 11, 3, 1));

//				//======== panel1 ========
//				{
//					// 2014
//					RowSpec[] aRowSpec = RowSpec.decodeSpecs("default");
//					panel1.setLayout(new FormLayout(
//							new ColumnSpec[]{
//									FormFactory.DEFAULT_COLSPEC,
//									FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
//									FormFactory.DEFAULT_COLSPEC,
//									FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
//									FormFactory.DEFAULT_COLSPEC
//							},
//							new RowSpec[]{
//							FormFactory.PREF_ROWSPEC,
//							FormFactory.LINE_GAP_ROWSPEC,
//							FormFactory.DEFAULT_ROWSPEC,
//							FormFactory.LINE_GAP_ROWSPEC,
//							FormFactory.DEFAULT_ROWSPEC,
//							FormFactory.LINE_GAP_ROWSPEC}
//							));

					//---- btnX ----
					btnX.setText("Horizontal");
					contentPanel.add(btnX, cc.xywh(1, 13, 1, 1));

					//---- btnY ----
					btnY.setText("Vertical");
					contentPanel.add(btnY,cc.xywh(3, 13, 1, 1));
//				}

//				contentPanel.add(panel1, cc.xywh(1, 15, 5, 1));
				contentPanel.add(separator1, cc.xywh(1, 19, 11, 1));
				labelV.setText("Vector Sampling Increment: ");
				contentPanel.add(labelV, cc.xy(1, 21));
				fldV.setEditable(true);
				fldV.setToolTipText("Enter 1 to show all vectors, 5 for every 5th vector, etc.");
				fldV.setSize(20,10);	// trying to make the box larger
				int anInt = 1;
				fldV.setValue(new Integer(anInt)); 	// default value is 1 (show all vectors)
				fldV.addPropertyChangeListener("value", this);
				contentPanel.add(fldV, cc.xywh(3, 21,1,1));	// 2 columns, 1 row
				contentPanel.add(separatorV, cc.xywh(1, 23,11,1));
				pack();
				dialogPane.add(contentPanel, BorderLayout.CENTER);

				//======== buttonBar ========
				{
					buttonBar.setBorder(Borders.BUTTON_BAR_GAP_BORDER);
					// 2014
					RowSpec[] bRowSpec = RowSpec.decodeSpecs("pref");
					buttonBar.setLayout(new FormLayout(
							new ColumnSpec[]{
									FormFactory.GLUE_COLSPEC,
									FormFactory.BUTTON_COLSPEC,
									FormFactory.RELATED_GAP_COLSPEC,
									FormFactory.BUTTON_COLSPEC
							},
							bRowSpec));

					//---- okButton ----
					okButton.setText("OK");
					buttonBar.add(okButton, cc.xy(2, 1));

					//---- cancelButton ----
					cancelButton.setText("Cancel");
					buttonBar.add(cancelButton, cc.xy(4, 1));
				}
				dialogPane.add(buttonBar, BorderLayout.SOUTH);
				pack();
			}
			contentPane.add(dialogPane, BorderLayout.CENTER);
			pack();
			setLocationRelativeTo(getOwner());
			// JFormDesigner - End of component initialization  //GEN-END:initComponents
		}
	}
	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner non-commercial license
	private JPanel dialogPane;
	private JPanel contentPanel;
	private JLabel statusLbl;
	private JComponent separator2;
	private JScrollPane scrollPane1;
	private JList formulaList;
	private JLabel label1;
	private JLabel labelV;		// 2015 "Vector Sampling Increment"
	private JFormattedTextField fldV;	// 2015 integer value for vector sampling increment
	private JComponent separatorV;
	private JTextField fldX;
	private JLabel label2;
	private JTextField fldY;
//	private JPanel panel1;
	private JButton btnX;
	private JButton btnY;
	private JComponent separator1;
	private JPanel buttonBar;
	private JButton okButton;
	private JButton cancelButton;
	// JFormDesigner - End of variables declaration  //GEN-END:variables

	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Object source = evt.getSource();
		if(source == fldV)
			{
			getVectorSamplingInc();	// check for integer and appropriate values only
			}
		// nothing for other fields
	}
}
