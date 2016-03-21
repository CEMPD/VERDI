package anl.verdi.plot.gui;

import java.awt.ComponentOrientation;
//import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.FontMetrics;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;
import javax.swing.JMenuBar;

import java.awt.Color;

//import javax.swing.SwingConstants;







import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.geotools.map.MapContent;
import org.geotools.renderer.GTRenderer;
import org.geotools.swing.JMapPane;
import org.geotools.swing.RenderingExecutor;

import javax.swing.border.LineBorder;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.font.TextAttribute;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.NumberFormat;

// class forms the basis for the VERDI panel that displays the entire GTTilePlot
// title, subtitle1, subtitle2, axes, axis ticks and labels, footers, legend, JMapPane for geographic content
public class GTTilePlotPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4854488557959521575L;
	static final Logger Logger = LogManager.getLogger(GTTilePlotPanel.class.getName());
	// pull declaration of all other components from constructor to here as class-level data members
	private GridBagLayout gbl_contentPane;	// overall layout manager
	private GridBagConstraints c;
	private BoxLayout titlesLayout;
	private JMenuBar menuBar;			// bar at top of contentPane; plot-specific menu items
	private JToolBar toolBar;			// toolBar just below bar in contentPane; widgets for the
											// user to define what to plot (time step, etc.)
	private JPanel titlesPanel;			// titlesPanel just below toolBar in contentPane; title, subtitles 1 & 2
	private JPanel rangeAxisLabel;		// rangeAxisLabel at left side of contentPane; axis label for Range axis
	private JPanel rangeTickLabels;		// rangeTickLabels just to right of rangeAxisLabel in contentPane; tick marks and values
	private JPanel domainTickLabels;	// domainTickLabels just below chart in contentPane; tick marks and values
	private JPanel domainAxisLabel;		// domainAxisLabel just below domainTickLabels in contentPane; axis label for Domain axis
	private JPanel footersPanel;		// footersPanel below domainAxisLabel in contentPane; footers 1 & 2 & annotations legend
	private JPanel legendPanel;			// legendPanel at right side of contentPane; legend for the tile chart colors
	protected JMapPane topMapPanel;		// primary chart (geospatial) for tile chart and associated shapefiles
	
	private final static int BORDER = 1;	// 1 blank row/column between outer line of outermost widget to first widget 
	private final static int BAR_HT = 1;
	private final static int TOOLBAR_HT = 3;
	private final static int TITLES_PANEL_HT = 8;
	private final static int RANGE_AXIS_LABEL_WIDTH = 4;
	private final static int RANGE_TICK_LABELS_WIDTH = 4;
	private final static int TOP_MAP_PANEL_WIDTH = 150;
	private final static int TOP_MAP_PANEL_HT = 100;
	private final static int DOMAIN_TICK_LABELS_HT = 4;
	private final static int DOMAIN_AXIS_LABEL_HT = 4;
	private final static int FOOTERS_PANEL_HT = 4;
	private final static int LEGEND_PANEL_WIDTH = 20;
	private final static int VMIN = 2;	// minimum vertical space between components in same panel
	
	private final static boolean RIGHT_TO_LEFT = false;

	// Log-related

	protected boolean log = false;
//	private boolean preLog = false;
	private double logBase = 10.0; //Math.E;	

	// titles & footers
	private Font tFont, s1Font, s2Font;				// fonts for title, subtitle1, subtitle2
	private Color tColor, s1Color, s2Color;			// colors for title, subtitle1, subtitle2
	private String tString, s1String, s2String;		// strings to print for title, subtitle1, subtitle2
	private Font f1Font, f2Font;					// fonts for footer1, footer2
	private Color f1Color, f2Color;					// colors for footer1, footer2
	private String f1String, f2String;				// strings to print for footer1, footer2

	// legend-related
	private boolean showLegend = true;				// default to show the legend for the tiles
	private String unitStr;							// unit of measure
	private String logStr;							// " (Log"
	private String baseStr;							// base of log string
	private Boolean uShowTick;						// true/false show tick for units
	private Color uTickColor;						// color for tick marks
	private Integer labelCnt;						// number of tick marks (1/label)
	private Font labelFont;							// Font for tick labels
	private Font unitsFont;							// Font for units
	private Color unitsClr;							// Color for units
	private int xMaximum;							// maximum X position for JPanel (xOffset + width)
	private int yMinimum;							// minimum Y position for JPanel (yOffset)
	private int yMaximum;							// maximum Y position for JPanel (yMinimum + height)
	private double[] legendLevels;					// value associated with the break point between legend levels
	private Color[] legendColors;					// Color for each range of values in legend and tile plot
	
	private MapContent myMapContent;
	private RenderingExecutor myRenderingExecutor;
	private GTRenderer myGTRenderer;
	protected NumberFormat numberFormat;

	/**
	 * Create the overall frame and all of its components.
	 */
	public GTTilePlotPanel() {

		setBorder(new EmptyBorder(1, 1, 1, 1));
		
		gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{0};
		gbl_contentPane.rowHeights = new int[]{0};
		gbl_contentPane.columnWeights = new double[]{Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{Double.MIN_VALUE};
		if (RIGHT_TO_LEFT) {
			setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		}
		setLayout(gbl_contentPane);
		c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;	//resize horizontally and vertically
		gbl_contentPane.setConstraints(this, c);

		// first define components that go across the top of the frame

		menuBar = new JMenuBar() {// bar menu strip at top of contentPane
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D) g;
				Logger.debug("JMenuBar.paintComponent");
			}
		};	
		// start upper-left corner of window, 1-cell height, lt. gray background
		menuBar.setBackground(Color.LIGHT_GRAY ); 
		c.gridx = BORDER;
		c.gridy = BORDER;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridheight = BAR_HT;
		gbl_contentPane.setConstraints(menuBar, c);
		add(menuBar);

		toolBar = new JToolBar() {	// contains time step, layer, stats, animation widgets
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D) g;
				Logger.debug("JToolBar.paintComponent");
			}
		};	
		// start just below bar, 3 cell height, lt. gray background
		toolBar.setFloatable(false);
		toolBar.setBackground(Color.LIGHT_GRAY);
		c.gridx = BORDER;
		c.gridy = BORDER + BAR_HT;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridheight = TOOLBAR_HT;
		gbl_contentPane.setConstraints(toolBar, c);
		add(toolBar);

		titlesPanel = new JPanel() {	// contains title, subtitle1, subtitle2
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g){
				super.paintComponent(g); 	// have to start with this 
				Logger.debug("in paintComponent, ready to get graphics for titlesPanel");
				Graphics2D g2 = (Graphics2D) g;
				Logger.debug("in PaintComponent: g2 = " + g2.toString());
				int yTitle = 0;		// default string heights to 0 (no string to display)
				int ys1String = 0;
				int ys2String = 0;
				int width = titlesPanel.getWidth();		// says width = 0
				
				Logger.debug("tFont = " + tFont + ", tColor = " + tColor + "tString = " + tString);
				Logger.debug("width = " + width);
				
				if(tString != null && !tString.trim().isEmpty())	// draw title if not null or empty
				{
					Logger.debug("tString not empty, proceeding, g = " + g2.toString());
					g2.setFont(tFont);
					g2.setColor(tColor);
					FontMetrics tMetrx = g2.getFontMetrics(tFont);
					int xTitle = width/2 - tMetrx.stringWidth(tString)/2;		// here attempt to center the string
					yTitle = VMIN + tMetrx.getHeight()/2; // tFont.getSize()/2;	// change from code in TilePlot.java
					g2.drawString(tString, xTitle, yTitle);
				}
				if(s1String != null && !s1String.trim().isEmpty())
				{
					Logger.debug("s1String not empty, proceeding");
					g2.setFont(s1Font);
					g2.setColor(s1Color);
					FontMetrics s1Metrx = g2.getFontMetrics(s1Font);
					int xs1String = width/2 - s1Metrx.stringWidth(s1String)/2;	// here attempt to center the string
					ys1String = VMIN + yTitle + s1Metrx.getHeight()/2;
					g2.drawString(s1String, xs1String, ys1String);
				}
				if(s2String != null && !s2String.trim().isEmpty())
				{
					Logger.debug("s2String not empty, proceeding");
					g2.setFont(s2Font);
					g2.setColor(s2Color);
					FontMetrics s2Metrx = g2.getFontMetrics(s2Font);
					int xs2String = width/2 - s2Metrx.stringWidth(s2String)/2;	// here attempt to center the string
					ys2String = VMIN + yTitle + ys1String + s2Metrx.getHeight()/2;
					g2.drawString(s2String, xs2String, ys2String);
				}
				width = titlesPanel.getWidth();
				Logger.debug("width of titlesPanel now = " + width);
				
			}
		};
		// start just below toolBar, 8 cell height, white background
		titlesLayout = new BoxLayout(titlesPanel,BoxLayout.Y_AXIS);		// declare BoxLayout for this JPanel
		titlesPanel.setLayout(titlesLayout);
		titlesPanel.setBackground(Color.WHITE);
		titlesPanel.setPreferredSize(new Dimension(100,100));
		c.fill = GridBagConstraints.BOTH;	// adjust this object both horizontally and vertically
		c.gridx = BORDER;
		c.gridy = BORDER + BAR_HT + TOOLBAR_HT;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridheight = TITLES_PANEL_HT;
		gbl_contentPane.setConstraints(titlesPanel, c);
		add(titlesPanel);

		// next, define components for left side
		rangeAxisLabel = new JPanel() {	// contains range axis title
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D) g;
				Logger.debug("rangeAxisLabel paintComponent");
			}
		};	
		// go vertically from just above top of domain tick labels object to just under titlesPanel
		rangeAxisLabel.setBackground(Color.WHITE);
		c.gridx = BORDER;
		c.gridy = BORDER + BAR_HT + TOOLBAR_HT + TITLES_PANEL_HT;
		c.gridwidth = RANGE_AXIS_LABEL_WIDTH;
		c.gridheight = GridBagConstraints.REMAINDER;
		gbl_contentPane.setConstraints(rangeAxisLabel, c);
		add(rangeAxisLabel);

		rangeTickLabels = new JPanel() {	// contains range axis, ticks, and labels
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D) g;
				Logger.debug("rangeTickLabels paintComponent");
			}
		};	
		// go vertically just to right of rangeAxisLabel
		c.gridx = BORDER + RANGE_AXIS_LABEL_WIDTH;
		c.gridy = BORDER + BAR_HT + TOOLBAR_HT + TITLES_PANEL_HT;
		c.gridwidth = RANGE_TICK_LABELS_WIDTH;
		c.gridheight = GridBagConstraints.REMAINDER;
		gbl_contentPane.setConstraints(rangeTickLabels, c);
		add(rangeTickLabels);

		// next, define components for across bottom
		domainTickLabels = new JPanel() {	// contains domain axis, ticks, and labels
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D) g;
				Logger.debug("domainTickLabels paintComponent");
			}
		};	
		// go horizontally just above domainAxisLabel from right edge of rangeTickLabels
		// to left edge of legendPanel
		c.gridx = BORDER + RANGE_AXIS_LABEL_WIDTH + RANGE_TICK_LABELS_WIDTH;
		c.gridy = BORDER + BAR_HT + TOOLBAR_HT + TITLES_PANEL_HT + TOP_MAP_PANEL_HT;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridheight = DOMAIN_TICK_LABELS_HT;
		gbl_contentPane.setConstraints(domainTickLabels, c);
		add(domainTickLabels);

		domainAxisLabel = new JPanel() {	// contains domain axis title
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D) g;
				Logger.debug("domainAxisLabel paintComponent");
			}
		};	
		// go horizontally just above footersPanel from right edge of rangeTickLabels 
		// to left edge of legendPanel
		c.gridx = BORDER + RANGE_AXIS_LABEL_WIDTH + RANGE_TICK_LABELS_WIDTH;
		c.gridy = BORDER + BAR_HT + TOOLBAR_HT + TITLES_PANEL_HT + TOP_MAP_PANEL_HT + DOMAIN_TICK_LABELS_HT;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridheight = DOMAIN_AXIS_LABEL_HT;
		gbl_contentPane.setConstraints(domainAxisLabel, c);
		add(domainAxisLabel);

		footersPanel = new JPanel() {	// contains up to 3 optional footers
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D) g;
				Logger.debug("footersPanel paintComponent");
				int yf1String = 0;		// default string heights to 0 (no string to display)
				int yf2String = 0;
				int width = footersPanel.getWidth();
				
				Logger.debug("f1String = " + f1String + ", f1Font = " + f1Font + ", f1Color = " + f1Color);
				Logger.debug("width = " + width);
				
				if(f1String != null && !f1String.trim().isEmpty())	// draw footer1 if not null or empty
				{
					Logger.debug("f1String not empty, proceeding, g = " + g2.toString());
					g2.setFont(f1Font);
					g2.setColor(f1Color);
					FontMetrics tMetrx = g2.getFontMetrics(f1Font);
					int xf1String = width/2 - tMetrx.stringWidth(f1String)/2;		// here attempt to center the string
					yf1String = VMIN + tMetrx.getHeight()/2; // tFont.getSize()/2;	// change from code in TilePlot.java
					g2.drawString(f1String, xf1String, yf1String);
				}
				if(f2String != null && !f2String.trim().isEmpty())	// draw footer2 if not null or empty
				{
					Logger.debug("f2String not empty, proceeding");
					g2.setFont(f2Font);
					g2.setColor(f2Color);
					FontMetrics s1Metrx = g2.getFontMetrics(f2Font);
					int xf2String = width/2 - s1Metrx.stringWidth(f2String)/2;	// here attempt to center the string
					yf2String = VMIN + yf1String + s1Metrx.getHeight()/2;
					g2.drawString(f2String, xf2String, yf2String);
				}
				width = footersPanel.getWidth();
				Logger.debug("width of footersPanel now = " + width);
			}
		};	
		// go horizontally across the entire very bottom of contentPane
		c.gridx = BORDER;
		c.gridy = BORDER + BAR_HT + TOOLBAR_HT + TITLES_PANEL_HT + TOP_MAP_PANEL_HT + DOMAIN_TICK_LABELS_HT + DOMAIN_AXIS_LABEL_HT;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridheight = FOOTERS_PANEL_HT;
		gbl_contentPane.setConstraints(footersPanel, c);
		add(footersPanel);

		// then, define legend (right side)
		legendPanel = new JPanel() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D) g;
				Logger.debug("legendPanel paintComponent");
				if(showLegend)
				{
				// parts originally from TilePlot.drawLegend
				final int colors = legendColors.length;
				String unitStrAll = unitStr;
				AttributedString as = null;
				final Color currentColor = g2.getColor();		// get the color from the legendPanel
				final int binWidth = 20;	// of color bar in pixels
				final int ticSize = 3;		// of level tick marks in pixels
				int space = 6;				// space between 2 visual components
				int height = legendPanel.getHeight();
				final int yRange = yMaximum - yMinimum;
				Logger.debug("in paintComponent for legendPanel; yMaximum = " + yMaximum + 
						", yMinimum = " + yMinimum + ", height = " + height);
				int width = legendPanel.getWidth();
				Logger.debug("width = " + width);
				final int binHeight = yRange / colors;
				final int xOffset = binWidth;
				int subStart = 0, subEnd = 0;
				if(log)
				{
					unitStrAll += logStr;
					subStart = unitStrAll.length();
					unitStrAll += baseStr;
					subEnd = unitStrAll.length();
					unitStrAll += " )";
				}
				as = new AttributedString(unitStrAll);
				
				// Estimate the margin between the plot and the legend
				String maxLenLabel = "";
				
				for (int color = 0; color <= colors; ++color) {
					String label = gFormat(legendLevels[color]);
					if (maxLenLabel.length() < label.length()) maxLenLabel = label;
				}
				Font gFont = g2.getFont();	// get the font for the legendPanel
				int maxLabelLen = g2.getFontMetrics(labelFont == null ? gFont : labelFont).stringWidth(maxLenLabel);
				
				final int x = xMaximum + xOffset;
				int legendBoxX = x, legendBoxY = yMinimum - space;

				// Draw unit string:
				if (unitsFont != null) {
					g2.setFont(unitsFont);
					
					if (log) {
						as.addAttribute(TextAttribute.FONT, unitsFont, 0, subStart);
						as.addAttribute(TextAttribute.FONT, unitsFont, subEnd +1, unitStrAll.length());
						as.addAttribute(TextAttribute.SUPERSCRIPT, TextAttribute.SUPERSCRIPT_SUB, subStart, subEnd);
					} else
						as.addAttribute(TextAttribute.FONT, unitsFont);
				}
				
				if (unitsClr != null) {
					g2.setColor(unitsClr);
				}
				int unitHeight = g2.getFontMetrics().getHeight();
				int unitWidth = g2.getFontMetrics().getMaxAdvance();
				int unitStrX = x + unitHeight;
				int unitStrY = yMaximum - yRange/2 + (unitStrAll.length() * unitWidth)/8;
				final double theta = Math.toRadians(90.0);
				g2.translate(unitStrX, unitStrY);
				g2.rotate(-theta);
				AttributedCharacterIterator aci = as.getIterator();
				g2.drawString(aci, 0, 0);
				g2.rotate(theta);
				g2.translate(-unitStrX, -unitStrY);
				g2.setFont(gFont);
				
				// Draw level values and tic marks in the current color:
				final int xTic = unitStrX + unitHeight/2 + space + maxLabelLen + space;
				final boolean[] showLevelValues = getLevelValues(labelCnt == null ? colors : labelCnt, legendLevels.length);
				
				g2.setColor(uTickColor);
				if (labelFont != null) g2.setFont(labelFont);
				FontMetrics lFontMtrx = g2.getFontMetrics(); // Of level value characters in pixels.
				int halfCharHeight = lFontMtrx.getHeight() / 2;
				
				for (int color = 0; color <= colors; ++color) {
					final double value = legendLevels[color];
					final String label = gFormat(value);
					final int labelLen = lFontMtrx.stringWidth(label);
					final int xLabel = xTic - labelLen - space;
					final int yTic = yMaximum - color * binHeight;
					final int yLabel = yTic + halfCharHeight;
					
					if (showLevelValues[color] && uShowTick)
						g2.drawString(label, xLabel, yLabel);

					if (uShowTick) g2.drawLine(xTic, yTic, xTic + ticSize, yTic);
				}
				
				// Draw color bar:
				int colorBarX = (uShowTick) ? xTic + ticSize : unitStrX + unitHeight + space;
				
				for (int color = 0; color < colors; ++color) {
					final int y = yMaximum - (color + 1) * binHeight;
					g2.setColor(legendColors[color]);
					g2.fillRect(colorBarX, y, binWidth, binHeight);
				}

				// Draw box around color bar:
				final int y = yMaximum - colors * binHeight;
				g2.setColor(Color.BLACK);
				g2.drawRect(colorBarX, y, binWidth, colors*binHeight);
				
				int legendBoxWidth = colorBarX - x + binWidth + unitHeight / 2;
				int legendBoxHeight = space + yRange + halfCharHeight * 2 + 2 * space; //add space to top and bottom of the legend
				
				// Draw legend box
				g2.setColor(Color.BLACK);
				g2.drawRect(legendBoxX, legendBoxY - halfCharHeight, legendBoxWidth, legendBoxHeight);
				
				g2.setColor(currentColor); // Restore original color.
				}
			}
		};	
		legendPanel.setBorder(new LineBorder(new Color(0, 0, 0)));		// black BORDER around legend area
		// go vertically just above footersPanel to just below titlesPanel
		c.gridx = BORDER + RANGE_AXIS_LABEL_WIDTH + RANGE_TICK_LABELS_WIDTH + TOP_MAP_PANEL_WIDTH;
		c.gridy = BORDER + BAR_HT + TOOLBAR_HT + TITLES_PANEL_HT;
		c.gridwidth = LEGEND_PANEL_WIDTH;
		c.gridheight = GridBagConstraints.REMAINDER;
		gbl_contentPane.setConstraints(legendPanel, c);
		add(legendPanel);

		// finally, JMapPane as large, central component
		topMapPanel = new JMapPane() {	// large center of overall contentPane
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D) g;
				Logger.debug("topMapPanel paintComponent");
			}
		};	
		// expand both horizontally and vertically
		// holds geographic data for raster layer (tile plot) and vector layer(s) (geographic boundaries)
		c.gridx = BORDER + RANGE_AXIS_LABEL_WIDTH + RANGE_TICK_LABELS_WIDTH;
		c.gridy = BORDER + BAR_HT + TOOLBAR_HT + TITLES_PANEL_HT;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridheight = GridBagConstraints.REMAINDER;
		c.weightx = 1.0;
		c.weighty = 1.0;
		gbl_contentPane.setConstraints(topMapPanel, c);
		add(topMapPanel);
		Logger.debug("all done with constructing GTTilePlotPanel");
		Logger.debug("titlesPanel = " + titlesPanel.toString());
	}

	/**
	 * This member function does not actually return level values (that purpose could be construed from the name)
	 * but returns a vector (1-D array) of boolean values for whether or not to display the associated labels.
	 * This could support showing every 2nd label, every 3rd label, etc. in addition to every label.
	 * @param labelCnt	number of labels to show
	 * @param length	total number of label slots
	 * @return
	 */
	private boolean[] getLevelValues(int labelCnt, int length) {
		boolean[] show = new boolean[length];
		int labels = labelCnt - 1;
		int multiple = Math.round((float)(length - 1) / (float)labels);
		
		for (int i = 0; i < length; i++)
			show[i] = true;
		
		if (multiple == 0) 
			return show;
		
		for (int i = 1; i < length - 1; i++) 
			show[i] = (i % multiple == 0);
		
		return show;
	}
	
	/**
	 * Set a premade JMenuBar object
	 * @param aBar	the JMenuBar object that was already fully populated
	 */
	public void setMenuBar(JMenuBar aBar)
	{
		menuBar = aBar;
	}
	
	/**
	 * Get the JMenuBar object
	 * @return	bar (the JMenuBar at the top of the overall frame)
	 */
	public JMenuBar getMenuBar()
	{
		return menuBar;
	}
	
	/**
	 * Set a premade JToolBar
	 * @param aToolBar	the JToolBar object that was already fully populated
	 */
	public void setToolBar(JToolBar aToolBar)
	{
		toolBar = aToolBar;
	}
	
	/**
	 * Return the JToolBar
	 * @return	the toolBar object
	 */
	public JToolBar getToolBar()
	{
		return toolBar;
	}
	
	/**
	 * Return the JPanel for the titles
	 * @return	the titlesPanel object
	 */
	public JPanel getTitlesPanel()
	{
		return titlesPanel;
	}
	
	/**
	 * Return the JPanel for the legend
	 * @return	the legendPanel object
	 */
	public JPanel getLegendPanel()
	{
		return legendPanel;
	}
	
	/**
	 * Return the JMapPane container for the tile chart and the shapefiles 
	 * @return the topMapPanel object
	 */
	public JMapPane getMap()
	{
		return topMapPanel;
	}
	
	/**
	 * setTitlesPanel member function to Set information for the titlesPanel (title, subtitle1, subtitle2; all optional)
	 * NOTE: t* for title, s1* for subtitle 1, s2* for subtitle 2
	 * @param tFont	Font for title
	 * @param tColor	Color for title
	 * @param tString	String for title
	 * @param s1Font	Font for subtitle #1
	 * @param s1Color	Color for subtitle #1
	 * @param s1String	String for subtitle #1
	 * @param s2Font	Font for subtitle #2
	 * @param s2Color	Color for subtitle #2
	 * @param s2String	String for subtitle #2
	 */
	public void setTitlesPanel(Font tFont, Color tColor, String tString,
			Font s1Font, Color s1Color, String s1String,
			Font s2Font, Color s2Color, String s2String)
	{
	    this.tFont = tFont;
	    this.tColor = tColor;
	    this.tString = tString;
	    
	    this.s1Font = s1Font;
	    this.s1Color = s1Color;
	    this.s1String = s1String;
	    
	    this.s2Font = s2Font;
	    this.s2Color = s2Color;
	    this.s2String = s2String;
	}
	
	/**
	 * setFootersPanel member function to set values required to draw footers when needed
	 * @param f1Font	Font for footer #1
	 * @param f1Color	Color for footer #1
	 * @param f1String	String for footer #1
	 * @param f2Font	Font for footer #2
	 * @param f2Color	Color for footer #2
	 * @param f2String	String for footer #2
	 */
	public void setFootersPanel (Font f1Font, Color f1Color, String f1String,
			Font f2Font, Color f2Color, String f2String)
	{
		this.f1Font = f1Font;
		this.f1Color = f1Color;
		this.f1String = f1String;
		
		this.f2Font = f2Font;
		this.f2Color = f2Color;
		this.f2String = f2String;
	}
	
	/**
	 * setLegendPanel member function to set values required to draw legend
	 * Originally from TilePlot.drawLegend.
	 * @param uShowTick	true/false show tick for units
	 * @param labelCnt	number of tick marks (1/label)
//	 * @param xMaximum
//	 * @param yMinimum
//	 * @param yMaximum
	 * @param uTickColor	color for tick marks
	 * @param unitsClr	Color for units
	 * @param labelFont	Font for tick labels
	 * @param unitsFont	Font for units
	 * @param baseStr	base of log string
	 * @param logStr	" (Log"
	 * @param unitStr	unit of measure
	 * @param legendLevels	value associated with the break point between legend levels
	 * @param legendColors	Color for each range of values in legend and tile plot
	 */
	public void setLegendPanel(Boolean uShowTick, Integer labelCnt, // int xMaximum, int yMinimum, int yMaximum,
			Color uTickColor, Color unitsClr, Font labelFont, Font unitsFont, 
			String baseStr, String logStr, String unitStr, double[] legendLevels, Color[] legendColors)
	{
		// copy argument values into class data members
		this.uShowTick = uShowTick;
		this.labelCnt = labelCnt;
//		this.xMaximum = xMaximum;
//		this.yMinimum = yMinimum;
//		this.yMaximum = yMaximum;
		this.uTickColor = uTickColor;
		this.unitsClr = unitsClr;
		this.labelFont = labelFont;
		this.unitsFont = unitsFont;
		this.baseStr = baseStr;
		this.logStr = logStr;
		this.unitStr = unitStr;
		this.legendLevels = legendLevels;
		this.legendColors = legendColors;
	}
	
	/**
	 * setLog member function; set the boolean value (true = log transform, false = regular values)
	 * @param aLog	true/false value of boolean for setting value of class data member log
	 */
	public void setLog(boolean aLog)
	{
		this.log = aLog;
	}
	
//	@Override
//	public void paintComponent(Graphics g) {
//		super.paintComponent(g); 	// have to start with this
//		System.out.println("in master paintComponent");
//	}
	
	/**
	 * Return the MapContent object for the geospatial panel
	 * @return myMapContent
	 */
	public MapContent getMapContent()
	{
		return myMapContent;
	}
	
	/**
	 * Return the Rendering Executor object for the geospatial panel
	 * @return myRenderingExecutor
	 */
	public RenderingExecutor getRenderingExecutor()
	{
		return myRenderingExecutor;
	}
	
	/**
	 * Return the GTRenderer object for the geospatial panel
	 * @return myGTRenderer
	 */
	public GTRenderer getRenderer()
	{
		return myGTRenderer;
	}
	
	/**
	 * Return the title string to calling program
	 * @return	String the title string
	 */
	public String getTString()
	{
		return tString;
	}
	
	/**
	 * gFormat - %g-like formatted string of value.
	 * 
	 * @post return != null
	 * @post return.length() <= 11
	 */

	private String gFormat(double value) {
		return numberFormat.format(value);
	}
	
	/**
	 * setShowLegend member function to set boolean value to show the legend for the tile plot true/false
	 * @param aShowLegend	the boolean value to show the legend (true) or not (false)
	 */
	public void setShowLegend(boolean aShowLegend)
	{
		showLegend = aShowLegend;
		Logger.debug("showLegend now set to " + showLegend);		// test true
	}
	
	/**
	 * getShowLegend member function to retrieve whether the legend for the tile plot is to be shown
	 * @return	true = show the legend for the tile plot, false = leave the legend blank
	 */
	public boolean getShowLegend()
	{
		return showLegend;
	}

	/**
	 * Launch the simple test.
	 */
	public static void main(String[] args) {
//		EventQueue.invokeLater(new Runnable() {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {	// internally calls EventQueue.invokeLater
			public void run() {	// run() required by Interface Runnable
				JFrame mainFrame = new JFrame("Main window");
				mainFrame.setPreferredSize(new Dimension(500, 500));
				mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);		// JFrame.EXIT_ON_CLOSE);
				GTTilePlotPanel frame = new GTTilePlotPanel();	// called constructor for GTTilePlotPanel
				try {
					System.out.println("GTTilePlotPanel frame = " + frame);
					frame.setTitlesPanel(new Font(Font.DIALOG, Font.BOLD, 15),Color.BLACK,"Here is my TITLE string",
							new Font(Font.SANS_SERIF, Font.ITALIC, 24), Color.BLUE, "Here is a blue subtitle1 string",
							new Font(Font.SERIF,Font.BOLD|Font.ITALIC,33), Color.RED, "Here is a red subtitle2 STRING");
					System.out.println("back from setTitlesPanel");
				} catch (Exception e) {
					System.out.println("here I caught an exception");
					e.printStackTrace();
				}
				mainFrame.add(frame);
				
				mainFrame.pack();
				mainFrame.setVisible(true);
			}
		});
	}
}
