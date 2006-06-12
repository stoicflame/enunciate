package net.sf.enunciate.contract.jaxb;

import com.sun.mirror.declaration.FieldDeclaration;
import net.sf.jelly.apt.decorations.declaration.PropertyDeclaration;

import javax.xml.bind.annotation.XmlElement;

/**
 * An accessor that is marshalled in xml to an xml element.
 *
 * @author Ryan Heaton
 */
public class ElementAccessor extends Accessor {

  public ElementAccessor(FieldDeclaration delegate) {
    super(delegate);
  }

  public ElementAccessor(PropertyDeclaration delegate) {
    super(delegate);
  }

  // Inherited.
  public String getAccessorName() {
    String propertyName = getSimpleName();

    XmlElement xmlElement = getAnnotation(XmlElement.class);
    if ((xmlElement != null) && (!"##default".equals(xmlElement.name()))) {
      propertyName = xmlElement.name();
    }

    return propertyName;
  }

}
