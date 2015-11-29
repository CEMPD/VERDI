package anl.verdi.plot.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jfree.chart.ChartTheme;
import org.jfree.chart.JFreeChart;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import anl.verdi.plot.config.ThemeConfig;
import anl.verdi.plot.util.PlotProperties;

/**
 * 
 * @author qun
 *
 */
public class ThemeDialog extends JDialog {
	private static final long serialVersionUID = 1117048665491590187L;
	private static final Logger Logger = LogManager.getLogger(ThemeDialog.class.getName());
	private JFreeChart chart;

	public ThemeDialog(Frame owner, JFreeChart chart) {
		super(owner);
		initComponents();
		addListeners();
		this.chart = chart;
	}

	public ThemeDialog(Dialog owner, JFreeChart chart) {
		super(owner);
		initComponents();
		addListeners();
		this.chart = chart;
	}

	private void addListeners() {
		applButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				try {
					commit();
				} catch (Exception e) {
					String msg = e.getMessage();
					JOptionPane.showMessageDialog(ThemeDialog.this, msg, 
							"Configuration Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				try {
					commit();
					exit();
				} catch (Exception e) {
					String msg = e.getMessage();
					JOptionPane.showMessageDialog(ThemeDialog.this, msg, 
							"Configuration Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				exit();
			}
		});
	}

	private void exit() {
		this.dispose();
	}

	// commit the theme configuration
	private void commit() throws Exception {
		ThemeConfig theme = new ThemeConfig();
		themePanel.setThemeProperties(theme);
		ChartTheme chartTheme = theme.getTheme();
		PlotProperties.getInstance().setCurrentTheme(chartTheme);
		chartTheme.apply(chart);
	}

	private void initComponents() {
		dialogPane = new JPanel();
		contentPanel = new JPanel();
		themePanel = new ChartThemePanel();
		buttonBar = new JPanel();
		applButton = new JButton();
		okButton = new JButton();
		cancelButton = new JButton();
		CellConstraints cc = new CellConstraints();

		// ======== this ========
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setModal(true);
		setTitle("Edit Chart Theme");
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		// ======== dialogPane ========
		{
			dialogPane.setBorder(Borders.DIALOG_BORDER);
			dialogPane.setLayout(new BorderLayout());

			// ======== contentPanel ========
			{
				JScrollPane scrollPane = new JScrollPane(themePanel);
				contentPanel.setLayout(new BorderLayout());
				contentPanel.add(scrollPane, BorderLayout.CENTER);
			}
			dialogPane.add(contentPanel, BorderLayout.CENTER);

			// ======== buttonBar ========
			{
				buttonBar.setBorder(Borders.BUTTON_BAR_GAP_BORDER);
				buttonBar.setLayout(new FormLayout(new ColumnSpec[] {
						FormFactory.GLUE_COLSPEC, FormFactory.BUTTON_COLSPEC,
						FormFactory.RELATED_GAP_COLSPEC, FormFactory.BUTTON_COLSPEC,
						FormFactory.RELATED_GAP_COLSPEC,
						FormFactory.BUTTON_COLSPEC }, RowSpec
						.decodeSpecs("pref")));
				
				// ---- applButton ----
				applButton.setText("Apply");
				buttonBar.add(applButton, cc.xy(2, 1));

				// ---- okButton ----
				okButton.setText("OK");
				buttonBar.add(okButton, cc.xy(4, 1));

				// ---- cancelButton ----
				cancelButton.setText("Cancel");
				buttonBar.add(cancelButton, cc.xy(6, 1));
			}
			dialogPane.add(buttonBar, BorderLayout.SOUTH);
		}
		contentPane.add(dialogPane, BorderLayout.CENTER);
		pack();
		setLocationRelativeTo(getOwner());
	}

	private JPanel dialogPane;
	private JPanel contentPanel;
	private ChartThemePanel themePanel;
	private JPanel buttonBar;
	private JButton applButton;
	private JButton okButton;
	private JButton cancelButton;
}
