package net.sf.enunciate.contract.jaxb;

import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.type.TypeMirror;
import net.sf.jelly.apt.decorations.declaration.DecoratedFieldDeclaration;

import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;

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

  // Inherited.
  public String getPropertyName() {
    return getSimpleName();
  }

  // Inherited.
  public TypeMirror getPropertyType() {
    return getType();
  }

  // Inherited.
  public boolean isXmlValue() {
    return getAnnotation(XmlValue.class) != null;
  }

  // Inherited.
  public boolean isXmlMixed() {
    return getAnnotation(XmlMixed.class) != null;
  }
}
