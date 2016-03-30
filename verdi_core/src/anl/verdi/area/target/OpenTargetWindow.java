package anl.verdi.area.target;

import java.awt.Frame;
import java.io.File;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;		// 2015
import org.apache.logging.log4j.Logger;			// 2015 replacing System.out.println with logger messages

import anl.verdi.area.AreaFile;
import anl.verdi.area.AreaFilePanel;
import anl.verdi.area.WDTWizardWindow;

/**
 * 
 * File Name:OpenTargetWindow.java Description: The class shows a window for
 * selecting a target file
 * 
 * @version March 2004
 * @author Mary Ann Bitz
 * @author Argonne National Lab
 * 
 */
public class OpenTargetWindow extends WDTWizardWindow {
  /**
	 * 
	 */
	private static final long serialVersionUID = -7336905067654714414L;
	static final Logger Logger = LogManager.getLogger(OpenTargetWindow.class.getName());

	ProjectionCard projectionCard;
	TargetFileCard targetFileCard;
	AreaFilePanel areaPanel;
  /**
   * Constructs a window to select a target file
   * 
   * @param fr
   *          parent window
   * @param title
   *          the title of the window
   * @param state
   *          whether the window should be blocking
   */
  public OpenTargetWindow(Frame fr, AreaFilePanel panel, String title, boolean state) {
    super(fr, title, state);
    
    areaPanel=panel;
    // add the cards in
    targetFileCard = new TargetFileCard(this);
    addCard(targetFileCard, "File");
    Logger.debug("added targetFileCard");
    int[] buttons = { NEXT, CANCEL };
    pushCard("File", buttons);

    projectionCard = new ProjectionCard(this);
    addCard(projectionCard, "Coordinates");
    Logger.debug("added projectionCard");

    // pack and show it
    pack();
    setLocationRelativeTo(fr);
    setVisible(true);
  }

  /**
   * Move to the next panel
   */
  public void nextAction() {
    if (getCurrentCard() == targetFileCard) {
      if(!targetFileCard.nextAction())
    	  return;
      File[] files=targetFileCard.getSelectedFiles();
      if(files!=null&&files.length!=0){
      
        String fileName=TargetFileCard.getDefaultPath();
        int[] buttons = {BACK,FINISH,CANCEL};
        
        // change the shp to dbf
        String fileDBF=null;
        if(fileName.endsWith(".shp")||fileName.endsWith("SHP")){
          int len = fileName.length();
          fileDBF=fileName.substring(0,len-3)+"dbf";
        }else return;
        
        try{
        projectionCard.initialize(fileDBF,fileName);
        }catch(Exception e){
          return;
        }
        pushCard("Coordinates", buttons);
      }
    }
  }

  /**
   * Called when the window is done
   */
  public boolean doAction() {
    if (!projectionCard.nextAction())
      return false;
    // load the files and projections
    targetFileCard.doAction(projectionCard.getNameString(), projectionCard.getProjection());
    Logger.debug("getNameString = " + projectionCard.getName() + ", getProjection = " + projectionCard.getProjection());
    
    // update the areaFilePanel
    // update the list of area files
	ArrayList<AreaFile>files = Target.getSources();
	for(AreaFile areaFile:files){
		areaPanel.loadAreaFile(areaFile);
	}
	areaPanel.addAreas(Target.getTargets());
	areaPanel.updateTilePlots(true);
    return true;
  }
}
