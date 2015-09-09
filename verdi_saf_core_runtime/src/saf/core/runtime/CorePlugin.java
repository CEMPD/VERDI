package saf.core.runtime;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.java.plugin.Plugin;
import org.java.plugin.PluginLifecycleException;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;

import simphony.util.messages.MessageCenter;

/**
 * @author Nick Collier
 * @version $Revision: 1.6 $ $Date: 2006/02/07 20:39:51 $
 */
public class CorePlugin extends Plugin {

	private static final String APP_RUN_ID = "IApplicationRunnable";
	private IApplicationRunnable appRunner;

	protected void doStart() throws Exception {

	}

	protected void doStop() throws Exception {

	}


	public void run(String[] args) {
		try {
			System.out.println("in saf.core.runtime.CorePlugin.run");
			loadAppPlugin();
			System.out.println("back from loadAppPlugin");
			loadUIPlugin(args);		// added args
			System.out.println("back from loadUIPlug");
			runApplicationRunnable(args);
			System.out.println("back from runApplicationRunnable");
		} catch (Exception ex) {
			MessageCenter.getMessageCenter(getClass()).error("Error instantiating plugins", ex);
		}
	}

	private void loadUIPlugin(String[] args) throws PluginLifecycleException, NoSuchMethodException,
	IllegalAccessException, InvocationTargetException 	// added args
	{
		try{
			Plugin plugin = getManager().getPlugin("saf.core.ui");
			//		plugin.getClass().getMethod("initialize", new Class[]{}).invoke(plugin);	// 2014
			Class<? extends Plugin> aClass = plugin.getClass();
			Method aMethod = aClass.getMethod((String) "initialize", new Class[]{});

			aMethod.invoke(plugin);

		} catch(InvocationTargetException itEx)
		{
			System.err.println("caught an InvocationTargetException in CorePlugin.loadUIPlugin; printing .getCause()");
			System.err.print(itEx.getCause());
		}
		catch(Exception ex) {
			System.err.println("Caught an Exception in CorePlugin.java");
			System.err.print(ex.getCause());
			System.err.println();
		}
	}

	private void loadAppPlugin() throws PluginLifecycleException, ClassNotFoundException,
	IllegalAccessException, InstantiationException, PluginDefinitionException {
		ExtensionPoint extPoint = getManager().getRegistry().getExtensionPoint(getDescriptor().getId(), APP_RUN_ID);
		if (extPoint.getConnectedExtensions().size() != 1) {
			throw new PluginDefinitionException("Plugin must implement one and only one IApplicationRunnable");
		}
		Extension ext = (Extension) extPoint.getConnectedExtensions().iterator().next();
		Plugin plugin = getManager().getPlugin(ext.getDeclaringPluginDescriptor().getId());
		Class pluginCls = plugin.getClass();
		Class appRunnerClass = pluginCls.getClassLoader().loadClass(ext.getParameter("class").valueAsString());
		if (pluginCls.equals(appRunnerClass)) {
			appRunner = (IApplicationRunnable) plugin;
		} else {
			appRunner = (IApplicationRunnable) appRunnerClass.newInstance();
		}
	}

	private void runApplicationRunnable(String[] args) {
		System.out.println("into runApplicationRunnable");
		appRunner.run(args);
		System.out.println("did appRunner.run, returning");
	}
}

