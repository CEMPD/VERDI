package saf.core.ui;

import org.java.plugin.registry.Extension;
import saf.core.runtime.PluginDefinitionException;

/**
 * Processes Menu spec extension points.
 *
 * @author Nick Collier
 * @version $Revision: 1.1 $ $Date: 2006/01/03 14:40:45 $
 */
public class MenuProcessor extends ExtPointProcessor {

	private MenuTreeDescriptor mtDescriptor;

	public MenuProcessor(MenuTreeDescriptor mtDescriptor) {
		this.mtDescriptor = mtDescriptor;
	}

	protected void process(UIPlugin plugin, Extension.Parameter param) throws PluginDefinitionException {
		plugin.processMenuSpec(param, mtDescriptor);
	}
}
