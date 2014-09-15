package anl.verdi.boot;

import org.java.plugin.PluginClassLoader;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.standard.StandardPluginLifecycleHandler;

import simphony.util.messages.MessageCenter;

/**
 * LifecycleHandler that creates PavePluginClassLoaders.
 * 
 * @author Nick Collier
 * @version $Revision$ $Date$
 * Jo Ellen Brandmeyer, Ph.D., UNC, CEMPD 3/2014: corrected error that msgCenter.debug required 2 arguments,
 * per Eclipse's suggestion added a 2nd argument as "null"
 */
public class VerdiPluginLifecycleHandler extends StandardPluginLifecycleHandler {

	private static final MessageCenter msgCenter = MessageCenter.getMessageCenter(VerdiPluginLifecycleHandler.class);

	@Override
	protected PluginClassLoader createPluginClassLoader(PluginDescriptor descriptor) {
		msgCenter.debug("Creating class loader", (Object[]) null);
		return new VerdiPluginClassLoader(getPluginManager(), descriptor, getClass().getClassLoader());
	}
}
