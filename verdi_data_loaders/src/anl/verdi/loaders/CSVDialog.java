package anl.verdi.loaders;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;

/**
 * Simple dialog for user mapping of CSV file columns to data axis
 *
 * @author Eric Tatara
 * @version $Revision$ $Date$
 */

public class CSVDialog extends JDialog {
	
	private static final long serialVersionUID = 7124452230090946348L;
	private JPanel mainPane;
	private CSVDataset dataset;
	private String[] fields;
	private Map<String,String> columnNameMap;

	public CSVDialog(CSVDataset dataset, List<String> columnNames, String[] fields){
		this.dataset = dataset;
    this.fields = fields;
		
		columnNameMap = new HashMap<String,String>();

		setTitle("CSV DataSset");
		setSize(250, 450);
    setLocationByPlatform(true);
		
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		mainPane = new JPanel();
		mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.PAGE_AXIS));

		mainPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

		JTextPane pane = new JTextPane();
		pane.setBackground(Color.white);
		pane.setText("Select columns from the CSV file to map to each data axis.");
		pane.setEditable(false);
		pane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

		mainPane.add(pane);

		for (String s : fields){
			mainPane.add(Box.createRigidArea(new Dimension(0, 5)));
			SubPanel panel = new SubPanel(s,columnNames);
			mainPane.add(panel);
		}

		mainPane.add(Box.createRigidArea(new Dimension(0, 5)));
		mainPane.add(new buttonPanel(this));

		mainPane.add(Box.createGlue());

		getContentPane().add(mainPane);
		this.setModal(true);
		this.setVisible(true);
	}

	private class buttonPanel extends JPanel implements ActionListener{
		
		private static final long serialVersionUID = -1614272277064953910L;
		private JButton okButton, cancelButton;
		private JDialog dialog;

		public buttonPanel(JDialog dialog){
			this.dialog = dialog;

			okButton = new JButton("OK");
			okButton.setActionCommand("ok");
			okButton.addActionListener(this);

			cancelButton = new JButton("Cancel");
			cancelButton.setActionCommand("cancel");
			cancelButton.addActionListener(this);

			okButton.setLayout(new BoxLayout(okButton,BoxLayout.PAGE_AXIS));
			add(okButton);

			cancelButton.setLayout(new BoxLayout(cancelButton,BoxLayout.PAGE_AXIS));
			add(cancelButton);

			setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
			okButton.setAlignmentY(TOP_ALIGNMENT);
			cancelButton.setAlignmentY(TOP_ALIGNMENT);
		}

		public void actionPerformed(ActionEvent e) {

			if (e.getActionCommand().equals("cancel") ){ 

				if (dataset != null)
					dataset.setCanceledGuiInput(true);
				dialog.setVisible(false);
			}

			if (e.getActionCommand().equals("ok") ){ 

				if (columnNameMap.keySet().size() == fields.length){
					dataset.setColumnNameMap(columnNameMap);
					dialog.setVisible(false);
				}
				else
					JOptionPane.showMessageDialog(null, "Please enter all column names");
			}
		}
	}

	private class SubPanel extends JPanel implements ActionListener,PropertyChangeListener {
		
		private static final long serialVersionUID = -3539051925644219369L;
		JTextField textField;
		JComboBox chooser;

		String title;

		public SubPanel(String myTitle, List<String> columnNames) {

			setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder(myTitle),
					BorderFactory.createEmptyBorder(5,5,5,5)));

			title = myTitle;

			textField = new JTextField(10);
			textField.addPropertyChangeListener(this);	

			chooser = new JComboBox(columnNames.toArray());
			chooser.setSelectedIndex(-1);
			chooser.addActionListener(this);

			JPanel textPanel = new JPanel(); 			
			textPanel.setLayout(new BoxLayout(textPanel,BoxLayout.PAGE_AXIS));
			textPanel.setBorder(BorderFactory.createEmptyBorder(0,0,0,5));
			textPanel.add(textField);

			JPanel chooserPanel = new JPanel();
			chooserPanel.setLayout(new BoxLayout(chooserPanel,BoxLayout.PAGE_AXIS));
			chooserPanel.add(chooser);
			chooserPanel.add(Box.createHorizontalStrut(100));

			setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
			add(textPanel);
			add(chooserPanel);
			textPanel.setAlignmentY(TOP_ALIGNMENT);
			chooserPanel.setAlignmentY(TOP_ALIGNMENT);
		}

		public void actionPerformed(ActionEvent e) {

			// set the column name via combo box
			String chosenText = (String)chooser.getSelectedItem();

			textField.setText(chosenText);
			columnNameMap.put(title, chosenText);
		}

		public void propertyChange(PropertyChangeEvent e) {

			// optionally sets the column name via type in
//			columnNameMap.put(title, e.getPropertyName());
		}
	}

}
