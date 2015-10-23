package saf.core.ui;

import org.java.plugin.registry.Extension;
import saf.core.runtime.PluginDefinitionException;

/**
 * Processes ActionSpec extension points.
 *
 * @author Nick Collier
 * @version $Revision: 1.1 $ $Date: 2006/01/03 14:40:45 $
 */
public class ActionProcessor extends ExtPointProcessor {

	protected void process(UIPlugin plugin, Extension.Parameter param) throws PluginDefinitionException {
		plugin.processActionSpec(param);
	}
}
