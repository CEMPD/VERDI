package anl.verdi.area.target;

import java.awt.Component;
import java.awt.Frame;
import java.io.File;

import javax.swing.JDialog;
import javax.swing.filechooser.FileFilter;

import anl.gui.window.dialog.FilePanel;


/**
 * 
 * File Name:ExportWindow.java
 * Description:
 * This file is used to pick a file for exporting results.
 * 
 * @version June 8, 2004
 * @author Mary Ann Bitz
 * @author Argonne National Lab
 *
 */
public class ExportWindow extends FilePanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2019555411016758024L;
	/** the default path to the options file */
	protected static String defaultPath = FilePanel.currentPath;

	/**
	 * Constructor for the file window
	 */
	ExportWindow(Component win, String title, String initialFile, String extension) {
		super(win, title, initialFile);
		if(extension.equals(".txt")){
			addChoosableFileFilter(new FileFilter() {
				public boolean accept(File file) {
					if (file.isDirectory()) {
						return true;
					}
					if (!file.isFile())
						return false;
					String extension = getExtension(file);
					if (extension == null)
						return false;
					if (extension.equals("txt"))
						return true;
					return false;
				}
				public String getDescription() {
					return "Text Files (*.txt)";
				}
			});}else{
				addChoosableFileFilter(new FileFilter() {
					public boolean accept(File file) {
						if (file.isDirectory()) {
							return true;
						}
						if (!file.isFile())
							return false;
						String extension = getExtension(file);
						if (extension == null)
							return false;
						if (extension.equals("csv"))
							return true;
						return false;
					}
					public String getDescription() {
						return "CSV Files (*.csv)";
					}
				});
			}
	}
	// string for default files
	public String getFilterDescription() {
		return "All Files";
	}
	// what to do when they complete a selection
	public void approveSelection() {
		super.approveSelection();

		// get the file they picked
		File file = getSelectedFile();
		if (file != null) {
			defaultPath = file.getAbsolutePath();
		}
		// close the window
		closeWindow();
	}

	/**
	 * Called when the user cancels a selection.
	 */
	public void cancelSelection() {
		super.cancelSelection();
		closeWindow();
	}
	/**
	 * Called when the user wants to close the window.
	 *
	 */
	public void closeWindow() {
		((JDialog)parent).setVisible(false);
		((JDialog)parent).dispose();
	}

	/**
	 * Gets the name of the export file selected by the user.
	 * @param frame parent window
	 * @param name title to show in the file window
	 * @return the name of the file selected
	 */
	public static boolean exportFile(Frame frame, String title,String extension) {
		JDialog dialog = new JDialog(frame, title, true);
		ExportWindow expWin = new ExportWindow(dialog,title,defaultPath,extension);
		dialog.getContentPane().add(expWin);
		dialog.pack();
		dialog.setVisible(true);

		File file= expWin.getSelectedFile();
		if(file==null)return false;

		// get the result values
		//ArrayList results=Target.calculateAllDepositions();
		String fileName = file.getAbsolutePath();

		//	add the extension if necessary
		if(!file.exists()){
			String ext=FilePanel.getExtension(file);
			if(ext==null){
				fileName=fileName+extension;
				file = new File(fileName);
			}
		}
		// check if it exists
//		if(file.exists()){
//			// warn them
//			String[] messages = {"The file "+file.getName()+" already exists.","It will be overwritten."};
//			//WarningWindow win = new WarningWindow(WDTMainWindow.mainWindow,"Export Warning",messages);
//			//if(!win.continueFlag)return false;
//		}
		/*
    TargetExportCalculator calculator = new TargetExportCalculator();
    calculator.showIt = false;

    ArrayList files=new ArrayList();  
    files.add(fileName);
    calculator.loadFiles=false;
    calculator.setFilesAndVariables(null,null,null,null,files);
    if(extension.equals(".txt"))Target.useFixedWidth=true;
    else Target.useFixedWidth=false;
    WDTMainWindow.mainWindow.runTask(calculator, "Calculating results for one grid...");
		//	export to this file
		//FileHelper.writeDataLines(fileName,new Vector(results));
		 * */

		return true;
	}
}
