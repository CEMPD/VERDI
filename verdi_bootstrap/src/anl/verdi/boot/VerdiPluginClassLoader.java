package anl.verdi.boot;

import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;

import org.java.plugin.PluginManager;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.standard.StandardPluginClassLoader;

/**
 * Classloader used by Verdi. This extends jpf's StandardPluginClassLoader
 * but overrides getPermissions to return all permissions. This allows jpf to
 * work with webstart.
 * 
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class VerdiPluginClassLoader extends StandardPluginClassLoader {

	public VerdiPluginClassLoader(PluginManager pluginManager, PluginDescriptor pluginDescriptor, ClassLoader classLoader) {
		super(pluginManager, pluginDescriptor, classLoader);
	}


	/**
	 * Returns the permissions for the given codesource object.
	 * The implementation of this method first calls super.getPermissions
	 * and then adds permissions based on the URL of the codesource.
	 * <p/>
	 * If the protocol of this URL is "jar", then the permission granted
	 * is based on the permission that is required by the URL of the Jar
	 * file.
	 * <p/>
	 * If the protocol is "file"
	 * and the path specifies a file, then permission to read that
	 * file is granted. If protocol is "file" and the path is
	 * a directory, permission is granted to read all files
	 * and (recursively) all files and subdirectories contained in
	 * that directory.
	 * <p/>
	 * If the protocol is not "file", then
	 * to connect to and accept connections from the URL's host is granted.
	 *
	 * @param codesource the codesource
	 * @return the permissions granted to the codesource
	 */
	@Override
	protected PermissionCollection getPermissions(CodeSource codesource) {
		Permissions permissions = new Permissions();
		permissions.add(new AllPermission());
		return permissions;
	}
}
