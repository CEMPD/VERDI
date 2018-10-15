package anl.verdi.plot.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import anl.verdi.gui.FormulaListElement;

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
public class ScatterDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7989407856308796565L;
	private FormulaListElement xElement, yElement;

	public ScatterDialog(Frame owner) {
		super(owner);
		initComponents();
		setTitle("Scatter Plot");
	}

	public ScatterDialog(Dialog owner) {
		super(owner);
		initComponents();
		setTitle("Scatter Plot");
	}

	public FormulaListElement getXElement() {
		return xElement;
	}

	public FormulaListElement getYElement() {
		return yElement;
	}

	public void setFormulas(java.util.List<FormulaListElement> elements) {
		DefaultListModel model = new DefaultListModel();
		for (FormulaListElement item : elements) {
			model.addElement(item);
		}
		formulaList.setModel(model);
		formulaList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

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
		    String val = ((FormulaListElement)formulaList.getSelectedValue()).getFormula();
			  fldX.setText(val);
		  }
		});

		btnY.addActionListener(new ActionListener() {
		  public void actionPerformed(ActionEvent evt) {
		    String val = ((FormulaListElement)formulaList.getSelectedValue()).getFormula();
			  fldY.setText(val);
		  }
		});

		okButton.addActionListener(new ActionListener() {
		  public void actionPerformed(ActionEvent evt) {
		    if (fldX.getText().length() > 0 && fldY.getText().length() > 0) {
			    findAxisFormulas();
			    dispose();
		    } else {
			    JOptionPane.showMessageDialog(ScatterDialog.this, "You must select a formula for both the X and Y axes");
		    }
		  }
		});

		cancelButton.addActionListener(new ActionListener() {
		  public void actionPerformed(ActionEvent evt) {
		    dispose();
		  }
		});
	}

    public void setVisible(boolean b) {
    	Point p = getLocation();
    	setLocation(0, p.y);
        super.setVisible(b);
    }
	private void findAxisFormulas() {
		DefaultListModel model = (DefaultListModel) formulaList.getModel();
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

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner non-commercial license
		DefaultComponentFactory compFactory = DefaultComponentFactory.getInstance();
		dialogPane = new JPanel();
		contentPanel = new JPanel();
		separator2 = compFactory.createSeparator("Select Formulas for Axes");
		scrollPane1 = new JScrollPane();
		formulaList = new JList();
		label1 = new JLabel();
		fldX = new JTextField();
		label2 = new JLabel();
		fldY = new JTextField();
		btnX = new JButton();
		btnY = new JButton();
		separator1 = compFactory.createSeparator("");
		buttonBar = new JPanel();
		okButton = new JButton();
		cancelButton = new JButton();
		CellConstraints cc = new CellConstraints();

		//======== this ========
		setModal(true);
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
				ColumnSpec[] bColumnSpec = ColumnSpec.decodeSpecs("max(min;75dlu)");
				contentPanel.setLayout(new FormLayout(
					new ColumnSpec[] {
						FormFactory.PREF_COLSPEC,
						FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
						FormFactory.PREF_COLSPEC,
						FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
						aColumnSpec[0],
						FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
						FormFactory.RELATED_GAP_COLSPEC,
						FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
						bColumnSpec[0]
					},
					new RowSpec[] {
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
//							FormFactory.PREF_COLSPEC,
//							FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
//							FormFactory.PREF_COLSPEC,
//							FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
//							new ColumnSpec("min(min;5dlu):grow"),
//							FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
//							FormFactory.RELATED_GAP_COLSPEC,
//							FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
//							new ColumnSpec("max(min;75dlu)")
//						},
//						new RowSpec[] {
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
				contentPanel.add(separator2, cc.xywh(1, 1, 9, 1));

				//======== scrollPane1 ========
				{
					scrollPane1.setViewportView(formulaList);
				}
				contentPanel.add(scrollPane1, cc.xywh(1, 3, 5, 9));

				//---- label1 ----
				label1.setText("X-Axis:");
				contentPanel.add(label1, cc.xywh(8, 3, 2, 1));

				//---- fldX ----
				fldX.setEditable(false);
				contentPanel.add(fldX, cc.xy(9, 5));

				//---- label2 ----
				label2.setText("Y-Axis:");
				contentPanel.add(label2, cc.xywh(8, 7, 2, 1));

				//---- fldY ----
				fldY.setEditable(false);
				contentPanel.add(fldY, cc.xy(9, 9));

				//---- btnX ----
				btnX.setText("X-Axis");
				contentPanel.add(btnX, cc.xy(1, 13));

				//---- btnY ----
				btnY.setText("Y-Axis");
				contentPanel.add(btnY, cc.xy(3, 13));
				contentPanel.add(separator1, cc.xywh(1, 15, 9, 1));
			}
			dialogPane.add(contentPanel, BorderLayout.CENTER);

			//======== buttonBar ========
			{
				buttonBar.setBorder(Borders.BUTTON_BAR_GAP_BORDER);
				RowSpec[] aRowSpec = RowSpec.decodeSpecs("pref");
				buttonBar.setLayout(new FormLayout(
					new ColumnSpec[] {
						FormFactory.GLUE_COLSPEC,
						FormFactory.BUTTON_COLSPEC,
						FormFactory.RELATED_GAP_COLSPEC,
						FormFactory.BUTTON_COLSPEC
					},
					aRowSpec));
//				buttonBar.setLayout(new FormLayout(
//						new ColumnSpec[] {
//							FormFactory.GLUE_COLSPEC,
//							FormFactory.BUTTON_COLSPEC,
//							FormFactory.RELATED_GAP_COLSPEC,
//							FormFactory.BUTTON_COLSPEC
//						},
//						RowSpec.decodeSpecs("pref")));

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
		pack();
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner non-commercial license
	private JPanel dialogPane;
	private JPanel contentPanel;
	private JComponent separator2;
	private JScrollPane scrollPane1;
	private JList formulaList;
	private JLabel label1;
	private JTextField fldX;
	private JLabel label2;
	private JTextField fldY;
	private JButton btnX;
	private JButton btnY;
	private JComponent separator1;
	private JPanel buttonBar;
	private JButton okButton;
	private JButton cancelButton;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
