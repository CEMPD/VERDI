package anl.verdi.plot.gui;

import java.awt.ComponentOrientation;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;
import javax.swing.JMenuBar;

import java.awt.Color;

import javax.swing.SwingConstants;

import org.geotools.swing.JMapPane;
import javax.swing.border.LineBorder;

// class forms the basis for the VERDI panel that displays the entire FastTilePlot
// title, subtitle1, subtitle2, axes, axis ticks and labels, footers, legend, JMapPane for geographic content
public class FastTilePlotPanel extends JPanel {

	private JPanel contentPane;	// overall pane
	// pull declaration of all other components from constructor to here as class-level data members
	GridBagLayout gbl_contentPane;
	GridBagConstraints c;
	JMenuBar bar;
	JToolBar toolBar;
	JPanel titlesPanel;
	JPanel rangeAxisLabel;
	JPanel rangeTickLabels;
	JPanel domainTickLabels;
	JPanel domainAxisLabel;
	JPanel footersPanel;
	JPanel legendPanel;
	JMapPane topMapPanel;
	
	int border = 1;				// 1 blank row/column between outer line of outermost widget to first widget 
	int bar_ht = 1;
	int toolBar_ht = 3;
	int titlesPanel_ht = 8;
	int rangeAxisLabel_width = 4;
	int rangeTickLabels_width = 4;
	int topMapPanel_width = 150;
	int topMapPanel_ht = 100;
	int domainTickLabels_ht = 4;
	int domainAxisLabel_ht = 4;
	int footersPanel_ht = 4;
	int legendPanel_width = 20;
	
	final static boolean RIGHT_TO_LEFT = false;

	/**
	 * Create the frame.
	 */
	public FastTilePlotPanel() {

		contentPane = new JPanel();		// outer container is a JPanel
		contentPane.setBorder(new EmptyBorder(1, 1, 1, 1));
		
		gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{0};
		gbl_contentPane.rowHeights = new int[]{0};
		gbl_contentPane.columnWeights = new double[]{Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{Double.MIN_VALUE};
		if (RIGHT_TO_LEFT) {
			contentPane.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		}
		contentPane.setLayout(gbl_contentPane);
		c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;	//resize horizontally and vertically
		gbl_contentPane.setConstraints(contentPane, c);

		// first define components that go across the top of the frame

		bar = new JMenuBar();	// bar menu strip at top of contentPane
		// start upper-left corner of window, 1-cell height, lt. gray background
		bar.setBackground(Color.LIGHT_GRAY ); 
		c.gridx = border;
		c.gridy = border;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridheight = bar_ht;
		gbl_contentPane.setConstraints(bar, c);
		contentPane.add(bar);

		toolBar = new JToolBar();	// contains time step, layer, stats, animation widgets
		// start just below bar, 3 cell height, lt. gray background
		toolBar.setFloatable(false);
		toolBar.setBackground(Color.LIGHT_GRAY);
		c.gridx = border;
		c.gridy = border + bar_ht;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridheight = toolBar_ht;
		gbl_contentPane.setConstraints(toolBar, c);
		contentPane.add(toolBar);

		titlesPanel = new JPanel();	// contains title, subtitle1, subtitle2
		// start just below toolBar, 8 cell height, white background
		titlesPanel.setBackground(Color.WHITE);
		c.fill = GridBagConstraints.BOTH;	// adjust this object both horizontally and vertically
		c.gridx = border;
		c.gridy = border + bar_ht + toolBar_ht;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridheight = titlesPanel_ht;
		gbl_contentPane.setConstraints(titlesPanel, c);
		contentPane.add(titlesPanel);

		// next, define components for left side
		rangeAxisLabel = new JPanel();	// contains range axis title
		// go vertically from just above top of domain tick labels object to just under titlesPanel
		rangeAxisLabel.setBackground(Color.WHITE);
		c.gridx = border;
		c.gridy = border + bar_ht + toolBar_ht + titlesPanel_ht;
		c.gridwidth = rangeAxisLabel_width;
		c.gridheight = GridBagConstraints.REMAINDER;
		gbl_contentPane.setConstraints(rangeAxisLabel, c);
		contentPane.add(rangeAxisLabel);

		rangeTickLabels = new JPanel();	// contains range axis, ticks, and labels
		// go vertically just to right of rangeAxisLabel
		c.gridx = border + rangeAxisLabel_width;
		c.gridy = border + bar_ht + toolBar_ht + titlesPanel_ht;
		c.gridwidth = rangeTickLabels_width;
		c.gridheight = GridBagConstraints.REMAINDER;
		gbl_contentPane.setConstraints(rangeTickLabels, c);
		contentPane.add(rangeTickLabels);

		// next, define components for across bottom
		domainTickLabels = new JPanel();	// contains domain axis, ticks, and labels
		// go horizontally just above domainAxisLabel from right edge of rangeTickLabels
		// to left edge of legendPanel
		c.gridx = border + rangeAxisLabel_width + rangeTickLabels_width;
		c.gridy = border + bar_ht + toolBar_ht + titlesPanel_ht + topMapPanel_ht;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridheight = domainTickLabels_ht;
		gbl_contentPane.setConstraints(domainTickLabels, c);
		contentPane.add(domainTickLabels);

		domainAxisLabel = new JPanel();	// contains domain axis title
		// go horizontally just above footersPanel from right edge of rangeTickLabels 
		// to left edge of legendPanel
		c.gridx = border + rangeAxisLabel_width + rangeTickLabels_width;
		c.gridy = border + bar_ht + toolBar_ht + titlesPanel_ht + topMapPanel_ht + domainTickLabels_ht;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridheight = domainAxisLabel_ht;
		gbl_contentPane.setConstraints(domainAxisLabel, c);
		contentPane.add(domainAxisLabel);

		footersPanel = new JPanel();	// contains up to 3 optional footers
		// go horizontally across the entire very bottom of contentPane
		c.gridx = border;
		c.gridy = border + bar_ht + toolBar_ht + titlesPanel_ht + topMapPanel_ht + domainTickLabels_ht + domainAxisLabel_ht;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridheight = footersPanel_ht;
		gbl_contentPane.setConstraints(footersPanel, c);
		contentPane.add(footersPanel);

		// then, define legend (right side)
		legendPanel = new JPanel();
		legendPanel.setBorder(new LineBorder(new Color(0, 0, 0)));		// black border around legend area
		// go vertically just above footersPanel to just below titlesPanel
		c.gridx = border + rangeAxisLabel_width + rangeTickLabels_width + topMapPanel_width;
		c.gridy = border + bar_ht + toolBar_ht + titlesPanel_ht;
		c.gridwidth = legendPanel_width;
		c.gridheight = GridBagConstraints.REMAINDER;
		gbl_contentPane.setConstraints(legendPanel, c);
		contentPane.add(legendPanel);

		// finally, JMapPane as large, central component
		topMapPanel = new JMapPane();	// large center of overall contentPane
		// expand both horizontally and vertically
		// holds geographic data for raster layer (tile plot) and vector layer(s) (geographic boundaries)
		c.gridx = border + rangeAxisLabel_width + rangeTickLabels_width;
		c.gridy = border + bar_ht + toolBar_ht + titlesPanel_ht;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridheight = GridBagConstraints.REMAINDER;
		c.weightx = 1.0;
		c.weighty = 1.0;
		gbl_contentPane.setConstraints(topMapPanel, c);
		contentPane.add(topMapPanel);
	}
	
	/**
	 * Set a premade JMenuBar object
	 * @param aBar	the JMenuBar object that was already fully populated
	 */
	public void setBar(JMenuBar aBar)
	{
		bar = aBar;
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
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					FastTilePlotPanel frame = new FastTilePlotPanel();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
