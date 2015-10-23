package saf.core.ui;

/**
 * @author Nick Collier
 * @version $Revision: 1.3 $ $Date: 2005/12/09 20:16:16 $
 */
public interface BarItemDescriptor {
	
	void fillBars(GUIBarManager barManager, Workspace workspace);

	String getMenuID();

	String getToolbarGroupID();
}
