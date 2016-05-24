package anl.verdi.area;

// Java classes
import java.awt.Frame;

import anl.gui.window.dialog.WizardWindow;


/**
 * File Name:OpenModelFileWindow.java
 * Description:
 * 
 * 
 * @version April 2004
 * @author Mary Ann Bitz
 * @author Argonne National Laboratory
 */
public class WDTWizardWindow extends WizardWindow {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3001761876777311057L;
	protected String preTitle;

	/**
	 * Constructor for the window to open model files
	 * @param fr the parent window
	 * @param title the title of the window
	 * @param state whether the window should be blocking
	 */
	public WDTWizardWindow(Frame fr, String title, boolean state) {
		super(fr, title, state);
		preTitle=title;
	}


	protected void showCard(CardChoice card) {
		super.showCard(card);
		setTitle(getTitleFor(card.toString()));
	}
	public String[] getCardNames(){return null;}
	public String[] getCardTitles(){return null;}
	public String getTitleFor(String card){
		String[] cardNames=getCardNames();
		String[] cardTitles=getCardTitles();
		if(cardNames!=null){
			for(int i=0;i<cardNames.length;i++){
				if(cardNames[i]==card){
					if(preTitle!=null)return preTitle+": "+cardTitles[i];
					return cardTitles[i];
				}
			}
		}
		if(preTitle!=null)return preTitle+": "+card;
		return card;
	}


}
