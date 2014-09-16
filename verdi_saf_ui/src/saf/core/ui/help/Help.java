package saf.core.ui.help;

import javax.help.HelpSet;
import javax.help.HelpSetException;
import javax.help.HelpBroker;
import javax.help.CSH;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Handles the incorporation of user specified help sets into java help.
 *
 * @author Nick Collier
 * @version $Revision: 1.2 $ $Date: 2005/12/16 17:04:50 $
 */
public class Help {

	private HelpSet masterHelpSet;
	private HelpBroker broker;

	public Help(ClassLoader parentLoader, URL helpSet) throws IOException {
		URLClassLoader loader = new URLClassLoader(new URL[]{helpSet}, parentLoader);
		URL masterURL = getURL(parentLoader, helpSet);
		
		try {
			masterHelpSet = new HelpSet(loader, masterURL);
			broker = masterHelpSet.createHelpBroker();
		} catch (HelpSetException ep) {
			IOException ex = new IOException("Unable to create master help set");
			ex.initCause(ep);
			throw ex;
		}
	}

	public void addHelpSet(ClassLoader parentLoader, URL helpSet) throws IOException {
		URLClassLoader loader = new URLClassLoader(new URL[]{helpSet}, parentLoader);
		URL url = getURL(parentLoader, helpSet);
		try {
			HelpSet set = new HelpSet(loader, url);
			masterHelpSet.add(set);
		} catch (HelpSetException ep) {
			IOException ex = new IOException("Unable to create master help set");
			ex.initCause(ep);
			throw ex;
		}
	}

	public void displayHelp(Object source) {
		CSH.DisplayHelpFromSource display = new CSH.DisplayHelpFromSource(broker);
		display.actionPerformed(new ActionEvent(source, ActionEvent.ACTION_PERFORMED, ""));
	}

	private URL getURL(ClassLoader loader, URL url) throws IOException {
		if (url.getFile().endsWith(".jar")) {
			return getURLFromJar(loader, url);
		} else {
			return url;
		}
	}

	private URL getURLFromJar(ClassLoader loader, URL url) throws IOException {
		JarInputStream jar = new JarInputStream(url.openStream());
		ZipEntry entry = jar.getNextEntry();
		while (entry != null) {
			String entryName = entry.getName();
			if (entryName.endsWith(".hs")) {
				URL[] urls = new URL[]{url};
				URLClassLoader urlLoader = new URLClassLoader(urls, loader);
				return HelpSet.findHelpSet(urlLoader, entryName);
			}
			entry = (ZipEntry) jar.getNextEntry();
		}
		throw new IOException("Help set jar file '" + url.getFile() + "' is missing help set (*.hs) file");
	}

	public static void main(String[] args) {
		try {
			//Help help = new Help(Help.class.getClassLoader(), new File("C:\\jars\\jh2.0\\demos\\hs\\merge\\Master.hs"));
			Help help = new Help(Help.class.getClassLoader(), new File("C:\\jars\\jh2.0\\demos\\hsjar\\animals.jar").toURL());
			help.displayHelp(new javax.swing.JPanel());
			help.addHelpSet(Help.class.getClassLoader(), new File("C:\\jars\\jh2.0\\demos\\hsjar\\invertebrates.jar").toURL());

		} catch (IOException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}

	}

	/**
	 * Creates an action that will display the java help.
	 *
	 * @return an action to run java help.
	 */
	public Action createAction() {
		return new AbstractAction("Help Topics") {
			
			ActionListener listener = new CSH.DisplayHelpFromSource(broker);

			public void actionPerformed(ActionEvent e) {
				listener.actionPerformed(e);
			}
		};
	}
}
