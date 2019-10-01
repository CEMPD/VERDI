package anl.verdi.plot.gui;

import java.awt.Font;
import java.util.GregorianCalendar;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages

import anl.verdi.data.Axes;
import anl.verdi.data.CoordAxis;
import anl.verdi.data.DataFrameAxis;
import anl.verdi.data.MPASDataFrameIndex;
import anl.verdi.util.Utilities;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * @author User #2
 */
public class TimeConstantAxisPanel extends JPanel {
	
	boolean doubleConstant = false;

	private static final long serialVersionUID = -1747899384194778482L;
	static final Logger Logger = LogManager.getLogger(TimeConstantAxisPanel.class.getName());
	private class SpinnerListener implements ChangeListener {
		public void stateChanged(ChangeEvent e) {
			JSpinner source = (JSpinner)e.getSource();
      if (spinnersOn) {
	      int val = ((Number)source.getValue()).intValue() - 1;
	      if (frameAxes != null) {
		      GregorianCalendar date = frameAxes.getDate(val);
		      timeLabel.setText(Utilities.formatShortDate(date));
	      }
      }
		}
	}

	private Axes axes;
	private Axes frameAxes; //MPAS files need the separate axes
	private boolean spinnersOn = false;
	double displayIncrement = 1;

	public TimeConstantAxisPanel(boolean doubles) {
		Logger.debug("in constructor for TimeConstantAxisPanel");
		doubleConstant = doubles;
		initComponents();
		timeSpinner.addChangeListener(new SpinnerListener());
	}

	public void addSpinnerListeners(ChangeListener time, ChangeListener axis) {
		timeSpinner.addChangeListener(time);
		axisSpinner.addChangeListener(axis);
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner non-commercial license
		label1 = new JLabel();
		timeSpinner = new JSpinner();
		timeLabel = new JLabel();
		lblConstantAxis = new JLabel();
		if (doubleConstant)
			axisSpinner = new JSpinner(new DoubleSpinnerModel());
		else
			axisSpinner = new JSpinner();
		CellConstraints cc = new CellConstraints();

		//======== this ========
		// 2014
		ColumnSpec[] aColumnSpec = ColumnSpec.decodeSpecs("max(min;40dlu)");
		ColumnSpec[] bColumnSpec = ColumnSpec.decodeSpecs("max(default;50dlu)");
		ColumnSpec[] cColumnSpec = ColumnSpec.decodeSpecs("max(default;40dlu)");
		RowSpec[] aRowSpec = RowSpec.decodeSpecs("default");
		setLayout(new FormLayout(
			new ColumnSpec[] {
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
//				new ColumnSpec[] {
//					FormFactory.PREF_COLSPEC,
//					FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
//					new ColumnSpec("max(min;40dlu)"),
//					FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
//					new ColumnSpec("max(default;50dlu)"),
//					FormFactory.UNRELATED_GAP_COLSPEC,
//					FormFactory.DEFAULT_COLSPEC,
//					FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
//					new ColumnSpec("max(default;40dlu)")
//				},
//				RowSpec.decodeSpecs("default")));

		//---- label1 ----
		label1.setText("Time Step:");
		add(label1, cc.xy(1, 1));
		add(timeSpinner, cc.xy(3, 1));

		//---- timeLabel ----
		timeLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		add(timeLabel, cc.xy(5, 1));

		//---- lblConstantAxis ----
		lblConstantAxis.setText("Layer:");
		add(lblConstantAxis, cc.xy(7, 1));
		add(axisSpinner, cc.xy(9, 1));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	public void setConstantAxisLabel(String label) {
		lblConstantAxis.setText(label);
	}

	public int getTime() {
		return ((Integer)timeSpinner.getValue()).intValue() - 1;
	}

	/**
	 * Gets the <b>true</b> true axis index rather than
	 * the displayed one which is offset by 1.
	 *
	 * @return the <b>true</b> true axis index rather than
	 * the displayed one which is offset by 1.
	 */
	public double getAxisValue() {
		return ((Number)axisSpinner.getValue()).doubleValue() - displayIncrement;
	}

	public void setTime(int time) {
		timeSpinner.setValue(new Integer(time + 1));
	}

	public void init(Axes axes, CoordAxis constantAxis, Axes frameAxes, int timeStep, double constant) {
		this.axes = axes;
		this.frameAxes = frameAxes;
		spinnersOn = false;
		if (constantAxis.getClass().getName().indexOf("MPAS") != -1)
			displayIncrement = 0;
		CoordAxis time = frameAxes.getTimeAxis();
		int minI = (int) (time.getRange().getOrigin() + 1);
		int maxI = minI + (int) time.getRange().getExtent() - 1;
		SpinnerNumberModel model = (SpinnerNumberModel) timeSpinner.getModel();
		model.setMinimum(minI);
		model.setMaximum(maxI);
		model.setValue(new Integer(timeStep + 1));
		GregorianCalendar date = frameAxes.getDate(timeStep);
		Logger.debug("in TimeConstantAxisPanel init function, computed GregorianCalendar date" );
		timeLabel.setText(Utilities.formatShortDate(date));

		// we want the range to start with 1 rather than 0
		// so we add 1.  Not needed for MPAS - range is already adjusted
		double minD = constantAxis.getRange().getOrigin() + displayIncrement;
		//min = constant + displayIncrement;
		double maxD = minD + constantAxis.getRange().getExtent() - 1;
		if (constantAxis.getClass().getName().indexOf("MPAS") != -1)
			--maxD;

		model = (SpinnerNumberModel) axisSpinner.getModel();
		model.setMinimum(minD);
		model.setMaximum(maxD);
		// offset by one so row / col seems to start at 1
		model.setValue(new Double(constant + (double)displayIncrement));
		spinnersOn = true;
	}



	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner non-commercial license
	private JLabel label1;
	private JSpinner timeSpinner;
	private JLabel timeLabel;
	private JLabel lblConstantAxis;
	private JSpinner axisSpinner;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
