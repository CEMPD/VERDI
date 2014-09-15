package anl.verdi.plot.gui;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeListener;

import anl.verdi.data.Axes;
import anl.verdi.data.CoordAxis;
import anl.verdi.data.DataFrameAxis;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;


/**
 * @author User #2
 */
public class LayerChartPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2834253243629674032L;
	public static final int NO_LAYER = -1;

	public LayerChartPanel() {
		initComponents();
	}

	public void addSpinnerListener(ChangeListener layer) {
		layerSpinner.addChangeListener(layer);
	}

	public int getLayer() {
		return ((Integer) layerSpinner.getValue()).intValue() - 1;
	}

	/**
	 * Initializes the layer spinner. If layer is NO_LAYER then
	 * the layer spinner is disabled.
	 *
	 * @param axes the axes
	 * @param layer the starting layer
	 */
	public void init(Axes<DataFrameAxis> axes, int layer) {
		if (layer == NO_LAYER) {
			layerSpinner.setEnabled(false);
		} else {
			CoordAxis layerAxis = axes.getZAxis();
			int min = (int) (layerAxis.getRange().getOrigin() + 1); //Want layer start from 1 so add 1
			int max = min + (int) layerAxis.getRange().getExtent() - 1;
			SpinnerNumberModel model = (SpinnerNumberModel) layerSpinner.getModel();
			model.setMinimum(min);
			model.setMaximum(max);
			model.setValue(new Integer(layer + 1));
		}
	}


	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner non-commercial license
		label2 = new JLabel();
		layerSpinner = new JSpinner();
		CellConstraints cc = new CellConstraints();

		//======== this ========
		// 2014
		ColumnSpec[] aColumnSpec = ColumnSpec.decodeSpecs("max(default;40dlu)");
		RowSpec[] aRowSpec = RowSpec.decodeSpecs("default"); 
		setLayout(new FormLayout(
						new ColumnSpec[]{
										FormFactory.DEFAULT_COLSPEC,
										FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
										aColumnSpec[0]
						},
						aRowSpec));
//		setLayout(new FormLayout(
//				new ColumnSpec[]{
//								FormFactory.DEFAULT_COLSPEC,
//								FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
//								new ColumnSpec("max(default;40dlu)")
//				},
//				RowSpec.decodeSpecs("default")));

		//---- label2 ----
		label2.setText("Layer:");
		add(label2, cc.xy(1, 1));
		add(layerSpinner, cc.xy(3, 1));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner non-commercial license
	private JLabel label2;
	private JSpinner layerSpinner;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
