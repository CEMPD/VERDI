package anl.verdi.plot.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import anl.verdi.core.VerdiConstants;
import anl.verdi.data.Axes;
import anl.verdi.data.CoordAxis;
import anl.verdi.data.DataFrame;
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
	
	private boolean meshInput = false;

	public VerticalCrossDialog(Frame owner, boolean meshInput) {
		super(owner);
		this.meshInput = meshInput;
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
		
		ButtonGroup renderGroup = new ButtonGroup();
		renderGroup.add(layer);
		renderGroup.add(elevation);

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
		
		layer.setSelected(true);
	}

	public boolean isCanceled() {
		return canceled;
	}

	public void init(DataFrame frame) {
		Axes axes = null;
		if (meshInput)
			axes = frame.getDataset().get(0).getCoordAxes();
		else	
			axes = frame.getAxes();
		SpinnerNumberModel model = (SpinnerNumberModel) xSpinner.getModel();
		CoordAxis axis = axes.getXAxis();
		// show axis starting at 1 rather than 0
		int min = (int) (axis.getRange().getOrigin() + 1);
		int max = min + (int) axis.getRange().getExtent() - 1;
		if (meshInput) {
			--min;
			--max;
		}
		model.setMinimum(min);
		model.setMaximum(max);
		xSpinner.setValue(new Integer(min));

		model = (SpinnerNumberModel) ySpinner.getModel();
		axis = axes.getYAxis();
		// show axis starting at 1 rather than 0
		min = (int) (axis.getRange().getOrigin() + 1);
		max = min + (int) axis.getRange().getExtent();
		if (meshInput) {
			--min;
			--max;
		}
		model.setMinimum(min);
		model.setMaximum(max);
		ySpinner.setValue(new Integer(min));
		//sliceSize.setEnabled(true);
		/*model = (SpinnerNumberModel) sliceSize.getModel();
		model.setMinimum(1);
		model.setMaximum(10);
		sliceSize.setValue(new Integer(1));*/
	}

	// return the true 0 based value
	public double getColumn() {
		return ((Number)xSpinner.getValue()).doubleValue() - 1;
	}

	// return the true 0 based value
	public double getRow() {
		return ((Number)ySpinner.getValue()).doubleValue() - 1;
	}
	
	public double getSliceSize() {
		return ((Number)sliceSize.getValue()).doubleValue();
	}
	
	/*public String getSliceUnits() {
		return (String)sliceUnits.getSelectedItem();
	}*/

	public boolean isXSelected() {
		return rbX.isSelected();
	}
	
	public boolean displayLayer() {
		return layer.isSelected();
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner non-commercial license
		DefaultComponentFactory  compFactory = DefaultComponentFactory.getInstance();
		dialogPane = new JPanel();
		contentPanel = new JPanel();
		separator1 = compFactory.createSeparator("Select Cross Section");
		rbX = new JRadioButton();
		lblColumn = new JLabel();
		xSpinner = new JSpinner(new DoubleSpinnerModel());
		//xSpinner = new JSpinner();
		rbY = new JRadioButton();
		lblRow = new JLabel();
		ySpinner = new JSpinner(new DoubleSpinnerModel());
		//ySpinner = new JSpinner();
		separator2 = compFactory.createSeparator("Display Mode");
		buttonBar = new JPanel();
		okButton = new JButton();
		cancelButton = new JButton();
		elevation = new JRadioButton();
		layer = new JRadioButton();
		lblSliceSize = new JLabel();
		//DoubleSpinnerModel sliceModel = new DoubleSpinnerModel(2, 0, 100000, 1);
		DoubleSpinnerModel sliceModel = new DoubleSpinnerModel(1, 0, 100000, 1);
		//DoubleSpinnerModel sliceModel = new DoubleSpinnerModel();
		/*sliceModel.setMinimum(0.0);
		sliceModel.setMaximum(100000.0);
		sliceModel.setValue(2.0);*/
		sliceSize = new JSpinner(sliceModel);
		
		//String[] availableUnits = new String[] { VerdiConstants.UNITS_DEG, VerdiConstants.UNITS_MI, VerdiConstants.UNITS_KM };
		//sliceUnits = new JComboBox<String>(availableUnits);
		separator3 = compFactory.createSeparator("");
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
				if (meshInput)
					lblColumn.setText("Longitude:");
				else
					lblColumn.setText("Column:");
				contentPanel.add(lblColumn, cc.xy(3, 5));
				contentPanel.add(xSpinner, cc.xy(5, 5));

				//---- rbY ----
				rbY.setText("Y-Axis Cross Section");
				contentPanel.add(rbY, cc.xywh(1, 7, 5, 1));

				//---- lblRow ----
				if (meshInput)
					lblRow.setText("Latitude:");
				else
					lblRow.setText("Row:");
				
				
				contentPanel.add(lblRow, cc.xy(3, 9));
				contentPanel.add(ySpinner, cc.xy(5, 9));
				contentPanel.add(separator2, cc.xywh(1, 11, 7, 1));
				
				layer.setText("Layer");
				contentPanel.add(layer, cc.xywh(1, 13, 5, 1));
				
				elevation.setText("Elevation");
				contentPanel.add(elevation, cc.xywh(1, 15, 5, 1));
				if (!meshInput) {
					layer.setSelected(true);
					layer.setEnabled(false);
					elevation.setEnabled(false);
				}
				
				lblSliceSize.setText("Cross Section Size");
				contentPanel.add(lblSliceSize, cc.xy(3, 17));
				//JPanel slicePanel = new JPanel();
				//slicePanel.add(sliceSize);
				//slicePanel.add(sliceUnits);
				contentPanel.add(sliceSize, cc.xy(5, 17));
				//contentPanel.add(sliceUnits, cc.xywh(7, 17,1,1));
				
				contentPanel.add(separator3,  cc.xywh(1, 19, 7,  1));
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
	private JRadioButton layer;
	private JRadioButton elevation;
	private JComponent separator3;
	private JLabel lblSliceSize;
	private JSpinner sliceSize;
	//private JComboBox sliceUnits;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
