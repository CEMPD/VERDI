package anl.verdi.plot.anim;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.ListModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages

import anl.verdi.data.Axes;
import anl.verdi.data.DataFrame;
import anl.verdi.data.DataFrameAxis;
import anl.verdi.plot.gui.Plot;
import anl.verdi.plot.types.TimeAnimatablePlot;
import anl.verdi.plot.util.AnimationListener;
import anl.verdi.util.DateRange;
import anl.verdi.util.Utilities;

import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.Sizes;
import com.jidesoft.swing.CheckBoxList;


/**
 * @author User #2
 */
public class MultiPlotAnimation extends JPanel implements AnimationListener {
	static final Logger Logger = LogManager.getLogger(MultiPlotAnimation.class.getName());

	private static final long serialVersionUID = -6833233954013917115L;
	private DateRange currentRange = null;
	private int currentSteps = -1;
	private Set<Integer> badIndices = new HashSet<Integer>();
	private Set<Integer> currentSelections = new HashSet<Integer>();
	private JButton startButton;
	private DateModel dateModel = new DateModel();
	private MultiPlotAnimator animator;
	private File gifFile;

	private class DateModel {

		long start, end, interval;

		public Date getDate(int index) {
			long val = start + interval * index;
			if (val > end) val = end;
			return new Date(val);
		}

		boolean init(long start, long end, long interval) {
			boolean retVal = false;
			if (this.start != start) {
				this.start = start;
				retVal = true;
			}

			if (this.end != end) {
				this.end = end;
				retVal = true;
			}

			if (this.interval != interval) {
				this.interval = interval;
				retVal = true;
			}
			return retVal;
		}
	}

	private class ListRenderer extends DefaultListCellRenderer {

		/**
		 * 
		 */
		private static final long serialVersionUID = -7787305653195238102L;

		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (badIndices.contains(index)) {
				comp.setForeground(Color.RED);
			} else {
				comp.setForeground(Color.BLACK);
			}
			return comp;
		}
	}

	private static class PlotData {
		private Plot plot;
		private String label;
		private DateRange range;

		PlotData(Plot plot) {
			this.plot = plot;
			DataFrame frame = plot.getData().get(0);
			Axes<DataFrameAxis> axes = frame.getAxes();
			int origin = axes.getTimeAxis().getOrigin();
			GregorianCalendar start = axes.getDate(origin);
			GregorianCalendar end = axes.getDate(origin + axes.getTimeAxis().getExtent() - 1);
			Logger.debug("in MultiPlotAnimation just computed start and end GregorianCalendar");
			range = new DateRange(start, end);

			StringBuffer buf = new StringBuffer();
			DataFrame dataFrame = plot.getData().get(0);
			buf.append(dataFrame.getVariable().getName());
			buf.append(" (");
			buf.append(Utilities.formatShortDate(start));
			buf.append(" - ");
			buf.append(Utilities.formatShortDate(end));
			buf.append(", ");
			buf.append(axes.getTimeAxis().getExtent() - 1);
			buf.append(" Steps)");
			label = buf.toString();
		}

		public String toString() {
			return label;
		}

		public DateRange getRange() {
			return range;
		}

		public Plot getPlot() {
			return plot;
		}

		/**
		 * Gets the number of timesteps in the specified range, or -1
		 * if the range is invalid for this plto.
		 *
		 * @param range the date range
		 * @return the number of timesteps in the specified range, or -1
		 *         if the range is invalid for this plto.
		 */
		public int getStepCount(DateRange range) {
			Axes<DataFrameAxis> axes = plot.getData().get(0).getAxes();
			int start = axes.getTimeStep(new Date(range.getStart()));
			if (start == Axes.TIME_STEP_NOT_FOUND) return -1;
			int end = axes.getTimeStep(new Date(range.getEnd()));
			if (end == Axes.TIME_STEP_NOT_FOUND) return -1;
			return end - start;
		}
	}

	public MultiPlotAnimation() {
		Logger.debug("in constructor for MultiPlotAnimation.java");
		initComponents();

		((SpinnerNumberModel) startSpinner.getModel()).setMinimum(0);
		((SpinnerNumberModel) startSpinner.getModel()).setMaximum(0);
		((SpinnerNumberModel) endSpinner.getModel()).setMaximum(0);
		((SpinnerNumberModel) endSpinner.getModel()).setMaximum(0);

		addListeners();
	}

	private void addListeners() {
		startSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Integer val = (Integer) startSpinner.getValue();
				startLbl.setText(Utilities.formatShortDate(dateModel.getDate(val.intValue())));
			}
		});

		endSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Integer val = (Integer) endSpinner.getValue();
				endLbl.setText(Utilities.formatShortDate(dateModel.getDate(val.intValue())));
			}
		});

		plotList.getCheckBoxListSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					processListSelection();
				}
			}
		});
		plotList.setCellRenderer(new ListRenderer());

		gifChk.addActionListener(new ActionListener() {
		  public void actionPerformed(ActionEvent evt) {
			  gifFileBtn.setEnabled(gifChk.isSelected());
				gifFileLbl.setEnabled(gifChk.isSelected());
				if (gifChk.isSelected() && gifFile == null) {
					getGifFile();
				}
		  }
		});

		gifFileBtn.addActionListener(new ActionListener() {
		  public void actionPerformed(ActionEvent evt) {
		    getGifFile();
		  }
		});
	}

	private void processListSelection() {
		badIndices.clear();
		int[] vals = plotList.getCheckBoxListSelectedIndices();
		Set<Integer> selectedSet = new HashSet<Integer>();
		for (int val : vals) {
			selectedSet.add(val);
		}
		if (!selectedSet.containsAll(currentSelections)) currentRange = null;
		currentSelections = selectedSet;
		if (vals.length == 0) currentRange = null;
		else {
			ListModel listModel = plotList.getModel();
			if (currentRange == null || vals.length == 1) {
				PlotData plotData = (PlotData) listModel.getElementAt(vals[0]);
				currentRange = plotData.getRange();
				currentSteps = plotData.getStepCount(currentRange);
			}
			for (int val : vals) {
				PlotData plotData = (PlotData) listModel.getElementAt(val);
				DateRange range = plotData.getRange();
				DateRange newRange = currentRange.overlap(range);
				if (newRange == null) {
					badIndices.add(val);
				} else {
					int steps = plotData.getStepCount(newRange);
					if (steps == -1 || steps != currentSteps) {
						badIndices.add(val);
					} else {
						currentRange = newRange;
					}
				}
			}
		}
		boolean enabled = badIndices.isEmpty() && vals.length > 0;
		startButton.setEnabled(enabled);
		startSpinner.setEnabled(enabled);
		startLbl.setEnabled(enabled);
		endSpinner.setEnabled(enabled);
		endLbl.setEnabled(enabled);

		if (enabled) {
			setSpinners();
		}
	}

	private void getGifFile() {
		JFileChooser chooser = new JFileChooser();
		if (gifFile != null) chooser.setSelectedFile(gifFile);
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setFileFilter(new FileFilter() {

			public boolean accept(File f) {
				if (f.isDirectory()) return true;
				return f.getName().toLowerCase().endsWith(".gif");
			}

			public String getDescription() {
				return "GIF (*.gif)";
			}
		});

		File f = null;
		if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			f = chooser.getSelectedFile();
			if (f != null) {
				if (!f.getName().toLowerCase().endsWith(".gif")) {
					f = new File(f.getAbsolutePath() + ".gif");
				}
				gifFile = f;
				gifFileLbl.setText(gifFile.getName());
			}
		}
		if (f == null && gifFile == null) {
			gifFileBtn.setSelected(false);
		}
	}

	private void start() {
		animator = new MultiPlotAnimator();
		Object[] objs = plotList.getCheckBoxListSelectedValues();
		File file = null;
		if (gifChk.isSelected() && gifFile != null) file = gifFile;

		for (Object obj : objs) {
			PlotData data = (PlotData) obj;
			Axes<DataFrameAxis> axes = data.getPlot().getData().get(0).getAxes();
			int val = ((Integer) startSpinner.getValue()).intValue();
			int val2 = ((Integer) endSpinner.getValue()).intValue();
			int startIndex = Math.min(val, val2);
			int endIndex = Math.max(val, val2);
			int start = axes.getTimeStep(dateModel.getDate(startIndex));
			int end = axes.getTimeStep(dateModel.getDate(endIndex));
			animator.addPlotToAnimate((TimeAnimatablePlot) data.getPlot(), start, end, gifFile);
		}
		animator.addAnimationListener(this);
		startButton.setText("Stop");
		animator.start();
	}


	public void animationStopped() {
		startButton.setText("Start");
	}

	private void setSpinners() {
		if (currentRange == null) {
			startLbl.setText("");
			endLbl.setText("");
			startSpinner.setValue(0);
			startSpinner.setEnabled(false);
			startLbl.setEnabled(false);
			endSpinner.setEnabled(false);
			endSpinner.setValue(0);
			endLbl.setEnabled(false);
		} else {
			long start = currentRange.getStart();
			long end = currentRange.getEnd();
			boolean changed = dateModel.init(start, end, (end - start) / currentSteps);
			if (changed) {
				((SpinnerNumberModel) startSpinner.getModel()).setMinimum(0);
				((SpinnerNumberModel) startSpinner.getModel()).setMaximum(currentSteps);
				((SpinnerNumberModel) endSpinner.getModel()).setMaximum(0);
				((SpinnerNumberModel) endSpinner.getModel()).setMaximum(currentSteps);
				startSpinner.setValue(0);
				endSpinner.setValue(currentSteps);
				startLbl.setText(Utilities.formatShortDate(dateModel.getDate(0)));
				endLbl.setText(Utilities.formatShortDate(dateModel.getDate(currentSteps)));
			}
		}
	}

	public void init(JButton startButton, java.util.List<Plot> plots) {
		this.startButton = startButton;

		this.startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (MultiPlotAnimation.this.startButton.getText().equals("Start")) {
					start();
				} else {
					if (animator != null) animator.stop();
					MultiPlotAnimation.this.startButton.setText("Start");
				}
			}
		});

		this.startButton.setEnabled(false);
		startSpinner.setEnabled(false);
		startLbl.setEnabled(false);
		endSpinner.setEnabled(false);
		endLbl.setEnabled(false);

		Collections.sort(plots, new Comparator<Plot>() {
			public int compare(Plot o1, Plot o2) {	// 2014 added in .tlLowerCase() to remove case-sensitivity
				return o1.getData().get(0).getVariable().getName().toLowerCase().
								compareTo(o2.getData().get(0).getVariable().getName().toLowerCase());
			}
		});
		DefaultListModel model = new DefaultListModel();
		plotList.setModel(model);
		for (Plot plot : plots) {
			model.addElement(new PlotData(plot));
		}
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner non-commercial license
		DefaultComponentFactory compFactory = DefaultComponentFactory.getInstance();
		separator1 = compFactory.createSeparator("Select Plots");
		scrollPane1 = new JScrollPane();
		plotList = new CheckBoxList();
		panel1 = new JPanel();
		label1 = new JLabel();
		startSpinner = new JSpinner();
		startLbl = new JLabel();
		label2 = new JLabel();
		endSpinner = new JSpinner();
		endLbl = new JLabel();
		panel2 = new JPanel();
		gifChk = new JCheckBox();
		gifFileLbl = new JLabel();
		gifFileBtn = new JButton();
		separator2 = compFactory.createSeparator("");
		CellConstraints cc = new CellConstraints();

		//======== this ========
		// 2014
		ColumnSpec[] aColumnSpec = ColumnSpec.decodeSpecs("default:grow");
		RowSpec aRowSpec = new RowSpec(RowSpec.TOP, Sizes.dluY(84), 0.5);
		RowSpec bRowSpec = new RowSpec(RowSpec.FILL, Sizes.PREFERRED, 0.5);
		setLayout(new FormLayout(
			aColumnSpec,
			new RowSpec[] {
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				aRowSpec,
				FormFactory.RELATED_GAP_ROWSPEC,
				bRowSpec,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.LINE_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC
			}));
//		setLayout(new FormLayout(
//				ColumnSpec.decodeSpecs("default:grow"),
//				new RowSpec[] {
//					FormFactory.DEFAULT_ROWSPEC,
//					FormFactory.RELATED_GAP_ROWSPEC,
//					new RowSpec(RowSpec.TOP, Sizes.dluY(84), 0.5),
//					FormFactory.RELATED_GAP_ROWSPEC,
//					new RowSpec(RowSpec.FILL, Sizes.PREFERRED, 0.5),
//					FormFactory.RELATED_GAP_ROWSPEC,
//					FormFactory.DEFAULT_ROWSPEC,
//					FormFactory.LINE_GAP_ROWSPEC,
//					FormFactory.DEFAULT_ROWSPEC
//				}));
		add(separator1, cc.xy(1, 1));

		//======== scrollPane1 ========
		{
			scrollPane1.setViewportView(plotList);
		}
		add(scrollPane1, cc.xy(1, 3));

		//======== panel1 ========
		{
			// 2014
			ColumnSpec[] dColumnSpec = ColumnSpec.decodeSpecs("max(pref;40dlu)");
			ColumnSpec[] eColumnSpec = ColumnSpec.decodeSpecs("max(pref;40dlu):grow");
			panel1.setLayout(new FormLayout(
				new ColumnSpec[] {
					new ColumnSpec(Sizes.dluX(71)),
					FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
					dColumnSpec[0],
					FormFactory.UNRELATED_GAP_COLSPEC,
					eColumnSpec[0]
				},
				new RowSpec[] {
					FormFactory.DEFAULT_ROWSPEC,
					FormFactory.LINE_GAP_ROWSPEC,
					FormFactory.DEFAULT_ROWSPEC
				}));
//			panel1.setLayout(new FormLayout(
//					new ColumnSpec[] {
//						new ColumnSpec(Sizes.dluX(71)),
//						FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
//						new ColumnSpec("max(pref;40dlu)"),
//						FormFactory.UNRELATED_GAP_COLSPEC,
//						new ColumnSpec("max(pref;40dlu):grow")
//					},
//					new RowSpec[] {
//						FormFactory.DEFAULT_ROWSPEC,
//						FormFactory.LINE_GAP_ROWSPEC,
//						FormFactory.DEFAULT_ROWSPEC
//					}));

			//---- label1 ----
			label1.setText("Starting Time Step:");
			panel1.add(label1, cc.xy(1, 1));
			panel1.add(startSpinner, cc.xy(3, 1));

			//---- startLbl ----
			startLbl.setFont(new Font("Tahoma", Font.BOLD, 11));
			panel1.add(startLbl, cc.xy(5, 1));

			//---- label2 ----
			label2.setText("Ending Time Step:");
			panel1.add(label2, cc.xy(1, 3));
			panel1.add(endSpinner, cc.xy(3, 3));

			//---- endLbl ----
			endLbl.setFont(new Font("Tahoma", Font.BOLD, 11));
			panel1.add(endLbl, cc.xy(5, 3));
		}
		add(panel1, cc.xywh(1, 5, 1, 1, CellConstraints.DEFAULT, CellConstraints.TOP));

		//======== panel2 ========
		{
			// 2014
			ColumnSpec fColumnSpec = new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW);
			RowSpec[] fRowSpec = RowSpec.decodeSpecs("default");
			panel2.setLayout(new FormLayout(
				new ColumnSpec[] {
					FormFactory.DEFAULT_COLSPEC,
					FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
					fColumnSpec,
					FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
					FormFactory.DEFAULT_COLSPEC
				},
				fRowSpec));
//			panel2.setLayout(new FormLayout(
//					new ColumnSpec[] {
//						FormFactory.DEFAULT_COLSPEC,
//						FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
//						new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW),
//						FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
//						FormFactory.DEFAULT_COLSPEC
//					},
//					RowSpec.decodeSpecs("default")));

			//---- gifChk ----
			gifChk.setText("Make Animated GIF(s)");
			panel2.add(gifChk, cc.xy(1, 1));
			panel2.add(gifFileLbl, cc.xy(3, 1));

			//---- gifFileBtn ----
			gifFileBtn.setText("...");
			gifFileBtn.setMaximumSize(new Dimension(23, 23));
			gifFileBtn.setMinimumSize(new Dimension(23, 23));
			gifFileBtn.setPreferredSize(new Dimension(23, 23));
			gifFileBtn.setEnabled(false);
			panel2.add(gifFileBtn, cc.xy(5, 1));
		}
		add(panel2, cc.xy(1, 7));
		add(separator2, cc.xy(1, 9));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner non-commercial license
	private JComponent separator1;
	private JScrollPane scrollPane1;
	private CheckBoxList plotList;
	private JPanel panel1;
	private JLabel label1;
	private JSpinner startSpinner;
	private JLabel startLbl;
	private JLabel label2;
	private JSpinner endSpinner;
	private JLabel endLbl;
	private JPanel panel2;
	private JCheckBox gifChk;
	private JLabel gifFileLbl;
	private JButton gifFileBtn;
	private JComponent separator2;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
