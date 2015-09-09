package anl.verdi.gui;
/* ******************************************************************
 * 
 * 	COPYRIGHT NOTIFICATION
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
 * 	GOVERNMENT LICENSE AND DISCLAIMER
 * 
 * *****************************************************************
 * NOTICE: The Government is granted for itself and others acting on its behalf a paid-up, non-exclusive, irrevocable worldwide license in this data to reproduce, prepare derivative works, and perform publicly and display publicly.  Beginning five (5) years after December 2003 the Government is granted for itself and others acting on its behalf a paid-up, non-exclusive, irrevocable worldwide license in this data to reproduce, prepare derivative works, distribute copies to the public, perform publicly and display publicly, and to permit others to do so.   NEITHER THE UNITED STATES GOVERNMENT NOR ANY AGENCY THEREOF, NOR THE UNIVERSITY OF CHICAGO, NOR ANY OF THEIR EMPLOYEES, MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR ASSUMES ANY LEGAL LIABILITY OR RESPONSIBILITY FOR THE ACCURACY, COMPLETENESS, OR USEFULNESS OF ANY INFORMATION, APPARATUS, PRODUCT, OR PROCESS DISCLOSED, OR REPRESENTS THAT ITS USE WOULD NOT INFRINGE PRIVATELY OWNED RIGHTS.
 *   
 * ******************************************************************
 * LICENSING INQUIRIES MAY BE DIRECTED TO THE OFFICE OF TECHNOLOGY TRANSFER AT ARGONNE NATIONAL LABORATORY.
 * ******************************************************************
 */

/******************************************************/
/* extawt Package                                     */
/* Developed by Argonne National Laboratory           */ 
/* Date: May 1997                                     */
/*                                                    */
/* File Name: OKWindow.java                           */ 
/* Description: This class is used for windows with   */
/* OK buttons.                                        */
/*                                                    */
/******************************************************/

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JPanel;

import anl.gui.button.ExtendedButton;
import anl.gui.panel.layout.GridBagPanel;

/**
 * A dialog window that contains an OK button.
 *
 * @version 1.0  May 1997
 * @author  Mary Ann Bitz, ANL
 */
public class OKWindow extends ExtendedDialog implements ActionListener,WindowListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3435615189753697845L;
	/**
	 * The panel in it that contains the button.
	 */
	protected JPanel buttonPanel;
	protected Frame parentFrame;
	Cursor oldCursor = null;
	boolean isBusy = false;

	public OKWindow(Dialog fr,String title,boolean state) {
    // change by MAW to always use a frame instead of a dialog for OKWindow
	  super(null,title,state);
		// set layout of window
		getContentPane().setLayout(new BorderLayout());

		// add Buttons at bottom
		buttonPanel = new GridBagPanel(GridBagPanel.HORIZONTAL);
			
		getContentPane().add("South",buttonPanel);
		// create the OK button
		buttonPanel.add(new ExtendedButton("OK",this));

		addWindowListener(this);    
	}
	/**
	 * Constructs an OKWindow beneath the given window, with the 
	 * indicated title and modality
	 * @param fr parent frame of this window
	 * @param title title of this window
	 * @param state whether the window should be modal
	 */
	public OKWindow(Frame fr,String title,boolean state){
		super(fr,title,state);
		//super(title);
		parentFrame = fr;
		// set layout of window
		getContentPane().setLayout(new BorderLayout());

		// add Buttons at bottom
		buttonPanel = new GridBagPanel(GridBagPanel.HORIZONTAL);
			
		getContentPane().add("South",buttonPanel);
		// create the OK button
		buttonPanel.add(new ExtendedButton("OK",this));

		addWindowListener(this);

		//Set OK button as default button for this window - Chuck added this 14Sep04
		Component cList[] = buttonPanel.getComponents();
		for (int i = 0; i < cList.length; ++i) {
			if (cList[i] instanceof ExtendedButton) {
				if (((ExtendedButton)cList[i]).getText().equals("OK")) {
					buttonPanel.getRootPane().setDefaultButton((ExtendedButton)cList[i]);
				}
			}
		}

	}

	
	/**
	 * Constructs an OKWindow beneath the given window, with the 
	 * indicated title and modality
	 * @param fr parent frame of this window
	 * @param title title of this window
	 * @param state whether the window should be modal
	 */
	public OKWindow(Frame fr,String title,boolean state, String[] buttons){
		super(fr,title,state);
		//super(title);
		parentFrame = fr;
		// set layout of window
		getContentPane().setLayout(new BorderLayout());

		// add Buttons at bottom
		buttonPanel = new GridBagPanel(GridBagPanel.HORIZONTAL);
			
		getContentPane().add("South",buttonPanel);
		// create the OK button
		//buttonPanel.add(new ExtendedButton("OK",this));
		for(int i=0;i<buttons.length;i++) {
			buttonPanel.add(new ExtendedButton(buttons[i],this));
		}

		addWindowListener(this);

		//Set OK button as default button for this window - Chuck added this 14Sep04
		Component cList[] = buttonPanel.getComponents();
		for (int i = 0; i < cList.length; ++i) {
			if (cList[i] instanceof ExtendedButton) {
//				if (((ExtendedButton)cList[i]).getText().equals("OK")) {
//					buttonPanel.getRootPane().setDefaultButton((ExtendedButton)cList[i]);
//				}
				if (((ExtendedButton)cList[i]).getText().equals(buttons[0])) {
					buttonPanel.getRootPane().setDefaultButton((ExtendedButton)cList[i]);
				}
			}
		}

	}

	
	// handle button presses
	/**
	 * Event handler that is called when a button is pressed.
	 */
	public void actionPerformed(ActionEvent event){
		String command=event.getActionCommand();
		if(command=="OK"){
			oKAction();
			return;
		}
                if (command=="Print"){
                        doPrint();
                        return;
                }
	}
        public void addPrintButton() {
                ((GridBagPanel)buttonPanel).addAt(new ExtendedButton("Print",this), 1);
        }
	public void busy(){
		if (isBusy) return;
		// Save old cursor if not already done
   	oldCursor = getCursor();
   	setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}
	// redefine this to do the intended action
	/**
	 * Method called when a button is pressed indicating the action should
	 * be performed.
	 * Should be defined for all subclasses.
	 */
	public boolean doAction(){
		return true;
	}
  public void doPrint(){}
	public Frame getParentFrame() {
		return parentFrame;
	}
	public void notBusy(){
	    isBusy = false;
   	    setCursor(oldCursor);
	}
	// default actions
	/**
	 * Method called when the OK button is pressed indicating the action should
	 * be performed, and the window should be disposed.
	 */
	public void oKAction(){
		if(doAction())dispose();
	}
	public static void setChildrenEnabled(Component item,boolean state){
	  item.setEnabled(state);
	  if((item instanceof Container)){
	    Container c = (Container)item;
	    Component[] items = ((Container)c).getComponents();
			if(items !=null){
			  int count = items.length;
	      for(int i=0;i<count;i++){
		  setChildrenEnabled(items[i],state);
	      }
			}
		  }
	}
	public void centerOver(Component comp){
		Point pt=comp.getLocation();
		Dimension mainSize = comp.getSize();
		Dimension prefSize = getSize();
		int ptx=pt.x+(mainSize.width-prefSize.width)/2;
		int pty=pt.y+(mainSize.height-prefSize.height)/2;
		setLocation(ptx,pty);
	}
		/**
		 * Redefined setEnabled to change the contents of the window
		 * Allows the window to still be moved around
		 * @param state whether it is enabled or not
		*/
	public void setEnabled(boolean state){
		  setChildrenEnabled(getContentPane(),state);
	}
  
  
	public void windowActivated(WindowEvent e){}
	public void windowClosed(WindowEvent e){}
	public void windowClosing(WindowEvent e){}
	public void windowDeactivated(WindowEvent e){}
	public void windowDeiconified(WindowEvent e){}
	public void windowIconified(WindowEvent e){}
	public void windowOpened(WindowEvent e){}
}
