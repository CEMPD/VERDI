package anl.verdi.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionListener;

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
 * @author User #2
 */
public class AddFormulaPanel extends JPanel implements FormulaEditor {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4713729303896867669L;
	private FormulaElementCreator creator;

	public AddFormulaPanel() {
		System.out.println("in AddFormulaPanel default constructor");
		initComponents();
		initializeButtons();
		fldFormula.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				String formula = fldFormula.getText().trim();
				if (formula.length() > 0)
					addToList(formula);
			}
		});

		fldFormula.addMouseListener(new FocusClickFix());
		btnAdd.addMouseListener(new FocusClickFix());
		btnDelete.addMouseListener(new FocusClickFix());
	}

	public JList getList() {
		return formulaList;
	}

	public void setFormulaListModel(FormulaListModel model) {
		formulaList.setModel(model);
		model.setSelectionModel(formulaList.getSelectionModel());
	}

	private void initializeButtons() {
		btnDelete.setMaximumSize(new Dimension(16, 16));
		btnDelete.setPreferredSize(btnDelete.getMaximumSize());
		btnDelete.setMinimumSize(btnDelete.getMaximumSize());

		btnDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				int index = formulaList.getSelectedIndex();
				if (index > -1) {
					FormulaListModel listModel = ((FormulaListModel) formulaList
							.getModel());
					listModel.removeFormulaAt(index);
					if (listModel.getSize() - 1 >= index)
						formulaList.setSelectedIndex(index);
					else
						formulaList.setSelectedIndex(-1);
				}
			}
		});

		btnAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				String formula = fldFormula.getText().trim();
				if (formula.length() > 0)
					addToList(formula);
			}
		});
	}

	public void setCreator(FormulaElementCreator creator) {
		this.creator = creator;
	}

	public void add(String text) {
		String temp = fldFormula.getText();
		fldFormula.setText(temp == null ? text : temp + " " + text);
	}

	private void addToList(String strFormula) {
		FormulaListElement element = creator.create(strFormula);
		if (element != null) {
			FormulaListModel model = (FormulaListModel) formulaList.getModel();
			int index = model.addFormula(element);
			formulaList.setSelectedIndex(index);
		}
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		// Generated using JFormDesigner non-commercial license
		btnDelete = new JButton();
		scrollPane1 = new JScrollPane();
		formulaList = new JList();
		fldFormula = new JTextField();
		btnAdd = new JButton();
		CellConstraints cc = new CellConstraints();

		// ======== this ========
		setBorder(new TitledBorder("Formulas"));
		// 2014
		ColumnSpec aColumnSpec = new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW);
		RowSpec aRowSpec = new RowSpec(RowSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW);
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
				aColumnSpec,
				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC },
				new RowSpec[] {
						FormFactory.DEFAULT_ROWSPEC,
						FormFactory.LINE_GAP_ROWSPEC,
						aRowSpec,
						FormFactory.LINE_GAP_ROWSPEC,
						FormFactory.DEFAULT_ROWSPEC }));
//		setLayout(new FormLayout(new ColumnSpec[] {
//				FormFactory.DEFAULT_COLSPEC,
//				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
//				new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT,
//						FormSpec.DEFAULT_GROW),
//				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
//				FormFactory.DEFAULT_COLSPEC },
//				new RowSpec[] {
//						FormFactory.DEFAULT_ROWSPEC,
//						FormFactory.LINE_GAP_ROWSPEC,
//						new RowSpec(RowSpec.FILL, Sizes.DEFAULT,
//								FormSpec.DEFAULT_GROW),
//						FormFactory.LINE_GAP_ROWSPEC,
//						FormFactory.DEFAULT_ROWSPEC }));

		// 2014 set up file names
		String verdiHome = Tools.getVerdiHome();		// 2014 new method for reading in an image file
		String separator = "/";		// use forward slash only for constructor ImageIcon(String filename);
		String pathName = verdiHome + separator + "plugins" + separator + "core" + separator + "icons"
				 + separator;
		

		// ---- btnDelete ----
		String fileMinus = new String(pathName + "Minus.png");
//		btnDelete.setIcon(new ImageIcon(getClass().getResource("/minus.png")));
		btnDelete.setIcon(new ImageIcon(fileMinus));
		btnDelete.setToolTipText("Delete formula");
		add(btnDelete, cc.xy(1, 1));

		// ======== scrollPane1 ========
		{
			scrollPane1.setViewportView(formulaList);
		}
		add(scrollPane1, cc.xywh(1, 3, 5, 1));
		add(fldFormula, cc.xywh(1, 5, 3, 1));

		// ---- btnAdd ----
		btnAdd.setText("Add");
		add(btnAdd, cc.xy(5, 5));

		formulaList.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent evt) {
				if (evt.isPopupTrigger()) {
					JPopupMenu menu = new JPopupMenu();
					menu.add(addVariableToEditor).setEnabled(true);
					menu.show(evt.getComponent(), evt.getX(), evt.getY());
				}
			}

			public void mouseReleased(MouseEvent evt) {
				if (evt.isPopupTrigger()) {
					JPopupMenu menu = new JPopupMenu();
					menu.add(addVariableToEditor).setEnabled(true);
					menu.show(evt.getComponent(), evt.getX(), evt.getY());
				}
			}

			public void mouseClicked(MouseEvent evt) {
				//
			}
		});

		// JFormDesigner - End of component initialization
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	// Generated using JFormDesigner non-commercial license
	private JButton btnDelete;
	private JScrollPane scrollPane1;
	private JList formulaList;
	private JTextField fldFormula;
	private JButton btnAdd;
	// JFormDesigner - End of variables declaration //GEN-END:variables

	private Action addVariableToEditor = new AbstractAction(
			"Add Variable(s) to Formula Editor") {
		private static final long serialVersionUID = -9127227616777471655L;

		public void actionPerformed(ActionEvent e) {
			String formula = "";
//			Object[] elemnts = formulaList.getSelectedValues();	// getSelectedValues deprecated for JList
			List elemnts = formulaList.getSelectedValuesList();
			for (Object elemnt : elemnts) {
				formula += ((FormulaListElement) elemnt).getFormula() + "  ";
			}

			fldFormula.setText(fldFormula.getText() + " " + formula.trim());
			fldFormula.requestFocusInWindow();
		}
	};

	public void addFormulaSelectionListener(ListSelectionListener listener) {
		formulaList.addListSelectionListener(listener);
	}

}
