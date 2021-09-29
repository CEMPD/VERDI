package anl.verdi.plot.color;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
import org.geotools.brewer.color.ColorBrewer;
import org.geotools.brewer.color.PaletteType;

import anl.verdi.data.DataUtilities;

import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.Sizes;

/*
 * Created by JFormDesigner on Wed Mar 28 14:11:11 EDT 2007
 */

/**
 * @author User #2
 */
public class PalettePanel extends JPanel {
	private static final long serialVersionUID = -8109206936055484044L;
	static final Logger Logger = LogManager.getLogger(PalettePanel.class.getName());
	private ColorMapTableModel model;
//	private boolean autoInterval = true;
	private int intervalInx = 0; // to replace autoInterval: 0: auto, 1: custom, 2: log
	private int scaleIndex = 0;
	private int preScaleIndex = 0; 
	private PaletteType paletteType;
	
	DataUtilities.MinMax defaultMinMax;
	ColorMap defaultColorMap;
	
	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	// Generated using JFormDesigner non-commercial license
	private JComponent separator1;
	private JScrollPane scrollPane1;
	private PaletteList paletteList;
	private JComponent separator2;
	private JLabel label1;
	private JComboBox intervalType;
	private JLabel labelScale;
	private JComboBox scaleType;
	private JPanel panel1;
	private JLabel label2;
	private JPanel panel0;
	private JPanel panel0_1;
	private JLabel labelFormat;
	private JTextField formatFld;
	private JTextField minFld;
	private JLabel label3;
	private JTextField maxFld;
	private JButton rebuildBtn;
	private JScrollPane scrollPane2;
	private JTable paletteTable;
	
	private JTextField fldLogBase;
	private JLabel lblLogBase;	
	private JButton reverseBtn;
	private JButton resetBtn;
	
	public void enableScale( boolean enable) {
		this.scaleType.setEnabled( enable);
	}
	
	private void initComponents() {
		Logger.debug("in PalettePanel.initComponents");
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		// Generated using JFormDesigner non-commercial license
		DefaultComponentFactory compFactory = DefaultComponentFactory
				.getInstance();
		separator1 = compFactory.createSeparator("Palette");
		scrollPane1 = new JScrollPane();
		paletteList = new PaletteList();
		separator2 = compFactory.createSeparator("");
		label1 = compFactory.createLabel("Interval:");
		intervalType = new JComboBox();
		this.labelScale = new JLabel();
		this.scaleType = new JComboBox();
		panel0 = new JPanel();
		panel0_1 = new JPanel();
		panel1 = new JPanel();
		label2 = new JLabel();
		minFld = new JTextField();
		label3 = new JLabel();
		maxFld = new JTextField();
		labelFormat = new JLabel();
		formatFld = new JTextField("1", 6);
		this.lblLogBase = new JLabel("Log Base");
		this.fldLogBase = new JTextField("10", 6);
		
		rebuildBtn = new JButton();
		scrollPane2 = new JScrollPane();
		paletteTable = new JTable();
		reverseBtn = new JButton();
		resetBtn = new JButton();
		CellConstraints cc = new CellConstraints();

		// ======== this ========
		// 2014
		ColumnSpec[] aColumnSpec = ColumnSpec.decodeSpecs("min(default;180dlu):grow");
		RowSpec aRowSpec = new RowSpec(RowSpec.CENTER, Sizes.DEFAULT, 0.5);
		RowSpec bRowSpec = new RowSpec(RowSpec.FILL, Sizes.DEFAULT, 0.5);
		setLayout(new FormLayout(new ColumnSpec[] { FormFactory.PREF_COLSPEC,
				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
				FormFactory.PREF_COLSPEC,
				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
				aColumnSpec[0] }, new RowSpec[] {
				FormFactory.DEFAULT_ROWSPEC, FormFactory.LINE_GAP_ROWSPEC,
				aRowSpec,
				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.MIN_ROWSPEC,
				FormFactory.LINE_GAP_ROWSPEC, FormFactory.PREF_ROWSPEC, 
				FormFactory.PREF_ROWSPEC, FormFactory.LINE_GAP_ROWSPEC, 
				FormFactory.PREF_ROWSPEC, FormFactory.LINE_GAP_ROWSPEC,
				bRowSpec }));
//		setLayout(new FormLayout(new ColumnSpec[] { FormFactory.PREF_COLSPEC,
//				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
//				FormFactory.PREF_COLSPEC,
//				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
//				new ColumnSpec("min(default;150dlu):grow") }, new RowSpec[] {
//				FormFactory.DEFAULT_ROWSPEC, FormFactory.LINE_GAP_ROWSPEC,
//				new RowSpec(RowSpec.CENTER, Sizes.DEFAULT, 0.5),
//				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.MIN_ROWSPEC,
//				FormFactory.LINE_GAP_ROWSPEC, FormFactory.PREF_ROWSPEC, 
//				FormFactory.PREF_ROWSPEC, FormFactory.LINE_GAP_ROWSPEC, 
//				FormFactory.PREF_ROWSPEC, FormFactory.LINE_GAP_ROWSPEC,
//				new RowSpec(RowSpec.FILL, Sizes.DEFAULT, 0.5) }));
		add(separator1, cc.xywh(1, 1, 5, 1));

		// ======== scrollPane1 ========
		{
			scrollPane1.setPreferredSize(new Dimension(2, 200));
			scrollPane1.setBackground(UIManager.getColor("List.background"));

			// ---- paletteList ----
			paletteList.setVisibleRowCount(-1);
			paletteList.setOpaque(false);
			scrollPane1.setViewportView(paletteList);
		}
		add(scrollPane1, cc.xywh(1, 3, 5, 1));

		// ---- reverseBtn ----
		reverseBtn.setText("Reverse");
		add(reverseBtn, cc.xy(1, 5));
		
		resetBtn.setText("Reset");
		add(resetBtn, cc.xy(5, 5));


		add(separator2, cc.xywh(1, 6, 5, 1));
		
		// ======== panel0 ========
		{ 
			// 2014
			ColumnSpec bColumnSpec = new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW);
			ColumnSpec cColumnSpec = new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW);
			RowSpec[] cRowSpec = RowSpec.decodeSpecs("default");
			panel0.setLayout(new FormLayout(
				new ColumnSpec[] {
					FormFactory.DEFAULT_COLSPEC, 
					FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
					bColumnSpec,
					FormFactory.LABEL_COMPONENT_GAP_COLSPEC, 
					FormFactory.DEFAULT_COLSPEC,
					FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
					cColumnSpec
				    }, 
				cRowSpec));
//			panel0.setLayout(new FormLayout(
//					new ColumnSpec[] {
//						FormFactory.DEFAULT_COLSPEC,                   // 1
//						FormFactory.LABEL_COMPONENT_GAP_COLSPEC,       // 2
//						new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, // 3
//								FormSpec.DEFAULT_GROW),                //
//						FormFactory.LABEL_COMPONENT_GAP_COLSPEC,       // 4
//						FormFactory.DEFAULT_COLSPEC,                   // 5
//						FormFactory.LABEL_COMPONENT_GAP_COLSPEC,       // 6
//						new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, // 7
//								FormSpec.DEFAULT_GROW)                 //
////						FormFactory.LABEL_COMPONENT_GAP_COLSPEC,       // 8
////						FormFactory.DEFAULT_COLSPEC,                   // 9
////						FormFactory.LABEL_COMPONENT_GAP_COLSPEC,       //10
////						new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, //11
////								FormSpec.DEFAULT_GROW), 
//					    }, 
//					RowSpec.decodeSpecs("default")));

			panel0.add(label1, cc.xy(1, 1));
			
			intervalType.setModel(new DefaultComboBoxModel(new String[] {
					"Automatic", "Custom" }));
			this.scaleType.setModel(new DefaultComboBoxModel(new String[] {
					"Linear", "Logarithmic" }));
			
			intervalType.setPrototypeDisplayValue("Automatic");
			this.scaleType.setPrototypeDisplayValue("Linear");
			
			panel0.add(intervalType, cc.xy(3, 1));
			labelFormat.setText("Number Format:");
			panel0.add(labelFormat, cc.xy(5, 1));
			panel0.add(formatFld, cc.xy(7, 1));

//			this.labelScale.setText("Scale:");
//			panel0.add(this.labelScale, cc.xy(9, 1));
//			panel0.add(this.scaleType, cc.xy(11, 1));

		}
		add(panel0, cc.xy(1, 8));
		
		// ======== panel0_1 ========
		{ 
			// 2014
			ColumnSpec dColumnSpec = new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW);
			RowSpec[] dRowSpec = RowSpec.decodeSpecs("default");
			panel0_1.setLayout(new FormLayout(
				new ColumnSpec[] {
					FormFactory.DEFAULT_COLSPEC,                   // 9
					FormFactory.LABEL_COMPONENT_GAP_COLSPEC,       //10
					dColumnSpec
				    }, 
				dRowSpec));
//			panel0_1.setLayout(new FormLayout(
//					new ColumnSpec[] {
//						FormFactory.DEFAULT_COLSPEC,                   // 9
//						FormFactory.LABEL_COMPONENT_GAP_COLSPEC,       //10
//						new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, //11
//								FormSpec.DEFAULT_GROW) 
//					    }, 
//					RowSpec.decodeSpecs("default")));

			this.labelScale.setText("Scale:");
			panel0_1.add(this.labelScale, cc.xy(1, 1));
			panel0_1.add(this.scaleType, cc.xy(3, 1));

		}
		add(panel0_1, cc.xy(5, 8));

		// ======== panel1 ========
		{
			// 2014
			ColumnSpec fColumnSpec = new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW);
			ColumnSpec gColumnSpec = new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW);
			RowSpec[] hRowSpec = RowSpec.decodeSpecs("default");
			panel1.setLayout(new FormLayout(new ColumnSpec[] {
					FormFactory.DEFAULT_COLSPEC,
					FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
					fColumnSpec,
					FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
					FormFactory.DEFAULT_COLSPEC,
					FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
					gColumnSpec}, 
					hRowSpec));
//			panel1.setLayout(new FormLayout(new ColumnSpec[] {
//					FormFactory.DEFAULT_COLSPEC,
//					FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
//					new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT,
//							FormSpec.DEFAULT_GROW),
//					FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
//					FormFactory.DEFAULT_COLSPEC,
//					FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
//					new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT,
//							FormSpec.DEFAULT_GROW)}, RowSpec
//					.decodeSpecs("default")));

			// ---- label2 ----
			label2.setText("Min:");
			panel1.add(label2, cc.xy(1, 1));
			panel1.add(minFld, cc.xy(3, 1));

			// ---- label3 ----
			label3.setText("Max:");
			panel1.add(label3, cc.xy(5, 1));
			panel1.add(maxFld, cc.xy(7, 1));
		}
		add(panel1, cc.xy(1, 10));
		
		// ---- rebuildBtn ----
		rebuildBtn.setText("Rebuild");
		add(rebuildBtn, cc.xy(5, 10));

		// ======== scrollPane2 ========
		{

			// ---- paletteTable ----
			paletteTable.setPreferredScrollableViewportSize(new Dimension(450,
					150));
			scrollPane2.setViewportView(paletteTable);
		}
		add(scrollPane2, cc.xywh(1, 12, 5, 1));
		// JFormDesigner - End of component initialization
		// //GEN-END:initComponents
	}	

	public PalettePanel() {
		Logger.debug("in default constructor for PalettePanel");
		model = new ColorMapTableModel();
		initComponents();
		
		paletteTable.setModel(model);
		paletteTable.getColumnModel().getColumn(0).setCellRenderer(
				new CellRenderer());
		paletteTable.getColumnModel().getColumn(1).setCellRenderer(
				new NumberCellRenderer());

		paletteTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

		paletteList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				Palette palette = (Palette) paletteList.getSelectedValue();
				if (palette != null) {
					model.resetPalette(palette);
				}
			}
		});

		paletteTable.setCellSelectionEnabled(true);
		paletteTable.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				int col = paletteTable.getSelectedColumn();
				
				if (col == 0) {
					int row = paletteTable.getSelectedRow();
					if (row != -1) {
						Color color = (Color) model.getValueAt(row, 0);
						color = JColorChooser.showDialog(paletteTable,
								"Select Palette Color", color);
						if (color != null) {
							model.setValueAt(color, row, 0);
						}
					}
				}
				
				if (col == 1) {
					int row = paletteTable.getSelectedRow();
					if (row != -1) {
						String avalue = model.getValueAt(row, 1).toString();
						if (avalue != null) {
							try {
								model.setValueAt(avalue, row, 1);
							} catch (NumberFormatException e1) {
								JOptionPane.showMessageDialog(PalettePanel.this, e1.getMessage(),
										"Color Map Error", JOptionPane.ERROR_MESSAGE);
							}
						}
					}
				}
			}
		});

		intervalType.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				
				intervalInx = intervalType.getSelectedIndex();
				scaleIndex = scaleType.getSelectedIndex();
				model.setIntervalEditEnabled( intervalInx); // TODO: related to palletTable
				model.setScaleType( scaleIndex);
				if (intervalInx==0) { // auto
					minFld.setEnabled(true);
					maxFld.setEnabled(true);
					rebuildBtn.setEnabled(true);
					lblLogBase.setVisible( false);
					fldLogBase.setVisible( false);
				} else if (intervalInx==1) { // custom
					minFld.setEnabled(false);
					maxFld.setEnabled(false);
					rebuildBtn.setEnabled(false);
					lblLogBase.setVisible( false);
					fldLogBase.setVisible( false);
				} 
				if (scaleIndex == 0) {
					lblLogBase.setVisible( false);
					fldLogBase.setVisible( false);
				} else if ( scaleIndex == 1){
					lblLogBase.setVisible( false);
					fldLogBase.setVisible( false);
				} else {
					// report error
				}
				Logger.debug("in intervalType actionPerformed: intervalInx = " + intervalInx);
				
				if ( scaleIndex != preScaleIndex	) {
					
					if ( scaleIndex == 1) {
						double baseValue = Math.E;
						String base = fldLogBase.getText();
						if ( !base.trim().equalsIgnoreCase("E")) {
							baseValue = Double.parseDouble( fldLogBase.getText());
						}
						model.setLogBase( base);
					}
					
					double min = 0, max = 0;
					try {
						min = model.getColorMap().getMin();
						max = model.getColorMap().getMax();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					
					String minConv = "f";
					String maxConv = "f";
					
					if (Math.abs(min) < 0.001)
						minConv = "e";

					if (Math.abs(max) < 0.001)
						maxConv = "e";

					minFld.setText(String.format("%.6" + minConv, min));
					maxFld.setText(String.format("%.6" + maxConv, max));
					minFld.setCaretPosition(0);
					maxFld.setCaretPosition(0);					
				}
				
				preScaleIndex = scaleIndex;
				Logger.debug("in intervalType actionPerformed: minFld = " + minFld.getText() + ", maxFld = " + maxFld.getText());
			}
		});
		
		scaleType.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				scaleIndex = scaleType.getSelectedIndex();
				model.setScaleType( scaleIndex);
				if (scaleIndex == 0) {
					lblLogBase.setVisible( false);
					fldLogBase.setVisible( false);
				} else if ( scaleIndex == 1){
					lblLogBase.setVisible( false);
					fldLogBase.setVisible( false);
				} else {
					// report error
				}
				if ( scaleIndex != preScaleIndex	) {
					
					if ( scaleIndex == 1) {
						double baseValue = Math.E;
						String base = fldLogBase.getText();
						if ( !base.trim().equalsIgnoreCase("E")) {
							baseValue = Double.parseDouble( fldLogBase.getText());
						}
						model.setLogBase( base);
					}
					
					double min = 0, max = 0;
					try {
						min = model.getColorMap().getMin();
						max = model.getColorMap().getMax();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					String minConv = "f";
					String maxConv = "f";
					
					if (Math.abs(min) < 0.001)
						minConv = "e";

					if (Math.abs(max) < 0.001)
						maxConv = "e";

					minFld.setText(String.format("%.6" + minConv, min));
					maxFld.setText(String.format("%.6" + maxConv, max));
					minFld.setCaretPosition(0);
					maxFld.setCaretPosition(0);					
				}
				
				preScaleIndex = scaleIndex;
			}
		});
		
		this.fldLogBase.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				try {
					double baseValue = Math.E;
					String base = fldLogBase.getText();
					if ( !base.trim().equalsIgnoreCase("E")) {
						baseValue = Double.parseDouble( fldLogBase.getText());
					}
					model.setLogBase( base);
					
					double min = 0, max =0;
					try {
						min = model.getColorMap().getMin();
						max = model.getColorMap().getMax();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					String minConv = "f";
					String maxConv = "f";
					
					if (Math.abs(min) < 0.001)
						minConv = "e";

					if (Math.abs(max) < 0.001)
						maxConv = "e";

					minFld.setText(String.format("%.6" + minConv, min));
					maxFld.setText(String.format("%.6" + maxConv, max));
					minFld.setCaretPosition(0);
					maxFld.setCaretPosition(0);	
					
					intervalInx = intervalType.getSelectedIndex();
					Logger.debug("in fldLogBase actionPerformed; intervalInx = " + intervalInx);
					Logger.debug("minFld = " + minFld.getText() + ", maxFld = " + maxFld.getText());
					model.setIntervalEditEnabled( intervalInx); 
					
				} catch (NumberFormatException ex) {
					// TODO: show error message
					JOptionPane.showMessageDialog(PalettePanel.this, ex.getMessage(),
							"Log Base Error", JOptionPane.ERROR_MESSAGE);					
				}
			}
		});
		
		formatFld.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				try {
					model.resetColorMap(getColorMap());
					paletteTable.revalidate();
				} catch (Exception e) {
					e.printStackTrace(); //let it be best effort method
				}
			}
		});

		rebuildBtn.addActionListener(new ActionListener() { // TODO: JIZHEN
			public void actionPerformed(ActionEvent evt) {
				Logger.debug("in rebuildBtn actionPerformed: intervalType.getSelectedIndex = " + intervalType.getSelectedIndex());
				rebuild();
			}
		});

		reverseBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (model.getColorMap().getPalette().isReverseColors()) {
					model.getColorMap().getPalette().setReverseColors(false);
				} else {
					model.getColorMap().getPalette().setReverseColors(true);
				}
				paletteTable.revalidate();
				paletteTable.repaint();
			}
		});
		
		resetBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				model.resetColors();
				initMap(defaultColorMap,  defaultMinMax);
			}
		});
	}
	
	public void rebuild() {
		if (!rebuildBtn.isEnabled())
			return;
		if ( intervalType.getSelectedIndex() != 2) {
			String text = minFld.getText().trim();
			double min, max;
			try {
				min = Double.valueOf(text);
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(PalettePanel.this, "'" + text
						+ " is not a valid number", "Color Map Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			text = maxFld.getText().trim();

			try {
				max = Double.valueOf(text);
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(PalettePanel.this, "'" + text
						+ " is not a valid number", "Color Map Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			if (min > max) {
				double tmp = max;
				max = min;
				min = tmp;
			}

			ColorMap oldMap = model.getColorMap();
			ColorMap map = null; //new ColorMap(palette, sType, min, max);	
			boolean chgColorMap = false;	// default is to not change the color map
			if ( oldMap != null) {
				map = oldMap;
			} else {
				map = new ColorMap();
				chgColorMap = true;
				//Don't reset the color palette unless the user chnaged color maps- the user
				//may have made changes
				Palette palette = (Palette) paletteList.getSelectedValue();
				map.setPalette(palette);
				map.setPaletteType(getPaletteType());	
			}
		
			ColorMap.ScaleType sType = ColorMap.ScaleType.LINEAR;
			if ( scaleType.getSelectedIndex() == 1) {
				sType = ColorMap.ScaleType.LOGARITHM;
			} 
			map.setScaleType(sType);
			if ( sType == ColorMap.ScaleType.LINEAR ) {
				map.setMinMax(min, max);
			} else {
				map.setLogMinMax(min,max);
			}

			try {
				map = resetNumberFormat(map, formatFld.getText());
			} catch (Exception e) {
				e.printStackTrace(); //let it be a best effort method.
			}

			if(chgColorMap)		// don't want to always reset the color map
				model.resetColorMap(map);
			model.setIntervalEditEnabled( intervalType.getSelectedIndex());	
			paletteTable.revalidate();
		} else {
			double baseValue = Math.E;
			String base = fldLogBase.getText();
			if ( !base.trim().equalsIgnoreCase("E")) {
				baseValue = Double.parseDouble( fldLogBase.getText());
			}
			model.setLogBase( base);
			double min=0, max=0;
			try {
				min = model.getColorMap().getMin();
				max = model.getColorMap().getMax();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			String minConv = "f";
			String maxConv = "f";

			if (Math.abs(min) < 0.001)
				minConv = "e";

			if (Math.abs(max) < 0.001)
				maxConv = "e";

			minFld.setText(String.format("%.6" + minConv, min));
			maxFld.setText(String.format("%.6" + maxConv, max));
			minFld.setCaretPosition(0);
			maxFld.setCaretPosition(0);					
			model.setIntervalEditEnabled( intervalType.getSelectedIndex());	
		}
		Logger.debug("in rebuildBtn actionPerformed: minFld = " + minFld.getText() + ", maxFld = " + maxFld.getText());

	}

	public void setPalettes(List<Palette> palettes) {
		Logger.debug("in PalettePanel.setPalettes");
		paletteList.setData(palettes);
		int index = findPaletteIndex(model.getColorMap().getPalette());
		if (palettes.size() > 0) {
			if (index == -1)
				paletteList.setSelectedIndex(0);
			else
				paletteList.setSelectedIndex(index);
			Rectangle bounds = paletteList.getCellBounds(index, index);
			if (bounds != null)
				paletteList.scrollRectToVisible(bounds);
		} else {
			model.resetColorMap(new ColorMap());
		}
	}

	private int findPaletteIndex(Palette palette) {
		Logger.debug("in PalettePanel.finePaletteIndex");
		ListModel model = paletteList.getModel();
		int index = -1;
		for (int i = 0; i < model.getSize(); i++) {
			Palette p = (Palette) model.getElementAt(i);
			if (p.getDescription().equals(palette.getDescription())) {
				index = i;
				break;
			}
		}

		return index;
	}

	public void initMap(ColorMap colorMap, DataUtilities.MinMax minMax) {
		Logger.debug("in PalettePanel.initMap");
		defaultMinMax = minMax;
		try {
			defaultColorMap = new ColorMap(colorMap.getPalette(), colorMap.getMin(), colorMap.getMax());
		} catch (Exception e2) {
			e2.printStackTrace();
		}
		ColorMap.IntervalType iType = colorMap.getIntervalType();
		if (iType == ColorMap.IntervalType.AUTOMATIC) {
			intervalType.setSelectedItem("Automatic");
		} else if ( iType == ColorMap.IntervalType.CUSTOM) {
			intervalType.setSelectedItem("Custom");
		} else {
			Logger.error("Error type of color map " + colorMap.getIntervalType().toString() + "is not recognized.");
		}
		
		scaleType.setVisible( true);
		labelScale.setVisible( true);
		ColorMap.ScaleType sType = colorMap.getScaleType();
		scaleType.setSelectedItem("Linear");
		double baseValue = colorMap.getLogBase();
		if ( baseValue == Math.E) {
			fldLogBase.setText("E");
		} else {
			fldLogBase.setText(new Double(baseValue).toString());
		}
		
		int index = findPaletteIndex(colorMap.getPalette());
		if (index == -1)
			index = 0;

		paletteList.setSelectedIndex(index);
		paletteList.scrollRectToVisible(paletteList.getCellBounds(index, index));

		String minConv = "f";
		String maxConv = "f";
		double min = minMax.getMin();
		double max = minMax.getMax();

		if ( intervalType.getSelectedIndex() == 2) { // logarithm
			min = Math.log(min)/Math.log(baseValue);
			max = Math.log(max)/Math.log(baseValue);
		}
		
		if (Math.abs(min) < 0.001)
			minConv = "e";

		if (Math.abs(max) < 0.001)
			maxConv = "e";

		minFld.setText(String.format("%.6" + minConv, min));
		maxFld.setText(String.format("%.6" + maxConv, max));
		minFld.setCaretPosition(0);
		maxFld.setCaretPosition(0);

		maxFld.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				maxFld.selectAll();
				maxFld.requestFocusInWindow();
			}
		});

		minFld.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				minFld.selectAll();
				minFld.requestFocusInWindow();
			}
		});
		
		formatFld.setText("%5.3f");
		Logger.debug("formatFld just set to %5.3f");
		
		if (minConv.equalsIgnoreCase("e") || maxConv.equalsIgnoreCase("e") )
		{
			formatFld.setText("%5.3E");
			Logger.debug("formatFld just set to %5.3E");
		}
		
		try {
			Logger.debug("ready to try and see if getNumberFormat returns null");
			if (colorMap.getNumberFormat() != null)
				formatFld.setText(colorMap.getFormatString());
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		model.resetColorMap(colorMap);
		paletteTable.revalidate();
	}



	// JFormDesigner - End of variables declaration //GEN-END:variables

	public ColorMap getColorMap() throws Exception {
		Logger.debug("in PalettePanel.getColorMap");
		ColorMap map = model.getColorMap();
		String format = formatFld.getText();
		
		if (format != null && !format.trim().isEmpty())
			return resetNumberFormat(map, format);
		
		return map;
	}

	/**
	 * Take the format string input by the user, check the construction, create a proper Java pattern,
	 * and apply it to the breakpoint values on the tile plot legend
	 * @param map	the current ColorMap
	 * @param format	the String entered by the user
	 * @return
	 * @throws Exception
	 */
	private ColorMap resetNumberFormat(ColorMap map, String format) throws Exception {
		Logger.debug("in PalettePanel.resetNumberFormat, format = " + format);
		map.setFormatString(format);
		if (format == null) {
			Logger.debug("format is null; returning map without changes");
		}
		return map;
	}

	private class CellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = -4766649744060292439L;

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			JLabel label = (JLabel) super.getTableCellRendererComponent(table,
					value, isSelected, hasFocus, row, column);
			label.setText("");
			SquareIcon icon = new SquareIcon((Color) value, 12);
			label.setIcon(icon);
			label.setHorizontalAlignment(JLabel.CENTER);
			return label;
		}
	}

	private class NumberCellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 4651016553448777932L;

		NumberFormat format;

		public NumberCellRenderer() {
			format = new DecimalFormat();
			format.setMaximumFractionDigits(3);
		}

		public void setValue(Object value) {
			setText((value == null) ? "" : value.toString());
			//setEnabled(!autoInterval);
			setEnabled(intervalInx == 1);
		}
		
		public String getText() {
			String text = super.getText();
			return text;
		}
		
	    public void setText(String text) {
	    	super.setText(text);
	    }
	}
	



	public ColorMap.PaletteType getPaletteType() {
		Logger.debug("in PalettePanel.getPaletteType");
		if (paletteType == ColorBrewer.SEQUENTIAL)
			return ColorMap.PaletteType.SEQUENTIAL;
		
		if (paletteType == ColorBrewer.QUALITATIVE)
			return ColorMap.PaletteType.QUALITATIVE;
		
		return ColorMap.PaletteType.DIVERGING;
	}

	public void setPaletteType(PaletteType paletteType) {
		Logger.debug("in PalettePanel.setPaletteType");
		this.paletteType = paletteType;
		model.resetPaletteType(getPaletteType());
	}

	public void setForFastTitle() {
		Logger.debug("in PalettePanel.setForFastTitle");
		ColorMap.IntervalType iType = ColorMap.IntervalType.AUTOMATIC;
		ColorMap.ScaleType sType = ColorMap.ScaleType.LINEAR;
		
		if ( this.model != null && this.model.getColorMap() != null) {
			sType = this.model.getColorMap().getScaleType();
			iType = this.model.getColorMap().getIntervalType();
		}
		
		if ( iType == ColorMap.IntervalType.AUTOMATIC) {
			intervalType.setSelectedIndex(0);
		} else if ( iType == ColorMap.IntervalType.CUSTOM) {
			intervalType.setSelectedIndex(1);
		} else {
			Logger.debug("Error: IntervalType should not be Logarithm");
		}
		

		this.labelScale.setVisible( true);
		this.scaleType.setVisible( true);
		if ( sType == ColorMap.ScaleType.LOGARITHM) {
			this.scaleType.setSelectedIndex(1);
		} else {
			this.scaleType.setSelectedIndex(0);
		}			
	
	}

}
