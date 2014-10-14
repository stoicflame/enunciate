package org.codehaus.enunciate.contract.json;

/**
 * Json list type, with a specific element type.
 * @author Steven Cummings
 */
public class JsonListTypeDefinition implements JsonType {
  private final JsonType elementType;

  /**
   * Create a new JsonListTypeDefinition.
   * @param elementType List element type (must not be null).
   */
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
