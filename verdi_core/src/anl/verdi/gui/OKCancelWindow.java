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
/* File Name: OKCancelWindow.java                     */ 
/* Description: This class is used for windows with   */
/* OK and Cancel buttons.                             */
/*                                                    */
/******************************************************/

import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

import anl.gui.button.ExtendedButton;

/**
 * A dialog window that contains an OK and a Cancel button.
 *
 * @version 1.0  May 1997
 * @author  Mary Ann Bitz, ANL
 */
public class OKCancelWindow extends OKWindow {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7385405936281166803L;
	public OKCancelWindow(Dialog fr,String title,boolean state) {
	super(fr,title,state);
	// add the cancel button
	buttonPanel.add(new ExtendedButton("Cancel",this));
		buttonPanel.setLayout(new FlowLayout());

	}
	/**
	 * Constructs an OKCancelWindow beneath the given window, with the
	 * indicated title and modality
	 * @param fr parent frame of this window
	 * @param title title of this window
	 * @param state whether the window should be modal
	 */
	public OKCancelWindow(Frame fr,String title,boolean state){
	    super(fr,title,state);
	    // add the cancel button
	    buttonPanel.add(new ExtendedButton("Cancel",this));
		buttonPanel.setLayout(new FlowLayout());
	    
	}
	// handle button presses
	/**
	 * Event handler that is called when a button is pressed.
	 */
	public void actionPerformed(ActionEvent event){
		String command=event.getActionCommand();
		if(command=="Cancel"){
			cancelAction();
			return;
		}
		super.actionPerformed(event);
	}
	// default actions
	/**
	 * Method called when the cancel button is pressed indicating 
	 * the action should not be performed and the window should be disposed.
	 */
	public void cancelAction(){
		dispose();
	}
	public void windowClosing(WindowEvent e){
		cancelAction();
	}
}
