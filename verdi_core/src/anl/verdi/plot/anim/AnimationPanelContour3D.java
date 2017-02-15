// 2014 copy of AnimationPanel specifically for Contour3D so on-screen animation is supported but
// generation of animated GIF, etc. is disabled (currently hangs VERDI)

package anl.verdi.plot.anim;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.GregorianCalendar;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import anl.verdi.data.Axes;
import anl.verdi.data.DataFrameAxis;
import anl.verdi.plot.types.TimeAnimatablePlot;
import anl.verdi.plot.util.AnimationListener;
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
public class AnimationPanelContour3D extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5856679324306579177L;

	private PlotAnimator animator;
	
	private static File lastChosenFolder;
	
	private static final int DEFAULT_DELAY = 200;
	private static final int MINIMUM_DELAY = 50;
	private static final int MAXIMUM_DELAY = 3000;


	private class SpinnerListener implements ChangeListener {

		private JLabel label;

		public SpinnerListener(JLabel fld) {
			this.label = fld;
		}

		public void stateChanged(ChangeEvent e) {
			JSpinner source = (JSpinner) e.getSource();

			int val = ((Number) source.getValue()).intValue() - 1;
			if (axes != null) {
				GregorianCalendar date = axes.getDate(val);
				label.setText(Utilities.formatShortDate(date));
			}
		}
	}

	private Axes<DataFrameAxis> axes;
	private JDialog dialog;
	private TimeAnimatablePlot plot;
	private File movieFile, gifFile, aviFile;

	public AnimationPanelContour3D() {
		initComponents();
		initListeners();
	}

	private void initListeners() {
		startBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (startBtn.getText().equals("Start")) {
					start();
				} else {
					if (animator != null) animator.stop();
					startBtn.setText("Start");
				}
			}
		});

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
		
		aviChk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				aviFileBtn.setEnabled(aviChk.isSelected());
				aviFileLbl.setEnabled(aviChk.isSelected());
				if (aviChk.isSelected() && aviFile == null) {
					getAviFile();
				}
			}
		});

		aviFileBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				getAviFile();
			}
		});

		movieChk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				fileBtn.setEnabled(movieChk.isSelected());
				fileLbl.setEnabled(movieChk.isSelected());
				if (movieChk.isSelected() && movieFile == null) {
					getMovieFile();
				}
			}
		});

		fileBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				getMovieFile();
			}
		});
	}

	private void getGifFile() {
		JFileChooser chooser = new JFileChooser();
		
		if (lastChosenFolder != null)
			chooser.setCurrentDirectory(lastChosenFolder);
		
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
				lastChosenFolder = gifFile.getParentFile();
			}
		}
		if (f == null && gifFile == null) {
			gifFileBtn.setSelected(false);
		}
	}
	
	private void getAviFile() {
		JFileChooser chooser = new JFileChooser();
		
		if (lastChosenFolder != null)
			chooser.setCurrentDirectory(lastChosenFolder);
		
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setFileFilter(new FileFilter() {

			public boolean accept(File f) {
				if (f.isDirectory()) return true;
				return f.getName().toLowerCase().endsWith(".avi");
			}

			public String getDescription() {
				return "Audio Video Interleave (*.avi)";
			}
		});

		File f = lastChosenFolder;
		if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			f = chooser.getSelectedFile();
			if (f != null) {
				if (!f.getName().endsWith(".avi")) {
					f = new File(f.getAbsolutePath() + ".avi");
				}
				aviFile = f;
				aviFileLbl.setText(aviFile.getName());
				lastChosenFolder = aviFile.getParentFile();
			}
		}
		if (f == null && aviFile == null) {
			aviFileBtn.setSelected(false);
		}
	}

	private void getMovieFile() {
		JFileChooser chooser = new JFileChooser();
		
		if (lastChosenFolder != null)
			chooser.setCurrentDirectory(lastChosenFolder);
		
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setFileFilter(new FileFilter() {

			public boolean accept(File f) {
				if (f.isDirectory()) return true;
				return f.getName().toLowerCase().endsWith(".mov");
			}

			public String getDescription() {
				return "Quicktime (*.mov)";
			}
		});

		File f = null;
		if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			f = chooser.getSelectedFile();
			if (f != null) {
				if (!f.getName().endsWith(".mov")) {
					f = new File(f.getAbsolutePath() + ".mov");
				}
				movieFile = f;
				fileLbl.setText(movieFile.getName());
				lastChosenFolder = movieFile.getParentFile();
			}
		}
		if (f == null && movieFile == null) {
			fileBtn.setSelected(false);
		}
	}

	private void start() {
		startBtn.setText("Stop");
		int min = ((Integer) minSpinner.getValue()).intValue() - 1;
		int max = ((Integer) maxSpinner.getValue()).intValue() - 1;
		int end = Math.max(min, max) - axes.getTimeAxis().getOrigin();
		int start = Math.min(min, max) - axes.getTimeAxis().getOrigin();
		
		int delay = DEFAULT_DELAY;
		try {
			delay = Integer.parseInt(delayFld.getText());
		} catch (NumberFormatException e) {
			// badly formatted values will use previously set default delay
		}
		if (delay < MINIMUM_DELAY) delay = MINIMUM_DELAY;
		if (delay > MAXIMUM_DELAY) delay = MAXIMUM_DELAY;
		// update text field in case value was changed
		delayFld.setText(String.valueOf(delay));

		animator = new PlotAnimator(plot);
		File mFile = movieFile;
		File gFile = gifFile;
		File avFile = aviFile;
		
		if (!movieChk.isSelected()) mFile = null;
		if (!gifChk.isSelected()) gFile = null;
		if (!aviChk.isSelected()) avFile = null;
		
		animator.addAnimationListener(new AnimationListener() {
			public void animationStopped() {
				startBtn.setText("Start");
			}
		});
		animator.start(start, end, delay, mFile, gFile, avFile);
	}

	public void init(Axes<DataFrameAxis> axes, TimeAnimatablePlot plot) {
		this.axes = axes;
		this.plot = plot;
		DataFrameAxis time = axes.getTimeAxis();
		this.axes = axes;
		int min = (int) (time.getRange().getOrigin() + 1);
		int max = min + (int) time.getRange().getExtent() - 1;

		SpinnerNumberModel model = (SpinnerNumberModel) minSpinner.getModel();
		model.setMinimum(min);
		model.setMaximum(max);
		model = (SpinnerNumberModel) maxSpinner.getModel();
		model.setMinimum(min);
		model.setMaximum(max);
		minSpinner.setValue(new Integer(min));
		maxSpinner.setValue(new Integer(max));
		maxDate.setText(Utilities.formatShortDate(axes.getDate(max - 1)));
		minDate.setText(Utilities.formatShortDate(axes.getDate(min - 1)));

		minSpinner.addChangeListener(new SpinnerListener(minDate));
		maxSpinner.addChangeListener(new SpinnerListener(maxDate));

		Window window = SwingUtilities.getWindowAncestor(plot.getPanel());

		if (window instanceof JFrame) 
			dialog = new JDialog((JFrame) window, "Animate Plot", false);
		else 
			dialog = new JDialog((JDialog) window, "Animate Plot", false);

		dialog.setLayout(new BorderLayout());
		dialog.add(this, BorderLayout.CENTER);
		dialog.setLocationRelativeTo(plot.getPanel());
		dialog.pack();
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setVisible(true);
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner non-commercial license
		DefaultComponentFactory compFactory = DefaultComponentFactory.getInstance();
		label1 = compFactory.createLabel("Starting Time Step:");
		minSpinner = new JSpinner();
		label2 = compFactory.createLabel("Ending Time Step:");
		maxSpinner = new JSpinner();
		delayLbl = compFactory.createLabel("Frame Delay (ms):");
		delayFld = new JTextField();
		delayFld.setText(String.valueOf(DEFAULT_DELAY));
		minDate = compFactory.createLabel("");
		movieChk = new JCheckBox();
		movieChk.setEnabled(false); 	// 2014
		maxDate = compFactory.createLabel("");
		gifChk = new JCheckBox();
		gifChk.setEnabled(true); 		// 2014
		gifFileLbl = new JLabel();
		gifFileBtn = new JButton();
		aviChk = new JCheckBox();
		aviChk.setEnabled(false);
		aviFileLbl = new JLabel();
		aviFileBtn = new JButton();
		fileLbl = new JLabel();
		fileBtn = new JButton();
		separator1 = compFactory.createSeparator("");
		panel1 = new JPanel();
		startBtn = new JButton();
		CellConstraints cc = new CellConstraints();

		//======== this ========
		setBorder(new TitledBorder("Animate Plot"));
		// 2014
		ColumnSpec[] aColumnSpec = ColumnSpec.decodeSpecs("max(pref;40dlu)");
		ColumnSpec bColumnSpec = new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW);
		setLayout(new FormLayout(
						new ColumnSpec[]{
										FormFactory.DEFAULT_COLSPEC,
										FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
										aColumnSpec[0],
										FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
										bColumnSpec,
										FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
										FormFactory.PREF_COLSPEC
						},
						new RowSpec[]{
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
										FormFactory.PREF_ROWSPEC,
										FormFactory.LINE_GAP_ROWSPEC,
										FormFactory.DEFAULT_ROWSPEC
						}));
//		setLayout(new FormLayout(
//				new ColumnSpec[]{
//								FormFactory.DEFAULT_COLSPEC,
//								FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
//								new ColumnSpec("max(pref;40dlu)"),
//								FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
//								new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW),
//								FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
//								FormFactory.PREF_COLSPEC
//				},
//				new RowSpec[]{
//								FormFactory.DEFAULT_ROWSPEC,
//								FormFactory.LINE_GAP_ROWSPEC,
//								FormFactory.DEFAULT_ROWSPEC,
//								FormFactory.LINE_GAP_ROWSPEC,
//								FormFactory.DEFAULT_ROWSPEC,
//								FormFactory.LINE_GAP_ROWSPEC,
//								FormFactory.DEFAULT_ROWSPEC,
//								FormFactory.LINE_GAP_ROWSPEC,
//								FormFactory.DEFAULT_ROWSPEC,
//								FormFactory.LINE_GAP_ROWSPEC,
//								FormFactory.PREF_ROWSPEC,
//								FormFactory.LINE_GAP_ROWSPEC,
//								FormFactory.DEFAULT_ROWSPEC
//				}));
		add(label1, cc.xy(1, 1));
		add(minSpinner, cc.xy(3, 1));
		add(label2, cc.xy(1, 3));
		add(maxSpinner, cc.xy(3, 3));

		//---- minDate ----
		minDate.setFont(new Font("Tahoma", Font.BOLD, 11));
		add(minDate, cc.xywh(5, 1, 3, 1));
		
		//---- frame delay ----
		add(delayLbl, cc.xy(1, 5));
		add(delayFld, cc.xy(3, 5));

		//---- movieChk ----
		movieChk.setText("Make Movie");
		add(movieChk, cc.xy(1, 11));

		//---- maxDate ----
		maxDate.setFont(new Font("Tahoma", Font.BOLD, 11));
		add(maxDate, cc.xywh(5, 3, 3, 1));

		//---- gifChk ----
		gifChk.setText("Make Animated GIF");
		add(gifChk, cc.xy(1, 7));
		add(gifFileLbl, cc.xywh(3, 7, 3, 1));
		
		//---- aviChk ----
		aviChk.setText("Make AVI");
		add(aviChk, cc.xy(1, 9));
		add(aviFileLbl, cc.xywh(3, 9, 3, 1));

		//---- gifFileBtn ----
		gifFileBtn.setText("...");
		gifFileBtn.setMaximumSize(new Dimension(23, 23));
		gifFileBtn.setMinimumSize(new Dimension(23, 23));
		gifFileBtn.setPreferredSize(new Dimension(23, 23));
		gifFileBtn.setEnabled(false);
		add(gifFileBtn, cc.xy(7, 7));
		
		//---- aviFileBtn ----
		aviFileBtn.setText("...");
		aviFileBtn.setMaximumSize(new Dimension(23, 23));
		aviFileBtn.setMinimumSize(new Dimension(23, 23));
		aviFileBtn.setPreferredSize(new Dimension(23, 23));
		aviFileBtn.setEnabled(false);
		add(aviFileBtn, cc.xy(7, 9));

		//---- fileLbl ----
		fileLbl.setEnabled(false);
		add(fileLbl, cc.xywh(3, 9, 3, 1));

		//---- fileBtn ----
		fileBtn.setText("...");
		fileBtn.setEnabled(false);
		fileBtn.setMaximumSize(new Dimension(23, 23));
		fileBtn.setMinimumSize(new Dimension(23, 23));
		fileBtn.setPreferredSize(new Dimension(23, 23));
		add(fileBtn, cc.xy(7, 11));
		add(separator1, cc.xywh(1, 13, 7, 1));

		//======== panel1 ========
		{
			// 2014
			ColumnSpec cColumnSpec = new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW);
			RowSpec[] aRowSpec = RowSpec.decodeSpecs("default");
			panel1.setLayout(new FormLayout(
							new ColumnSpec[]{
											cColumnSpec,
											FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
											FormFactory.DEFAULT_COLSPEC,
											FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
											FormFactory.DEFAULT_COLSPEC
							},
							aRowSpec));
//			panel1.setLayout(new FormLayout(
//					new ColumnSpec[]{
//									new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW),
//									FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
//									FormFactory.DEFAULT_COLSPEC,
//									FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
//									FormFactory.DEFAULT_COLSPEC
//					},
//					RowSpec.decodeSpecs("default")));

			//---- startBtn ----
			startBtn.setText("Start");
			panel1.add(startBtn, cc.xy(5, 1));
		}
		add(panel1, cc.xywh(1, 15, 7, 1));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner non-commercial license
	private JLabel label1;
	private JSpinner minSpinner;
	private JLabel label2;
	private JSpinner maxSpinner;
	private JLabel minDate;
	private JCheckBox movieChk;
	private JLabel maxDate;
	private JCheckBox gifChk;
	private JLabel gifFileLbl;
	private JButton gifFileBtn;
	private JLabel fileLbl;
	private JButton fileBtn;
	private JCheckBox aviChk;
	private JLabel aviFileLbl;
	private JButton aviFileBtn;
	private JComponent separator1;
	private JPanel panel1;
	private JButton startBtn;
	private JLabel delayLbl;
	private JTextField delayFld;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
