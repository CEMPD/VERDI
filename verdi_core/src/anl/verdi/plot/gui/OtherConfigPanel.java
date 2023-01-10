package anl.verdi.plot.gui;

import java.awt.Color;

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import anl.verdi.plot.config.PlotConfiguration;
import anl.verdi.plot.config.TimeSeriesPlotConfiguration;
//import anl.verdi.plot.config.VectorPlotConfiguration;		// 2014 removed old Vector Plot

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;


/**
 * @author User #2
 */
public class OtherConfigPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3891337545375068585L;
	public OtherConfigPanel() {
		initComponents();
//		vectorPanel.init(Color.BLACK, "Vector Arrow Color");
	}

	public PlotConfiguration fillConfiguration(PlotConfiguration config) {
		if (gridLinePanel.isEnabled()) config = gridLinePanel.fillConfiguration(config);
//		if (vectorPanel.isEnabled()) {
//			config.putObject(VectorPlotConfiguration.VECTOR_COLOR, vectorPanel.getSelectedColor());
//		}

		config = gisLinePanel.fillConfiguration(config);
		if (seriesColorPanel.isEnabled()) {
			config.putObject(TimeSeriesPlotConfiguration.SERIES_COLOR, seriesColorPanel.getSelectedColor());
		}
		
		if (plotPointPanel.isEnabled()) {
			plotPointPanel.fillConfiguration(config);
		}
		return config;

	}

	public void setPlotPointPanelEnabled(boolean enabled) {
		plotPointPanel.setEnabled(enabled);
	}
	
	public void setGridLinePanelEnabled(boolean enabled) {
		gridLinePanel.setEnabled(enabled);
	}

//	public void setVectorPanelEnabled(boolean enabled) {
//		vectorPanel.setEnabled(enabled);
//	}

	public void setSeriesColorEnabled(boolean enabled) {
		seriesColorPanel.setEnabled(enabled);
	}

	public void initPlotPoints(int size) {
		plotPointPanel.init(size);
	}

	public void initGridLines(Color color, boolean showLines) {
		gridLinePanel.init(color, showLines);
	}

	public void initGISLines(Color color, int width) {
		gisLinePanel.init(color, width);
	}

//	public void initVector(Color color) {
//		vectorPanel.init(color, "Vector Arrow Color");
//	}

	public void initSeries(Color color) {
		seriesColorPanel.init(color, "Series Color");
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner non-commercial license
		gridLinePanel = new GridLinePanel();
		gisLinePanel = new GISLinePanel();
		plotPointPanel = new PlotPointPanel();
//		vectorPanel = new SimpleColorPanel();
		seriesColorPanel = new SimpleColorPanel();
		CellConstraints cc = new CellConstraints();

		//======== this ========
		// 2014
		ColumnSpec[] aColumnSpec = ColumnSpec.decodeSpecs("default:grow");
		setLayout(new FormLayout(
			aColumnSpec,
			new RowSpec[] {
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.LINE_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.LINE_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.LINE_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC
			}));
//		setLayout(new FormLayout(
//				ColumnSpec.decodeSpecs("default:grow"),
//				new RowSpec[] {
//					FormFactory.DEFAULT_ROWSPEC,
//					FormFactory.LINE_GAP_ROWSPEC,
//					FormFactory.DEFAULT_ROWSPEC,
//					FormFactory.LINE_GAP_ROWSPEC,
//					FormFactory.DEFAULT_ROWSPEC
//				}));
		add(gridLinePanel, cc.xy(1, 1));
		
		add(gisLinePanel, cc.xy(1, 3));
		
		//add(gridLinePanel, cc.xy(1,  5));

//		//---- vectorPanel ----
//		vectorPanel.setBorder(new TitledBorder("Vector Arrow Color"));
//		add(vectorPanel, cc.xy(1, 3));
//
		//---- seriesColorPanel ----
		seriesColorPanel.setBorder(new TitledBorder("Series Color"));
		add(seriesColorPanel, cc.xy(1, 5));
		
		add(plotPointPanel, cc.xy(1, 7));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner non-commercial license
	private GridLinePanel gridLinePanel;
	private GISLinePanel gisLinePanel;
	private PlotPointPanel plotPointPanel;
//	private SimpleColorPanel vectorPanel;
	private SimpleColorPanel seriesColorPanel;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
