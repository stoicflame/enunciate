package net.sf.enunciate.contract.jaxb;

import com.sun.mirror.declaration.FieldDeclaration;
import net.sf.jelly.apt.decorations.declaration.DecoratedFieldDeclaration;

import javax.xml.bind.annotation.XmlTransient;

/**
 * A field accessor.
 *
 * @author Ryan Heaton
 */
public class FieldAccessor extends DecoratedFieldDeclaration implements Accessor {

  public FieldAccessor(FieldDeclaration delegate) {
    super(delegate);

    if (getAnnotation(XmlTransient.class) != null) {
      throw new IllegalArgumentException("An xml-transient field cannot be an accessor.");
    }
  }

  public String getPropertyName() {
    return getSimpleName();
  }
}
