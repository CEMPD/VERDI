package anl.verdi.plot.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.Sizes;
import com.l2fprod.common.swing.JFontChooser;

import anl.verdi.plot.config.ThemeConfig;
import anl.verdi.plot.util.PlotProperties;


/**
 * @author qun
 */
public class ChartThemePanel extends JPanel {
	private static final long serialVersionUID = -3469212269628821152L;
	static final Logger Logger = LogManager.getLogger(ChartThemePanel.class.getName());

	public ChartThemePanel() {
		Logger.debug("in constructor for ChartThemePanel, ready to call initComponents");
		config = PlotProperties.getInstance().getThemeConfig();
		initComponents();
		Logger.debug("back from initComponents");
	}

	private Font selectFont(JTextField fontFld) {
		Font font = JFontChooser.showDialog(this, "Select Font", null);
		if (font != null) {
			fontFld.setEnabled(true);
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
		
		return font;
	}
	
	private Font setFontField(JTextField fontFld, String key) {
		if (config != null && config.getObject(key) != null) {
			Font font = (Font)config.getObject(key);
			fontFld.setEnabled(true);
			float size = fontFld.getFont().getSize();
			fontFld.setFont(font.deriveFont(size));
			String strStyle;
			if (font.isBold()) {
				strStyle = font.isItalic() ? "bolditalic" : "bold";
			} else {
				strStyle = font.isItalic() ? "italic" : "plain";
			}
			fontFld.setText(font.getName() + ", " + strStyle + ", " + font.getSize());
			return font;
		}
		
		fontFld.setEnabled(false);
		return null;
	}

	private Color selectColor(JTextField colorFld) {
		Color color = JColorChooser.showDialog(this, "Select Color", colorFld.getBackground());
		if (color != null) {
			colorFld.setEnabled(true);
			colorFld.setBackground(color);
		}
		
		return color;
	}
	
	private Color setColorField(JTextField colorFld, String key) {
		if (config != null && config.getObject(key) != null) {
			Color color = (Color)config.getObject(key);
			colorFld.setEnabled(true);
			colorFld.setBackground(color);
			return color;
		}
		
		colorFld.setEnabled(false);
		return null;
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ChartThemePanel titles = new ChartThemePanel();

		frame.setLayout(new BorderLayout());
		frame.add(titles, BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
	}

	private void initComponents() {
		fontPanel = createFontsPanel();
		titlePanel = createTitlesPanel();
		legendNLabelsPanel = createLabelsPanel();
		miscPanel = createMiscPanel();
		CellConstraints cc = new CellConstraints();

		ColumnSpec[] aColumnSpec = ColumnSpec.decodeSpecs("default:grow");
		setLayout(new FormLayout(
						aColumnSpec,
						new RowSpec[]{
										FormFactory.DEFAULT_ROWSPEC,
										FormFactory.LINE_GAP_ROWSPEC,
										FormFactory.DEFAULT_ROWSPEC,
										FormFactory.LINE_GAP_ROWSPEC,
										FormFactory.DEFAULT_ROWSPEC,
										FormFactory.LINE_GAP_ROWSPEC,
										FormFactory.DEFAULT_ROWSPEC
						}));
		add(fontPanel, cc.xy(1, 1));
		add(titlePanel, cc.xy(1, 3));
		add(legendNLabelsPanel, cc.xy(1, 5));
		add(miscPanel, cc.xy(1, 7));
	}
	
	private JPanel createFontsPanel() {
		JPanel contentPanel = new JPanel();

		useShadowLbl = new JLabel();
		useShadowBox = new JCheckBox();
		xlargeFontLbl = new JLabel();
		xlargeFontFld = new JTextField();
		xlargeFontBtn = new JButton();
		largeFontLbl = new JLabel();
		largeFontFld = new JTextField();
		largeFontBtn = new JButton();
		regFontLbl = new JLabel();
		regFontFld = new JTextField();
		regFontBtn = new JButton();
		smallFontLbl = new JLabel();
		smallFontFld = new JTextField();
		samllFontBtn = new JButton();
		
		CellConstraints cc = new CellConstraints();
		contentPanel.setBorder(new TitledBorder("Shadow & Font"));
		contentPanel.setLayout(getFormLayout());
		
		//---- Shadow theme ----
		useShadowLbl.setText("Show Shadow:");
		contentPanel.add(useShadowLbl, cc.xy(1, 1));
		contentPanel.add(useShadowBox, cc.xy(3, 1));
		
		if (config != null && config.getObject(ThemeConfig.SHOW_SHADOW) != null) {
			boolean show = (Boolean)config.getObject(ThemeConfig.SHOW_SHADOW);
			useShadowBox.setSelected(show);
		}

		//---- X-Large Font font theme ----
		xlargeFontLbl.setText("Title Font:");
		contentPanel.add(xlargeFontLbl, cc.xy(1, 3));
		contentPanel.add(xlargeFontFld, cc.xy(3, 3));
		xlargeFont = setFontField(xlargeFontFld, ThemeConfig.X_LARGE_FONT);
		xlargeFontBtn.setText("Select");
		xlargeFontBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				xlargeFont = selectFont(xlargeFontFld);
			}
		});
		contentPanel.add(xlargeFontBtn, cc.xy(5, 3));
		
		//---- Large font theme ----
		largeFontLbl.setText("Subtitle(s) Font:");
		contentPanel.add(largeFontLbl, cc.xy(1, 5));
		contentPanel.add(largeFontFld, cc.xy(3, 5));
		largeFont = setFontField(largeFontFld, ThemeConfig.LARGE_FONT);
		largeFontBtn.setText("Select");
		largeFontBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				largeFont = selectFont(largeFontFld);
			}
		});
		contentPanel.add(largeFontBtn, cc.xy(5, 5));
		
		//---- Regular font theme ----
		regFontLbl.setText("Tick Label(s) Font:");
		contentPanel.add(regFontLbl, cc.xy(1, 7));
		contentPanel.add(regFontFld, cc.xy(3, 7));
		regularFont = setFontField(regFontFld, ThemeConfig.REGULAR_FONT);
		regFontBtn.setText("Select");
		regFontBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				regularFont = selectFont(regFontFld);
			}
		});
		contentPanel.add(regFontBtn, cc.xy(5, 7));

		//---- Small font theme ----
		smallFontLbl.setText("Small Font:");
		contentPanel.add(smallFontLbl, cc.xy(1, 9));
		contentPanel.add(smallFontFld, cc.xy(3, 9));
		smallFont = setFontField(smallFontFld, ThemeConfig.SMALL_FONT);
		samllFontBtn.setText("Select");
		samllFontBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				smallFont = selectFont(smallFontFld);
			}
		});
		contentPanel.add(samllFontBtn, cc.xy(5, 9));
		
		return contentPanel;
	}
	
	private JPanel createTitlesPanel() {
		JPanel contentPanel = new JPanel();
		titleColorLbl = new JLabel();
		titleColorFld = new JTextField();
		titleColorBtn = new JButton();
		subTitleColorLbl = new JLabel();
		subTitleColorFld = new JTextField();
		subTitleColorBtn = new JButton();
		CellConstraints cc = new CellConstraints();
		contentPanel.setBorder(new TitledBorder("Title"));
		contentPanel.setLayout(getFormLayout());
		
		//---- title theme ----
		titleColorLbl.setText("Title Paint:");
		contentPanel.add(titleColorLbl, cc.xy(1, 1));
		titleColor = setColorField(titleColorFld, ThemeConfig.TITLE_PAINT);
		titleColorFld.setEditable(false);
		contentPanel.add(titleColorFld, new CellConstraints(3, 1, 1, 1, CellConstraints.DEFAULT, CellConstraints.DEFAULT, new Insets(4, 0, 4, 0)));
		titleColorBtn.setText("Select");
		titleColorBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				titleColor = selectColor(titleColorFld);
			}
		});
		contentPanel.add(titleColorBtn, cc.xy(5, 1));

		//---- subtitle theme ----
		subTitleColorLbl.setText("Subtitle Paint:");
		contentPanel.add(subTitleColorLbl, cc.xy(1, 3));
		subTitleColor = setColorField(subTitleColorFld, ThemeConfig.SUBTITLE_PAINT);
		subTitleColorFld.setEditable(false);
		contentPanel.add(subTitleColorFld, new CellConstraints(3, 3, 1, 1, CellConstraints.DEFAULT, CellConstraints.DEFAULT, new Insets(4, 0, 4, 0)));
		subTitleColorBtn.setText("Select");
		subTitleColorBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				subTitleColor = selectColor(subTitleColorFld);
			}
		});
		contentPanel.add(subTitleColorBtn, cc.xy(5, 3));
		
		return contentPanel;
	}
	
	private JPanel createLabelsPanel() {
		JPanel contentPanel = new JPanel();
		legendColorLbl = new JLabel();
		legendColorFld = new JTextField();
		legendColorBtn = new JButton();
		
		legendBgColorLbl = new JLabel();
		legendBgColorFld = new JTextField();
		legendBgColorBtn = new JButton();
		
		axisLableColorLbl = new JLabel();
		axisLableColorFld = new JTextField();
		axisLableColorBtn = new JButton();
		
		tickLabelColorLbl = new JLabel();
		tickLabelColorFld = new JTextField();
		tickLabelColorBtn = new JButton();
		
		CellConstraints cc = new CellConstraints();
		contentPanel.setBorder(new TitledBorder("Legend & Labels"));
		contentPanel.setLayout(getFormLayout());
		
		//---- Legend theme ----
		legendColorLbl.setText("Legend Paint:");
		contentPanel.add(legendColorLbl, cc.xy(1, 1));
		legendColor = setColorField(legendColorFld, ThemeConfig.LEGEND_PAINT);
		legendColorFld.setEditable(false);
		contentPanel.add(legendColorFld, new CellConstraints(3, 1, 1, 1, CellConstraints.DEFAULT, CellConstraints.DEFAULT, new Insets(4, 0, 4, 0)));
		legendColorBtn.setText("Select");
		legendColorBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				legendColor = selectColor(legendColorFld);
			}
		});
		contentPanel.add(legendColorBtn, cc.xy(5, 1));

		//---- Legend background theme ----
		legendBgColorLbl.setText("Legend Bg Paint:");
		contentPanel.add(legendBgColorLbl, cc.xy(1, 3));
		legendBgColor = setColorField(legendBgColorFld, ThemeConfig.LEGEND_BG_PAINT);
		legendBgColorFld.setEditable(false);
		contentPanel.add(legendBgColorFld, new CellConstraints(3, 3, 1, 1, CellConstraints.DEFAULT, CellConstraints.DEFAULT, new Insets(4, 0, 4, 0)));
		legendBgColorBtn.setText("Select");
		legendBgColorBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				legendBgColor = selectColor(legendBgColorFld);
			}
		});
		contentPanel.add(legendBgColorBtn, cc.xy(5, 3));
		
		//---- Axis Label theme ----
		axisLableColorLbl.setText("Axis Label Paint:");
		contentPanel.add(axisLableColorLbl, cc.xy(1, 5));
		axisLableColor = setColorField(axisLableColorFld, ThemeConfig.AXIS_LABEL_PAINT);
		axisLableColorFld.setEditable(false);
		contentPanel.add(axisLableColorFld, new CellConstraints(3, 5, 1, 1, CellConstraints.DEFAULT, CellConstraints.DEFAULT, new Insets(4, 0, 4, 0)));
		axisLableColorBtn.setText("Select");
		axisLableColorBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				axisLableColor = selectColor(axisLableColorFld);
			}
		});
		contentPanel.add(axisLableColorBtn, cc.xy(5, 5));
		
		//---- Tick Label theme ----
		tickLabelColorLbl.setText("Tick Label Paint:");
		contentPanel.add(tickLabelColorLbl, cc.xy(1, 7));
		tickLabelColor = setColorField(tickLabelColorFld, ThemeConfig.TICK_LABEL_PAINT);
		tickLabelColorFld.setEditable(false);
		contentPanel.add(tickLabelColorFld, new CellConstraints(3, 7, 1, 1, CellConstraints.DEFAULT, CellConstraints.DEFAULT, new Insets(4, 0, 4, 0)));
		tickLabelColorBtn.setText("Select");
		tickLabelColorBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				tickLabelColor = selectColor(tickLabelColorFld);
			}
		});
		contentPanel.add(tickLabelColorBtn, cc.xy(5, 7));
		
		return contentPanel;
	}
	
	private JPanel createMiscPanel() {
		JPanel contentPanel = new JPanel();
		chartBgColorLbl = new JLabel();
		chartBgColorFld = new JTextField();
		chartBgColorBtn = new JButton();
		
		plotBgColorLbl = new JLabel();
		plotBgColorFld = new JTextField();
		plotBgColorBtn = new JButton();
		
		plotOlColorLbl = new JLabel();
		plotOlColorFld = new JTextField();
		plotOlColorBtn = new JButton();
		
		domainGlColorLbl = new JLabel();
		domainGlColorFld = new JTextField();
		domainGlColorBtn = new JButton();

		rangeGlColorLbl = new JLabel();
		rangeGlColorFld = new JTextField();
		rangeGlColorBtn = new JButton();
		
		baselineColorLbl = new JLabel();
		baselineColorFld = new JTextField();
		baselineColorBtn = new JButton();
		
		crosshairColorLbl = new JLabel();
		crosshairColorFld = new JTextField();
		crosshairColorBtn = new JButton();
		
		itemLabelColorLbl = new JLabel();
		itemLabelColorFld = new JTextField();
		itemLabelColorBtn = new JButton();
		
		shadowColorLbl = new JLabel();
		shadowColorFld = new JTextField();
		shadowColorBtn = new JButton();
		
		CellConstraints cc = new CellConstraints();
		contentPanel.setBorder(new TitledBorder("Miscellaneous"));
		contentPanel.setLayout(getFormLayout());
		
		//---- Legend theme ----
		chartBgColorLbl.setText("Chart Bg Paint:");
		contentPanel.add(chartBgColorLbl, cc.xy(1, 1));
		chartBgColor = setColorField(chartBgColorFld, ThemeConfig.CHART_BG_PAINT);
		chartBgColorFld.setEditable(false);
		contentPanel.add(chartBgColorFld, new CellConstraints(3, 1, 1, 1, CellConstraints.DEFAULT, CellConstraints.DEFAULT, new Insets(4, 0, 4, 0)));
		chartBgColorBtn.setText("Select");
		chartBgColorBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				chartBgColor = selectColor(chartBgColorFld);
			}
		});
		contentPanel.add(chartBgColorBtn, cc.xy(5, 1));

		//---- Legend background theme ----
		plotBgColorLbl.setText("Plot Bg Paint:");
		contentPanel.add(plotBgColorLbl, cc.xy(1, 3));
		plotBgColor = setColorField(plotBgColorFld, ThemeConfig.PLOT_BG_PAINT);
		plotBgColorFld.setEditable(false);
		contentPanel.add(plotBgColorFld, new CellConstraints(3, 3, 1, 1, CellConstraints.DEFAULT, CellConstraints.DEFAULT, new Insets(4, 0, 4, 0)));
		plotBgColorBtn.setText("Select");
		plotBgColorBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				plotBgColor = selectColor(plotBgColorFld);
			}
		});
		contentPanel.add(plotBgColorBtn, cc.xy(5, 3));
		
		//---- Plot outline theme ----
		plotOlColorLbl.setText("Plot Outline Paint:");
		contentPanel.add(plotOlColorLbl, cc.xy(1, 5));
		plotOlColor = setColorField(plotOlColorFld, ThemeConfig.PLOT_OUTLINE_PAINT);
		plotOlColorFld.setEditable(false);
		contentPanel.add(plotOlColorFld, new CellConstraints(3, 5, 1, 1, CellConstraints.DEFAULT, CellConstraints.DEFAULT, new Insets(4, 0, 4, 0)));
		plotOlColorBtn.setText("Select");
		plotOlColorBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				plotOlColor = selectColor(plotOlColorFld);
			}
		});
		contentPanel.add(plotOlColorBtn, cc.xy(5, 5));
		
		//---- Domain Gridline theme ----
		domainGlColorLbl.setText("Domain Grdln Paint:");
		contentPanel.add(domainGlColorLbl, cc.xy(1, 7));
		domainGlColor = setColorField(domainGlColorFld, ThemeConfig.DOMAIN_GRDLN_PAINT);
		domainGlColorFld.setEditable(false);
		contentPanel.add(domainGlColorFld, new CellConstraints(3, 7, 1, 1, CellConstraints.DEFAULT, CellConstraints.DEFAULT, new Insets(4, 0, 4, 0)));
		domainGlColorBtn.setText("Select");
		domainGlColorBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				domainGlColor = selectColor(domainGlColorFld);
			}
		});
		contentPanel.add(domainGlColorBtn, cc.xy(5, 7));
		
		//---- Range Gridline theme ----
		rangeGlColorLbl.setText("Range Grdln Paint:");
		contentPanel.add(rangeGlColorLbl, cc.xy(1, 9));
		rangeGlColor = setColorField(rangeGlColorFld, ThemeConfig.RANGE_GRDLN_PAINT);
		rangeGlColorFld.setEditable(false);
		contentPanel.add(rangeGlColorFld, new CellConstraints(3, 9, 1, 1, CellConstraints.DEFAULT, CellConstraints.DEFAULT, new Insets(4, 0, 4, 0)));
		rangeGlColorBtn.setText("Select");
		rangeGlColorBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				rangeGlColor = selectColor(rangeGlColorFld);
			}
		});
		contentPanel.add(rangeGlColorBtn, cc.xy(5, 9));
		
		//---- Baseline theme ----
		baselineColorLbl.setText("Baseline Paint:");
		contentPanel.add(baselineColorLbl, cc.xy(1, 11));
		baselineColor = setColorField(baselineColorFld, ThemeConfig.BASELINE_PAINT);
		baselineColorFld.setEditable(false);
		contentPanel.add(baselineColorFld, new CellConstraints(3, 11, 1, 1, CellConstraints.DEFAULT, CellConstraints.DEFAULT, new Insets(4, 0, 4, 0)));
		baselineColorBtn.setText("Select");
		baselineColorBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				baselineColor = selectColor(baselineColorFld);
			}
		});
		contentPanel.add(baselineColorBtn, cc.xy(5, 11));
		
		//---- Cross Hair theme ----
		crosshairColorLbl.setText("Crosshair Paint:");
		contentPanel.add(crosshairColorLbl, cc.xy(1, 13));
		crosshairColor = setColorField(crosshairColorFld, ThemeConfig.CROSSHAIR_PAINT);
		crosshairColorFld.setEditable(false);
		contentPanel.add(crosshairColorFld, new CellConstraints(3, 13, 1, 1, CellConstraints.DEFAULT, CellConstraints.DEFAULT, new Insets(4, 0, 4, 0)));
		crosshairColorBtn.setText("Select");
		crosshairColorBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				crosshairColor = selectColor(crosshairColorFld);
			}
		});
		contentPanel.add(crosshairColorBtn, cc.xy(5, 13));
		
		//---- Item Label theme ----
		itemLabelColorLbl.setText("Item Label Paint:");
		contentPanel.add(itemLabelColorLbl, cc.xy(1, 15));
		itemLabelColor = setColorField(itemLabelColorFld, ThemeConfig.ITEM_LABEL_PAINT);
		itemLabelColorFld.setEditable(false);
		contentPanel.add(itemLabelColorFld, new CellConstraints(3, 15, 1, 1, CellConstraints.DEFAULT, CellConstraints.DEFAULT, new Insets(4, 0, 4, 0)));
		itemLabelColorBtn.setText("Select");
		itemLabelColorBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				itemLabelColor = selectColor(itemLabelColorFld);
			}
		});
		contentPanel.add(itemLabelColorBtn, cc.xy(5, 15));
		
		//---- Shadow theme ----
		shadowColorLbl.setText("Shadow Paint:");
		contentPanel.add(shadowColorLbl, cc.xy(1, 17));
		shadowColor = setColorField(shadowColorFld, ThemeConfig.SHADOW_PAINT);
		shadowColorFld.setEditable(false);
		contentPanel.add(shadowColorFld, new CellConstraints(3, 17, 1, 1, CellConstraints.DEFAULT, CellConstraints.DEFAULT, new Insets(4, 0, 4, 0)));
		shadowColorBtn.setText("Select");
		shadowColorBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				shadowColor = selectColor(shadowColorFld);
			}
		});
		contentPanel.add(shadowColorBtn, cc.xy(5, 17));
		
		return contentPanel;
	}
	
	private FormLayout getFormLayout() {
		ColumnSpec aColumnSpec = new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW);
		
		return new FormLayout(
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
		});
	}
    
	public void setThemeProperties(ThemeConfig theme) {
		theme.putObject(ThemeConfig.SHOW_SHADOW, useShadowBox.isSelected());
		if (xlargeFont != null) theme.putObject(ThemeConfig.X_LARGE_FONT, xlargeFont);
		if (largeFont != null) theme.putObject(ThemeConfig.LARGE_FONT, largeFont);
		if (regularFont != null) theme.putObject(ThemeConfig.REGULAR_FONT, regularFont);
		if (smallFont != null) theme.putObject(ThemeConfig.SMALL_FONT, smallFont);
		if (titleColor != null) theme.putObject(ThemeConfig.TITLE_PAINT, titleColor);
		if (subTitleColor != null) theme.putObject(ThemeConfig.SUBTITLE_PAINT, subTitleColor);
		if (legendColor != null) theme.putObject(ThemeConfig.LEGEND_PAINT, legendColor);
		if (legendBgColor != null) theme.putObject(ThemeConfig.LEGEND_BG_PAINT, legendBgColor);
		if (axisLableColor != null) theme.putObject(ThemeConfig.AXIS_LABEL_PAINT, axisLableColor);
		if (tickLabelColor != null) theme.putObject(ThemeConfig.TICK_LABEL_PAINT, tickLabelColor);
		if (chartBgColor != null) theme.putObject(ThemeConfig.CHART_BG_PAINT, chartBgColor);
		if (plotBgColor != null) theme.putObject(ThemeConfig.PLOT_BG_PAINT, plotBgColor);
		if (plotOlColor != null) theme.putObject(ThemeConfig.PLOT_OUTLINE_PAINT, plotOlColor);
		if (domainGlColor != null) theme.putObject(ThemeConfig.DOMAIN_GRDLN_PAINT, domainGlColor);
		if (rangeGlColor != null) theme.putObject(ThemeConfig.RANGE_GRDLN_PAINT, rangeGlColor);
		if (baselineColor != null) theme.putObject(ThemeConfig.BASELINE_PAINT, baselineColor);
		if (crosshairColor != null) theme.putObject(ThemeConfig.CROSSHAIR_PAINT, crosshairColor);
		if (itemLabelColor != null) theme.putObject(ThemeConfig.ITEM_LABEL_PAINT, itemLabelColor);
		if (shadowColor != null) theme.putObject(ThemeConfig.SHADOW_PAINT, shadowColor);
	}
	
    private JLabel useShadowLbl;
    private JCheckBox useShadowBox;
	private JLabel xlargeFontLbl;
	private JTextField xlargeFontFld;
	private JButton xlargeFontBtn;
	private JLabel largeFontLbl;
	private JTextField largeFontFld;
	private JButton largeFontBtn;
	private JLabel regFontLbl;
	private JTextField regFontFld;
	private JButton regFontBtn;
	private JLabel smallFontLbl;
	private JTextField smallFontFld;
	private JButton samllFontBtn;
	
	private JLabel titleColorLbl;
	private JTextField titleColorFld;
	private JButton titleColorBtn;
	private JLabel subTitleColorLbl;
	private JTextField subTitleColorFld;
	private JButton subTitleColorBtn;
	
	private JLabel legendColorLbl;
	private JTextField legendColorFld;
	private JButton legendColorBtn;	
	private JLabel legendBgColorLbl;
	private JTextField legendBgColorFld;
	private JButton legendBgColorBtn;	
	private JLabel axisLableColorLbl;
	private JTextField axisLableColorFld;
	private JButton axisLableColorBtn;	
	private JLabel tickLabelColorLbl;
	private JTextField tickLabelColorFld;
	private JButton tickLabelColorBtn;
	
	private JLabel chartBgColorLbl;
	private JTextField chartBgColorFld;
	private JButton chartBgColorBtn;
	
	private JLabel plotBgColorLbl;
	private JTextField plotBgColorFld;
	private JButton plotBgColorBtn;
	
	private JLabel plotOlColorLbl;
	private JTextField plotOlColorFld;
	private JButton plotOlColorBtn;
	
	private JLabel domainGlColorLbl;
	private JTextField domainGlColorFld;
	private JButton domainGlColorBtn;
	
	private JLabel rangeGlColorLbl;
	private JTextField rangeGlColorFld;
	private JButton rangeGlColorBtn;
	
	private JLabel baselineColorLbl;
	private JTextField baselineColorFld;
	private JButton baselineColorBtn;
	
	private JLabel crosshairColorLbl;
	private JTextField crosshairColorFld;
	private JButton crosshairColorBtn;
	
	private JLabel itemLabelColorLbl;
	private JTextField itemLabelColorFld;
	private JButton itemLabelColorBtn;
	
	private JLabel shadowColorLbl;
	private JTextField shadowColorFld;
	private JButton shadowColorBtn;
	
	private JPanel fontPanel;
	private JPanel titlePanel;
	private JPanel legendNLabelsPanel;
	private JPanel miscPanel;

	protected Font xlargeFont, largeFont, regularFont, smallFont; 
	protected Color titleColor, subTitleColor, legendColor, legendBgColor,
	          axisLableColor, tickLabelColor, chartBgColor, plotBgColor,
	          plotOlColor, domainGlColor, rangeGlColor, baselineColor,
	          crosshairColor, itemLabelColor, shadowColor;
	
	private ThemeConfig config;
}
