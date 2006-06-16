package net.sf.enunciate.contract.jaxb.types;

import com.sun.mirror.type.ReferenceType;
import com.sun.mirror.type.TypeVariable;
import net.sf.jelly.apt.decorations.type.DecoratedTypeVariable;

import java.util.Collection;

/**
 * @author Ryan Heaton
 */
public class XmlTypeVariable extends DecoratedTypeVariable implements XmlTypeMirror {

  private final XmlTypeMirror typeBounds;

  public XmlTypeVariable(TypeVariable delegate) throws XmlTypeException {
    super(delegate);

    Collection<ReferenceType> bounds = delegate.getDeclaration().getBounds();
    if (bounds.isEmpty()) {
      this.typeBounds = KnownXmlType.ANY_TYPE;
    }
    else {
      try {
        this.typeBounds = XmlTypeDecorator.decorate(bounds.iterator().next());
      }
      catch (XmlTypeException e) {
        throw new XmlTypeException("Problem with type bounds: " + e.getMessage());
      }
    }
  }

  /**
   * The name of the type bounds.
   *
   * @return The name of the type bounds.
   */
  public String getName() {
    return null;
  }

  /**
   * The namespace of the type bounds.
   *
   * @return The namespace of the type bounds.
   */
  public String getNamespace() {
    return null;
  }

  /**
   * A type variable can never be anonymous.
   *
   * @return A type variable can never be anonymous.
   */
  public boolean isAnonymous() {
    return false;
  }

}
