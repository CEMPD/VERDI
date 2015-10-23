package saf.core.ui;

import org.java.plugin.registry.Extension;
import saf.core.runtime.PluginDefinitionException;

/**
 * Processes StatusField spec extension points.
 *
 * @author Nick Collier
 * @version $Revision: 1.1 $ $Date: 2006/06/01 16:32:24 $
 */
public class StatusFieldProcessor extends ExtPointProcessor {

	private StatusBarDescriptor descriptor = new StatusBarDescriptor();

	protected void process(UIPlugin plugin, Extension.Parameter param) throws PluginDefinitionException {
		plugin.processStatusFieldSpec(param, descriptor);
	}

	public StatusBarDescriptor getDescriptor() {
		return descriptor;
	}
}
