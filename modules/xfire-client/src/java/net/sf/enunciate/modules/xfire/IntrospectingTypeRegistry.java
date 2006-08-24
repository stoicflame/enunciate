package net.sf.enunciate.modules.xfire;

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

  @Override
  protected TypeCreator createTypeCreator() {
    return new IntrospectingTypeCreator(super.createTypeCreator());
  }
}
