package anl.verdi.plot.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import anl.verdi.plot.config.PlotConfiguration;
import anl.verdi.plot.config.TilePlotConfiguration;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.Sizes;


/**
 * @author User #2
 */
public class GISLinePanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4754283587955348186L;
	public GISLinePanel() {
		initComponents();
		addListeners();
	}

	private void addListeners() {
		colorButton.addActionListener(new ActionListener() {
		  public void actionPerformed(ActionEvent evt) {
		    selectColor();
		  }
		});
	}

	private void selectColor() {
		Color color = JColorChooser.showDialog(this, "Select Color", colorFld.getBackground());
		if (color != null) {
			colorFld.setBackground(color);
		}
	}
	
	public void init(Color color, int width) {
		widthFld.setText(Integer.toString(width));
		colorFld.setBackground(color);
	}

	public boolean isShowGridLines() {
		return false;
	}
	
	public int getLineWidth() {
		return Integer.valueOf(widthFld.getText());
	}

	public Color getSelectedColor() {
		return colorFld.getBackground();
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner non-commercial license
		widthLabel = new JLabel();
		widthFld = new JTextField();
		colorLabel = new JLabel();
		colorFld = new JTextField();
		colorButton = new JButton();
		CellConstraints cc = new CellConstraints();

		//======== this ========
		setBorder(new TitledBorder("GIS Layer Lines"));
		// 2014
		ColumnSpec aColumnSpec = new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW);
		setLayout(new FormLayout(
			new ColumnSpec[] {
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
				aColumnSpec,
				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC
			},
			new RowSpec[] {
				FormFactory.LINE_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.LINE_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC
			}));
//		setLayout(new FormLayout(
//				new ColumnSpec[] {
//					FormFactory.DEFAULT_COLSPEC,
//					FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
//					new ColumnSpec(ColumnSpec.FILL, Sizes.DEFAULT, FormSpec.DEFAULT_GROW),
//					FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
//					FormFactory.DEFAULT_COLSPEC
//				},
//				new RowSpec[] {
//					FormFactory.DEFAULT_ROWSPEC,
//					FormFactory.LINE_GAP_ROWSPEC,
//					FormFactory.DEFAULT_ROWSPEC
//				}));

		//---- showBox ----
		widthLabel.setText("Width:");
		add(widthLabel, cc.xy(1, 2));
		add(widthFld, cc.xy(3,  2));

		//---- colorLabel ----
		colorLabel.setText("Color:");
		add(colorLabel, cc.xy(1, 4));

		//---- colorFld ----
		colorFld.setBackground(Color.black);
		colorFld.setEditable(false);
		add(colorFld, new CellConstraints(3, 4, 1, 1, CellConstraints.DEFAULT, CellConstraints.DEFAULT, new Insets( 4, 0, 4, 0)));

		//---- colorButton ----
		colorButton.setText("Select");
		add(colorButton, cc.xy(5, 4));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner non-commercial license
	private JLabel widthLabel;
	private JTextField widthFld;
	private JLabel colorLabel;
	private JTextField colorFld;
	private JButton colorButton;
	// JFormDesigner - End of variables declaration  //GEN-END:variables

	public PlotConfiguration fillConfiguration(PlotConfiguration config) {
		config.putObject(TilePlotConfiguration.LAYER_LINE_SIZE, widthFld.getText());
		config.putObject(TilePlotConfiguration.LAYER_LINE_COLOR, colorFld.getBackground());
		return config;
	}
}
