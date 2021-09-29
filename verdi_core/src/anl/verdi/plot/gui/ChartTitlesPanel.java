package anl.verdi.plot.gui;

// Appears to set up the configuration object config with string, font, and color for
// title, subtitle1, and subtitle2

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
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
public class ChartTitlesPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3469212269628821152L;
	public ChartTitlesPanel() {
		initComponents();
		subtitle1Panel.setBorder(BorderFactory.createTitledBorder("Subtitle 1"));
		subtitle2Panel.setBorder(BorderFactory.createTitledBorder("Subtitle 2"));
	}

	public void initTitle(boolean use, String title, Font font, Color color) {
		titlePanel.init(use, title, font, color);
	}

	public void initSubTitle1(boolean use, String title, Font font, Color color) {
		subtitle1Panel.init(use, title, font, color);
	}

	public void initSubTitle2(boolean use, String title, Font font, Color color) {
		subtitle2Panel.init(use, title, font, color);
	}

	public PlotConfiguration fillConfiguration(PlotConfiguration config) {
		if (titlePanel.useTitle() && (titlePanel.getTitle() != null || titlePanel.getTitle().length()>1)) {
			config.setShowTitle("TRUE");	// Feb 2016 explicitly save value of checkbox
			config.setTitle(titlePanel.getTitle());
			config.putObject(PlotConfiguration.TITLE_FONT, titlePanel.getSelectedFont());
			config.putObject(PlotConfiguration.TITLE_COLOR, titlePanel.getSelectedColor());
		} else { 	// Dec 2015 If user deselects the Show Title checkbox OR selects checkbox but either title is null or length < 1
			//titlePanel.setTitleNull();
			config.setTitle(titlePanel.getTitle());
			config.setShowTitle("FALSE");	// Feb 2016 explicitly save value of checkbox
		}

		if (subtitle1Panel.useTitle()) {
			config.setShowSubtitle1("TRUE");	// Feb 2016 explicitly save value of checkbox
			config.setSubtitle1(subtitle1Panel.getTitle());
			config.putObject(PlotConfiguration.SUBTITLE_1_FONT, subtitle1Panel.getSelectedFont());
			config.putObject(PlotConfiguration.SUBTITLE_1_COLOR, subtitle1Panel.getSelectedColor());
		} else {
			config.setSubtitle1("");
			config.setShowSubtitle1("FALSE");	// Feb 2016 explicitly save value of checkbox
		}

		if (subtitle2Panel.useTitle()) {
			config.setShowSubtitle2("TRUE");	// Feb 2016 explicitly save value of checkbox
			config.setSubtitle2(subtitle2Panel.getTitle());
			config.putObject(PlotConfiguration.SUBTITLE_2_FONT, subtitle2Panel.getSelectedFont());
			config.putObject(PlotConfiguration.SUBTITLE_2_COLOR, subtitle2Panel.getSelectedColor());
		} else {
			config.setSubtitle2("");
			config.setShowSubtitle2("FALSE");	// Feb 2016 explicitly save value of checkbox
		}

		return config;
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ChartTitlesPanel titles = new ChartTitlesPanel();
		titles.initTitle(true, "Title", new JLabel().getFont(), Color.RED);
		titles.initSubTitle1(false, "Subtitle 1", new JLabel().getFont(), Color.GREEN);
		titles.initSubTitle2(true, "Subtitle 2", new JLabel().getFont(), Color.BLUE);

		frame.setLayout(new BorderLayout());
		frame.add(titles, BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner non-commercial license
		titlePanel = new ChartTitlePanel();
		subtitle1Panel = new ChartTitlePanel();
		subtitle2Panel = new ChartTitlePanel();
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
		add(titlePanel, cc.xy(1, 1));
		add(subtitle1Panel, cc.xy(1, 3));
		add(subtitle2Panel, cc.xy(1, 5));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner non-commercial license
	private ChartTitlePanel titlePanel;
	private ChartTitlePanel subtitle1Panel;
	private ChartTitlePanel subtitle2Panel;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
