package net.sf.enunciate.modules.xfire_client;

import org.codehaus.xfire.aegis.type.DefaultTypeMappingRegistry;
import org.codehaus.xfire.aegis.type.TypeCreator;

/**
 * Type registry that uses an {@link IntrospectingTypeCreator}.
 *
 * @author Ryan Heaton
 */
public class IntrospectingTypeRegistry extends DefaultTypeMappingRegistry {

  public IntrospectingTypeRegistry() {
    super(true);
  }

  protected TypeCreator createTypeCreator() {
    return new IntrospectingTypeCreator(super.createTypeCreator());
  }
}
