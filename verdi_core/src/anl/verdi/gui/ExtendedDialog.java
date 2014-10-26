package anl.verdi.gui;
/* ******************************************************************
 * 
 *  COPYRIGHT NOTIFICATION
 * 
 * ***************************************************************** 
 * 
 * COPYRIGHT 2004 UNIVERSITY OF CHICAGO
 * 
 * THIS SOFTWARE DISCLOSES MATERIAL PROTECTABLE UNDER COPYRIGHT LAWS 
 * OF THE UNITED STATES AND FURTHER DISSEMINATION IS PROHIBITED WITHOUT PRIOR WRITTEN CONSENT OF ARGONNE NATIONAL LABORATORY'S PATENT COUNSEL.
 * 
 * *****************************************************************
 * 
 * ARGONNE NATIONAL LABORATORY (ANL), WITH FACILITIES IN THE STATES OF ILLINOIS AND IDAHO, IS OWNED BY THE UNITED STATES GOVERNMENT, AND OPERATED BY THE UNIVERSITY OF CHICAGO UNDER PROVISION OF A CONTRACT WITH THE DEPARTMENT OF ENERGY.
 * 
 * ******************************************************************
 * 
 *  GOVERNMENT LICENSE AND DISCLAIMER
 * 
 * *****************************************************************
 * NOTICE: The Government is granted for itself and others acting on its behalf a paid-up, non-exclusive, irrevocable worldwide license in this data to reproduce, prepare derivative works, and perform publicly and display publicly.  Beginning five (5) years after December 2003 the Government is granted for itself and others acting on its behalf a paid-up, non-exclusive, irrevocable worldwide license in this data to reproduce, prepare derivative works, distribute copies to the public, perform publicly and display publicly, and to permit others to do so.   NEITHER THE UNITED STATES GOVERNMENT NOR ANY AGENCY THEREOF, NOR THE UNIVERSITY OF CHICAGO, NOR ANY OF THEIR EMPLOYEES, MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR ASSUMES ANY LEGAL LIABILITY OR RESPONSIBILITY FOR THE ACCURACY, COMPLETENESS, OR USEFULNESS OF ANY INFORMATION, APPARATUS, PRODUCT, OR PROCESS DISCLOSED, OR REPRESENTS THAT ITS USE WOULD NOT INFRINGE PRIVATELY OWNED RIGHTS.
 *   
 * ******************************************************************
 * LICENSING INQUIRIES MAY BE DIRECTED TO THE OFFICE OF TECHNOLOGY TRANSFER AT ARGONNE NATIONAL LABORATORY.
 * ******************************************************************
 */
import java.awt.Container;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;

import org.apache.logging.log4j.LogManager;	// 2014
import org.apache.logging.log4j.Logger;	// 2014 replacing System.out.println with logger messages
//import java.awt.Dialog.ModalityType;


import javax.swing.JDialog;

/**
 * This class overrides the show method on the dialog to show its parent window
 * @author widing
 *
 */
public class ExtendedDialog extends JDialog {
  /**
	 * 
	 */
	private static final long serialVersionUID = -6658824518812288993L;
	static final Logger Logger = LogManager.getLogger(ExtendedDialog.class.getName());
static Frame defaultParent=null;
  public static void setDefaultParent(Frame win){
    defaultParent=win;
  }
  public static Frame getDefaultParent(){
    return defaultParent;
  }

  public ExtendedDialog(Frame owner) throws HeadlessException {
    super(owner==null ? defaultParent : owner);
  }

  public ExtendedDialog(Frame owner, boolean modal) throws HeadlessException {
    super(owner==null ? defaultParent : owner, modal);
  }

  public ExtendedDialog(Frame owner, String title) throws HeadlessException {
    super(owner==null ? defaultParent : owner, title);
  }

  public ExtendedDialog(Frame owner, String title, boolean modal)
      throws HeadlessException {
    super(owner==null ? defaultParent : owner, title, modal);
  }

  public ExtendedDialog(Frame owner, String title, boolean modal,
      GraphicsConfiguration gc) {
    super(owner==null ? defaultParent : owner, title, modal, gc);
  }

  
  public void show(){
    // code added to pop the parent window to the front
	  if(isModal()){
      try{
        Container container = getParent();
        if(container!=null){
          ExtendedOptionPane.setWindowAlwaysOnTop(container,true);
          super.setVisible(true);			// 2014 deprecated show();
        } 
      }catch(Exception e)
        {
        	Logger.error("caught exception in ExtendedDialog.show " + e.getMessage());
        }finally{
        Container container = getParent();
        if(container!=null){
          ExtendedOptionPane.setWindowAlwaysOnTop(container,false);
        }
      }
    }
    else{
      super.setVisible(true);			// 2014 deprecated show();
    }
  }
}
