package anl.verdi.plot.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

import anl.verdi.plot.config.PlotConfiguration;

import com.jgoodies.forms.factories.Borders;


/**
 * @author User #2
 */
public class LabelsPanel extends JPanel {
	private static final long serialVersionUID = 1830241699874657362L;
	private boolean zRemoved = false;

	public LabelsPanel() {
		initComponents();
	}

	public PlotConfiguration fillConfiguration(PlotConfiguration config) {

		config.putObject(PlotConfiguration.DOMAIN_LABEL, domainPanel.getText());
		config.putObject(PlotConfiguration.DOMAIN_FONT, domainPanel.getSelectedFont());
		config.putObject(PlotConfiguration.DOMAIN_COLOR, domainPanel.getSelectedColor());
		config.putObject(PlotConfiguration.DOMAIN_SHOW_TICK, domainPanel.isShowLabels());
		config.putObject(PlotConfiguration.DOMAIN_TICK_FONT, domainPanel.getSelectedTickFont());
		config.putObject(PlotConfiguration.DOMAIN_TICK_COLOR, domainPanel.getSelectedTickColor());
		config.putObject(PlotConfiguration.DOMAIN_TICK_NUMBER, domainPanel.getNumberOfLabels());


		config.putObject(PlotConfiguration.RANGE_LABEL, rangePanel.getText());
		config.putObject(PlotConfiguration.RANGE_FONT, rangePanel.getSelectedFont());
		config.putObject(PlotConfiguration.RANGE_COLOR, rangePanel.getSelectedColor());
		config.putObject(PlotConfiguration.RANGE_SHOW_TICK, rangePanel.isShowLabels());
		config.putObject(PlotConfiguration.RANGE_TICK_FONT, rangePanel.getSelectedTickFont());
		config.putObject(PlotConfiguration.RANGE_TICK_COLOR, rangePanel.getSelectedTickColor());
		config.putObject(PlotConfiguration.RANGE_TICK_NUMBER, rangePanel.getNumberOfLabels());

		if (!zRemoved) {
			config.putObject(PlotConfiguration.Z_LABEL, zAxisPanel.getText());
			config.putObject(PlotConfiguration.Z_FONT, zAxisPanel.getSelectedFont());
			config.putObject(PlotConfiguration.Z_COLOR, zAxisPanel.getSelectedColor());
			config.putObject(PlotConfiguration.Z_SHOW_TICK, zAxisPanel.isShowLabels());
			config.putObject(PlotConfiguration.Z_TICK_FONT, zAxisPanel.getSelectedTickFont());
			config.putObject(PlotConfiguration.Z_TICK_COLOR, zAxisPanel.getSelectedTickColor());
			config.putObject(PlotConfiguration.Z_TICK_NUMBER, zAxisPanel.getNumberOfLabels());
		}
		
		footersPanel.fillConfiguration(config);

		config.putObject(PlotConfiguration.UNITS, unitsPanel.getText());
		Font font = unitsPanel.getSelectedFont();
		if (font != null) config.putObject(PlotConfiguration.UNITS_FONT, font);
		Color color = unitsPanel.getSelectedColor();
		if (color != null) config.putObject(PlotConfiguration.UNITS_COLOR, color);
		config.putObject(PlotConfiguration.UNITS_SHOW_TICK, unitsPanel.isShowLabels());
		config.putObject(PlotConfiguration.UNITS_TICK_FONT, unitsPanel.getSelectedTickFont());
		config.putObject(PlotConfiguration.UNITS_TICK_COLOR, unitsPanel.getSelectedTickColor());
		config.putObject(PlotConfiguration.UNITS_TICK_NUMBER, unitsPanel.getNumberOfLabels());

		return config;
	}

	public void initDomain(String title, Font font, Color color) {
		domainPanel.initLabel(title, font, color);
	}

	public void initDomainTick(Boolean show, Font font, Color color, Integer num) {
		domainPanel.initTicks(show, font, color, num);
	}

	public void initRange(String title, Font font, Color color) {
		rangePanel.initLabel(title, font, color);
	}

	public void initRangeTick(boolean show, Font font, Color color, Integer num) {
		rangePanel.initTicks(show, font, color, num);
	}

	public void initUnits(String title, Font font, Color color) {
		unitsPanel.initLabel(title, font, color);
	}

	public void initUnitsTick(Boolean show, Font font, Color color, Integer num) {
		unitsPanel.initTicks(show, font, color, num);
	}

	public void initZ(String title, Font font, Color color) {
		zAxisPanel.initLabel(title, font, color);
	}

	public void initZTick(boolean show, Font font, Color color, Integer num) {
		zAxisPanel.initTicks(show, font, color, num);
	}

	public void removeZ() {
		tabPane.removeTabAt(2);
		zRemoved = true;
	}
	
	public void initFooter1(boolean show, String text, Font font, Color color) {
		footersPanel.initLine1((text != null && !text.trim().isEmpty()), text, font, color);
	}
	
	public void initFooter2(boolean show, String text, Font font, Color color) {
		footersPanel.initLine2((text != null && !text.trim().isEmpty()), text, font, color);
	}
	
	public void initObsLegend(boolean show, String text, Font font, Color color) {
		footersPanel.initObsLegend(show, text, font, color);
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner non-commercial license
		tabPane = new JTabbedPane();
		domainPanel = new LabelPanel();
		rangePanel = new LabelPanel();
		zAxisPanel = new LabelPanel();
		unitsPanel = new LabelPanel();
		footersPanel = new FootersPanel();

		//======== this ========
		setLayout(new BorderLayout());

		//======== tabPane ========
		{
			tabPane.setBorder(Borders.DLU7_BORDER);

			//---- domainPanel ----
			domainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
			tabPane.addTab("Domain Axis", domainPanel);

			//---- rangePanel ----
			rangePanel.setBorder(new EmptyBorder(5, 5, 5, 5));
			tabPane.addTab("Range Axis", rangePanel);

			//---- zAxisPanel ----
			zAxisPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
			tabPane.addTab("Z Axis", zAxisPanel);

			//---- unitsPanel ----
			unitsPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
			tabPane.addTab("Legend", unitsPanel);
			
			//---- footersPanel ----
			footersPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
			tabPane.addTab("Footer", footersPanel);
		}
		add(tabPane, BorderLayout.CENTER);
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner non-commercial license
	private JTabbedPane tabPane;
	private LabelPanel domainPanel;
	private LabelPanel rangePanel;
	private LabelPanel zAxisPanel;
	private LabelPanel unitsPanel;
	private FootersPanel footersPanel;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
