package saf.core.ui;

import org.java.plugin.registry.Extension;
import saf.core.runtime.PluginDefinitionException;
import saf.core.ui.help.Help;

/**
 * Processes help set extension points.
 *
 * @author Nick Collier
 * @version $Revision: 1.1 $ $Date: 2006/01/03 14:40:45 $
 */
public class HelpSetProcessor extends ExtPointProcessor {
	
	private Help help = null;

	protected void process(UIPlugin plugin, Extension.Parameter param) throws PluginDefinitionException {
		// help can be null at first
		help = plugin.processHelpSet(param, help);
	}

	public Help getHelp() {
		return help;
	}
}
