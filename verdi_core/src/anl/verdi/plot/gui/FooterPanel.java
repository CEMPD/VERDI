package anl.verdi.plot.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.Sizes;
import com.l2fprod.common.swing.JFontChooser;

/**
 * @author User #2
 */
public class FooterPanel extends JPanel {
	private static final long serialVersionUID = -2634158020842001526L;
	private Font selectedFont;
	private boolean disableText = false;
	private boolean textFieldModified = false;

	public FooterPanel() {
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

		useBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				doEnable(useBox.isSelected());
			}
		});
		
		textFld.addKeyListener(new KeyAdapter(){
			public void keyPressed(KeyEvent e) {
				textFieldModified = true;
			}
			
			public void keyReleased(KeyEvent e) {
				textFieldModified = true;
				String text = textFld.getText();
				if (text == null || text.trim().isEmpty()) useBox.setSelected(false);
			}
			
			public void keyTyped(KeyEvent e) {
				textFieldModified = true;
			}
		});
	}

	private void doEnable(boolean enabled) {
		if (!disableText) textFld.setEnabled(enabled);
		fontFld.setEnabled(enabled);
		colorFld.setEnabled(enabled);
		fontBtn.setEnabled(enabled);
		colorBtn.setEnabled(enabled);
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
	
	public void disableText() {
		disableText = true;
		textFld.setEnabled(false);
	}

	public void init(boolean enabled, String title, Font font, Color color) {
		useBox.setSelected(enabled);
		doEnable(enabled);
		if (!disableText) textFld.setText(title);
		colorFld.setBackground(color);
		initFont(font);
	}

	private void initFont(Font font) {
		if (font == null) {
			fontFld.setEnabled(false);
			fontBtn.setEnabled(false);
		} else {
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
	}

	public Font getSelectedFont() {
		return selectedFont;
	}

	public Color getSelectedColor() {
		return colorFld.getBackground();
	}

	public String getTextFieldContent() {
		return textFld.getText();
	}

	public boolean useTextField() {
		return useBox.isSelected();
	}
	
	public boolean textFieldModified() {
		return textFieldModified;
	}

	private void initComponents() {
		label4 = new JLabel();
		useBox = new JCheckBox();
		label1 = new JLabel();
		textFld = new JTextField();
		label2 = new JLabel();
		fontFld = new JTextField();
		fontBtn = new JButton();
		label3 = new JLabel();
		colorFld = new JTextField();
		colorBtn = new JButton();
		CellConstraints cc = new CellConstraints();

		//======== this ========
		setBorder(new TitledBorder("Footer"));
		// 2014
		ColumnSpec aColumnSpec = new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW);
		setLayout(new FormLayout(
						new ColumnSpec[]{
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
										FormFactory.DEFAULT_ROWSPEC
						}));
//		setLayout(new FormLayout(
//				new ColumnSpec[]{
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
//								FormFactory.DEFAULT_ROWSPEC
//				}));

		//---- label4 ----
		label4.setText("Show Text");
		add(label4, cc.xy(1, 1));
		add(useBox, cc.xy(3, 1));

		//---- label1 ----
		label1.setText("Text:");
		add(label1, cc.xy(1, 3));
		add(textFld, cc.xy(3, 3));

		//---- label2 ----
		label2.setText("Font:");
		add(label2, cc.xy(1, 5));

		//---- fontFld ----
		fontFld.setEditable(false);
		add(fontFld, cc.xy(3, 5));

		//---- fontBtn ----
		fontBtn.setText("Select");
		add(fontBtn, cc.xy(5, 5));

		//---- label3 ----
		label3.setText("Color:");
		add(label3, cc.xy(1, 7));

		//---- colorFld ----
		colorFld.setBackground(Color.black);
		colorFld.setEditable(false);
		add(colorFld, new CellConstraints(3, 7, 1, 1, CellConstraints.DEFAULT, CellConstraints.DEFAULT, new Insets(4, 0, 4, 0)));

		//---- colorBtn ----
		colorBtn.setText("Select");
		add(colorBtn, cc.xy(5, 7));
	}

	private JLabel label4;
	private JCheckBox useBox;
	private JLabel label1;
	private JTextField textFld;
	private JLabel label2;
	private JTextField fontFld;
	private JButton fontBtn;
	private JLabel label3;
	private JTextField colorFld;
	private JButton colorBtn;
}
