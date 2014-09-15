package anl.verdi.plot.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import anl.verdi.data.Axes;
import anl.verdi.data.DataFrameAxis;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.DefaultComponentFactory;
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
public class VerticalCrossDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3011350220615294692L;
	private boolean canceled = false;

	public VerticalCrossDialog(Frame owner) {
		super(owner);
		initComponents();
		initButtons();
		setTitle("Vertical Cross Section");
	}

	public VerticalCrossDialog(Dialog owner) {
		super(owner);
		initComponents();
		initButtons();
		setTitle("Vertical Cross Section");
	}

	private void initButtons() {
		ButtonGroup grp = new ButtonGroup();
		grp.add(rbX);
		grp.add(rbY);

		rbX.addActionListener(new ActionListener() {
		  public void actionPerformed(ActionEvent evt) {
		    xSpinner.setEnabled(true);
			  ySpinner.setEnabled(false);
			  lblColumn.setEnabled(true);
			  lblRow.setEnabled(false);
		  }
		});

		rbY.addActionListener(new ActionListener() {
		  public void actionPerformed(ActionEvent evt) {
		    xSpinner.setEnabled(false);
			  ySpinner.setEnabled(true);
			  lblColumn.setEnabled(false);
			  lblRow.setEnabled(true);
		  }
		});

		okButton.addActionListener(new ActionListener() {
		  public void actionPerformed(ActionEvent evt) {
			  canceled = false;
		    VerticalCrossDialog.this.dispose();
		  }
		});

		cancelButton.addActionListener(new ActionListener() {
		  public void actionPerformed(ActionEvent evt) {
			  canceled = true;
		    VerticalCrossDialog.this.dispose();
		  }
		});
		rbX.setSelected(true);
		ySpinner.setEnabled(false);
		lblRow.setEnabled(false);
	}

	public boolean isCanceled() {
		return canceled;
	}

	public void init(Axes<DataFrameAxis> axes) {
		SpinnerNumberModel model = (SpinnerNumberModel) xSpinner.getModel();
		DataFrameAxis axis = axes.getXAxis();
		// show axis starting at 1 rather than 0
		int min = (int) (axis.getRange().getOrigin() + 1);
		int max = min + (int) axis.getRange().getExtent() - 1;
		model.setMinimum(min);
		model.setMaximum(max);
		xSpinner.setValue(new Integer(min));

		model = (SpinnerNumberModel) ySpinner.getModel();
		axis = axes.getYAxis();
		// show axis starting at 1 rather than 0
		min = (int) (axis.getRange().getOrigin() + 1);
		max = min + (int) axis.getRange().getExtent();
		model.setMinimum(min);
		model.setMaximum(max);
		ySpinner.setValue(new Integer(min));
	}

	// return the true 0 based value
	public int getColumn() {
		return ((Number)xSpinner.getValue()).intValue() - 1;
	}

	// return the true 0 based value
	public int getRow() {
		return ((Number)ySpinner.getValue()).intValue() - 1;
	}

	public boolean isXSelected() {
		return rbX.isSelected();
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner non-commercial license
		DefaultComponentFactory compFactory = DefaultComponentFactory.getInstance();
		dialogPane = new JPanel();
		contentPanel = new JPanel();
		separator1 = compFactory.createSeparator("Select Cross Section");
		rbX = new JRadioButton();
		lblColumn = new JLabel();
		xSpinner = new JSpinner();
		rbY = new JRadioButton();
		lblRow = new JLabel();
		ySpinner = new JSpinner();
		separator2 = compFactory.createSeparator("");
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
				ColumnSpec[] aColumnSpec = ColumnSpec.decodeSpecs("max(default;50dlu)");
				ColumnSpec bColumnSpec = new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW);
				contentPanel.setLayout(new FormLayout(
					new ColumnSpec[] {
						FormFactory.DEFAULT_COLSPEC,
						FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
						FormFactory.DEFAULT_COLSPEC,
						FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
						aColumnSpec[0],
						FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
						bColumnSpec
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
						FormFactory.DEFAULT_ROWSPEC
					}));
//				contentPanel.setLayout(new FormLayout(
//						new ColumnSpec[] {
//							FormFactory.DEFAULT_COLSPEC,
//							FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
//							FormFactory.DEFAULT_COLSPEC,
//							FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
//							new ColumnSpec("max(default;50dlu)"),
//							FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
//							new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW)
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
//							FormFactory.DEFAULT_ROWSPEC
//						}));
				contentPanel.add(separator1, cc.xywh(1, 1, 7, 1));

				//---- rbX ----
				rbX.setText("X-Axis Cross Section");
				contentPanel.add(rbX, cc.xywh(1, 3, 5, 1));

				//---- lblColumn ----
				lblColumn.setText("Column:");
				contentPanel.add(lblColumn, cc.xy(3, 5));
				contentPanel.add(xSpinner, cc.xy(5, 5));

				//---- rbY ----
				rbY.setText("Y-Axis Cross Section");
				contentPanel.add(rbY, cc.xywh(1, 7, 5, 1));

				//---- lblRow ----
				lblRow.setText("Row:");
				contentPanel.add(lblRow, cc.xy(3, 9));
				contentPanel.add(ySpinner, cc.xy(5, 9));
				contentPanel.add(separator2, cc.xywh(1, 11, 7, 1));
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
	private JComponent separator1;
	private JRadioButton rbX;
	private JLabel lblColumn;
	private JSpinner xSpinner;
	private JRadioButton rbY;
	private JLabel lblRow;
	private JSpinner ySpinner;
	private JComponent separator2;
	private JPanel buttonBar;
	private JButton okButton;
	private JButton cancelButton;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
