package saf.core.runtime;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains the attributes and plugin id parsed using the PluginReader.
 * This encapsulates all the attributes contained in a particular plugin
 * file.
 *
 * @author Nick Collier
 */
public class PluginAttributes {

  private String pid;
  private Map<String, SimpleAttribute> attributes = new HashMap<String, SimpleAttribute>();

  /**
   * Creates a PluginAttributes for the specified plugin id.
   *
   * @param pluginId the plugin id
   */
  public PluginAttributes(String pluginId) {
    this.pid = pluginId;
  }

  /**
   * Adds an attribute
   *
   * @param id    the attributes id
   * @param value the attributes value
   */
  public void addAttribute(String id, String value) {
    attributes.put(id, new SimpleAttribute(id, value));
  }

  /**
   * Gets a count of the attributes.
   *
   * @return the attribute count
   */
  public int getAttributeCount() {
    return attributes.size();
  }

  /**
   * Gets the Attribute with the specified id.
   *
   * @param id the id of the attribute to get
   * @return the attribute with the specified id, or null if no such
   *         attribute is found.
   */
  public SimpleAttribute getAttribute(String id) {
    return attributes.get(id);
  }

  /**
   * Gets the plugin id.
   *
   * @return the plugin id.
   */
  public String getPluginId() {
    return pid;
  }

}
