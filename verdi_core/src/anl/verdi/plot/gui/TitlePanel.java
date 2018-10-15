package anl.verdi.plot.gui;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.jgoodies.forms.factories.DefaultComponentFactory;
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
public class TitlePanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 220035230460764927L;
	public TitlePanel() {
		initComponents();
	}

	public void setText(String text) {
		lblText.setText(text);
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner non-commercial license
		DefaultComponentFactory compFactory = DefaultComponentFactory.getInstance();
		lblText = new JLabel();
		separator1 = compFactory.createSeparator("");
		CellConstraints cc = new CellConstraints();

		//======== this ========
		setBackground(Color.white);
		// 2014
		ColumnSpec[] aColumnSpec = ColumnSpec.decodeSpecs("default:grow");
		RowSpec aRowSpec = new RowSpec(RowSpec.CENTER, Sizes.DEFAULT, FormSpec.DEFAULT_GROW);
		setLayout(new FormLayout(
			aColumnSpec,
			new RowSpec[] {
				aRowSpec,
				FormFactory.LINE_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC
			}));
//		setLayout(new FormLayout(
//				ColumnSpec.decodeSpecs("default:grow"),
//				new RowSpec[] {
//					new RowSpec(RowSpec.CENTER, Sizes.DEFAULT, FormSpec.DEFAULT_GROW),
//					FormFactory.LINE_GAP_ROWSPEC,
//					FormFactory.DEFAULT_ROWSPEC
//				}));

		//---- lblText ----
		lblText.setFont(new Font("Tahoma", Font.BOLD, 16));
		lblText.setHorizontalAlignment(SwingConstants.CENTER);
		add(lblText, cc.xy(1, 1));
		add(separator1, cc.xy(1, 3));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner non-commercial license
	private JLabel lblText;
	private JComponent separator1;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
