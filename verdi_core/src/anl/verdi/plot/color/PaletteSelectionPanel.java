package anl.verdi.plot.color;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
import org.geotools.brewer.color.BrewerPalette;
import org.geotools.brewer.color.ColorBrewer;
import org.geotools.brewer.color.PaletteType;

import anl.verdi.data.DataUtilities;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.Sizes;

/*
 * Created by JFormDesigner on Wed Mar 28 14:25:47 EDT 2007
 */

/**
 * @author User #2
 */
public class PaletteSelectionPanel extends JPanel {
	private static final long serialVersionUID = 895188842367162164L;
	static final Logger Logger = LogManager.getLogger(PaletteSelectionPanel.class.getName());
	private ColorBrewer brewer = ColorBrewer.instance();
	private PavePaletteCreator palBrewer = new PavePaletteCreator();

	public PaletteSelectionPanel() {
		Logger.debug("in default constructor for PaletteSelectionPanel");
		initComponents();
		createPalettes();

		typeCmb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (getPaletteType() == ColorBrewer.SEQUENTIAL) {
					((SpinnerNumberModel) tileSpinner.getModel())
							.setMaximum(64);
				} else {
					((SpinnerNumberModel) tileSpinner.getModel())
							.setMaximum(new Integer(12));
				}
				createPalettes();
			}
		});

		tileSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				createPalettes();
			}
		});
	}

	private PaletteType getPaletteType() {
		Logger.debug("in PaletteSelectionPanel.getPaletteType = " + typeCmb.getSelectedItem().toString());
		String type = typeCmb.getSelectedItem().toString();
		if (type.equals("Sequential"))
			return ColorBrewer.SEQUENTIAL;
		else if (type.equals("Qualitative"))
			return ColorBrewer.QUALITATIVE;
		else
			return ColorBrewer.DIVERGING;
	}

	private void createPalettes() {
		Logger.debug("in PaletteSelectionPanel.createPalettes");
		int tileCount = ((Integer) tileSpinner.getValue()).intValue();
		Logger.debug("\ttileCount = " + tileCount);
		PaletteType paletteType = getPaletteType();
		BrewerPalette[] pals = brewer.getPalettes(paletteType, tileCount);
		Logger.debug("\tjust did brewer.getPalettes");
		java.util.List<Palette> palettes = new ArrayList<Palette>();
		
//		//if diverging add custom INVERTED versions of the diverging palettes...
//		if (paletteType.equals(ColorBrewer.DIVERGING)) {
//			for (BrewerPalette pal : pals) {
//				Color[] colors = pal.getColors(tileCount);
//				palettes.add(new Palette(colors, pal.getDescription()));
//			}
//		}
		for (BrewerPalette pal : pals) {
			Color[] colors = pal.getColors(tileCount);
			palettes.add(new Palette(colors, pal.getDescription(), false));
			Logger.debug("for each BrewerPalette, palettes.add " + pal.getDescription());
		}

		if (paletteType.equals(ColorBrewer.SEQUENTIAL)) {
			Logger.debug("ColorBrewer is SEQUENTIAL, doing palettes.addAll for tileCount = " + tileCount);
			palettes.addAll(palBrewer.createPalettes(tileCount));
		}

		palettePanel.setPalettes(palettes);
		palettePanel.setPaletteType(paletteType);
	}

	public void init(ColorMap map, DataUtilities.MinMax minMax) {
		Logger.debug("in PaletteSelectionPanel.init for ColorMap and MinMax");
		ColorMap.PaletteType type = map.getPaletteType();
		if (type != null) {
			if (type.equals(ColorMap.PaletteType.QUALITATIVE))
				typeCmb.setSelectedItem("Qualitative");
			else if (type.equals(ColorMap.PaletteType.DIVERGING))
				typeCmb.setSelectedItem("Diverging");
			else
				typeCmb.setSelectedItem("Sequential");
		}
		tileSpinner.setValue(new Integer(map.getColorCount()));
		Logger.debug("calling palettePanel.initMap for map and minMax");
		palettePanel.initMap(map, minMax);
	}
	
	public void setForFastTitle(boolean isForFastTitle) {
		Logger.debug("in PaletteSelectionPanel.setForFastTitle");
		if ( palettePanel != null) {
			palettePanel.setForFastTitle( isForFastTitle );
		}
	}	
	
	private void initComponents() {
		Logger.debug("in PaletteSelectionPanel.initComponents");
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		// Generated using JFormDesigner non-commercial license
		label1 = new JLabel();
		tileSpinner = new JSpinner();
		label2 = new JLabel();
		typeCmb = new JComboBox();
		palettePanel = new PalettePanel();
		CellConstraints cc = new CellConstraints();

		// ======== this ========
		setBorder(new EmptyBorder(5, 5, 5, 5)); 
		// 2014
		ColumnSpec aColumnSpec = new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW);
		ColumnSpec bColumnSpec = new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW);
		RowSpec aRowSpec = new RowSpec(RowSpec.FILL, Sizes.DEFAULT, 0.6);
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
				aColumnSpec,
				FormFactory.UNRELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
				bColumnSpec }, new RowSpec[] {
				FormFactory.PREF_ROWSPEC, FormFactory.LINE_GAP_ROWSPEC,
				aRowSpec }));
//		setLayout(new FormLayout(new ColumnSpec[] {
//				FormFactory.DEFAULT_COLSPEC,
//				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
//				new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT,
//						FormSpec.DEFAULT_GROW),
//				FormFactory.UNRELATED_GAP_COLSPEC,
//				FormFactory.DEFAULT_COLSPEC,
//				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
//				new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT,
//						FormSpec.DEFAULT_GROW) }, new RowSpec[] {
//				FormFactory.PREF_ROWSPEC, FormFactory.LINE_GAP_ROWSPEC,
//				new RowSpec(RowSpec.FILL, Sizes.DEFAULT, 0.6) }));

		// ---- label1 ----
		label1.setText("Number of Tiles:");
		add(label1, cc.xy(1, 1));

		// ---- tileSpinner ----
		tileSpinner.setModel(new SpinnerNumberModel(8, 2, 12, 1));
		add(tileSpinner, cc.xy(3, 1));

		// ---- label2 ----
		label2.setText("Palette Type:");
		add(label2, cc.xy(5, 1));

		// ---- typeCmb ----
		typeCmb.setModel(new DefaultComboBoxModel(new String[] { "Diverging",
				"Qualitative", "Sequential" }));
		typeCmb.setSelectedIndex(2);
		add(typeCmb, cc.xy(7, 1));
		add(palettePanel, cc.xywh(1, 3, 7, 1));
		// JFormDesigner - End of component initialization
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	// Generated using JFormDesigner non-commercial license
	private JLabel label1;
	private JSpinner tileSpinner;
	private JLabel label2;
	private JComboBox typeCmb;
	private PalettePanel palettePanel;

	// JFormDesigner - End of variables declaration //GEN-END:variables

	public ColorMap getColorMap() throws Exception {
		Logger.debug("in PaletteSelectionPanel.getColorMap");
		ColorMap map = palettePanel.getColorMap();
		String type = typeCmb.getSelectedItem().toString();
		if (type.equals("Qualitative"))
			map.setPaletteType(ColorMap.PaletteType.QUALITATIVE);
		else if (type.equals("Diverging"))
			map.setPaletteType(ColorMap.PaletteType.DIVERGING);
		else
			map.setPaletteType(ColorMap.PaletteType.SEQUENTIAL);
		return map;
	}
	
	public void enableScale( boolean enable) {
		Logger.debug("in PaletteSelectionPanel.enableScale = " + enable);
		palettePanel.enableScale( enable);
	}	
}
