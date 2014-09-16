package saf.core.runtime;

/**
 * Encapsulates an id, value pair.
 *
 * @author Nick Collier
 */
public class SimpleAttribute {

  private String id, value;

  /**
   * Creates a SimpleAttribute with the specified id and value.
   *
   * @param id the id of the attribute
   * @param value the value of the attribute
   */
  public SimpleAttribute(String id, String value) {
    this.id = id;
    this.value = value;
  }

  /**
   * Gets the attribute's id.
   *
   * @return the attribute's id.
   */
  public String getId() {
     return id;
   }

  /**
   * Gets the attribute's value.
   *
   * @return the attribute's value.
   */
   public String getValue() {
     return value;
   }

}
