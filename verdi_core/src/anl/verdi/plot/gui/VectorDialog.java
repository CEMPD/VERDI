package anl.verdi.plot.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import anl.verdi.gui.FormulaListElement;
import anl.verdi.util.CompatibilityChecker;
import anl.verdi.util.DateRange;

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
public class VectorDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7058720616089690083L;
	private FormulaListElement xElement, yElement, tileElement;
	private java.util.List<FormulaListElement> items = new ArrayList<FormulaListElement>();
	private CompatibilityChecker checker;
	private DateRange range;

	public VectorDialog(Frame owner) {
		super(owner);
		initComponents();
		addListeners();
	}

	public VectorDialog(Dialog owner) {
		super(owner);
		initComponents();
		addListeners();
	}

	public FormulaListElement getXElement() {
		return xElement;
	}

	public FormulaListElement getYElement() {
		return yElement;
	}

	public FormulaListElement getTileElement() {
		return tileElement;
	}

	public DateRange getResolvedDateRange() {
		return range;
	}

	public void init(java.util.List<FormulaListElement> elements, CompatibilityChecker checker) {
		this.checker = checker;
		DefaultListModel model = new DefaultListModel();
		for (FormulaListElement item : elements) {
			model.addElement(item);
		}
		formulaList.setModel(model);
		formulaList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		okButton.setEnabled(false);
	}

	private void checkCompatibility() {
		findAxisFormulas();
		statusLbl.setText(" ");
		if (items.size() == 1 || (items.size() > 1 && (xElement == null || yElement == null))) {
			statusLbl.setText("You must select a horizontal and vertical component");
			okButton.setEnabled(false);
			return;
		}

		range = checker.isCompatible(items);

		if (range == null) {
			statusLbl.setText("Selected formulas are not compatible with each other");
		}

		okButton.setEnabled(range != null);
	}

	private void addListeners() {

		clearTileBtn.addActionListener(new ActionListener() {
		  public void actionPerformed(ActionEvent evt) {
		    fldTile.setText("");
			  checkCompatibility();
		  }
		});
		
		formulaList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					btnX.setEnabled(formulaList.getSelectedIndex() != -1);
					btnY.setEnabled(formulaList.getSelectedIndex() != -1);
					tileBtn.setEnabled(formulaList.getSelectedIndex() != -1);
				}
			}
		});

		btnX.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				FormulaListElement item = (FormulaListElement) formulaList.getSelectedValue();
				String val = item.toString();
				fldX.setText(val);
				checkCompatibility();
			}
		});

		btnY.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				String val = formulaList.getSelectedValue().toString();
				fldY.setText(val);
				checkCompatibility();
			}
		});

		tileBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				String val = formulaList.getSelectedValue().toString();
				fldTile.setText(val);
				checkCompatibility();
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
				tileElement = null;
				dispose();
			}
		});
	}

	private void findAxisFormulas() {
		items.clear();
		DefaultListModel model = (DefaultListModel) formulaList.getModel();
		xElement = null;
		yElement = null;
		tileElement = null;
		for (int i = 0; i < model.size(); i++) {
			FormulaListElement element = (FormulaListElement) model.getElementAt(i);
			if (element.getFormula().equals(fldX.getText())) {
				xElement = element;
			}

			if (element.getFormula().equals(fldY.getText())) {
				yElement = element;
			}

			if (element.getFormula().equals(fldTile.getText())) {
				tileElement = element;
			}
		}
		if (xElement != null) items.add(xElement);
		if (yElement != null) items.add(yElement);
		if (tileElement != null) items.add(tileElement);
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
		fldX = new JTextField();
		label2 = new JLabel();
		fldY = new JTextField();
		label3 = new JLabel();
		fldTile = new JTextField();
		clearTileBtn = new JButton();
		panel1 = new JPanel();
		btnX = new JButton();
		btnY = new JButton();
		tileBtn = new JButton();
		separator1 = compFactory.createSeparator("");
		buttonBar = new JPanel();
		okButton = new JButton();
		cancelButton = new JButton();
		CellConstraints cc = new CellConstraints();

		//======== this ========
		setModal(true);
		setTitle("Vector Plot");
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
						bColumnSpec[0],
						FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
						FormFactory.DEFAULT_COLSPEC
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
//							new ColumnSpec("max(min;75dlu)"),
//							FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
//							FormFactory.DEFAULT_COLSPEC
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
//							FormFactory.DEFAULT_ROWSPEC
//						}));

				//---- statusLbl ----
				statusLbl.setForeground(Color.red);
				statusLbl.setText(" ");
				contentPanel.add(statusLbl, cc.xywh(1, 1, 9, 1));
				contentPanel.add(separator2, cc.xywh(1, 3, 9, 1));

				//======== scrollPane1 ========
				{
					scrollPane1.setViewportView(formulaList);
				}
				contentPanel.add(scrollPane1, cc.xywh(1, 5, 5, 11));

				//---- label1 ----
				label1.setText("Horizontal Component:");
				contentPanel.add(label1, cc.xywh(8, 5, 2, 1));

				//---- fldX ----
				fldX.setEditable(false);
				contentPanel.add(fldX, cc.xywh(9, 7, 3, 1));

				//---- label2 ----
				label2.setText("Vertical Component:");
				contentPanel.add(label2, cc.xywh(8, 9, 2, 1));

				//---- fldY ----
				fldY.setEditable(false);
				contentPanel.add(fldY, cc.xywh(9, 11, 3, 1));

				//---- label3 ----
				label3.setText("Tile (Optional):");
				contentPanel.add(label3, cc.xywh(8, 13, 2, 1));

				//---- fldTile ----
				fldTile.setBackground(new Color(224, 223, 227));
				fldTile.setEditable(false);
				contentPanel.add(fldTile, cc.xy(9, 15));

				//---- clearTileBtn ----
				clearTileBtn.setText("-");
				clearTileBtn.setToolTipText("Clear Tile");
				contentPanel.add(clearTileBtn, cc.xy(11, 15));

				//======== panel1 ========
				{
					// 2014
					RowSpec[] aRowSpec = RowSpec.decodeSpecs("default");
					panel1.setLayout(new FormLayout(
						new ColumnSpec[] {
							FormFactory.DEFAULT_COLSPEC,
							FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
							FormFactory.DEFAULT_COLSPEC,
							FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
							FormFactory.DEFAULT_COLSPEC
						},
						aRowSpec));
//					panel1.setLayout(new FormLayout(
//							new ColumnSpec[] {
//								FormFactory.DEFAULT_COLSPEC,
//								FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
//								FormFactory.DEFAULT_COLSPEC,
//								FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
//								FormFactory.DEFAULT_COLSPEC
//							},
//							RowSpec.decodeSpecs("default")));

					//---- btnX ----
					btnX.setText("Horiz.");
					panel1.add(btnX, cc.xy(1, 1));

					//---- btnY ----
					btnY.setText("Vert.");
					panel1.add(btnY, cc.xy(3, 1));

					//---- tileBtn ----
					tileBtn.setText("Tile");
					panel1.add(tileBtn, cc.xy(5, 1));
				}
				contentPanel.add(panel1, cc.xywh(1, 17, 5, 1));
				contentPanel.add(separator1, cc.xywh(1, 19, 11, 1));
			}
			dialogPane.add(contentPanel, BorderLayout.CENTER);

			//======== buttonBar ========
			{
				buttonBar.setBorder(Borders.BUTTON_BAR_GAP_BORDER);
				// 2014
				RowSpec[] bRowSpec = RowSpec.decodeSpecs("pref");
				buttonBar.setLayout(new FormLayout(
					new ColumnSpec[] {
						FormFactory.GLUE_COLSPEC,
						FormFactory.BUTTON_COLSPEC,
						FormFactory.RELATED_GAP_COLSPEC,
						FormFactory.BUTTON_COLSPEC
					},
					bRowSpec));
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
	private JLabel statusLbl;
	private JComponent separator2;
	private JScrollPane scrollPane1;
	private JList formulaList;
	private JLabel label1;
	private JTextField fldX;
	private JLabel label2;
	private JTextField fldY;
	private JLabel label3;
	private JTextField fldTile;
	private JButton clearTileBtn;
	private JPanel panel1;
	private JButton btnX;
	private JButton btnY;
	private JButton tileBtn;
	private JComponent separator1;
	private JPanel buttonBar;
	private JButton okButton;
	private JButton cancelButton;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
