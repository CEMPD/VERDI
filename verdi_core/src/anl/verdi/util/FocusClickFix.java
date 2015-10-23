package anl.verdi.util;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractButton;
import javax.swing.JComponent;

/**
 * Mouse listener that works around problem the view selection problem.
 * If a view is not selected and a user clicks on a text field or
 * a button, then the parent view gets selected but the text field
 * does not get focus, and the button is not clicked.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class FocusClickFix extends MouseAdapter {


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		JComponent source = (JComponent) e.getSource();
		if (!source.hasFocus()) {
			if (source instanceof AbstractButton) {
				((AbstractButton)source).doClick();
			}
			source.requestFocus();
		}
	}
}
