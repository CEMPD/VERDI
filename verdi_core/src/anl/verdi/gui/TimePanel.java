package anl.verdi.gui;

import java.awt.Font;
import java.awt.event.ActionListener;
import java.util.GregorianCalendar;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import anl.verdi.data.Axes;
import anl.verdi.data.CoordAxis;
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
	private class SpinnerListener implements ChangeListener {

		private JLabel label;
		public SpinnerListener(JLabel fld) {
			this.label = fld;
		}

		public void stateChanged(ChangeEvent e) {
			JSpinner source = (JSpinner)e.getSource();
			if (slidersOn) {
				int val = ((Number)source.getValue()).intValue() - 1;
				if (axes != null) {
					GregorianCalendar date = axes.getDate(val);
					label.setText(Utilities.formatShortDate(date));
				}
			}
		}
	}

	private Axes<CoordAxis> axes;
	private boolean slidersOn = false;

	public TimePanel() {
		System.out.println("in constructor for TimePanel");
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

	/**
	 * Resets the spinners according to the time axis,
	 * and sets their values to the specified min and max.
	 *
	 * @param axes the current dataset axes
	 * @param timeMin the min time
	 * @param timeMax the max time
	 */
	public void reset(Axes<CoordAxis> axes, int timeMin, int timeMax, boolean isUsed) {
		slidersOn = false;
		CoordAxis time = axes.getTimeAxis();
		this.axes = axes;
		int maxStep = (int) time.getRange().getExtent() - 1;
		SpinnerNumberModel model = (SpinnerNumberModel) minSpinner.getModel();
		model.setMinimum(1);
		model.setMaximum(maxStep + 1);
		model = (SpinnerNumberModel) maxSpinner.getModel();
		model.setMinimum(1);
		model.setMaximum(maxStep + 1);
		minSpinner.setValue(new Integer(timeMin + 1));
		maxSpinner.setValue(new Integer(timeMax + 1));
		maxDate.setText(Utilities.formatShortDate(axes.getDate(timeMax)));
		minDate.setText(Utilities.formatShortDate(axes.getDate(timeMin)));
		chkEnable.setSelected(isUsed);
		slidersOn = true;

		if (timeMin == timeMax) {
			minSpinner.setEnabled(false);
			maxSpinner.setEnabled(false);
			chkEnable.setEnabled(false);
		}

		setBorder(new TitledBorder("Time Steps (1 - "+ (maxStep + 1) + ")"));
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
}
