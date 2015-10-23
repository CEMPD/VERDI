/*CopyrightHere*/
package simphony.settings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * This handles IO operations on {@link simphony.settings.SettingsRegistry}s. It does not handle
 * converting between different implementations of {@link simphony.settings.SettingsRegistry}s, so
 * you cannot serialize one implementation and read it in with a different implementation.
 * 
 * @author Jerry Vos
 */
public class SettingsIO {
	public static final String SETTINGS_FILE_NAME = "SettingsFileName";

	public static boolean USE_DOM_DRIVER = false;
	
	private static XStream getXStream() {
		if (USE_DOM_DRIVER) {
			return new XStream(new DomDriver());
		}
		return new XStream();
	}
	
	/**
	 * This loads from the specified file a {@link SettingsRegistry}. If the file does not exist
	 * this will return a new {@link SettingsRegistry} with the given id, otherwise it will load one
	 * from the specified file and set its id to be the specified id.<p/>
	 * 
	 * If the registry has already been loaded this will return the previously loaded one. As with
	 * the store methods, this will not attempt to reload the next registry in the chain. 
	 * 
	 * @param registryId
	 *            the id for the registry
	 * @param fileName
	 *            the name of the file that the registry may be loaded from
	 * @return a registry with the specified id
	 */
	public static SettingsRegistry loadSettings(String registryId, String fileName) {
		XStream xstream = getXStream();

		SettingsRegistry registry = null;
		File settingsFile = new File(fileName);
		if (!settingsFile.exists()) {
			registry = new SettingsRegistry(registryId);
		} else {
			try {
				registry = (SettingsRegistry) xstream.fromXML(new FileReader(fileName));
			} catch (FileNotFoundException ex) {
				registry = new SettingsRegistry(registryId);
			}
			

			registry.setRegistryId(registryId);
			registry.put(SETTINGS_FILE_NAME, fileName);			
		}

		return registry;
	}

	/**
	 * Stores the specified registry to the specified file.<p/>
	 * 
	 * This will <em>not</em> store the pointer to the next registry, nor will it store the next
	 * registry itself.
	 * 
	 * @param registry
	 *            the registry to store
	 * @param fileName
	 *            the name of the file to store the registry to
	 * @throws IOException 
	 */
	public static void storeSettings(SettingsRegistry registry, String fileName)
			throws IOException {
		XStream xstream = getXStream();

		SettingsRegistry nextRegistry = registry.getNext();

		xstream.toXML(registry, new FileWriter(fileName));

		registry.setNext(nextRegistry);
	}
}
