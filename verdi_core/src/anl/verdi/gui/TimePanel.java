package anl.verdi.gui;

import java.awt.Font;
import java.awt.event.ActionListener;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages

import anl.verdi.data.CoordAxis;
import anl.verdi.data.TimeCoordAxis;
import anl.verdi.util.FocusClickFix;
import anl.verdi.util.Utilities;

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
public class TimePanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9045127791832834587L;
	static final Logger Logger = LogManager.getLogger(TimePanel.class.getName());
	private class SpinnerListener implements ChangeListener {

		private JLabel label;
		public SpinnerListener(JLabel fld) {
			this.label = fld;
		}

		public void stateChanged(ChangeEvent e) {
			JSpinner source = (JSpinner)e.getSource();
			if (slidersOn) {
				int val = ((Number)source.getValue()).intValue() - 1;
				if (timeAxis != null) {
					GregorianCalendar date = getDate(val);
					label.setText(Utilities.formatShortDate(date));
				}
			}
		}
	}
	
	private GregorianCalendar getDate(int val) {
		return ((TimeCoordAxis)timeAxis).getDate(val);
	}

	CoordAxis timeAxis;
	private boolean slidersOn = false;

	public TimePanel() {
		Logger.debug("in constructor for TimePanel");
		initComponents();
		maxSpinner.addChangeListener(new SpinnerListener(maxDate));
		minSpinner.addChangeListener(new SpinnerListener(minDate));
		setEnabled(false);

		chkEnable.addMouseListener(new FocusClickFix());
	}

	public void addListeners(ChangeListener minListener, ChangeListener maxListener, ActionListener useListener) {
		maxSpinner.addChangeListener(maxListener);
		minSpinner.addChangeListener(minListener);
		chkEnable.addActionListener(useListener);
	}

	String currentAxis = null;
	String currentRange = null;
	Map<String, Integer> timeValues = new HashMap<String, Integer>();

	/**
	 * Resets the spinners according to the time axis,
	 * and sets their values to the specified min and max.
	 *
	 * @param axes the current dataset axes
	 * @param timeMin the min time
	 * @param timeMax the max time
	 */
	public void reset(CoordAxis time, int timeMin, int timeMax, boolean isUsed) {
		int maxStep = (int) time.getRange().getExtent() - 1;
		SpinnerNumberModel model = (SpinnerNumberModel) minSpinner.getModel();
		model.setMinimum(1);
		model.setMaximum(maxStep + 1);
		
		String oldAxis = currentAxis;
		currentAxis = time.getName();
		String oldRange = currentRange;
		currentRange = time.getRange().toString();
		Integer oldMin = (Integer)minSpinner.getValue();
		Integer oldMax = (Integer)maxSpinner.getValue();
		boolean oldChecked = chkEnable.isSelected();
		if (oldAxis != null) {
			timeValues.put(oldRange + oldAxis + "min", oldMin);
			timeValues.put(oldRange + oldAxis + "max", oldMax);
			timeValues.put(oldRange + oldAxis + "use", oldChecked ? 1 : 0);
		}
		if (timeValues.containsKey(currentRange + currentAxis + "use"))
			isUsed = timeValues.get(currentRange + currentAxis + "use").equals(1);
		
		
		Integer newVal = timeValues.get(currentRange + currentAxis + "min");
		if (newVal == null)
			newVal = new Integer(timeMin + 1);
		minSpinner.setValue(newVal);
		
		newVal = timeValues.get(currentRange + currentAxis + "max");
		if (newVal == null)
			newVal = new Integer(timeMax + 1);
		
		model = (SpinnerNumberModel) maxSpinner.getModel();
		model.setMinimum(1);
		model.setMaximum(maxStep + 1);
		
		maxSpinner.setValue(newVal);
		chkEnable.setSelected(isUsed);
		slidersOn = isUsed;

		if (timeMin == timeMax) {
			minSpinner.setEnabled(false);
			maxSpinner.setEnabled(false);
			chkEnable.setEnabled(false);
		}

		String display = currentAxis.equals("Time") ? "" : currentAxis + " ";
		setBorder(new TitledBorder("Time Steps ( " + display + "1 - "+ (maxStep + 1) + ")"));
	}

	public void setEnabled(boolean val) {
		super.setEnabled(val);
		maxDate.setText("N/A");
		minDate.setText("N/A");
		for (int i = 0; i < getComponentCount(); i++) {
			getComponent(i).setEnabled(val);
		}
		if (!val) setBorder(new TitledBorder("Time Steps (N/A)"));
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner non-commercial license
		DefaultComponentFactory compFactory = DefaultComponentFactory.getInstance();
		chkEnable = new JCheckBox();
		label1 = compFactory.createLabel("Min:");
		minSpinner = new JSpinner();
		minDate = compFactory.createLabel("");
		label2 = compFactory.createLabel("Max:");
		maxSpinner = new JSpinner();
		maxDate = compFactory.createLabel("");
		CellConstraints cc = new CellConstraints();

		//======== this ========
		setBorder(new TitledBorder("Time Steps"));
		// 2014 - underlying jgoodies class changed
		ColumnSpec[] aColumnSpec = ColumnSpec.decodeSpecs("max(min;10dlu)");
		ColumnSpec[] bColumnSpec = ColumnSpec.decodeSpecs("max(pref;40dlu)");	
		ColumnSpec cColumnSpec = new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW);	

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

		//---- chkEnable ----
		chkEnable.setText("Use Time Range");
		add(chkEnable, cc.xywh(1, 1, 6, 1));
		add(label1, cc.xy(2, 3));
		add(minSpinner, cc.xy(4, 3));

		//---- minDate ----
		minDate.setFont(new Font("Tahoma", Font.BOLD, 11));
		add(minDate, cc.xy(6, 3));
		add(label2, cc.xy(2, 5));
		add(maxSpinner, cc.xy(4, 5));

		//---- maxDate ----
		maxDate.setFont(new Font("Tahoma", Font.BOLD, 11));
		add(maxDate, cc.xy(6, 5));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner non-commercial license
	private JCheckBox chkEnable;
	private JLabel label1;
	private JSpinner minSpinner;
	private JLabel minDate;
	private JLabel label2;
	private JSpinner maxSpinner;
	private JLabel maxDate;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
	
	public int getMin() {
		return ((SpinnerNumberModel) minSpinner.getModel()).getNumber().intValue() - 1;
	}
	public int getMax() {
		return ((SpinnerNumberModel) maxSpinner.getModel()).getNumber().intValue() - 1;
	}
	public boolean getTimeEnabled() {
		return chkEnable.isSelected();
	}
}
