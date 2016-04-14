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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	private boolean isForFastTitle = false;
	
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
		CellConstraints cc = new CellConstraints();

		// ======== this ========
		// 2014
		ColumnSpec[] aColumnSpec = ColumnSpec.decodeSpecs("min(default;150dlu):grow");
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

		// ---- rebuildBtn ----
		reverseBtn.setText("Reverse");
		add(reverseBtn, cc.xy(1, 5));

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
					Palette palette = (Palette) paletteList.getSelectedValue();
					ColorMap oldMap = model.getColorMap();
					ColorMap map = null; //new ColorMap(palette, sType, min, max);	
					boolean chgColorMap = false;	// default is to not change the color map
					if ( oldMap != null) {
						map = oldMap;
					} else {
						map = new ColorMap();
						chgColorMap = true;
					}
					map.setPalette(palette);
					map.setPaletteType(getPaletteType());					
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
		paletteList
				.scrollRectToVisible(paletteList.getCellBounds(index, index));

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
		
		if (minConv.equals("e") || maxConv.equals("e"))
		{
			formatFld.setText("%5.3e");
			Logger.debug("formatFld just set to %5.3e");
		}
		
		try {
			if (colorMap.getNumberFormat() != null)
				formatFld.setText(getFormat(colorMap.getNumberFormat()));
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
		if (format == null)
		{
			Logger.debug("format is null; returning map without changes");
			return map;
		}

		// 2016: declare & initialize parts of format string
		String cPattern = format.trim();   // trimmed input format that should be a subset of a C printf format
		boolean haveDot = true;		// expect a . in the cPattern
		boolean lead0 = false;		// integer (base) has all 0's (not #0) in pattern
		int fieldWidth = 0;			// input field width (entire field width)
		int fieldDecimal = 0;		// input decimal width
		int baseWidth = 1;			// input integer (base) width
		int cModifierLength = 0;	// width of any modifier at end of cPattern
		String cModifier = "";		// default to not having a modifier
		int i = 0;					// loop counter
		
		// pattern match check from old code
//		Pattern p = Pattern.compile("^%\\d*\\.\\d*[eEfFgGdD]\\d*$");	// need to change this to what VERDI allows/recognizes
//		Matcher m = p.matcher(cPattern);
		
		Logger.debug("cPattern = " + cPattern);
		i = 0;
		while(!Character.isDigit(cPattern.charAt(i)) && cPattern.charAt(i) != '.')
			i++;

		int startWidth = i;		// position of starting digit or . in cPattern
		int strLength = cPattern.length();		// length of entire trimmed string
		Logger.debug("length of cPattern = " + strLength + "; startWidth = " + startWidth);
		
		// does cPattern have a . ?
		int dot = cPattern.indexOf('.');	// look for . and get its position in cPattern
		Logger.debug("dot = " + dot);
		if(dot < 0)
		{
			haveDot = false;
			Logger.debug("haveDot = " + haveDot);
		}
		else if(dot > startWidth)
		{
			String width = cPattern.substring(startWidth, dot);	// substring(begIndex, endIndex+1)
			fieldWidth = Integer.valueOf(width);		// size of width portion of cPattern
			haveDot = true;
			String leadingChar = width.substring(0,1);
			if(leadingChar.compareTo("0") == 0)
			{
				lead0 = true;
			}
			Logger.debug("haveDot = " + haveDot + ", width = " + width + ", fieldWidth = " + fieldWidth);
		}	// else keep fieldWidth = 0 (initialized value)
		
		// now start parsing the decimal portion of the pattern
		int startDecimal = dot + 1;
		int endDecimal = startDecimal;
		while (endDecimal < strLength && Character.isDigit(cPattern.charAt(endDecimal)))
			endDecimal ++;
		endDecimal--;		// position of last digit in decimal portion of cPattern
		
		if(startDecimal < strLength && endDecimal < strLength)
		{
			String decimal = cPattern.substring(startDecimal, endDecimal + 1);
			fieldDecimal = Integer.valueOf(decimal);	// size of decimal portion of cPattern
			Logger.debug("startDecimal = " + startDecimal + ", endDecimal = " + endDecimal + 
					", decimal = " + decimal + ", fieldDecimal = " + fieldDecimal);
		}
		
		// parse any modifier at the end of the pattern
		if(endDecimal < strLength)
		{
			cModifier = cPattern.substring(endDecimal + 1, strLength).toUpperCase();
			cModifierLength = cModifier.length();
			Logger.debug("cModifier = " + cModifier + ", cModifierLength = " + cModifierLength);
		}		// else cModifierLength remains 0
		
		int nonBase = (haveDot ? 1 : 0) + fieldDecimal;		// sum of dot and decimal portion of cPattern
		Logger.debug("nonBase = " + nonBase);
		if(fieldWidth <= nonBase && haveDot)				// overall width too small; fieldWidth does not include modifierLength
		{
			baseWidth = 1;									// mandatory 1 char before .
			fieldWidth = nonBase + 1;						// overall width is nonBase + the 1 char
			Logger.debug("overall width too small: baseWidth = " + baseWidth + ", fieldWidth = " + fieldWidth);
		}
		else if(fieldWidth <= nonBase && !haveDot)			// have a fieldDecimal but not a base & no .
		{
			baseWidth = fieldDecimal;						// transfer fieldDecimal value to baseWidth
			fieldDecimal = 0;								// and have 0 for fieldDecimal
			Logger.debug("have fieldDecimal but no base and no .: baseWidth = " + baseWidth + ", fieldDecimal = " + fieldDecimal);
		}
		else if (fieldWidth > nonBase)						// fieldWidth is mathematically OK
		{
			baseWidth = fieldWidth - nonBase;				// baseWidth is overall width - nonBase
			Logger.debug("fieldWidth > nonBase: baseWidth = " + baseWidth);
		}
		
		// construct using StringBuffer to build a pattern here instead of via DecimalFormat function calls
		
		StringBuffer myPattern = new StringBuffer();
		i = baseWidth;
		while(i > 1)						// leading pattern for base (integer) 
		{
			if(lead0)
				myPattern.append("0");		// pattern lead with 0
			else
				myPattern.append("#");		// pattern did not lead with 0
			i--;
		}
		if(i>0)
		{
			myPattern.append("0");			// last character of base pattern is 0
		}
		if(haveDot)
		{
			myPattern.append(".");			// add the .
		}
		if(fieldDecimal >= 1)				// if have a decimal portion, start with a 0
		{
			myPattern.append("0");		
		}
		i=2;								// remainder of decimal places are optional (#)
		while(i <= fieldDecimal)
		{
			myPattern.append("#");
			i++;			
		}
		if(cModifierLength > 0)				// anything after the decimal pattern is appended
		{
			myPattern.append(cModifier);
			if(!cModifier.endsWith("0") && (cModifier.endsWith("E")))
			{
				myPattern.append(0);
			}
		}
		// ready to define the DecimalFormat
		Logger.debug("myPattern before conversion = " + myPattern.toString());
		DecimalFormat myDecimalFormat = new DecimalFormat(myPattern.toString());
		myDecimalFormat.setRoundingMode(RoundingMode.HALF_UP);	// default is HALF-EVEN
		
		// 2016 this method not working as expected in VERDI 1.6, Java 7; may want to try again in future
//		if(haveDot)
//		{
//			myDecimalFormat.setMaximumFractionDigits(fieldDecimal);	// size fraction digits are 1 - fieldDecimal
//			myDecimalFormat.setMinimumFractionDigits(1);
//		}
//		else
//		{
//			myDecimalFormat.setMaximumFractionDigits(0);		// size fraction digits are 0 (no .)
//			myDecimalFormat.setMinimumFractionDigits(0);
//		}
//		myDecimalFormat.setMinimumIntegerDigits(1);
//		myDecimalFormat.setMaximumIntegerDigits(baseWidth); 	// size base is 1 = baseWidth
//		Logger.debug("myDecimalFormat before DecimalFormatSymbols = " + myDecimalFormat.toPattern());
//		DecimalFormatSymbols newSymbols = new DecimalFormatSymbols(); // need way to pass in the modifier
//		newSymbols.setExponentSeparator(cModifier);
//		myDecimalFormat.setDecimalFormatSymbols(newSymbols); 	 
		Logger.debug("myDecimalFormat = " + myDecimalFormat.toPattern());
		
		map.setNumberFormat(myDecimalFormat); 					// assign this format to the color map
		return map;
	}
	
	private String getFormat(NumberFormat numberFormat) {
		Logger.debug("in PalettePanel.getFormat");
		if (numberFormat == null)
			return " %.3f";
		
		String format = ((DecimalFormat)numberFormat).toPattern().toUpperCase();
		Logger.debug("in getFormat, format = " + format);
		int exp = format.indexOf("E");
		int dot = format.indexOf(".");
//		String forStr = "%" + dot + ".";
		String forStr = "%" + (exp - dot + 1) + ".";		// 2016 trying to fix patterns
		
		if (exp > 0)
			forStr += (exp - dot - 1) + "E";
		else
			forStr += (format.length() - 1 - dot) + "f";
		
		Logger.debug("in getFormat, forStr = " + forStr);
		return  forStr;
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

	public void setForFastTitle(boolean isForFastTitle) {
		Logger.debug("in PalettePanel.setForFastTitle");
		this.isForFastTitle = isForFastTitle;
		ColorMap.IntervalType iType = ColorMap.IntervalType.AUTOMATIC;
		ColorMap.ScaleType sType = ColorMap.ScaleType.LINEAR;
		
		if ( this.model != null && this.model.getColorMap() != null) {
			if ( isForFastTitle ){
				this.model.getColorMap().setPlotType( ColorMap.PlotType.FAST_TILE);
			} else {
				this.model.getColorMap().setPlotType( ColorMap.PlotType.OTHER);
			}
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
		
		if ( this.isForFastTitle //&& this.model.getColorMap().getStatType() == ColorMap.StatType.NONE 
		    ) {
			this.labelScale.setVisible( true);
			this.scaleType.setVisible( true);
			if ( sType == ColorMap.ScaleType.LOGARITHM) {
				this.scaleType.setSelectedIndex(1);
			} else {
				this.scaleType.setSelectedIndex(0);
			}			
		} else {
			this.labelScale.setVisible( false);
			this.scaleType.setVisible( false);
		}	
	}

	public boolean isForFastTitle() {
		Logger.debug("in PalettePanel.isForFastTitle");
		return isForFastTitle;
	}
}
