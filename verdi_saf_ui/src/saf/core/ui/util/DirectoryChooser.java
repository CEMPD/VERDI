package saf.core.ui.util;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.l2fprod.common.swing.JDirectoryChooser;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;

import simphony.util.messages.MessageCenter;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
 * File chooser for directories. This inherits from JDirectory chooser which inherits from
 * FileChooser so all that is availble here.
 *
 * @author Nick Collier
 */
public class DirectoryChooser extends JDirectoryChooser {
	private static final long serialVersionUID = 1L;

	private static MessageCenter LOG = MessageCenter.getMessageCenter(DirectoryChooser.class);
	
	private static Icon HOME;
	private static Icon FOLDER_NEW;

	static {
		try {
			HOME = new ImageIcon(DirectoryChooser.class.getClassLoader().getResource("gohome.png"));
			FOLDER_NEW = new ImageIcon(DirectoryChooser.class.getClassLoader().getResource("folder_new.png"));
		} catch (Exception ex) {
			LOG.warn("Error loading directory chooser icons, they will not be used.", ex);
			HOME = null;
			FOLDER_NEW = null;
		}
	}
	
	/**
	 * Constructs a <code>DirectoryChooser</code> pointing to the user's
	 * default directory. This default depends on the operating system.
	 * It is typically the "My Documents" folder on Windows, and the
	 * user's home directory on Unix.
	 */
	public DirectoryChooser() {
	}

	/**
	 * Constructs a <code>DirectoryChooser</code> using the given <code>File</code>
	 * as the path. Passing in a <code>null</code> file
	 * causes the file chooser to point to the user's default directory.
	 * This default depends on the operating system. It is
	 * typically the "My Documents" folder on Windows, and the user's
	 * home directory on Unix.
	 *
	 * @param currentDirectory a <code>File</code> object specifying
	 *                         the path to a file or directory
	 */
	public DirectoryChooser(File currentDirectory) {
		super(currentDirectory);
	}

	/**
	 * Constructs a <code>DirectoryChooser</code> using the given current directory
	 * and <code>FileSystemView</code>.
	 */
	public DirectoryChooser(File currentDirectory, FileSystemView fsv) {
		super(currentDirectory, fsv);
	}

	/**
	 * Constructs a <code>DirectoryChooser</code> using the given path.
	 * Passing in a <code>null</code>
	 * string causes the file chooser to point to the user's default directory.
	 * This default depends on the operating system. It is
	 * typically the "My Documents" folder on Windows, and the user's
	 * home directory on Unix.
	 *
	 * @param currentDirectoryPath a <code>String</code> giving the path
	 *                             to a file or directory
	 */
	public DirectoryChooser(String currentDirectoryPath) {
		super(currentDirectoryPath);
	}

	/**
	 * Constructs a <code>DirectoryChooser</code> using the given current directory
	 * path and <code>FileSystemView</code>.
	 */
	public DirectoryChooser(String currentDirectoryPath, FileSystemView fsv) {
		super(currentDirectoryPath, fsv);
	}

	/**
	 * Constructs a <code>DirectoryChooser</code> using the given
	 * <code>FileSystemView</code>.
	 */
	public DirectoryChooser(FileSystemView fsv) {
		super(fsv);
	}


	private JPanel init() {
		FormLayout layout = new FormLayout("pref, 4dlu, pref, 4dlu, pref:grow",
						"pref, 3dlu, pref, 3dlu, pref:grow");
		PanelBuilder builder = new PanelBuilder(layout);
		builder.setDefaultDialogBorder();
		CellConstraints cc = new CellConstraints();

		JButton home = new JButton();
		if (HOME != null) {
			home.setIcon(HOME);
		}
		home.setToolTipText("Go to home directory");
		home.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				String home = System.getProperty("user.home");
				DirectoryChooser.this.setSelectedFile(new File(home));
			}
		});

		final JButton newFolder = new JButton();
		if (FOLDER_NEW != null) {
			newFolder.setIcon(FOLDER_NEW);
		}
		newFolder.setToolTipText("Create new directory");
		newFolder.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				String name = JOptionPane.showInputDialog(DirectoryChooser.this, "Enter a new folder name:");
				if (name != null) {
					File f = new File(getSelectedFile(), name);
					if (f.mkdirs()) {
						rescanCurrentDirectory();
						setSelectedFile(f);
						setCurrentDirectory(f);
					}
				}
			}
		});

		final JTextField field = new JTextField();
		field.setFont(field.getFont().deriveFont(Font.BOLD));
		field.setEnabled(true);
		field.setEditable(false);
		field.setBackground(new JLabel().getBackground());

		this.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				String prop = evt.getPropertyName();
				if (JDirectoryChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(prop)) {
					File file = getSelectedFile();
					boolean selected = file != null;
					newFolder.setEnabled(selected);
					if (selected) {
						field.setText(file.getAbsolutePath());
						field.setCaretPosition(field.getText().length() - 1);
					} else {
						field.setText("");
					}
					field.setToolTipText(field.getText());
				}
			}
		});


		builder.add(home, cc.xy(1, 1));
		builder.add(newFolder, cc.xy(3, 1));
		builder.add(field, cc.xyw(1, 3, 5));
		builder.add(this, cc.xyw(1, 5, 5));
		this.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		return builder.getPanel();
	}

	@Override
	protected JDialog createDialog(Component component) throws HeadlessException {
		JDialog dialog = super.createDialog(component);
		Container contentPane = dialog.getContentPane();
		contentPane.remove(0);
		contentPane.add(init(), BorderLayout.CENTER);
		dialog.pack();
		return dialog;
	}

	public JPanel getPanel() {
		return init();
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
		}


		DirectoryChooser chooser = new DirectoryChooser(new File("c:/src/repast.simphony"));
		chooser.showOpenDialog(null);
		/*
		JFrame f = new JFrame();
		f.add(chooser.mainPanel);
		f.pack();
		f.setVisible(true);
		*/
	}
}

