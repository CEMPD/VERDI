package saf.core.ui;

import java.awt.Desktop;
import java.awt.desktop.QuitEvent;
import java.awt.desktop.QuitHandler;
import java.awt.desktop.QuitResponse;
import java.awt.event.ActionEvent;

import saf.core.ui.actions.ActionFactory;

public class NativeAdapter implements QuitHandler {
	
	public void registerAdapter() {
		Desktop d = Desktop.getDesktop();
		d.setQuitHandler(this);
	}

	@Override
	public void handleQuitRequestWith(QuitEvent e, QuitResponse response) {
		ActionFactory.getInstance().getAction(GUIConstants.EXIT_ACTION).
        actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "exit"));

		
	}

}
