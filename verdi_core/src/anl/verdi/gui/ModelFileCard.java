/*
 * File Name:ModelFilePanel.java
 * Description:
 * 
 * 
 * @version Feb 25, 2006
 * @author Mary Ann Bitz
 * @author Argonne National Lab
 *
 */
package anl.verdi.gui;

import java.awt.Component;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.filechooser.FileFilter;

import anl.gui.panel.layout.SpringUtilities;
import anl.gui.window.dialog.FilePanel;
import anl.gui.window.dialog.WizardWindow;


public class ModelFileCard extends JPanel {
  /**
	 * 
	 */
	private static final long serialVersionUID = -9141627873950247987L;
ModelFilePanel filePanel;
  WizardWindow window;
  
  File[] selectedFiles;
  boolean single=false;
  /**
   * Default path for the model file
   */
  static String defaultPath;
  
  static {
    defaultPath = System.getProperty("modelData");
    if(defaultPath==null)defaultPath=FilePanel.getCurrentPath();
  }
  
  public ModelFileCard(WizardWindow win) {
    this(win,false);
    
  }
  int dataType;
  public ModelFileCard(WizardWindow win,boolean singlee) {
    // add the cards in
    super(new SpringLayout());
    window=win;
    
    filePanel = new ModelFilePanel(this, "Open Datasets",single);
    add(new JLabel(""));
    
      add(new JLabel("Select one or more Models-3 I/O data files containing model results."));
      add(new JLabel("These files will be located in the directory where you downloaded the files."));
      
    
    add(new JLabel(""));
    add(filePanel);
    SpringUtilities.makeCompactGrid(this, getComponentCount(), 1, 3, 3, 3, 3);
  }
  public boolean doAction(ArrayList vars){
    // get the name of the data files
//    File[] files=filePanel.getSelectedFiles();
    // put the files in the dataset 
    return true;
  }
  
  
  public boolean backAction(){
    
    return true;
  }
  public boolean nextAction(){
  if (filePanel.getSelectedFile() == null)
    return false;

//  // see if it is the right kind of file
//  data = filePanel.getModelData();
//  if (data == null) {
//    String[] messages = { "This file is not a Models-3 I/O file." };
//    ErrorWindow win = new ErrorWindow(window, window.getTitle() + ": Error Reading File", messages);
//    return false;
//  }
//  data.getTimes();
//  filePanel.approveSelection();
  return true;
  }
  class ModelFilePanel extends FilePanel {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = -4963773705195213296L;

	/**
     * Comment for <code>window</code>
     */

    // constructor to make files and show correct filters
    ModelFilePanel(Component win, String title,boolean single) {
      super(win, title, defaultPath);
//      FileFilter filter = getFileFilter();
      selectedFiles = null;
      setControlButtonsAreShown(false);
      if(!single)setMultiSelectionEnabled(true);
      // setAcceptAllFileFilterUsed(true);

      addChoosableFileFilter(new FileFilter() {
        public boolean accept(File file) {
          if (file.isDirectory()) {
            return true;
          }
          if (!file.isFile())
            return false;
//          String extension = getExtension(file);
          return true;
        }

        public String getDescription() {
          return "All Files";
        }
      });
    }

    /**
     * See if the file is valid
     * 
     * @param fileName
     *          the proposed name of the file
     * @return if the name is valid
     */
    public boolean validFile(String fileName) {
      File file = new File(fileName);
      if (file.isDirectory()) {
        return true;
      }
      if (!file.isFile())
        return false;
      String extension = getExtension(file);
      if (extension == null)
        return false;
      if (extension.equals("nc"))
        return true;
      return false;
    }

    /**
     * Get the description of the filter
     */
    public String getFilterDescription() {
      return "Models-3 I/O Files (*.nc)";
    }

    
  }
}