package saf.core.ui.util;

import com.l2fprod.common.swing.JDirectoryChooser;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;

/**
 * Utility methods for gui file and directory selection.
 *
 * @author Nick Collier
 * @version $Revision: 1.2 $ $Date: 2006/02/07 20:54:26 $
 */
public class FileChooserUtilities {

	private static Component parentComponent;

	/**
	 * Initials FileChooserUtilities with default parent component to use
	 * for file and directory dialogs.
	 *
	 * @param frame the frame to use.
	 */
	public static void init(Component frame) {
		parentComponent = frame;
	}

	/**
	 * Gets a file via an Open File dialog.
	 *
	 * @param dir the initial path to display in the dialog
	 * @return the selected file or null if no file was selected.
	 */
	public static File getOpenFile(File dir) {
		return getOpenFile(dir, null);
	}

	public static File[] getOpenFiles(File dir) {
		JFileChooser chooser = new JFileChooser(dir);
		chooser.setMultiSelectionEnabled(true);
		int res = chooser.showOpenDialog(parentComponent);
		if (res == JFileChooser.APPROVE_OPTION) return chooser.getSelectedFiles();
		return new File[0];
  }

	/**
	 * Gets a file via an Open File dialog.
	 *
	 * @param parent the dialog will be centered on the parent frame of this component
	 * @param dir the initial path to display in the dialog
	 * @return the selected file or null if no file was selected.
	 */
	public static File getOpenFile(Component parent, File dir) {
		return getOpenFile(parent, dir, null);
	}

	/**
	 * Gets a file via an Open File dialog.
	 *
	 * @param dir the initial path to display in the dialog
	 * @param filter the filter used to filter the displayed files
	 * @return the selected file or null if no file was selected.
	 */
	public static File getOpenFile(File dir, FileFilter filter) {
		return FileChooserUtilities.getOpenFile(parentComponent, dir, filter);
	}

	/**
	 * Gets a file via an Open File dialog.
	 *
	 * @param parent the dialog will be centered on the parent frame of this component
	 * @param dir the initial path to display in the dialog
	 * @param filter the filter used to filter the displayed files
	 * @return the selected file or null if no file was selected.
	 */
	public static File getOpenFile(Component parent, File dir, FileFilter filter) {
		JFileChooser chooser = new JFileChooser(dir);
		if (filter != null) chooser.setFileFilter(filter);
		int res = chooser.showOpenDialog(parent);
		if (res == JFileChooser.APPROVE_OPTION) return chooser.getSelectedFile();
		return null;
	}


	/**
	 * Gets a file via an Save File dialog.
	 *
	 * @param dir the initial path to display in the dialog
	 * @return the selected file or null if no file was selected.
	 */
	public static File getSaveFile(File dir) {
		return getSaveFile(dir, null);
	}

	/**
	 * Gets a file via an Save File dialog.
	 *
	 * @param parent the dialog will be centered on the parent frame of this component
	 * @param dir the initial path to display in the dialog
	 * @return the selected file or null if no file was selected.
	 */
	public static File getSaveFile(Component parent, File dir) {
		return FileChooserUtilities.getSaveFile(parent, dir, null);
	}

	/**
	 * Gets a file via an Save File dialog.
	 *
	 * @param dir the initial path to display in the dialog
	 * @param filter the filter used to filter the displayed files
	 * @return the selected file or null if no file was selected.
	 */
	public static File getSaveFile(File dir, FileFilter filter) {
		return FileChooserUtilities.getSaveFile(parentComponent, dir, filter);
	}

	/**
	 * Gets a file via an Save File dialog.
	 *
	 * @param parent the dialog will be centered on the parent frame of this component
	 * @param dir the initial path to display in the dialog
	 * @param filter the filter used to filter the displayed files
	 * @return the selected file or null if no file was selected.
	 */
	public static File getSaveFile(Component parent, File dir, FileFilter filter) {
		JFileChooser chooser = new JFileChooser(dir);
		if (filter != null) chooser.setFileFilter(filter);
		int res = chooser.showSaveDialog(parent);
		if (res == JFileChooser.APPROVE_OPTION) return chooser.getSelectedFile();
		return null;
	}

	/**
	 * Gets a File via a File Dialog.
	 *
	 * @param title the title of the dialog
	 * @param approveText the approve button text such as "Open" or "Save"
	 * @param dir the initial path to display in the dialog
	 * @return the selected file or null if no file was selected.
	 */
	public static File getFile(String title, String approveText, File dir) {
		return FileChooserUtilities.getFile(parentComponent, title, approveText, dir, null);
	}

	/**
	 * Gets a File via a File Dialog.
	 *
	 * @param title the title of the dialog
	 * @param approveText the approve button text such as "Open" or "Save"
	 * @param dir the initial path to display in the dialog
	 * @param filter the file filter to filter the displayed files
	 * @return the selected file or null if no file was selected.
	 */
	public static File getFile(String title, String approveText, File dir, FileFilter filter) {
		return FileChooserUtilities.getFile(parentComponent, title, approveText, dir, filter);
	}

	/**
	 * Gets a File via a File Dialog.
	 *
	 * @param parent the dialog will be centered on the parent frame of this component
	 * @param title the title of the dialog
	 * @param approveText the approve button text such as "Open" or "Save"
	 * @param dir the initial path to display in the dialog
	 * @param filter the file filter to filter the displayed files
	 * @return the selected file or null if no file was selected.
	 */
	public static File getFile(Component parent, String title, String approveText, File dir,
	                           FileFilter filter)
	{
		JFileChooser chooser = new JFileChooser(dir);
		if (filter != null) chooser.setFileFilter(filter);
		chooser.setDialogTitle(title);
		int res = chooser.showDialog(parent, approveText);
		if (res == JFileChooser.APPROVE_OPTION) return chooser.getSelectedFile();
		return null;
	}

	/**
	 * Gets a directory via an open directory  dialog.
	 *
	 * @param dir the initial path to display in dialog
	 * @return the choosen directory or null if no directory is chosen.
	 */
	public static File getOpenDirectory(File dir) {
		return FileChooserUtilities.getOpenDirectory(parentComponent, dir);
	}

	/**
	 * Gets a directory via an open directory dialog.
	 *
	 * @param parent the dialog will be centered on the parent frame of this component
	 * @param dir the initial path to display in dialog
	 * @return the choosen directory or null if no directory is chosen.
	 */
	public static File getOpenDirectory(Component parent, File dir) {
		DirectoryChooser chooser = new DirectoryChooser(dir);
		if (chooser.showOpenDialog(parent) != JDirectoryChooser.CANCEL_OPTION) {
			return chooser.getSelectedFile();
		}
		return null;
	}

	/**
	 * Gets a directory via a save directory  dialog.
	 *
	 * @param dir the initial path to display in dialog
	 * @return the choosen directory or null if no directory is chosen.
	 */
	public static File getSaveDirectory(File dir) {
		return FileChooserUtilities.getSaveDirectory(parentComponent, dir);
	}

	/**
	 * Gets a directory via a save directory dialog.
	 *
	 * @param parent the dialog will be centered on the parent frame of this component
	 * @param dir the initial path to display in dialog
	 * @return the choosen directory or null if no directory is chosen.
	 */
	public static File getSaveDirectory(Component parent, File dir) {
		DirectoryChooser chooser = new DirectoryChooser(dir);
		chooser.setApproveButtonText("Save");
		if (chooser.showSaveDialog(parent) != JDirectoryChooser.CANCEL_OPTION) {
			return chooser.getSelectedFile();
		}
		return null;
	}

	/**
	 * Gets a directory via a directory Dialog.
	 *
	 * @param title the title of the dialog
	 * @param approveButtonText the approve button text such as "Open" or "Save"
	 * @param dir the initial path to display in the dialog
	 * @return the selected directory or null if no directory was selected.
	 */
	public static File getDirectory(String title, String approveButtonText, File dir) {
		return FileChooserUtilities.getDirectory(parentComponent, title, approveButtonText, dir);
	}

	/**
	 * Gets a directory via a directory Dialog.
	 *
	 * @param parent the dialog will be centered on the parent frame of this component
	 * @param title the title of the dialog
	 * @param approveButtonText the approve button text such as "Open" or "Save"
	 * @param dir the initial path to display in the dialog
	 * @return the selected directory or null if no directory was selected.
	 */
	public static File getDirectory(Component parent, String title, String approveButtonText, File dir) {
		DirectoryChooser chooser = new DirectoryChooser(dir);
		chooser.setDialogTitle(title);
		if (chooser.showDialog(parent, approveButtonText) != JDirectoryChooser.CANCEL_OPTION) {
			return chooser.getSelectedFile();
		}
		return null;
	}
}
