package org.codehaus.enunciate.contract.json;

/**
 * @author Steven Cummings
 */
public enum JsonAnyTypeDefinition implements JsonType {

  INSTANCE;

  /**
   * {@inheritDoc}
   */
  public String getTypeName() {
    return "any";
  }
}
