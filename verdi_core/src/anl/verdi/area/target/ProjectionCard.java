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
package anl.verdi.area.target;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import anl.gui.panel.layout.SpringUtilities;
import anl.gui.window.dialog.ErrorWindow;
import anl.gui.window.dialog.WizardWindow;
import anl.translators.DBFTranslator;
import anl.translators.TranslatorData;

public class ProjectionCard extends JPanel {
  /**
	 * 
	 */
	private static final long serialVersionUID = 5583749836339606614L;
	static final Logger Logger = LogManager.getLogger(ProjectionCard.class.getName());
ProjectionPanel filePanel;
  WizardWindow window;
  JComboBox nameBox,coordBox;
  String[] coordStrings = {"Geographic (lat/lon)","Projected (x,y)"};
  String[] nameStrings = {"HUC","HUC_8","HUC_11","HUC_14"};
  ProjectionPanel projPanel;
  JLabel coordLabel,description;
  
  public ProjectionCard(WizardWindow win) {
    // add the cards in
    super(new SpringLayout());
    window=win;

    SpringLayout layout=(SpringLayout)getLayout();
   
    description = new JLabel("Please select the name field and the coordinate projection to use.");
    add(description);
    
    JLabel nameLabel = new JLabel("Name Field:");
    add(nameLabel);
    nameBox = new JComboBox(nameStrings);
    nameBox.setSelectedIndex(0);
    add(nameBox);
        
    // create the coord box and its label
    coordLabel = new JLabel("Coordinate System:");
    add(coordLabel);
    coordBox = new JComboBox(coordStrings);
    coordBox.setSelectedIndex(0);

    coordBox.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        JComboBox cb = (JComboBox)e.getSource();
        String coordName = (String)cb.getSelectedItem();
        setProjectionPanelInfo(coordName);
      }
    });
    add(coordBox);
    layout.putConstraint(SpringLayout.WEST,description,5,SpringLayout.WEST,this);
    layout.putConstraint(SpringLayout.NORTH,description,5,SpringLayout.NORTH,this);
    layout.putConstraint(SpringLayout.WEST,nameLabel,5,SpringLayout.WEST,this);
    layout.putConstraint(SpringLayout.NORTH,nameLabel,10,SpringLayout.SOUTH,description);
    layout.putConstraint(SpringLayout.WEST,nameBox,5,SpringLayout.EAST,nameLabel);
    layout.putConstraint(SpringLayout.NORTH,nameBox,10,SpringLayout.SOUTH,description);
    layout.putConstraint(SpringLayout.WEST,coordLabel,5,SpringLayout.WEST,this);
    layout.putConstraint(SpringLayout.NORTH,coordLabel,10,SpringLayout.SOUTH,nameBox);
    layout.putConstraint(SpringLayout.WEST,coordBox,5,SpringLayout.EAST,coordLabel);
    layout.putConstraint(SpringLayout.NORTH,coordBox,10,SpringLayout.SOUTH,nameBox);
    
    
        
    // create the projection panel of info
    projPanel = new ProjectionPanel();
    projPanel.setup();
    add(projPanel);
    layout.putConstraint(SpringLayout.WEST,projPanel,5,SpringLayout.WEST,this);
    layout.putConstraint(SpringLayout.NORTH,projPanel,25,SpringLayout.SOUTH,coordBox);

    SpringUtilities.setSize(layout,this,400,450,500,300,300,400);
//   set the initial visibility
    setProjectionPanelInfo(coordStrings[coordBox.getSelectedIndex()]);
    
    
  }
  CoordinateReferenceSystem fileCoords=null;
  public void initialize(String fileDBF,String fileName){
    // set the names of the fields
    DBFTranslator t = new DBFTranslator(fileDBF,null);
    t.initializeTranslator();
//    int n=1;
    TranslatorData data= t.getData();
    Iterator it = data.getDataKeys();
    
    // change the name combo box
    nameBox.removeAllItems();
    while(it.hasNext()){
      Object obj = it.next();
      Logger.debug(obj);
      nameBox.addItem(obj.toString());
    }
    
    t.closeTranslator();
    
    // find out if there is a projection file
    // TODO mab add projection reading code here
    fileCoords = Target.loadProjectionInfo(fileName);
    showCoordinateInfo(fileCoords==null);
    
   
    // repaint the panel
    invalidate();
    repaint();
  }
  // show coordinate info if there is no projection file provided
  // state is true if there is no file
  public void showCoordinateInfo(boolean state){
	  coordBox.setVisible(state);
	  coordLabel.setVisible(state);
	  if(state)description.setText("Please select the name field and the coordinate projection to use.");
	  else description.setText("Please select the name field.");
  }
  public CoordinateReferenceSystem getProjection(){
	if(fileCoords!=null)return fileCoords;
	return projPanel.getProjection();
  }
  public String getNameString(){
    String name= (String)nameBox.getSelectedItem();
    if(name!=null)name=name.trim();
    return name;
  }
  /**
   * Set the info in the projection panel based on the coordinate string
   * @param coordString
   */
  public void setProjectionPanelInfo(String coordString){
    if(coordString.equals(coordStrings[0])){
      projPanel.setVisible(false);
    }
    else{
      projPanel.setVisible(true);
      projPanel.setProjectionInfo((String)projPanel.projBox.getSelectedItem());
    }
  }
  public boolean doAction(String nameString,String projString){
    
    return true;
  }
  
  
  public boolean backAction(){
   return true; 
  }
  public boolean nextAction(){
    CoordinateReferenceSystem proj=getProjection();
    if(proj==null){
      String[] messages = {projPanel.getErrorString()};
      ErrorWindow win = new ErrorWindow(window,window.getTitle()+": Input Error",messages);
      return false;
    }
    return true;
  }
  
}