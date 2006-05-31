package net.sf.enunciate.contract.jaxb;

import net.sf.enunciate.contract.ValidationException;
import net.sf.jelly.apt.decorations.declaration.DecoratedMethodDeclaration;

import javax.xml.bind.annotation.XmlElement;

/**
 * A property setter, most usefully paired with a property getter.
 *
 * @author Ryan Heaton
 */
public class PropertySetter extends DecoratedMethodDeclaration {

  public PropertySetter(DecoratedMethodDeclaration delegate) {
    super(delegate);

    if (!delegate.isSetter()) {
      throw new ValidationException("A setter must be used to instantiate a property setter.");
    }
  }

  /**
   * The explicit property name of this getter, i.e. the one specified by an annotation, or null if none.
   *
   * @return The explicit property name of this getter, i.e. the one specified by an annotation, or null if none.
   */
  public String getExplicitPropertyName() {
    XmlElement xmlElement = getAnnotation(XmlElement.class);
    if ((xmlElement != null) && (!"##default".equals(xmlElement.name()))) {
      return xmlElement.name();
    }

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
