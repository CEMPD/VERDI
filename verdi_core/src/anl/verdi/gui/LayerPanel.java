package anl.verdi.gui;

import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeListener;

import anl.verdi.data.CoordAxis;
import anl.verdi.util.FocusClickFix;

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
public class LayerPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8965939962218675704L;
	public LayerPanel() {
		initComponents();
		setEnabled(false);
		chkEnable.addMouseListener(new FocusClickFix());
	}

	public void addListeners(ChangeListener minListener, ChangeListener maxListener, ActionListener useListener) {
		maxSpinner.addChangeListener(maxListener);
		minSpinner.addChangeListener(minListener);
		chkEnable.addActionListener(useListener);
	}

	/**
	 * Resets the spinners according to the layer axis,
	 * and sets their values to the specified min and max.
	 *
	 * @param axes the current dataset axes
	 * @param layerMin the min layer
	 * @param layerMax the max layer
	 * @param isLayerUsed whether layer is used in evaluation or not
	 */
	public void reset(CoordAxis layers, int layerMin, int layerMax, boolean isLayerUsed) {
		int maxStep = (int) layers.getRange().getExtent() - 1;
		SpinnerNumberModel model = (SpinnerNumberModel) minSpinner.getModel();
		model.setMinimum(1);
		model.setMaximum(maxStep + 1);
		
		String oldAxis = currentAxis;
		String oldRange = currentRange;
		currentRange = layers.getRange().toString();
		currentAxis = layers.getName();
		Integer oldMin = (Integer)minSpinner.getValue();
		Integer oldMax = (Integer)maxSpinner.getValue();
		boolean oldChecked = chkEnable.isSelected();
		if (oldAxis != null) {
			axisValues.put(oldRange + oldAxis + "min", oldMin);
			axisValues.put(oldRange + oldAxis + "max", oldMax);
			axisValues.put(oldRange + oldAxis + "use", oldChecked ? 1 : 0);
		}
		if (axisValues.containsKey(currentRange + currentAxis + "use"))
			isLayerUsed = axisValues.get(currentRange + currentAxis + "use").equals(1);
		Integer newVal = axisValues.get(currentRange + currentAxis + "min");
		if (newVal == null)
			newVal = new Integer(layerMin + 1);
		minSpinner.setValue(newVal);
		
		newVal = axisValues.get(currentRange + currentAxis + "max");
		if (newVal == null)
			newVal = new Integer(layerMax + 1);
		
		model = (SpinnerNumberModel) maxSpinner.getModel();
		model.setMinimum(1);
		model.setMaximum(maxStep + 1);
		
		maxSpinner.setValue(newVal);
		chkEnable.setSelected(isLayerUsed);
		setBorder(new TitledBorder("Layers (" + currentAxis + " 1 - " + (maxStep+1) + ")"));

		if (layerMin == layerMax) {
			minSpinner.setEnabled(false);
			maxSpinner.setEnabled(false);
			chkEnable.setEnabled(false);
		}
	}
	
	String currentAxis = null;
	String currentRange = null;
	Map<String, Integer> axisValues = new HashMap<String, Integer>();

	public void setEnabled(boolean val) {
		super.setEnabled(val);
		for (int i = 0; i < getComponentCount(); i++) {
			getComponent(i).setEnabled(val);
		}
		if (!val) setBorder(new TitledBorder("Layers (N/A)"));
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner non-commercial license
		DefaultComponentFactory compFactory = DefaultComponentFactory.getInstance();
		chkEnable = new JCheckBox();
		label1 = compFactory.createLabel("Min:");
		minSpinner = new JSpinner();
		label2 = compFactory.createLabel("Max:");
		maxSpinner = new JSpinner();
		CellConstraints cc = new CellConstraints();

		//======== this ========
		setBorder(new TitledBorder("Layers"));

		// 2014 - underlying jgoodies class changed
		ColumnSpec[] aColumnSpec = ColumnSpec.decodeSpecs("max(default;10dlu)");
		ColumnSpec[] bColumnSpec = ColumnSpec.decodeSpecs("max(pref;30dlu)");
		ColumnSpec cColumnSpec = new ColumnSpec(ColumnSpec.FILL, Sizes.PREFERRED, FormSpec.DEFAULT_GROW);
		setLayout(new FormLayout(
				new ColumnSpec[] {
						aColumnSpec[0],
						FormFactory.DEFAULT_COLSPEC,
						FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
						bColumnSpec[0],
						FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
						cColumnSpec
				},
				new RowSpec[] {
						FormFactory.DEFAULT_ROWSPEC,
						FormFactory.LINE_GAP_ROWSPEC,
						FormFactory.DEFAULT_ROWSPEC,
						FormFactory.LINE_GAP_ROWSPEC,
						FormFactory.DEFAULT_ROWSPEC
				}));
		//		setLayout(new FormLayout(
		//				new ColumnSpec[] {
		//					new ColumnSpec("max(default;10dlu)"),
		//					FormFactory.DEFAULT_COLSPEC,
		//					FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
		//					new ColumnSpec("max(pref;30dlu)"),
		//					FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
		//					new ColumnSpec(ColumnSpec.FILL, Sizes.PREFERRED, FormSpec.DEFAULT_GROW)
		//				},
		//				new RowSpec[] {
		//					FormFactory.DEFAULT_ROWSPEC,
		//					FormFactory.LINE_GAP_ROWSPEC,
		//					FormFactory.DEFAULT_ROWSPEC,
		//					FormFactory.LINE_GAP_ROWSPEC,
		//					FormFactory.DEFAULT_ROWSPEC
		//				}));

		//---- chkEnable ----
		chkEnable.setText("Use Layer Range");
		add(chkEnable, cc.xywh(1, 1, 6, 1));
		add(label1, cc.xy(2, 3));
		add(minSpinner, cc.xy(4, 3));
		add(label2, cc.xy(2, 5));
		add(maxSpinner, cc.xy(4, 5));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner non-commercial license
	private JCheckBox chkEnable;
	private JLabel label1;
	private JSpinner minSpinner;
	private JLabel label2;
	private JSpinner maxSpinner;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
	
	public int getMin() {
		return ((SpinnerNumberModel) minSpinner.getModel()).getNumber().intValue() - 1;
	}
	public int getMax() {
		return ((SpinnerNumberModel) maxSpinner.getModel()).getNumber().intValue() - 1;
	}
	public boolean getLayerEnabled() {
		return chkEnable.isSelected();
	}
}
