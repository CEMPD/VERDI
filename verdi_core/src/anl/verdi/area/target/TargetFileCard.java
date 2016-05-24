/*
 * File Name:TargetFileCard.java
 * Description:
 * 
 * 
 * @version Feb 25, 2006
 * @author Mary Ann Bitz
 * @author Argonne National Lab
 *
 */
package anl.verdi.area.target;

import java.awt.Component;
import java.io.File;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.filechooser.FileFilter;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import anl.gui.panel.layout.SpringUtilities;
import anl.gui.window.dialog.FilePanel;
import anl.gui.window.dialog.WizardWindow;


public class TargetFileCard extends JPanel {
	
  /**
	 * 
	 */
	private static final long serialVersionUID = 5773403333078748703L;
TargetPanel filePanel;
  WizardWindow window;
  boolean mustSelectOne=true;
//panel used to select files
  class TargetPanel extends FilePanel {
    /**
	 * 
	 */
	private static final long serialVersionUID = 4138748099347780811L;

	// constructor to make files and show correct filters
	TargetPanel(Component win,String title){
		super(win,title,defaultPath);
		//setCurrentPath(defaultPath);
		setMultiSelectionEnabled(true);
		setControlButtonsAreShown(false);
		addChoosableFileFilter(new FileFilter(){
			public boolean accept(File file) {
				if(file.isDirectory()){
					return true;
				}
				if(!file.isFile())return false;
				String extension = getExtension(file);
				if(extension==null)return false;
				if(extension.equals("shp"))return true;
				return false; 
			}
			public String getDescription() {
				return "Shapefiles (*.shp)";
			}
		});
	}

    // string for default files
    public String getFilterDescription() {
        return "All Files";
      }
    
    // what to do when they complete a selection
    public void approveSelection() {
      super.approveSelection();
    
      // get the file they picked
      File[] files=getSelectedFiles();
      if(files!=null&&(files.length!=0)){
        defaultPath=files[0].getAbsolutePath();
      }
    }
  }
  /**
   * Default path for the target files
   */
  static String defaultPath;
  
  static {
    defaultPath = System.getProperty("verdi.hucData");
    if(defaultPath==null)
    	defaultPath=FilePanel.getCurrentPath();
  }
  
  public TargetFileCard(WizardWindow win) {
    // add the cards in
    super(new SpringLayout());
    window=win;
    filePanel=new TargetPanel(win,"Open "+Target.NAME+"s");
    add(new JLabel(""));
    add(new JLabel("Select one or more shapefiles containing areas."));
    add(new JLabel("(An example file would be a shapefile containing HUC regions.)"));
    add(new JLabel(""));
//    JPanel helpPanel = new JPanel();
//    helpPanel.setLayout(new BoxLayout(helpPanel,BoxLayout.X_AXIS));
//    helpPanel.add(new JLabel("To view a map showing HUC boundaries, press this button."));
//    helpPanel.add(new ExtendedButton("View HUCs")
//    {
//      /**
//		 * 
//		 */
//		private static final long serialVersionUID = -8378408983952721555L;
//
//	public void actionPerformed(java.awt.event.ActionEvent event) {
//        // show the help file
//        //WDTMainWindow.mainWindow.doHelpMenu("About...",window);
//      };
//    });
    add(new JLabel(""));
    //add(helpPanel);
    add(filePanel);
    SpringUtilities.makeCompactGrid(this,6,1,3,3,3,3);
  }
  
  public File[] getSelectedFiles(){
    return filePanel.getSelectedFiles();
  }
  
  public boolean doAction(String nameString,CoordinateReferenceSystem proj){
    File[] files=filePanel.getSelectedFiles();
    if(files!=null){
      try{
       
        for(File file:files){
          Target.loadData(file,nameString,proj,true);  
        }
      }finally{
        // make sure all targets are in the area window
    	 // ArrayList<String> sourceNames=Target.getSourceNames();
    	 // ArrayList targets=Target.getSortedTargets();
      }
  }
    return true;
  }
  
  
  public boolean backAction(){
   return true; 
  }
  
  public boolean nextAction(){
    //  don't move if no file selected
    File[] file = getSelectedFiles();
    if(mustSelectOne&&file.length<=0)return false;
    if(file.length==0)return true;
    filePanel.approveSelection();
    return true;
  }
  
  /**
   * @return Returns the defaultPath.
   */
  public static String getDefaultPath() {
    return defaultPath;
  }
  
  public void setMustSelectOne(boolean mustSelectOne) {
    this.mustSelectOne = mustSelectOne;
  }
}