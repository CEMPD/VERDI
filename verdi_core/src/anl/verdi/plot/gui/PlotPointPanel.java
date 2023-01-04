package anl.verdi.plot.gui;

import java.awt.Component;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;

import anl.verdi.plot.config.PlotConfiguration;
import anl.verdi.plot.config.TilePlotConfiguration;

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
public class PlotPointPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4754283587955358186L;
	public PlotPointPanel() {
		initComponents();
	}

	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		for (Component comp : getComponents()) {
			comp.setEnabled(enabled);
		}
	}

	public void init(int dotSize) {
		sizeSpinner.setValue(dotSize);
	}


	public int getDotSize() {
		return (Integer)sizeSpinner.getValue();
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner non-commercial license
		sizeLabel = new JLabel();
		sizeSpinner = new JSpinner();
		CellConstraints cc = new CellConstraints();

		//======== this ========
		setBorder(new TitledBorder("Plot Points"));
		// 2014
		ColumnSpec aColumnSpec = new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW);
		setLayout(new FormLayout(
			new ColumnSpec[] {
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
				aColumnSpec,
				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC
			},
			new RowSpec[] {
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.LINE_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC
			}));
//		setLayout(new FormLayout(
//				new ColumnSpec[] {
//					FormFactory.DEFAULT_COLSPEC,
//					FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
//					new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW),
//					FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
//					FormFactory.DEFAULT_COLSPEC
//				},
//				new RowSpec[] {
//					FormFactory.DEFAULT_ROWSPEC,
//					FormFactory.LINE_GAP_ROWSPEC,
//					FormFactory.DEFAULT_ROWSPEC
//				}));


		//---- sizeLabel ----
		sizeLabel.setText("Dot Size:");
		add(sizeLabel, cc.xy(1, 1));

		//---- sizeSpinner ----
		sizeSpinner.setModel(new SpinnerNumberModel(8, 2, 15, 1));
		add(sizeSpinner, new CellConstraints(3, 1, 1, 1, CellConstraints.DEFAULT, CellConstraints.DEFAULT, new Insets( 0, 0, 0, 0)));

		JButton spacerButton = new JButton();
		spacerButton.setText("Select");
		JPanel spacerPanel = new JPanel();
		spacerPanel.setPreferredSize(spacerButton.getPreferredSize());
		add(spacerPanel, cc.xy(5, 1));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner non-commercial license
	private JLabel sizeLabel;
	private JSpinner sizeSpinner;
	// JFormDesigner - End of variables declaration  //GEN-END:variables

	public PlotConfiguration fillConfiguration(PlotConfiguration config) {
		config.putObject(TilePlotConfiguration.SCATTER_SHAPE_SIZE, sizeSpinner.getValue().toString());
		return config;
	}
}
