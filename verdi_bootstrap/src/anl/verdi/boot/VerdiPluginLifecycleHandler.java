package anl.verdi.boot;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
import org.java.plugin.PluginClassLoader;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.standard.StandardPluginLifecycleHandler;

/**
 * LifecycleHandler that creates PavePluginClassLoaders.
 * 
 * @author Nick Collier
 * @version $Revision$ $Date$
 * Jo Ellen Brandmeyer, Ph.D., UNC, CEMPD 3/2014: corrected error that msgCenter.debug required 2 arguments,
 * per Eclipse's suggestion added a 2nd argument as "null"
 */
public class VerdiPluginLifecycleHandler extends StandardPluginLifecycleHandler {
	static final Logger Logger = LogManager.getLogger(VerdiPluginLifecycleHandler.class.getName());

	@Override
	protected PluginClassLoader createPluginClassLoader(PluginDescriptor descriptor) {
		Logger.debug("Creating PluginClassloader in VerdiPluginLifecycleHandler");
		return new VerdiPluginClassLoader(getPluginManager(), descriptor, getClass().getClassLoader());
	}
}
