package anl.verdi.plot.gui;

import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import anl.verdi.plot.config.PlotConfiguration;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * @author User #2
 */
public class FootersPanel extends JPanel {
	private static final long serialVersionUID = 604253346376179488L;
	
	public FootersPanel() {
		initComponents();
		footer1Panel.setBorder(BorderFactory.createTitledBorder("Line One"));
		footer2Panel.setBorder(BorderFactory.createTitledBorder("Line Two"));
		obsLegendPanel.setBorder(BorderFactory.createTitledBorder("Observational Data Legend"));
	}

	public void initLine1(boolean use, String title, Font font, Color color) {
		footer1Panel.init(use, title, font, color);
	}

	public void initLine2(boolean use, String title, Font font, Color color) {
		footer2Panel.init(use, title, font, color);
	}

	public void initObsLegend(boolean use, String title, Font font, Color color) {
		obsLegendPanel.init(use, title, font, color);
	}

	public PlotConfiguration fillConfiguration(PlotConfiguration config) {
		if (footer1Panel.useTextField()) {
			String footer1_text = footer1Panel.getTextFieldContent();
			config.putObject(PlotConfiguration.FOOTER1, footer1_text);
			config.putObject(PlotConfiguration.FOOTER1_SHOW_LINE, true);
			config.putObject(PlotConfiguration.FOOTER1_FONT, footer1Panel.getSelectedFont());
			config.putObject(PlotConfiguration.FOOTER1_COLOR, footer1Panel.getSelectedColor());
			
			if (footer1_text == null || footer1_text.trim().isEmpty())
				config.putObject(PlotConfiguration.FOOTER1_AUTO_TEXT, true);
		} else {
			config.putObject(PlotConfiguration.FOOTER1, "");
			config.putObject(PlotConfiguration.FOOTER1_SHOW_LINE, false);
		}
		
		if (footer1Panel.textFieldModified())
			config.putObject(PlotConfiguration.FOOTER1_AUTO_TEXT, false);

		if (footer2Panel.useTextField()) {
			String footer2_text = footer2Panel.getTextFieldContent();
			config.putObject(PlotConfiguration.FOOTER2, footer2_text);
			config.putObject(PlotConfiguration.FOOTER2_SHOW_LINE, true);
			config.putObject(PlotConfiguration.FOOTER2_FONT, footer2Panel.getSelectedFont());
			config.putObject(PlotConfiguration.FOOTER2_COLOR, footer2Panel.getSelectedColor());
			
			if (footer2_text == null || footer2_text.trim().isEmpty())
				config.putObject(PlotConfiguration.FOOTER2_AUTO_TEXT, true);
		} else {
			config.putObject(PlotConfiguration.FOOTER2, "");
			config.putObject(PlotConfiguration.FOOTER2_SHOW_LINE, false);
		}
		
		if (footer2Panel.textFieldModified())
			config.putObject(PlotConfiguration.FOOTER2_AUTO_TEXT, false);

		if (obsLegendPanel.useTextField()) {
			config.putObject(PlotConfiguration.OBS_SHOW_LEGEND, true);
			config.putObject(PlotConfiguration.OBS_LEGEND_FONT, obsLegendPanel.getSelectedFont());
			config.putObject(PlotConfiguration.OBS_LEGEND_COLOR, obsLegendPanel.getSelectedColor());
		} else {
			config.putObject(PlotConfiguration.OBS_SHOW_LEGEND, false);
		}

		return config;
	}

	private void initComponents() {
		footer1Panel = new FooterPanel();
		footer2Panel = new FooterPanel();
		obsLegendPanel = new FooterPanel();
		obsLegendPanel.disableText();
		CellConstraints cc = new CellConstraints();

		//======== this ========
		// 2014
		ColumnSpec[] aColumnSpec = ColumnSpec.decodeSpecs("default:grow");
		setLayout(new FormLayout(
						aColumnSpec,
						new RowSpec[]{
										FormFactory.DEFAULT_ROWSPEC,
										FormFactory.LINE_GAP_ROWSPEC,
										FormFactory.DEFAULT_ROWSPEC,
										FormFactory.LINE_GAP_ROWSPEC,
										FormFactory.DEFAULT_ROWSPEC
						}));
//		setLayout(new FormLayout(
//				ColumnSpec.decodeSpecs("default:grow"),
//				new RowSpec[]{
//								FormFactory.DEFAULT_ROWSPEC,
//								FormFactory.LINE_GAP_ROWSPEC,
//								FormFactory.DEFAULT_ROWSPEC,
//								FormFactory.LINE_GAP_ROWSPEC,
//								FormFactory.DEFAULT_ROWSPEC
//				}));
		add(footer1Panel, cc.xy(1, 1));
		add(footer2Panel, cc.xy(1, 3));
		add(obsLegendPanel, cc.xy(1, 5));
	}

	private FooterPanel footer1Panel;
	private FooterPanel footer2Panel;
	private FooterPanel obsLegendPanel;
}
