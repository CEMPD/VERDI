package anl.verdi.plot.gui;

import java.awt.Font;
import java.util.GregorianCalendar;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import anl.verdi.data.Axes;
import anl.verdi.data.CoordAxis;
import anl.verdi.data.DataFrameAxis;
import anl.verdi.util.Utilities;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;


/**
 * @author User #2
 */
public class TimeLayerPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7183689501895236846L;
	private class SpinnerListener implements ChangeListener {
		public void stateChanged(ChangeEvent e) {
			JSpinner source = (JSpinner) e.getSource();
			if (spinnersOn) {
				int val = ((Number) source.getValue()).intValue() - 1;
				if (axes != null) {
					GregorianCalendar date = axes.getDate(val);
					timeLabel.setText(Utilities.formatShortDate(date));
				}
			}
		}
	}

	private Axes<DataFrameAxis> axes;
	private boolean spinnersOn = false;


	public TimeLayerPanel() {
System.out.println("in constructor for TimeLayerPanel");
		initComponents();
		timeSpinner.addChangeListener(new SpinnerListener());
	}

	public void addSpinnerListeners(ChangeListener time, ChangeListener layer) {
		timeSpinner.addChangeListener(time);
		layerSpinner.addChangeListener(layer);
	}

	public int getTime() {
		return ((Integer) timeSpinner.getValue()).intValue() - 1;
	}
	
	public int[] getAllTimeSteps() {
		if (noTimeSteps)
			return new int[0];
		
		return getAllSteps(timeSpinner);
	}

	public int[] getAllLayers() {
		if (noLayers)
			return new int[0];
		
		return getAllSteps(layerSpinner);
	}

	private int[] getAllSteps(JSpinner spinner) {
		SpinnerNumberModel model = (SpinnerNumberModel) spinner.getModel();
		int min = (Integer)model.getMinimum();
		int max = (Integer)model.getMaximum();
		int step = model.getStepSize().intValue();
		
		int[] steps = new int[(max - min)/step + 1];
		int len = steps.length;
		steps[0] = min - 1;
		steps[len-1] = max - 1;
		
		for (int i = 1; i < len - 1; i++)
			steps[i] = steps[i-1] + step;
		
		return steps;
	}
	
	public int getLayer() {
		return ((Integer) layerSpinner.getValue()).intValue() - 1;
	}

	public void setTime(int step) {
		timeSpinner.setValue(new Integer(step)+1);
	}
	
	public void setLayer(int layer) {
		layerSpinner.setValue(new Integer(layer)+1);
	}
	
	public boolean hasNoTimeSteps() {
		return noTimeSteps;
	}
	
	public boolean hasNoLayers() {
		return noLayers;
	}

	public void init(Axes<DataFrameAxis> axes, int timeStep, int layer, boolean layerEnabled) {
		this.axes = axes;
		spinnersOn = false;
		CoordAxis time = axes.getTimeAxis();
		int min = (int) ((time == null) ? 1 : time.getRange().getOrigin() + 1);
		int max = (time == null) ? 1 : min + (int) time.getRange().getExtent() - 1;
		SpinnerNumberModel model = (SpinnerNumberModel) timeSpinner.getModel();
		model.setMinimum(min);
		model.setMaximum(max);
		model.setValue(new Integer(timeStep + 1));
		GregorianCalendar date = axes.getDate(timeStep);
System.out.println("in TimeLayerPanel, init function, just computed GregorianCalendar date");
		timeLabel.setText(Utilities.formatShortDate(date == null ? new GregorianCalendar() : date));

		timeSpinner.setEnabled(min != max);
		noTimeSteps = (min == max);
		noLayers = !layerEnabled;

		CoordAxis layerAxis = axes.getZAxis();
		if (layerAxis != null) {
			min = (int) (layerAxis.getRange().getOrigin() + 1);
			max = min + (int) layerAxis.getRange().getExtent() - 1;
			model = (SpinnerNumberModel) layerSpinner.getModel();
			model.setMinimum(min);
			model.setMaximum(max);
			model.setValue(new Integer(layer) + 1);
		} else {
			min = max = 1;
			model = (SpinnerNumberModel) layerSpinner.getModel();
			model.setMinimum(min);
			model.setMaximum(max);
			model.setValue(new Integer(1));
		}
		
		layerSpinner.setEnabled(layerEnabled);
		spinnersOn = true;
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner non-commercial license
		label1 = new JLabel();
		timeSpinner = new JSpinner();
		timeLabel = new JLabel();
		label2 = new JLabel();
		layerSpinner = new JSpinner();
		CellConstraints cc = new CellConstraints();

		//======== this ========
		// 2014
		ColumnSpec[] aColumnSpec = ColumnSpec.decodeSpecs("max(min;40dlu)");
		ColumnSpec[] bColumnSpec = ColumnSpec.decodeSpecs("max(default;50dlu)");
		ColumnSpec[] cColumnSpec = ColumnSpec.decodeSpecs("max(default;40dlu)");
		RowSpec[] aRowSpec = RowSpec.decodeSpecs("default");
		setLayout(new FormLayout(
						new ColumnSpec[]{
										FormFactory.PREF_COLSPEC,
										FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
										aColumnSpec[0],
										FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
										bColumnSpec[0],
										FormFactory.UNRELATED_GAP_COLSPEC,
										FormFactory.DEFAULT_COLSPEC,
										FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
										cColumnSpec[0]
						},
						aRowSpec));
//		setLayout(new FormLayout(
//				new ColumnSpec[]{
//								FormFactory.PREF_COLSPEC,
//								FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
//								new ColumnSpec("max(min;40dlu)"),
//								FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
//								new ColumnSpec("max(default;50dlu)"),
//								FormFactory.UNRELATED_GAP_COLSPEC,
//								FormFactory.DEFAULT_COLSPEC,
//								FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
//								new ColumnSpec("max(default;40dlu)")
//				},
//				RowSpec.decodeSpecs("default")));

		//---- label1 ----
		label1.setText("Time Step:");
		add(label1, cc.xy(1, 1));
		add(timeSpinner, cc.xy(3, 1));

		//---- timeLabel ----
		timeLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		add(timeLabel, cc.xy(5, 1));

		//---- label2 ----
		label2.setText("Layer:");
		add(label2, cc.xy(7, 1));
		add(layerSpinner, cc.xy(9, 1));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner non-commercial license
	private JLabel label1;
	private JSpinner timeSpinner;
	private JLabel timeLabel;
	private JLabel label2;
	private JSpinner layerSpinner;
	private boolean noTimeSteps = false;
	private boolean noLayers = false;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
