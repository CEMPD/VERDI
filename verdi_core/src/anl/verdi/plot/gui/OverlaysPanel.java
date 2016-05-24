/*
 * Created by JFormDesigner on Tue Jun 19 13:44:23 EDT 2007
 */

package anl.verdi.plot.gui;

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
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.Sizes;

/**
 * @author User #2
 */
public class OverlaysPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1786002910057898602L;
	public OverlaysPanel() {
		initComponents();
	}

	public void init(int strokeSize, int shapeSize) {
		strokeSpn.setValue(strokeSize);
		sizeSpn.setValue(shapeSize);
	}

	public PlotConfiguration fillConfiguration(PlotConfiguration config) {
		config.putObject(TilePlotConfiguration.OBS_SHAPE_SIZE, (Integer)sizeSpn.getValue());
		config.putObject(TilePlotConfiguration.OBS_STROKE_SIZE, (Integer)strokeSpn.getValue());
		return config;
	}


	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner non-commercial license
		label1 = new JLabel();
		strokeSpn = new JSpinner();
		label2 = new JLabel();
		sizeSpn = new JSpinner();
		CellConstraints cc = new CellConstraints();

		//======== this ========
		setBorder(new TitledBorder("Overlays"));
		// 2014
		ColumnSpec aColumnSpec = new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, 0.3);
		setLayout(new FormLayout(
			new ColumnSpec[] {
				FormFactory.PREF_COLSPEC,
				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
				aColumnSpec
			},
			new RowSpec[] {
				FormFactory.PREF_ROWSPEC,
				FormFactory.LINE_GAP_ROWSPEC,
				FormFactory.PREF_ROWSPEC
			}));
//		setLayout(new FormLayout(
//				new ColumnSpec[] {
//					FormFactory.PREF_COLSPEC,
//					FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
//					new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, 0.3)
//				},
//				new RowSpec[] {
//					FormFactory.PREF_ROWSPEC,
//					FormFactory.LINE_GAP_ROWSPEC,
//					FormFactory.PREF_ROWSPEC
//				}));

		//---- label1 ----
		label1.setText("Stroke Size:");
		add(label1, cc.xy(1, 1));

		//---- strokeSpn ----
		strokeSpn.setModel(new SpinnerNumberModel(1, 1, null, 1));
		add(strokeSpn, cc.xy(3, 1));

		//---- label2 ----
		label2.setText("Shape Size:");
		add(label2, cc.xy(1, 3));

		//---- sizeSpn ----
		sizeSpn.setModel(new SpinnerNumberModel(8, 1, null, 1));
		add(sizeSpn, cc.xy(3, 3));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner non-commercial license
	private JLabel label1;
	private JSpinner strokeSpn;
	private JLabel label2;
	private JSpinner sizeSpn;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
