package org.codehaus.enunciate.contract.json;

import java.util.Locale;

/**
 * @author Steven Cummings
 */
public enum JsonSimpleTypeDefinition implements JsonType {

  STRING, BOOLEAN, NUMBER;

  private final String typeName = name().toLowerCase(Locale.US);

  public String getTypeName() {
    return typeName;
  }
}
