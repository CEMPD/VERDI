package saf.core.runtime;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
//import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
import org.java.plugin.JpfException;
import org.java.plugin.ObjectFactory;
import org.java.plugin.Plugin;
import org.java.plugin.PluginLifecycleException;
import org.java.plugin.PluginManager;
import org.java.plugin.PluginManager.PluginLocation;
import org.java.plugin.boot.DefaultPluginsCollector;
import org.java.plugin.registry.Identity;
import org.java.plugin.registry.IntegrityCheckReport;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.util.ExtendedProperties;
import org.xml.sax.SAXException;

import simphony.util.messages.MessageCenter;

/**
 * Boots the plugin infrastructure.
 * 
 * @author Nick Collier
 * @version $Revision: 1.11 $ $Date: 2006/06/01 16:31:11 $
 */
public class Boot {
	
	static final Logger Logger = LogManager.getLogger(Boot.class.getName());
//	protected static final MessageCenter msgCenter = MessageCenter.getMessageCenter(NetcdfDatasetFactory.class);


  private static final String PLUGIN_FOLDER_PROP = "pluginFolders";
  private static final String PLUGIN_DESCRIPTOR_PROP = "plugin.descriptors";
  private static String RUNTIME_DIR_ROOT = "./";
  private static String PLUGIN_RESTRICT_PREFIX = "plugin.restrict.";

  private static final String CORE_PLUGIN_ID = "saf.core.runtime";
  private MessageCenter center = MessageCenter.getMessageCenter(Boot.class);
  
  public Boot()
  {
	  // 2014 created default constructor
  }

  public PluginManager init(String[] args) {

    // load properties
//    if (args.length > 1)		// commented out a do-nothing block
//    {
//    //  RUNTIME_DIR_ROOT = args[1];
//    }
    System.setProperty("applicationRoot", RUNTIME_DIR_ROOT);
    center = MessageCenter.getMessageCenter(Boot.class);
    try {
      Properties props = new Properties();
      File file = new File(RUNTIME_DIR_ROOT, "boot.properties");
      InputStream strm;
      if (file.exists()) {
    	strm = new FileInputStream(file);
      } else {
        // try as resource
        strm = getClass().getClassLoader().getResourceAsStream("boot.properties");
      }
      try {
        props.load(strm);
       } finally {
        strm.close();
      }

      // Publish current folder as configuration parameter
      // to get it available as ${applicationRoot} variable
      // when extended properties are supported
      // 2014 NOTE: the following 2 lines appear to do the same thing
      props.put("applicationRoot", new File(".").getCanonicalPath());
     // props.put("applicationRoot", RUNTIME_DIR_ROOT);

      return initializePluginManager(findPluginLocations(props), props);	

    } catch (Exception ex) {
      center.error(ex.getMessage(), ex);
    }
    return null;
  }

  private void run(PluginManager pluginManager, String[] args) {
	  Plugin corePlugin;
    try {

//      Plugin corePlugin = pluginManager.getPlugin(CORE_PLUGIN_ID);	// 2014 NOTE: Javadoc for this function states "Note that this method will never return null."
//      if (corePlugin == null) {
//        throw new Exception("Cannot find core plugin");}
    	pluginManager.activatePlugin(CORE_PLUGIN_ID);
    	corePlugin = pluginManager.getPlugin(CORE_PLUGIN_ID);
    }
    catch (PluginLifecycleException pEx)
    {
    	center.error(pEx.getMessage(), pEx);
    	return;
    }
    catch (IllegalArgumentException iaEx)
    {
    	center.error(iaEx.getMessage(), iaEx);
    	return;
    }
    catch (Exception ex)
    {
    	center.error(ex.getMessage(), ex);
    	return;
    }
    try{

//      corePlugin.getClass().getMethod("run", String[].class).invoke(corePlugin, (Object) args);
   	// 2014 breaking up above statement; 1st get name of class at run-time for predetermined instantiated object
      Class<? extends Plugin> pClass = corePlugin.getClass();
//      System.out.println("just did corePlugin.getClass: " + corePlugin.getClass().toString());
//      Method[] someMethods = pClass.getDeclaredMethods();
//      System.out.println("Now try to list the methods");
//      for (Method aMethod : someMethods)
//      {
//    	  String methodName = aMethod.getName();
//    	  System.out.println("method name = " + methodName + " and its parameter types:");
//    	  Type[] someTypes = aMethod.getGenericParameterTypes();
//    	  for (Type aType : someTypes)
//    	  {
//    		  String myTypeString = aType.toString();
//    		  System.out.println("\t" + myTypeString);
//    	  }
//      }
      // 2nd get name of method at run-time that belongs to class
      Method aMethod = pClass.getMethod((String) "run", String[].class);
//      System.out.println("just did Method aMethod for " + String[].class);
      Logger.debug("just did Method aMethod for " + String[].class);
//      System.out.println("aMethod: " + aMethod);
      Logger.debug("aMethod: " + aMethod);
//      System.out.println("aMethod = " + aMethod.toGenericString());
      Logger.debug("aMethod = " + aMethod.toGenericString());
      // 3rd invoke actual method of that class on an object
//      System.out.println("ready to aMethod.invoke, args = " + args);
//      System.out.println("args = " + args.toString());
      Logger.debug("args = " + args.toString());
      aMethod.invoke(corePlugin, new java.lang.Object[] {args});

    } catch (InvocationTargetException itEx)
    {
    	
    	Logger.error("caught an InvocationTargetException in Boot.run; printing .getCause()");
    	Logger.error(itEx.getCause().toString() + ", " + itEx.toString());
    	Logger.error(itEx.getTargetException().getMessage());
//    	center.error(itEx.getCause(), itEx);
    }
    catch (Exception ex) {
    	Logger.error("Caught an Exception in Boot.java");
      center.error(ex.getMessage(), ex);
    }
  }

  private PluginManager initializePluginManager(Collection locations, Properties props)
      throws JpfException, IOException, ParserConfigurationException, SAXException {

//	  PluginManager pluginManager = ObjectFactory.newInstance(new ExtendedProperties(props)).createManager();
//    ObjectFactory fac = ObjectFactory.newInstance(new ExtendedProperties(props));
//   PluginManager pluginManager = fac.createManager();
	  ExtendedProperties extProperties = new ExtendedProperties(props);			// 2014
	  ObjectFactory objFactory = ObjectFactory.newInstance(extProperties); 
	  PluginManager pluginManager = objFactory.createManager();

    List<PluginLocation> validLocations = new ArrayList<PluginLocation>();

    List<Restriction> restrictions = new ArrayList<Restriction>();
    for (Object key : props.keySet()) {
      if (key.toString().startsWith(PLUGIN_RESTRICT_PREFIX)) {
        String strKey = key.toString();
        String restrictId = strKey.substring(strKey.lastIndexOf(".") + 1, strKey.length());
        String restrictVal = props.getProperty(strKey);
        restrictions.add(new Restriction(restrictId, restrictVal));
      }
    }

    if (restrictions.size() > 0) {
      for (Iterator iter = locations.iterator(); iter.hasNext();) 
      {
        PluginLocation location = (PluginLocation) iter.next();
        PluginReader reader = new PluginReader(location.getManifestLocation());
        PluginAttributes pluginAttribs = reader.parse();

        boolean pass = true;
        for (Restriction restriction : restrictions) {
          if (!restriction.pass(pluginAttribs)) {
            pass = false;
             break;
          }
        }

        if (pass) {
          validLocations.add(location);
        }
      }

    } else {
//      validLocations.addAll(locations);	// 2014 CHANGE TO ITERATE locations & ADD ONLY THOSE NOT ALREADY IN validLocations
    	for (Iterator anIterator = locations.iterator(); anIterator.hasNext();)
    	{
    		PluginLocation aLocation = (PluginLocation)anIterator.next();
    		if(!validLocations.contains(aLocation) )
    		{
    			// that location not already in validLocations, so add it
    			validLocations.add(aLocation);
    		}
    	}
    }
PluginLocation[] myLocations = validLocations.toArray(new PluginManager.PluginLocation[validLocations.size()]);
//for(int i=0; i<validLocations.size(); i++)
//{
//	java.net.URL aContextLocation = myLocations[i].getContextLocation();
//	System.out.println(aContextLocation.toString());
//	java.net.URL aManifestLocation = myLocations[i].getManifestLocation();
//	System.out.println(aManifestLocation.toString());
//}
Map<java.lang.String, Identity> map = pluginManager.publishPlugins(myLocations);
//    Map<java.lang.String, Identity> map = pluginManager.publishPlugins((PluginManager.PluginLocation[]) validLocations	//.toArray());
//        .toArray(new PluginManager.PluginLocation[validLocations.size()]));		// 2014
    // Check plug-in's integrity
    IntegrityCheckReport integrityCheckReport = pluginManager.getRegistry().checkIntegrity(
        pluginManager.getPathResolver(), true);
    if (integrityCheckReport.countErrors() > 0) {	// 2014 had been != 0
      // something wrong with the plugin set
      center.fatal(integrityCheckReport2str(integrityCheckReport), new RuntimeException("Invalid plugin configuration"));
      System.exit(1);
    }

    for (Iterator iter = map.values().iterator(); iter.hasNext();) {
      Identity id = (Identity) iter.next();
      PluginDescriptor desc = pluginManager.getRegistry().getPluginDescriptor(id.getId());
      pluginManager.getPluginClassLoader(desc);
    }
    return pluginManager;
  }

  private String integrityCheckReport2str(IntegrityCheckReport report) {
    StringBuffer buf = new StringBuffer();
    
    for (Iterator it = report.getItems().iterator(); it.hasNext();) {
      IntegrityCheckReport.ReportItem item = (IntegrityCheckReport.ReportItem) it.next();
      if (item.getSeverity() != IntegrityCheckReport.Severity.ERROR)	//ReportItem.SEVERITY_ERROR) {
      {
        continue;
      }
      buf.append(item.getMessage());
      buf.append("\n\n\n");
    }
    
    buf.append("full integrity check report:\r\n");
    buf.append("-------------- REPORT BEGIN -----------------\r\n");
    for (Iterator it = report.getItems().iterator(); it.hasNext();) {
      IntegrityCheckReport.ReportItem item = (IntegrityCheckReport.ReportItem) it.next();
      buf.append("\tseverity=").append(item.getSeverity()).append("; code=").append(item.getCode())
          .append("; message=").append(item.getMessage()).append("; source=")
          .append(item.getSource()).append("\r\n");
    }
    buf.append("-------------- REPORT END -----------------");
    return buf.toString();
  }

  private Collection findPluginLocations(Properties props) throws Exception {
    DefaultPluginsCollector collector = new DefaultPluginsCollector();
    ExtendedProperties eprops = new ExtendedProperties(props);

    if (props.containsKey(PLUGIN_DESCRIPTOR_PROP)) {
      // treat is as the jpf xml file that holds plugin locations
      eprops.put("org.java.plugin.boot.pluginsLocationsDescriptors",
          props.getProperty(PLUGIN_DESCRIPTOR_PROP));
    } else {
      String pluginFolder = RUNTIME_DIR_ROOT + props.getProperty(PLUGIN_FOLDER_PROP);
      eprops.put("org.java.plugin.boot.pluginsRepositories", pluginFolder);
    }
    collector.configure(eprops);
    Collection locations = collector.collectPluginLocations();

    String exclude = props.getProperty("pluginFolders.exclude", "");
    StringTokenizer tok = new StringTokenizer(exclude, ",");
    List<URL> urls = new ArrayList<URL>();
    while (tok.hasMoreTokens()) {
      String loc = tok.nextToken().trim();
      try {
        URL url = new File(loc).getCanonicalFile().toURI().toURL();
        urls.add(url);
      } catch (IOException ex) {
        center.warn("Error finding directory to exclude from plugin search", ex);
      }
    }

    if (urls.size() > 0) {
      for (Iterator iter = locations.iterator(); iter.hasNext();) {
        PluginManager.PluginLocation loc = (PluginManager.PluginLocation) iter.next();
        for (URL url : urls) {
          if (loc.getContextLocation().toExternalForm().startsWith(url.toExternalForm()))
            iter.remove();
        }
      }
    }

    return locations;
  }

  public static void main(String[] args) {
    Boot boot = new Boot();
  
    PluginManager manager = boot.init(args);
    if(manager == null)
    {
    	System.err.println(" boot.init returned null. Ending with exit(1)");
    	System.exit(1);
    }
    boot.run(manager, args);
  }

  private static class Restriction {

    private String id;
    private String val;

    private Restriction(String id, String val) {
      this.id = id;
      this.val = val;
    }

    private boolean pass(PluginAttributes pAttribs) {

      SimpleAttribute attrib = pAttribs.getAttribute(id);
      if (attrib == null)
        return false;

      return attrib.getValue().equals(val);
    }
  }
}
