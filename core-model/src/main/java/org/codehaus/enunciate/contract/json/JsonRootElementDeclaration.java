package org.codehaus.enunciate.contract.json;

import net.sf.jelly.apt.decorations.declaration.DecoratedClassDeclaration;

/**
 * <p>
 * Denotes a Json type definition that can be used as the top-level type of a json object passed as a representing to or from a web service call (RESTful resource or RPC).
 * </p>
 *
 * @author Steven Cummings
 */
public class JsonRootElementDeclaration extends DecoratedClassDeclaration {
  private final JsonTypeDefinition typeDefinition;

  /**
   * Create a new JsonRootElementDeclaration.
   *
   * @param typeDefinition Type that is declared as a possible JSON "root element".
   */
  public JsonRootElementDeclaration(final JsonTypeDefinition typeDefinition) {
    super(typeDefinition);
    this.typeDefinition = typeDefinition;
  }

  /**
   * @return The typeDefinition.
   */
  public JsonTypeDefinition getTypeDefinition() {
    return typeDefinition;
  }
}
