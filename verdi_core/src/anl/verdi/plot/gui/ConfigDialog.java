package anl.verdi.plot.gui;

// appears to be configuration dialog box; shows color map
// includes tabbed panels titles, labels, other, and overlay

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

import anl.verdi.data.DataUtilities;
import anl.verdi.data.DataUtilities.MinMax;
import anl.verdi.plot.color.ColorMap;
import anl.verdi.plot.color.PaletteSelectionPanel;
import anl.verdi.plot.config.PlotConfiguration;
import anl.verdi.plot.config.TilePlotConfiguration;
import anl.verdi.plot.gui.GTTilePlot;
//import anl.verdi.plot.config.TimeSeriesPlotConfiguration;
//import anl.verdi.plot.config.VectorPlotConfiguration;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/*
 * Created by JFormDesigner on Wed May 09 09:52:58 EDT 2007
 */

/**
 * @author User #2
 */
public class ConfigDialog extends JDialog {
	private static final long serialVersionUID = -2353833703730870749L;
	private Plot plot;

	public ConfigDialog(Frame owner) {
		super(owner);
		initComponents();
		addListeners();
	}

	public ConfigDialog(Dialog owner) {
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
					JOptionPane.showMessageDialog(ConfigDialog.this, msg, 
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
					JOptionPane.showMessageDialog(ConfigDialog.this, msg, 
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

	// writes the plot configuration to the PlotConfiguration object (read and used by other parts of VERDI)
	private void commit() throws Exception {
		PlotConfiguration config = new PlotConfiguration();
		if (tabbedPanel.indexOfTab("Color Map") != -1) {
			config.putObject(TilePlotConfiguration.COLOR_MAP, colorMapPanel.getColorMap());
		}

		int index = tabbedPanel.indexOfTab("Titles");

		if (index >= 0)
			config = titlesPanel.fillConfiguration(config);

		index = tabbedPanel.indexOfTab("Labels");

		if (index >= 0)
			config = labelsPanel.fillConfiguration(config);

		index = tabbedPanel.indexOfTab("Other");

		if (index >= 0)
			config = otherPanel.fillConfiguration(config);

		index = tabbedPanel.indexOfTab("Overlays");

		if (index >= 0)
			config = overlays.fillConfiguration(config);

		plot.configure(config, Plot.ConfigSource.GUI);
	}

	public void init(Plot plot) {
		init(plot, null);

	}

	public void init(Plot plot, DataUtilities.MinMax globalMinMax) {
		this.plot = plot;
		PlotConfiguration config = new PlotConfiguration(plot.getPlotConfiguration());
		ColorMap map = (ColorMap) config.getObject(TilePlotConfiguration.COLOR_MAP);
		if (map == null || globalMinMax == null) {
			tabbedPanel.remove(tabbedPanel.indexOfTab("Color Map"));
		} else {
			// changed by Mary Ann Bitz 09/01/09
			// globalMinMax did not seem to be used before
			if(globalMinMax!=null){
				colorMapPanel.init(map, new MinMax(globalMinMax.getMin(), globalMinMax.getMax()));
			}else{
				try {
					colorMapPanel.init(map, new MinMax(map.getMin(), map.getMax()));
				} catch (Exception e) {
					JOptionPane.showMessageDialog(ConfigDialog.this, e.getMessage(), 
							"Configuration Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}

		initTitles(config);
		initLabels(config);
		initOther(config);

		if (config.getObject(TilePlotConfiguration.OBS_SHAPE_SIZE) == null) {
			tabbedPanel.remove(tabbedPanel.indexOfTab("Overlays"));
		} else {
			initOverlays(config);
		}

		if ( plot instanceof GTTilePlot && colorMapPanel != null ){
			colorMapPanel.setForFastTitle( true);
		} else {
			colorMapPanel.setForFastTitle( false);
		}
	}

	private void initOverlays(PlotConfiguration config) {
		Integer stroke = (Integer) config
				.getObject(TilePlotConfiguration.OBS_STROKE_SIZE);
		Integer size = (Integer) config
				.getObject(TilePlotConfiguration.OBS_SHAPE_SIZE);
		overlays.init(stroke, size);
	}

	private void initOther(PlotConfiguration config) {
		Boolean showGrid = (Boolean) config.getObject(TilePlotConfiguration.SHOW_GRID_LINES);
		if (showGrid == null) {
			otherPanel.setGridLinePanelEnabled(false);
		} else {
			Color color = config.getColor(TilePlotConfiguration.GRID_LINE_COLOR);
			if (color == null)
				color = Color.BLACK;
			otherPanel.initGridLines(color, showGrid);
		}

//		2014 removed old Vector Plot
//		Color color = config.getColor(VectorPlotConfiguration.VECTOR_COLOR);
//		if (color == null) {
//			otherPanel.setVectorPanelEnabled(false);
//		} else {
//			otherPanel.initVector(color);
//		}
//
//		color = config.getColor(TimeSeriesPlotConfiguration.SERIES_COLOR);
//		if (color == null)
//			otherPanel.setSeriesColorEnabled(false);
//		else
//			otherPanel.initSeries(color);
	}

	private void initLabels(PlotConfiguration config) {
		labelsPanel.initDomain(
				config.getString(PlotConfiguration.DOMAIN_LABEL), config
						.getFont(PlotConfiguration.DOMAIN_FONT), config
						.getColor(PlotConfiguration.DOMAIN_COLOR));
		labelsPanel.initDomainTick((Boolean) config
				.getObject(PlotConfiguration.DOMAIN_SHOW_TICK), config
				.getFont(PlotConfiguration.DOMAIN_TICK_FONT), config
				.getColor(PlotConfiguration.DOMAIN_TICK_COLOR), (Integer)config
				.getObject(PlotConfiguration.DOMAIN_TICK_NUMBER));

		labelsPanel.initRange(config.getString(PlotConfiguration.RANGE_LABEL),
				config.getFont(PlotConfiguration.RANGE_FONT), config
						.getColor(PlotConfiguration.RANGE_COLOR));
		labelsPanel.initRangeTick((Boolean) config
				.getObject(PlotConfiguration.RANGE_SHOW_TICK), config
				.getFont(PlotConfiguration.RANGE_TICK_FONT), config
				.getColor(PlotConfiguration.RANGE_TICK_COLOR), (Integer)config
				.getObject(PlotConfiguration.RANGE_TICK_NUMBER));

		labelsPanel.initUnits(config.getString(PlotConfiguration.UNITS), config
				.getFont(PlotConfiguration.UNITS_FONT), config
				.getColor(PlotConfiguration.UNITS_COLOR));
		labelsPanel.initUnitsTick((Boolean) config
				.getObject(PlotConfiguration.UNITS_SHOW_TICK), config
				.getFont(PlotConfiguration.UNITS_TICK_FONT), config
				.getColor(PlotConfiguration.UNITS_TICK_COLOR), (Integer)config
				.getObject(PlotConfiguration.UNITS_TICK_NUMBER));
		
		labelsPanel.initFooter1((Boolean)config.getObject(PlotConfiguration.FOOTER1_SHOW_LINE), config
				.getString(PlotConfiguration.FOOTER1), config
				.getFont(PlotConfiguration.FOOTER1_FONT), config
				.getColor(PlotConfiguration.FOOTER1_COLOR));
		labelsPanel.initFooter2((Boolean)config.getObject(PlotConfiguration.FOOTER2_SHOW_LINE), config
				.getString(PlotConfiguration.FOOTER2), config
				.getFont(PlotConfiguration.FOOTER2_FONT), config
				.getColor(PlotConfiguration.FOOTER2_COLOR));
		labelsPanel.initObsLegend((Boolean)config.getObject(PlotConfiguration.OBS_SHOW_LEGEND), "", config
				.getFont(PlotConfiguration.OBS_LEGEND_FONT), config
				.getColor(PlotConfiguration.OBS_LEGEND_COLOR));

		if (config.getObject(PlotConfiguration.Z_LABEL) != null) {
			labelsPanel.initZ(config.getString(PlotConfiguration.Z_LABEL),
					config.getFont(PlotConfiguration.Z_FONT), config
							.getColor(PlotConfiguration.Z_COLOR));
			labelsPanel.initZTick((Boolean) config
					.getObject(PlotConfiguration.Z_SHOW_TICK), config
					.getFont(PlotConfiguration.Z_TICK_FONT), config
					.getColor(PlotConfiguration.Z_TICK_COLOR), (Integer)config
					.getObject(PlotConfiguration.Z_TICK_NUMBER));
		} else {
			labelsPanel.removeZ();
		}
	}

	private void initTitles(PlotConfiguration config) {
		String title = config.getTitle(); // plot.getTitle(); Feb 2016 use config here like everything else 
		String showTitle = config.getShowTitle();
		boolean bShowTitle = true; 
		if (showTitle.compareTo("FALSE") == 0)
			bShowTitle = false;
		titlesPanel.initTitle(bShowTitle, title,	// title != null && !title.trim().isEmpty(), title, 
				(Font) config.getObject(PlotConfiguration.TITLE_FONT), 
				(Color) config.getObject(PlotConfiguration.TITLE_COLOR));

		bShowTitle = true;
		String showSubTitle1 = config.getShowSubtitle1();
		if (showSubTitle1.compareTo("FALSE") == 0)
			bShowTitle = false;
		titlesPanel.initSubTitle1(bShowTitle,	// config.getSubtitle1().trim().length() > 0,
				config.getSubtitle1(), 
				(Font) config.getObject(PlotConfiguration.SUBTITLE_1_FONT),
				(Color) config.getObject(PlotConfiguration.SUBTITLE_1_COLOR));

		bShowTitle = true;
		String showSubTitle2 = config.getShowSubtitle2();
		if(showSubTitle2.compareTo("FALSE") == 0)
			bShowTitle = false;
		titlesPanel.initSubTitle2(bShowTitle,	// config.getSubtitle2().trim().length() > 0,
				config.getSubtitle2(), 
				(Font) config.getObject(PlotConfiguration.SUBTITLE_2_FONT),
				(Color) config.getObject(PlotConfiguration.SUBTITLE_2_COLOR));
	}

	public void initColorMap(ColorMap map, DataUtilities.MinMax minMax) {
		colorMapPanel.init(map, minMax);
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		// Generated using JFormDesigner non-commercial license
		dialogPane = new JPanel();
		contentPanel = new JPanel();
		tabbedPanel = new JTabbedPane();
		titlesPanel = new ChartTitlesPanel();
		colorMapPanel = new PaletteSelectionPanel();
		labelsPanel = new LabelsPanel();
		otherPanel = new OtherConfigPanel();
		overlays = new OverlaysPanel();
		buttonBar = new JPanel();
		applButton = new JButton();
		okButton = new JButton();
		cancelButton = new JButton();
		CellConstraints cc = new CellConstraints();

		// ======== this ========
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setModal(true);
		setTitle("Configure Plot");
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		// ======== dialogPane ========
		{
			dialogPane.setBorder(Borders.DIALOG_BORDER);
			dialogPane.setLayout(new BorderLayout());

			// ======== contentPanel ========
			{
				contentPanel.setLayout(new BorderLayout());

				// ======== tabbedPanel ========
				{
					tabbedPanel.addTab("Titles", titlesPanel);

					tabbedPanel.addTab("Color Map", colorMapPanel);

					tabbedPanel.addTab("Labels", labelsPanel);

					tabbedPanel.addTab("Other", otherPanel);

					tabbedPanel.addTab("Overlays", overlays);

				}
				contentPanel.add(tabbedPanel, BorderLayout.CENTER);
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
		// JFormDesigner - End of component initialization
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	// Generated using JFormDesigner non-commercial license
	private JPanel dialogPane;
	private JPanel contentPanel;
	private JTabbedPane tabbedPanel;
	private ChartTitlesPanel titlesPanel;
	private PaletteSelectionPanel colorMapPanel;
	private LabelsPanel labelsPanel;
	private OtherConfigPanel otherPanel;
	private OverlaysPanel overlays;
	private JPanel buttonBar;
	private JButton applButton;
	private JButton okButton;
	private JButton cancelButton;
	// JFormDesigner - End of variables declaration //GEN-END:variables
	
	public void enableScale( boolean enable) {
		colorMapPanel.enableScale( enable);
	}	
}
