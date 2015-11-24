package anl.verdi.plot.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.apache.logging.log4j.LogManager;		
import org.apache.logging.log4j.Logger;		

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import anl.verdi.data.DataUtilities;
import anl.verdi.plot.color.ColorMap;
import anl.verdi.plot.color.PaletteSelectionPanel;
import anl.verdi.plot.config.PlotConfiguration;

/**
 * 
 * @author qun
 *
 */
public class ThemeDialog extends JDialog {
	private static final long serialVersionUID = 1117048665491590187L;
	static final Logger Logger = LogManager.getLogger(ThemeDialog.class.getName());
	private Plot plot;

	public ThemeDialog(Frame owner) {
		super(owner);
		initComponents();
		addListeners();
	}

	public ThemeDialog(Dialog owner) {
		super(owner);
		initComponents();
		addListeners();
	}

	private void addListeners() {
		applButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				try {
					commit();
				} catch (Exception e) {
					String msg = e.getMessage();
					JOptionPane.showMessageDialog(ThemeDialog.this, msg, 
							"Configuration Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				try {
					commit();
					exit();
				} catch (Exception e) {
					String msg = e.getMessage();
					JOptionPane.showMessageDialog(ThemeDialog.this, msg, 
							"Configuration Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				exit();
			}
		});
	}

	private void exit() {
		this.dispose();
	}

	// commit the plot configuration
	private void commit() throws Exception {
		PlotConfiguration config = new PlotConfiguration();
		config.putObject(PlotConfiguration.PLOT_TYPE, plot.getType()); //NOTE: to differentiate plot types
		
//		if (tabbedPanel.indexOfTab("Color Map") != -1) {
//			config.putObject(TilePlotConfiguration.COLOR_MAP, colorMapPanel.getColorMap());
//		}
//
//		int index = tabbedPanel.indexOfTab("Titles");
//
//		if (index >= 0)
//			config = titlesPanel.fillConfiguration(config);
//
//		index = tabbedPanel.indexOfTab("Labels");
//
//		if (index >= 0)
//			config = labelsPanel.fillConfiguration(config);
//
//		index = tabbedPanel.indexOfTab("Other");
//
//		if (index >= 0)
//			config = otherPanel.fillConfiguration(config);
//
//		index = tabbedPanel.indexOfTab("Overlays");
//
//		if (index >= 0)
//			config = overlays.fillConfiguration(config);
//
//		plot.configure(config, Plot.ConfigSoure.GUI);
	}

	public void init(Plot plot) {
		init(plot, null);

	}

	public void init(Plot plot, DataUtilities.MinMax globalMinMax) {
		Logger.debug("just called init for ConfigDialog");
		this.plot = plot;
//		PlotConfiguration config = new PlotConfiguration(plot.getPlotConfiguration());
//		ColorMap map = (ColorMap) config.getObject(TilePlotConfiguration.COLOR_MAP);
//		if (map == null || globalMinMax == null) {
//			tabbedPanel.remove(tabbedPanel.indexOfTab("Color Map"));
//		} else {
//			// changed by Mary Ann Bitz 09/01/09
//			// globalMinMax did not seem to be used before
//			if(globalMinMax!=null){
//				colorMapPanel.init(map, new MinMax(globalMinMax.getMin(), globalMinMax.getMax()));
//			}else{
//				try {
//					colorMapPanel.init(map, new MinMax(map.getMin(), map.getMax()));
//				} catch (Exception e) {
//					JOptionPane.showMessageDialog(ThemeDialog.this, e.getMessage(), 
//							"Configuration Error", JOptionPane.ERROR_MESSAGE);
//				}
//			}
//		}
//
//		Logger.debug("ready to call initTitles(config)");
//		initTitles(config);
//		Logger.debug("just called initTitles(config)");
//		initLabels(config);
//		initOther(config);
//
//		if (config.getObject(TilePlotConfiguration.OBS_SHAPE_SIZE) == null) {
//			tabbedPanel.remove(tabbedPanel.indexOfTab("Overlays"));
//		} else {
//			initOverlays(config);
//		}
//
//		if ( plot instanceof FastTilePlot && colorMapPanel != null ){
//			colorMapPanel.setForFastTitle( true);
//		} else {
//			colorMapPanel.setForFastTitle( false);
//		}
	}

	private void initTitles(PlotConfiguration config) {
//		Logger.debug("in initTitles; ready to set title");
//		String title = plot.getTitle();
//		Logger.debug("title now set to: " + title);
//		titlesPanel.initTitle(title != null && !title.trim().isEmpty(), title, (Font) config
//				.getObject(PlotConfiguration.TITLE_FONT), (Color) config
//				.getObject(PlotConfiguration.TITLE_COLOR));
//
//		titlesPanel.initSubTitle1(config.getSubtitle1().trim().length() > 0,
//				config.getSubtitle1(), (Font) config
//						.getObject(PlotConfiguration.SUBTITLE_1_FONT),
//				(Color) config.getObject(PlotConfiguration.SUBTITLE_1_COLOR));
//
//		titlesPanel.initSubTitle2(config.getSubtitle2().trim().length() > 0,
//				config.getSubtitle2(), (Font) config
//						.getObject(PlotConfiguration.SUBTITLE_2_FONT),
//				(Color) config.getObject(PlotConfiguration.SUBTITLE_2_COLOR));
	}

	public void initColorMap(ColorMap map, DataUtilities.MinMax minMax) {
//		colorMapPanel.init(map, minMax);
	}

	private void initComponents() {
		dialogPane = new JPanel();
		contentPanel = new JPanel();
		themePanel = new ChartThemePanel();
		colorMapPanel = new PaletteSelectionPanel();
		buttonBar = new JPanel();
		applButton = new JButton();
		okButton = new JButton();
		cancelButton = new JButton();
		CellConstraints cc = new CellConstraints();

		// ======== this ========
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setModal(true);
		setTitle("Edit Chart Theme");
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		// ======== dialogPane ========
		{
			dialogPane.setBorder(Borders.DIALOG_BORDER);
			dialogPane.setLayout(new BorderLayout());

			// ======== contentPanel ========
			{
				contentPanel.setLayout(new BorderLayout());
				contentPanel.add(themePanel, BorderLayout.CENTER);
			}
			dialogPane.add(contentPanel, BorderLayout.CENTER);

			// ======== buttonBar ========
			{
				buttonBar.setBorder(Borders.BUTTON_BAR_GAP_BORDER);
				buttonBar.setLayout(new FormLayout(new ColumnSpec[] {
						FormFactory.GLUE_COLSPEC, FormFactory.BUTTON_COLSPEC,
						FormFactory.RELATED_GAP_COLSPEC, FormFactory.BUTTON_COLSPEC,
						FormFactory.RELATED_GAP_COLSPEC,
						FormFactory.BUTTON_COLSPEC }, RowSpec
						.decodeSpecs("pref")));
				
				// ---- applButton ----
				applButton.setText("Apply");
				buttonBar.add(applButton, cc.xy(2, 1));

				// ---- okButton ----
				okButton.setText("OK");
				buttonBar.add(okButton, cc.xy(4, 1));

				// ---- cancelButton ----
				cancelButton.setText("Cancel");
				buttonBar.add(cancelButton, cc.xy(6, 1));
			}
			dialogPane.add(buttonBar, BorderLayout.SOUTH);
		}
		contentPane.add(dialogPane, BorderLayout.CENTER);
		pack();
		setLocationRelativeTo(getOwner());
	}

	private JPanel dialogPane;
	private JPanel contentPanel;
	private ChartThemePanel themePanel;
	private PaletteSelectionPanel colorMapPanel;
	private JPanel buttonBar;
	private JButton applButton;
	private JButton okButton;
	private JButton cancelButton;
	
	public void enableScale( boolean enable) {
		colorMapPanel.enableScale( enable);
	}	
}