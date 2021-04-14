package anl.verdi.boot;

import java.net.URL;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.java.plugin.PluginManager;
import org.java.plugin.registry.Library;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.standard.StandardPluginClassLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Classloader used by Verdi. This extends jpf's StandardPluginClassLoader
 * but overrides getPermissions to return all permissions. This allows jpf to
 * work with webstart.
 * 
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class VerdiPluginClassLoader extends StandardPluginClassLoader {
	
	static final Logger Logger = LogManager.getLogger(VerdiPluginClassLoader.class.getName());

	public VerdiPluginClassLoader(PluginManager pluginManager, PluginDescriptor pluginDescriptor, ClassLoader classLoader) {
		super(pluginManager, pluginDescriptor, classLoader);
		//collectFilters();
		//Logger.error("VerdiPluginClassLoader " + hashCode() + " c1 " + subFilters);
	}
    
    private Map<String, ResourceFilter> resourceFilters;
    
    protected void collectFilters() {
        if (resourceFilters == null) {
            resourceFilters = new HashMap<String, ResourceFilter>();
        } else {
            resourceFilters.clear();
        }
        for (Library lib : getPluginDescriptor().getLibraries()) {
        	String path = getPluginManager().getPathResolver().resolvePath(lib,
                    lib.getPath()).toExternalForm();
        	ResourceFilter filter = new ResourceFilter(lib);
            resourceFilters.put(
                    path,
                    filter);
            //Fix for JDK11+ bug where class lookups from jars use url with jar: and !/
            //Copied code from StandardPluginClassLoader - too much protected/private to fix in class
            if (path.startsWith("jar:")) {
            	String pathCompat = path.substring("jar:".length(), path.length() - 2);
            	resourceFilters.put(pathCompat,  filter);
            }
        }
    }

    /*HashMap<String, Library> subFilters = null;
     
     	
    private static URL findClassBaseUrl(final Class<?> cls) {
        ProtectionDomain pd = cls.getProtectionDomain();
        if (pd != null) {
            CodeSource cs = pd.getCodeSource();
            if (cs != null) {
                return cs.getLocation();
            }
        }
        return null;
    }
    
    public void collectFilters() {
    	Logger.error("VerdiPluginClassLoader " + hashCode() + " collectFilters in");
    	super.collectFilters();
        if (subFilters == null) {
        	subFilters = new HashMap<String, Library>();
        } else {
        	subFilters.clear();
        }
        for (Library lib : getPluginDescriptor().getLibraries()) {
        	subFilters.put(
                    getPluginManager().getPathResolver().resolvePath(lib,
                            lib.getPath()).toExternalForm(),
                    lib);
        }
    	Logger.error("VerdiPluginClassLoader " + hashCode() + " collectFilters out " + subFilters);
    }
    
    protected void checkClassVisibility(final Class<?> cls,
            final StandardPluginClassLoader requestor)
            throws ClassNotFoundException {
    	if (cls.getName().indexOf("IAppConfigurator") != -1) {
    		URL url = findClassBaseUrl(cls);
    		Logger.error("External " + hashCode() + " URL " + url.getClass() + " ext: " + url.toExternalForm().getClass() + " " + url.toExternalForm() + " sub " + subFilters);
    	
    		Logger.error("Protocol: " + url.getProtocol());
    		
    		if (filterList != null)
    			Logger.error("Lib: " + filterList.get(url.toExternalForm()));
    		else
    			Logger.error("No filter");
    		for (String key : subFilters.keySet()) {
    			Logger.error("Filter key: " + key);
    		}
    		//return;
    	}
    	super.checkClassVisibility(cls,  requestor);
    
    }*/

    private static URL getClassBaseUrl(final Class<?> cls) {
        ProtectionDomain pd = cls.getProtectionDomain();
        if (pd != null) {
            CodeSource cs = pd.getCodeSource();
            if (cs != null) {
                return cs.getLocation();
            }
        }
        return null;
    }

    protected void checkClassVisibility(final Class<?> cls,
            final StandardPluginClassLoader requestor)
            throws ClassNotFoundException {
        if (this == requestor) {
            return;
        }
        URL lib = getClassBaseUrl(cls);
        if (lib == null) {
            return; // cls is a system class
        }
        ClassLoader loader = cls.getClassLoader();
        if (!(loader instanceof VerdiPluginClassLoader)) {
            return;
        }
        if (loader != this) {
            ((VerdiPluginClassLoader) loader).checkClassVisibility(cls,
                    requestor);
        } else {
            ResourceFilter filter = resourceFilters.get(lib.toExternalForm());
            if (filter == null) {
                Logger.warn("class not visible, no class filter found, lib=" + lib //$NON-NLS-1$
                        + ", class=" + cls + ", this=" + this //$NON-NLS-1$ //$NON-NLS-2$
                        + ", requestor=" + requestor); //$NON-NLS-1$
                throw new ClassNotFoundException("class " //$NON-NLS-1$
                        + cls.getName()
                        + " is not visible for plug-in " //$NON-NLS-1$
                        + requestor.getPluginDescriptor().getId()
                        + ", no filter found for library " + lib); //$NON-NLS-1$
            }
            if (!filter.isClassVisible(cls.getName())) {
                Logger.warn("class not visible, lib=" + lib //$NON-NLS-1$
                        + ", class=" + cls + ", this=" + this //$NON-NLS-1$ //$NON-NLS-2$
                        + ", requestor=" + requestor); //$NON-NLS-1$
                throw new ClassNotFoundException("class " //$NON-NLS-1$
                        + cls.getName() + " is not visible for plug-in " //$NON-NLS-1$
                        + requestor.getPluginDescriptor().getId());
            }
        }
    }

    protected static final class ResourceFilter {
        private boolean isPublic;

        private final Set<String> entries;

        protected ResourceFilter(final Library lib) {
            entries = new HashSet<String>();
            for (String exportPrefix : lib.getExports()) {
                if ("*".equals(exportPrefix)) { //$NON-NLS-1$
                    isPublic = true;
                    entries.clear();
                    break;
                }
                if (!lib.isCodeLibrary()) {
                    exportPrefix = exportPrefix.replace('\\', '.').replace('/',
                            '.');
                    if (exportPrefix.startsWith(".")) { //$NON-NLS-1$
                        exportPrefix = exportPrefix.substring(1);
                    }
                }
                entries.add(exportPrefix);
            }
        }

        protected boolean isClassVisible(final String className) {
            if (isPublic) {
                return true;
            }
            if (entries.isEmpty()) {
                return false;
            }
            if (entries.contains(className)) {
                return true;
            }
            int p = className.lastIndexOf('.');
            if (p == -1) {
                return false;
            }
            return entries.contains(className.substring(0, p) + ".*"); //$NON-NLS-1$
        }

        protected boolean isResourceVisible(final String resPath) {
            // quick check
            if (isPublic) {
                return true;
            }
            if (entries.isEmpty()) {
                return false;
            }
            // translate "path spec" -> "full class name"
            String str = resPath.replace('\\', '.').replace('/', '.');
            if (str.startsWith(".")) { //$NON-NLS-1$
                str = str.substring(1);
            }
            if (str.endsWith(".")) { //$NON-NLS-1$
                str = str.substring(0, str.length() - 1);
            }
            return isClassVisible(str);
        }
    }

    
    
    /*static java.util.Map<String, Object> filterList = null;
	
    protected Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
    	if (name.indexOf("IAppConfigurator") != -1) {
    		Logger.error("Loading IAppConfigurator");
    		org.java.plugin.PathResolver pr = getPluginManager().getPathResolver();
    		
    		filterList = new java.util.HashMap<String, Object>();
            for (Library lib : getPluginDescriptor().getLibraries()) {
            	URL url = pr.resolvePath(lib,  lib.getPath());
            	Logger.error("Key " + url.getClass() + " ext " + url.toExternalForm().getClass() + " " +  url.toExternalForm());
            	filterList.put(url.toExternalForm(), lib);
            }
    	}
    	return super.loadClass(name, resolve);
    }*/
	/*
    protected Class<?> loadClass(final String name, final boolean resolve)
            throws ClassNotFoundException {
    	Class<?> c = null;
    		try {
    			c = super.loadClass(name, resolve);
    		} catch (NoClassDefFoundError e) {
    			setProbeParentLoaderLast(true);
    			try {
    				c = super.loadClass(name, resolve);
    			} finally {
    				setProbeParentLoaderLast(false);
    			}
    		}
    	return c;
    }*/


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
