package saf.core.ui;

import org.java.plugin.registry.Extension;
import saf.core.runtime.PluginDefinitionException;
import saf.core.ui.dock.Perspective;

import java.util.List;

/**
 * Processes perspective spec extension points.
 *
 * @author Nick Collier
 * @version $Revision: 1.1 $ $Date: 2006/01/03 14:40:45 $
 */
public class PerspectiveProcessor extends ExtPointProcessor {
	
	private List<Perspective> perspectives;

	public PerspectiveProcessor(List<Perspective> perspectives) {
		this.perspectives = perspectives;
	}

	protected void process(UIPlugin plugin, Extension.Parameter param) throws PluginDefinitionException {
		plugin.processPerspective(param, perspectives);
	}
}
