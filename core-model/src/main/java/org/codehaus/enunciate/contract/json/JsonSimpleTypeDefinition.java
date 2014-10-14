package org.codehaus.enunciate.contract.json;

import java.util.Locale;

/**
 * Represents the JSON simple types (string, boolean, and number).
 * @author Steven Cummings
 */
public enum JsonSimpleTypeDefinition implements JsonType {

  /**
   * JSON string type.
   */
  STRING, 

  /**
   * JSON boolean type.
   */
  BOOLEAN,

  /**
   * JSON number type.
   */
  NUMBER;

  private final String typeName = name().toLowerCase(Locale.US);

  /**
   * {@inheritDoc}
   */
  public String getTypeName() {
    return typeName;
  }
}
