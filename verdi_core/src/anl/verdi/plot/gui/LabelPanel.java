package anl.verdi.plot.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.Sizes;
import com.l2fprod.common.swing.JFontChooser;

import anl.verdi.formula.Formula;


/**
 * @author User #2
 */
public class LabelPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7581915584019023506L;
	private Font selectedFont, selectedTickFont;

	public LabelPanel() {
		initComponents();
		addListeners();
	}

	private void addListeners() {
		fontBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				selectFont();
			}
		});

		colorBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				selectColor();
			}
		});

		tickFontBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				selectTickFont();
			}
		});

		tickColorBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				selectTickColor();
			}
		});
	}

	private void selectFont() {
		Font font = JFontChooser.showDialog(this, "Select Font", selectedFont);
		if (font != null) {
			initFont(font);
		}
	}

	private void selectColor() {
		Color color = JColorChooser.showDialog(this, "Select Color", colorFld.getBackground());
		if (color != null) {
			colorFld.setBackground(color);
		}
	}

	private void selectTickFont() {
		Font font = JFontChooser.showDialog(this, "Select Font", selectedTickFont);
		if (font != null) {
			initTickFont(font);
		}
	}

	private void selectTickColor() {
		Color color = JColorChooser.showDialog(this, "Select Color", tickColorFld.getBackground());
		if (color != null) {
			tickColorFld.setBackground(color);
		}
	}

	public void initLabel(String title, Font font, Color color) {
		if (title != null) textFld.setText(title);
		if (color != null) colorFld.setBackground(color);
		if (font != null) initFont(font);
	}

	/**
	 * Disables the tick part of the panel.
	 */
	private void disableTicks() {
		tickChk.setEnabled(false);
		tickSep.setEnabled(false);
		tickFontFld.setEnabled(false);
		tickColorFld.setEnabled(false);
		tickColorBtn.setEnabled(false);
		tickFontBtn.setEnabled(false);
		tickFontLbl.setEnabled(false);
		tickColorLbl.setEnabled(false);
	}

	public void initTicks(Boolean show, Font font, Color color, Integer numOfTickLabels, Formula.Type plottype) {
		initTicks(show, font, color, numOfTickLabels);
		
		if (!plottype.equals(Formula.Type.TIME_SERIES_LINE) && !plottype.equals(Formula.Type.TIME_SERIES_BAR)) {
			formatFld.setEnabled(false);
			vRadio.setEnabled(false);
			lsRadio.setEnabled(false);
			rsRadio.setEnabled(false);
		}
	}
	
	public void initTicks(Boolean show, Font font, Color color, Integer numOfTickLabels) {
		if (show == null) {
			disableTicks();
			return;
		}
		else if (font == null && color == null) {
			tickFontFld.setEnabled(false);
			tickColorFld.setEnabled(false);
			tickColorBtn.setEnabled(false);
			tickFontBtn.setEnabled(false);
			tickFontLbl.setEnabled(false);
			tickColorLbl.setEnabled(false);
		}

		tickChk.setSelected(show);
		if (color != null) tickColorFld.setBackground(color);
		if (font != null) initTickFont(font);
		if (numOfTickLabels != null) numberFld.setText(numOfTickLabels.toString());
	}

	private void initFont(Font font) {
		selectedFont = font;
		float size = fontFld.getFont().getSize();
		fontFld.setFont(font.deriveFont(size));
		String strStyle;
		if (font.isBold()) {
			strStyle = font.isItalic() ? "bolditalic" : "bold";
		} else {
			strStyle = font.isItalic() ? "italic" : "plain";
		}
		fontFld.setText(font.getName() + ", " + strStyle + ", " + font.getSize());
	}

	private void initTickFont(Font font) {
		selectedTickFont = font;
		float size = tickFontFld.getFont().getSize();
		tickFontFld.setFont(font.deriveFont(size));
		String strStyle;
		if (font.isBold()) {
			strStyle = font.isItalic() ? "bolditalic" : "bold";
		} else {
			strStyle = font.isItalic() ? "italic" : "plain";
		}
		tickFontFld.setText(font.getName() + ", " + strStyle + ", " + font.getSize());
	}

	public Font getSelectedFont() {
		return selectedFont;
	}

	public Color getSelectedColor() {
		return colorFld.getBackground();
	}

	public String getText() {
		return textFld.getText();
	}
	
	public Integer getNumberOfLabels() {
		try {
			return new Integer(numberFld.getText());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public Font getSelectedTickFont() {
		return selectedTickFont;
	}

	public Color getSelectedTickColor() {
		return tickColorFld.getBackground();
	}

	public boolean isShowLabels() {
		return tickChk.isSelected();
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner non-commercial license
		DefaultComponentFactory compFactory = DefaultComponentFactory.getInstance();
		separator1 = compFactory.createSeparator("Label");
		label1 = new JLabel();
		textFld = new JTextField();
		label2 = new JLabel();
		fontFld = new JTextField();
		fontBtn = new JButton();
		label3 = new JLabel();
		colorFld = new JTextField();
		colorBtn = new JButton();
		tickSep = compFactory.createSeparator("Ticks");
		tickChk = new JCheckBox();
		number = new JLabel();
		numberFld = new JTextField();
		tickFontLbl = new JLabel();
		tickFontFld = new JTextField();
		tickFontBtn = new JButton();
		tickColorLbl = new JLabel();
		tickColorFld = new JTextField();
		tickColorBtn = new JButton();
		
		//====Added to configure domain axis labels
		format = new JLabel();
		formatFld = new JTextField();
		orientation = new JLabel();
		orientationOption = new JPanel();
		vRadio = new JRadioButton("Vertical");
		lsRadio = new JRadioButton("Left Slant");
		rsRadio = new JRadioButton("Right Slant");
		radioGroup = new ButtonGroup();
		radioGroup.add(vRadio);
		radioGroup.add(lsRadio);
		radioGroup.add(rsRadio);
		orientationOption.add(vRadio);
		orientationOption.add(lsRadio);
		orientationOption.add(rsRadio);
		vRadio.setSelected(true);
		formatFld.setToolTipText("Use date and time patterns such as: MM/dd/yyyy HH:mm:ss");
		
		CellConstraints cc = new CellConstraints();

		//======== this ========
		setBorder(new TitledBorder("Title"));
		// 2014
		ColumnSpec aColumnSpec = new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW);
		setLayout(new FormLayout(
						new ColumnSpec[]{
										FormFactory.RELATED_GAP_COLSPEC,
										FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
										FormFactory.DEFAULT_COLSPEC,
										FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
										aColumnSpec,
										FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
										FormFactory.DEFAULT_COLSPEC
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
										FormFactory.DEFAULT_ROWSPEC,
										FormFactory.LINE_GAP_ROWSPEC,
										FormFactory.DEFAULT_ROWSPEC,
										FormFactory.LINE_GAP_ROWSPEC,
										FormFactory.DEFAULT_ROWSPEC,
										FormFactory.LINE_GAP_ROWSPEC,
										FormFactory.DEFAULT_ROWSPEC,
										FormFactory.LINE_GAP_ROWSPEC,
										FormFactory.DEFAULT_ROWSPEC
						}));
//		setLayout(new FormLayout(
//				new ColumnSpec[]{
//								FormFactory.RELATED_GAP_COLSPEC,
//								FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
//								FormFactory.DEFAULT_COLSPEC,
//								FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
//								new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW),
//								FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
//								FormFactory.DEFAULT_COLSPEC
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
//								FormFactory.DEFAULT_ROWSPEC,
//								FormFactory.LINE_GAP_ROWSPEC,
//								FormFactory.DEFAULT_ROWSPEC,
//								FormFactory.LINE_GAP_ROWSPEC,
//								FormFactory.DEFAULT_ROWSPEC,
//								FormFactory.LINE_GAP_ROWSPEC,
//								FormFactory.DEFAULT_ROWSPEC
//				}));
		add(separator1, cc.xywh(1, 1, 7, 1));

		//---- label1 ----
		label1.setText("Text:");
		add(label1, cc.xy(3, 3));
		add(textFld, cc.xy(5, 3));

		//---- label2 ----
		label2.setText("Font:");
		add(label2, cc.xy(3, 5));

		//---- fontFld ----
		fontFld.setEditable(false);
		add(fontFld, cc.xy(5, 5));

		//---- fontBtn ----
		fontBtn.setText("Select");
		add(fontBtn, cc.xy(7, 5));

		//---- label3 ----
		label3.setText("Color:");
		add(label3, cc.xy(3, 7));

		//---- colorFld ----
		colorFld.setBackground(Color.black);
		colorFld.setEditable(false);
		add(colorFld, new CellConstraints(5, 7, 1, 1, CellConstraints.DEFAULT, CellConstraints.DEFAULT, new Insets(4, 0, 4, 0)));

		//---- colorBtn ----
		colorBtn.setText("Select");
		add(colorBtn, cc.xy(7, 7));
		add(tickSep, cc.xywh(1, 9, 7, 1));

		//---- tickChk ----
		tickChk.setText("Show Tick Labels");
		add(tickChk, cc.xywh(3, 11, 3, 1));
		
		//---- number ----
		number.setText("Number:");
		add(number, cc.xy(3, 13));
		add(numberFld, cc.xy(5, 13));

		//---- tickFontLbl ----
		tickFontLbl.setText("Font:");
		add(tickFontLbl, cc.xy(3, 15));

		//---- tickFontFld ----
		tickFontFld.setEditable(false);
		add(tickFontFld, cc.xy(5, 15));

		//---- tickFontBtn ----
		tickFontBtn.setText("Select");
		add(tickFontBtn, cc.xy(7, 15));

		//---- tickColorLbl ----
		tickColorLbl.setText("Color:");
		add(tickColorLbl, cc.xy(3, 17));

		//---- tickColorFld ----
		tickColorFld.setBackground(Color.black);
		tickColorFld.setEditable(false);
		add(tickColorFld, cc.xy(5, 17));

		//---- tickColorBtn ----
		tickColorBtn.setText("Select");
		add(tickColorBtn, cc.xy(7, 17));
		
		//---- Format ----
		format.setText("Format:");
		add(format, cc.xy(3, 19));
		add(formatFld, cc.xy(5, 19));
		
		//---- Orientation ----
		orientation.setText("Orientation:");
		add(orientation, cc.xy(3, 21));
		add(orientationOption, cc.xy(5, 21));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner non-commercial license
	private JComponent separator1;
	private JLabel label1;
	private JTextField textFld;
	private JLabel label2;
	private JTextField fontFld;
	private JButton fontBtn;
	private JLabel label3;
	private JTextField colorFld;
	private JButton colorBtn;
	private JComponent tickSep;
	private JCheckBox tickChk;
	private JLabel number;
	private JTextField numberFld;
	private JLabel tickFontLbl;
	private JTextField tickFontFld;
	private JButton tickFontBtn;
	private JLabel tickColorLbl;
	private JTextField tickColorFld;
	private JButton tickColorBtn;
	private JLabel format;
	private JTextField formatFld;
	private JLabel orientation;
	private JPanel orientationOption;
	private JRadioButton vRadio;
	private JRadioButton lsRadio;
	private JRadioButton rsRadio;
	private ButtonGroup radioGroup;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
