package net.sf.enunciate.contract.jaxb;

import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.type.TypeMirror;
import net.sf.jelly.apt.decorations.declaration.DecoratedMemberDeclaration;
import net.sf.jelly.apt.decorations.declaration.PropertyDeclaration;

/**
 * An accessor for a field or method value into a type.
 *
 * @author Ryan Heaton
 */
public abstract class Accessor extends DecoratedMemberDeclaration {

  private final FieldDeclaration fieldDelegate;
  private final PropertyDeclaration methodDelegate;

  public Accessor(FieldDeclaration delegate) {
    super(delegate);

    this.fieldDelegate = delegate;
    this.methodDelegate = null;
  }

  public Accessor(PropertyDeclaration delegate) {
    super(delegate);

    this.fieldDelegate = null;
    this.methodDelegate = delegate;
  }

  /**
   * The name of the accessor.
   *
   * @return The name of the accessor.
   */
  public abstract String getAccessorName();

  /**
   * The type of the accessor.
   *
   * @return The type of the accessor.
   */
  public TypeMirror getAccessorType() {
    TypeMirror propertyType;
    if (fieldDelegate != null) {
      propertyType = fieldDelegate.getType();
    }
    else {
      propertyType = methodDelegate.getPropertyType();
    }
    return propertyType;
  }


}
