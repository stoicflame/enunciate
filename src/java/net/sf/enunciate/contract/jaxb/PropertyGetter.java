package net.sf.enunciate.contract.jaxb;

import net.sf.enunciate.contract.ValidationException;
import net.sf.jelly.apt.decorations.declaration.DecoratedMethodDeclaration;

/**
 * A property getter, most usefully paired with a property setter.
 *
 * @author Ryan Heaton
 */
public class PropertyGetter extends DecoratedMethodDeclaration {

  public PropertyGetter(DecoratedMethodDeclaration delegate) {
    super(delegate);

    if (!delegate.isGetter()) {
      throw new ValidationException("A getter must be used to instantiate a property getter.");
    }
  }

  /**
   * The explicit property name of this getter, i.e. the one specified by an annotation, or null if none.
   *
   * @return The explicit property name of this getter, i.e. the one specified by an annotation, or null if none.
   */
  public String getExplicitPropertyName() {
    fixme
    return null;
  }

  /**
   * The explicit property name, if exists, otherwise the default.
   *
   * @return The explicit property name, if exists, otherwise the default.
   */
  @Override
  public String getPropertyName() {
    String explicitPropertyName = getExplicitPropertyName();
    return explicitPropertyName == null ? super.getPropertyName() : explicitPropertyName;
  }

}
