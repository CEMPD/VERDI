package anl.verdi.area.target;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;

import anl.verdi.gui.FormulaListElement;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.Sizes;

/**
 * @author User #2
 */
public class FormulaDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5085805480891992298L;
	private String[] selectedFormulas; 

	public FormulaDialog(Frame owner) {
		super(owner);
		initComponents();
		setTitle("Area Information");
	}

	public FormulaDialog(Dialog owner) {
		super(owner);
		initComponents();
		setTitle("Area Information");
	}

	public void setFormulas(java.util.List<FormulaListElement> elements,String var) {
		FormulaListElement currentItem=null;
		DefaultListModel model = new DefaultListModel();
		//		DefaultListModel<FormulaListElement> model = new DefaultListModel<FormulaListElement>();
		for (FormulaListElement item : elements) {
			model.addElement(item);
			if(var!=null&&var.equals(item.getFormula())){
				currentItem=item;
			}
		}
		formulaList.setModel(model);
		formulaList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		if(currentItem!=null)formulaList.setSelectedValue(currentItem, true);

		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				// make a list of the formula selected
				DefaultListModel model = (DefaultListModel) formulaList.getModel();
				//				for (int i = 0; i < model.size(); i++) {		do-nothing loop
				////					FormulaListElement element = (FormulaListElement) model.getElementAt(i);
				//
				//				}
				//				Object[] vals=formulaList.getSelectedValues();
				List vals=formulaList.getSelectedValuesList();
				//				if(vals.length<=0){
				int valSize = vals.size();
				if(valSize<=0){
					// show warning and return
					errorMessage.setText("Select a formula.");
					return;
				}
				//				selectedFormulas=new String[vals.length];
				//				for(int i=0;i<vals.length;i++){
				selectedFormulas=new String[valSize];
				for(int i=0;i<valSize;i++){
					FormulaListElement element = (FormulaListElement)vals.get(i);	//[i];
					selectedFormulas[i]=element.getFormula();
				}
				dispose();
			}
		});

		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				dispose();
			}
		});
	}

	//	private void findAxisFormulas() {
	//		DefaultListModel model = (DefaultListModel) formulaList.getModel();
	//		for (int i = 0; i < model.size(); i++) {
	//			FormulaListElement element = (FormulaListElement) model.getElementAt(i);
	//
	//		}
	//	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner non-commercial license
		DefaultComponentFactory compFactory = DefaultComponentFactory.getInstance();
		dialogPane = new JPanel();
		contentPanel = new JPanel();
		separator2 = compFactory.createSeparator("View Area Values for the Selected Formulas");
		label2 = new JLabel();
		radioButton1 = new JRadioButton();
		radioButton2 = new JRadioButton();
		label1 = new JLabel();
		scrollPane1 = new JScrollPane();
		formulaList = new JList();
		buttonBar = new JPanel();
		separator1 = new JSeparator();
		errorMessage = new JLabel();
		okButton = new JButton();
		cancelButton = new JButton();
		CellConstraints cc = new CellConstraints();

		//======== this ========
		setModal(true);
		setTitle("Area Information");
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		//======== dialogPane ========
		{
			dialogPane.setBorder(Borders.DIALOG_BORDER);
			dialogPane.setMinimumSize(new Dimension(445, 400));
			dialogPane.setLayout(new BorderLayout());

			//======== contentPanel ========
			{
				contentPanel.setPreferredSize(new Dimension(400, 75));
				contentPanel.setMinimumSize(new Dimension(423, 75));

				// 2014 - underlyaing jgoodies class changed
				ColumnSpec[] aColumnSpec = ColumnSpec.decodeSpecs("min(min;5dlu):grow");
				ColumnSpec[] bColumnSpec = ColumnSpec.decodeSpecs("max(min;75dlu)");

				contentPanel.setLayout(new FormLayout(
						new ColumnSpec[] {
								FormFactory.DEFAULT_COLSPEC,
								FormFactory.PREF_COLSPEC,
								aColumnSpec[0],
								FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
								FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
								FormFactory.DEFAULT_COLSPEC,
								FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
								FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
								FormFactory.RELATED_GAP_COLSPEC,
								FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
								bColumnSpec[0],
								FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
								FormFactory.DEFAULT_COLSPEC
						},
						new RowSpec[] {
								FormFactory.DEFAULT_ROWSPEC,
								FormFactory.LINE_GAP_ROWSPEC,
								FormFactory.DEFAULT_ROWSPEC,
								FormFactory.LINE_GAP_ROWSPEC,
								FormFactory.DEFAULT_ROWSPEC
						}));
				//				contentPanel.setLayout(new FormLayout(
				//						new ColumnSpec[] {
				//								FormFactory.DEFAULT_COLSPEC,
				//								FormFactory.PREF_COLSPEC,
				//								new ColumnSpec("min(min;5dlu):grow"),
				//								FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
				//								FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
				//								FormFactory.DEFAULT_COLSPEC,
				//								FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
				//								FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
				//								FormFactory.RELATED_GAP_COLSPEC,
				//								FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
				//								new ColumnSpec("max(min;75dlu)"),
				//								FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
				//								FormFactory.DEFAULT_COLSPEC
				//						},
				//						new RowSpec[] {
				//								FormFactory.DEFAULT_ROWSPEC,
				//								FormFactory.LINE_GAP_ROWSPEC,
				//								FormFactory.DEFAULT_ROWSPEC,
				//								FormFactory.LINE_GAP_ROWSPEC,
				//								FormFactory.DEFAULT_ROWSPEC
				//						}));
				contentPanel.add(separator2, cc.xywh(1, 1, 13, 1));

				//---- label2 ----
				label2.setText("Areas:");
				contentPanel.add(label2, cc.xy(1, 3));

				//---- radioButton1 ----
				radioButton1.setText("Selected");
				radioButton1.setSelected(true);
				contentPanel.add(radioButton1, cc.xy(2, 3));

				//---- radioButton2 ----
				radioButton2.setText("All");
				contentPanel.add(radioButton2, cc.xywh(3, 3, 9, 1));

				//---- label1 ----
				label1.setText("Formulas:");
				contentPanel.add(label1, cc.xy(1, 5));
			}
			dialogPane.add(contentPanel, BorderLayout.NORTH);

			//======== scrollPane1 ========
			{
				scrollPane1.setMinimumSize(new Dimension(300, 400));
				scrollPane1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
				scrollPane1.setPreferredSize(new Dimension(350, 200));
				scrollPane1.setMaximumSize(new Dimension(1000, 1000));

				//---- formulaList ----
				formulaList.setMinimumSize(new Dimension(300, 200));
				formulaList.setMaximumSize(new Dimension(300, 500));
				formulaList.setPreferredSize(new Dimension(300, 200));
				scrollPane1.setViewportView(formulaList);
			}
			dialogPane.add(scrollPane1, BorderLayout.CENTER);

			//======== buttonBar ========
			{
				buttonBar.setBorder(Borders.BUTTON_BAR_GAP_BORDER);
				buttonBar.setPreferredSize(new Dimension(181, 60));
				buttonBar.setLayout(new FormLayout(
						new ColumnSpec[] {
								FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
								FormFactory.DEFAULT_COLSPEC,
								FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
								FormFactory.DEFAULT_COLSPEC,
								FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
								FormFactory.DEFAULT_COLSPEC,
								FormFactory.GLUE_COLSPEC,
								FormFactory.BUTTON_COLSPEC,
								FormFactory.RELATED_GAP_COLSPEC,
								FormFactory.BUTTON_COLSPEC
						},
						new RowSpec[] {
								new RowSpec(Sizes.dluY(15)),
								FormFactory.PREF_ROWSPEC,
								FormFactory.LINE_GAP_ROWSPEC,
								FormFactory.DEFAULT_ROWSPEC
						}));
				buttonBar.add(separator1, cc.xywh(2, 1, 9, 1));

				//---- errorMessage ----
				errorMessage.setFont(errorMessage.getFont().deriveFont(errorMessage.getFont().getStyle() | Font.BOLD));
				buttonBar.add(errorMessage, cc.xywh(2, 2, 6, 1));

				//---- okButton ----
				okButton.setText("OK");
				buttonBar.add(okButton, cc.xy(8, 2));

				//---- cancelButton ----
				cancelButton.setText("Cancel");
				buttonBar.add(cancelButton, cc.xy(10, 2));
			}
			dialogPane.add(buttonBar, BorderLayout.SOUTH);
		}
		contentPane.add(dialogPane, BorderLayout.CENTER);
		setSize(375, 345);
		setLocationRelativeTo(getOwner());

		//---- buttonGroup1 ----
		ButtonGroup buttonGroup1 = new ButtonGroup();
		buttonGroup1.add(radioButton1);
		buttonGroup1.add(radioButton2);
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner non-commercial license
	private JPanel dialogPane;
	private JPanel contentPanel;
	private JComponent separator2;
	private JLabel label2;
	private JRadioButton radioButton1;
	private JRadioButton radioButton2;
	private JLabel label1;
	private JScrollPane scrollPane1;
	private JList<DefaultListModel<FormulaListElement>> formulaList;
	private JPanel buttonBar;
	private JSeparator separator1;
	private JLabel errorMessage;
	private JButton okButton;
	private JButton cancelButton;
	// JFormDesigner - End of variables declaration  //GEN-END:variables

	public String[] getSelectedFormulas() {
		return selectedFormulas;
	}
	public boolean getSelectedAreasOnly(){
		return radioButton1.isSelected();
	}
}
