/*CopyrightHere*/
package simphony.settings;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * This is a simple class for storing settings for an application. This is at heart just a chain of
 * {@link java.util.Map}s with an id associated to them. This stores that id in itself through the
 * key {@link #REGISTRY_ID_KEY}.
 * 
 * @author Jerry Vos
 */
public class SettingsRegistry extends Hashtable<String, Object> {
	/**
	 * The key the registry id is stored under
	 */
	public static final String REGISTRY_ID_KEY = "SettingsRegistryId";

	private static final long serialVersionUID = 1315247041894184896L;
	
	private static final Map<String, SettingsRegistry> registryMap = new HashMap<String, SettingsRegistry>();

	private SettingsRegistry next;

	/**
	 * Constructs this registry with the specified key.
	 * 
	 * @see #putHere(String, Object) used for storing the registry id
	 * 
	 * @param registryId
	 *            the registry's id
	 */
	public SettingsRegistry(String registryId) {
		if (registryId != null) {
			putHere(REGISTRY_ID_KEY, registryId);
		}
	}

	/**
	 * Sets this registry's id. This is the same as <code>put(REGISTRY_ID_KEY, registryId)</code>.
	 * 
	 * @see #putHere(String, Object)
	 * 
	 * @param registryId
	 *            the id for this registry
	 */
	public synchronized void setRegistryId(String registryId) {
		putHere(REGISTRY_ID_KEY, registryId);
	}

	/**
	 * Retrieves this registry's id. This is the same as <code>get(REGISTRY_ID_KEY)</code>.
	 * 
	 * @return the id for this registry
	 */
	public synchronized String getRegistryId() {
		return super.get(REGISTRY_ID_KEY).toString();
	}

	/**
	 * This will try to find a value for the specified key in this registry, and if it cannot it
	 * will attempt to find one in the next registry (which also will attempt to check in itself and
	 * then the next, ...).
	 * 
	 * @param key
	 *            the key to search for a value for
	 * @return the value for the given key in the registry chain starting at this registry and
	 *         onwards
	 */
	@Override
	public synchronized Object get(Object key) {
		Object superResult = super.get(key);
		if (superResult != null) {
			return superResult;
		}

		if (next != null) {
			return next.get(key);
		}
		return null;
	}

	/**
	 * This attempts to find a registry with the given key already stored in it. If it cannot find
	 * one it will add the key and value to itself. If it can find one with the specified key it
	 * will set that value on that registry.
	 * 
	 * @see #putHere(String, Object)
	 * @see #getNext()
	 * 
	 * @param key
	 *            the key to store the value under
	 * @param value
	 *            the value to store
	 * @return the last value stored for the given key
	 */
	@Override
	public synchronized Object put(String key, Object value) {
		// first see if we can find a registry with the specified key
		SettingsRegistry regWithKey = findRegistryWithKey(key);
		if (regWithKey != null) {
			return regWithKey.putHere(key, value);
		} else {
			// couldn't find one so add it to ourselves
			return putHere(key, value);
		}

	}

	/**
	 * Stores the given value into this registry. This is like {@link #put(String, Object)}, except
	 * it will not attempt to find a registry with the specified key.
	 * 
	 * @param key
	 *            the key to store
	 * @param value
	 *            the value to store it under
	 * @return
	 */
	public synchronized Object putHere(String key, Object value) {
		return super.put(key, value);
	}

	protected SettingsRegistry findRegistryWithKey(String key) {
		// if this registry has the key, return itself
		if (super.get(key) != null) {
			return this;
		}

		// now attempt to see if down the chain something has this key
		if (next == null) {
			return null;
		}
		SettingsRegistry regWithKey = next.findRegistryWithKey(key);
		if (regWithKey != null) {
			return regWithKey;
		}

		// nothing had the key
		return null;
	}

	/**
	 * Retrieves the next registry in the chain (the one after this one).
	 * 
	 * @return the next registry
	 */
	public synchronized SettingsRegistry getNext() {
		return next;
	}

	/**
	 * Sets the next registry in the settings chain.
	 * 
	 * @param next
	 *            the next registry
	 */
	public synchronized void setNext(SettingsRegistry next) {
		this.next = next;
	}
	
	/**
	 * Retrives a static registry with the specified key. If another settings registry has been
	 * created with the specified id (using this method) that registry will be returned. If no
	 * registry has been created with the specified id, this will attempt to read one in using the
	 * specified file name. If reading the file name fails then a new SettingsRegistry will be
	 * created and returned.
	 * 
	 * @see SettingsIO#loadSettings(String, String)
	 * 
	 * @param registryId
	 *            the id of the registry
	 * @param fileName
	 *            the name of the settings file+
	 * @return a SettingsRegistry
	 */
	public static SettingsRegistry getRegistry(String registryId, String fileName) {
		SettingsRegistry registry = registryMap.get(registryId);
		if (registry != null) {
			return registry;
		}
		
		try {
			registry = SettingsIO.loadSettings(registryId, fileName);
		} catch (Exception ex) {
			registry = new SettingsRegistry(registryId);
		}
		return register(registry);
	}
	
	/**
	 * Retrives a static registry with the specified key. If no registry has
	 * been created or registered with the specified id, this will return null.
	 * 
	 * @see SettingsRegistry#getRegistry(String)
	 * @see SettingsRegistry#register(SettingsRegistry)
	 * 
	 * @param registryId
	 *            the id of the registry
	 * @return a SettingsRegistry
	 */
	public static SettingsRegistry getRegistry(String registryId) {
		return registryMap.get(registryId);
	}

	/**
	 * Registers a SettingsRegistry using its id from its {@link #getRegistryId()}. This will
	 * be returned from calls to {@link SettingsRegistry#getRegistry(String, String)} with
	 * the registry's id.
	 * 
	 * @param registry the registry to register
	 * 
	 * @return the passed in registry
	 */
	public static SettingsRegistry register(SettingsRegistry registry) {
		registryMap.put(registry.getRegistryId(), registry);
		return registry;
	}
}
