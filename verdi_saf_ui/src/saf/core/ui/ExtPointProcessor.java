package saf.core.ui;

import org.java.plugin.registry.ExtensionPoint;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.PluginManager;
import org.java.plugin.PluginLifecycleException;

import java.util.Iterator;
import java.util.Collection;

import saf.core.runtime.PluginDefinitionException;

/**
 * Abstract base class for classes that process UI extension points.
 *
 * @author Nick Collier
 * @version $Revision: 1.1 $ $Date: 2006/01/03 14:40:45 $
 */
public abstract class ExtPointProcessor {

	public void iterate(UIPlugin plugin, ExtensionPoint extPoint) throws PluginDefinitionException {
		for (Iterator iter = extPoint.getConnectedExtensions().iterator(); iter.hasNext();) {
			Extension ext = (Extension) iter.next();
			PluginDescriptor declaringPluginDescriptor = ext.getDeclaringPluginDescriptor();
			try {
				plugin.getManager().activatePlugin(declaringPluginDescriptor.getId());
				Collection actions = ext.getParameters();
				for (Iterator actionIter = actions.iterator(); actionIter.hasNext();) {
					Extension.Parameter param = (Extension.Parameter) actionIter.next();
					process(plugin, param);
				}
			} catch (PluginLifecycleException ex) {
				throw new PluginDefinitionException(ex);
			}
		}

	}

	protected abstract void process(UIPlugin plugin, Extension.Parameter param) throws PluginDefinitionException;
}
