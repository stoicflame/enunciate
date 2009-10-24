package org.codehaus.enunciate.contract.json;

/**
 * @author Steven Cummings
 */
public class JsonListTypeDefinition implements JsonType {
  private final JsonType elementType;

  public JsonListTypeDefinition(final JsonType elementType) {
    assert elementType != null : "elementType must not be null";

    this.elementType = elementType;    
  }

  /**
   * {@inheritDoc}
   */
  public String getTypeName() {
    return "list of " + elementType.getTypeName();
  }
}
