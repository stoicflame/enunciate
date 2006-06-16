package net.sf.enunciate.contract.jaxb.types;

import com.sun.mirror.type.ReferenceType;
import com.sun.mirror.type.WildcardType;
import net.sf.jelly.apt.decorations.type.DecoratedWildcardType;

import java.util.Collection;

/**
 * @author Ryan Heaton
 */
public class XmlWildcardType extends DecoratedWildcardType implements XmlTypeMirror {

  private final XmlTypeMirror upperBounds;

  public XmlWildcardType(WildcardType delegate) throws XmlTypeException {
    super(delegate);

    Collection<ReferenceType> upperBounds = delegate.getUpperBounds();
    if (upperBounds.isEmpty()) {
      this.upperBounds = KnownXmlType.ANY_TYPE;
    }
    else {
      try {
        this.upperBounds = XmlTypeDecorator.decorate(upperBounds.iterator().next());
      }
      catch (XmlTypeException e) {
        throw new XmlTypeException("Problem with wildcard bounds: " + e.getMessage());
      }
    }
  }

  /**
   * The name of this wildcard type's upper bounds.
   *
   * @return The name of this wildcard type's upper bounds.
   */
  public String getName() {
    return upperBounds.getName();
  }

  /**
   * The namespace for this wildcard type's upper bounds.
   *
   * @return The namespace for this wildcard type's upper bounds.
   */
  public String getNamespace() {
    return upperBounds.getNamespace();
  }

  /**
   * A wildcard type can never be anonymous.
   *
   * @return A wildcard type can never be anonymous.
   */
  public boolean isAnonymous() {
    return false;
  }

}
