package org.codehaus.enunciate.contract.json;

/**
 * Represents the JSON "any" type.
 * @author Steven Cummings
 */
public enum JsonAnyTypeDefinition implements JsonType {

  /**
   * Singleton instance.
   */
  INSTANCE;

  /**
   * {@inheritDoc}
   */
  public String getTypeName() {
    return "any";
  }
}
