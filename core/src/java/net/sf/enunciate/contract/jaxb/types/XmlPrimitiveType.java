package net.sf.enunciate.contract.jaxb.types;

import com.sun.mirror.type.PrimitiveType;
import net.sf.jelly.apt.decorations.type.DecoratedPrimitiveType;

import javax.xml.namespace.QName;

/**
 * @author Ryan Heaton
 */
public class XmlPrimitiveType extends DecoratedPrimitiveType implements XmlTypeMirror {

  public XmlPrimitiveType(PrimitiveType delegate) {
    super(delegate);
  }

  public String getName() {
    switch (getKind()) {
      case BOOLEAN:
        return "boolean";
      case BYTE:
        return "byte";
      case DOUBLE:
        return "double";
      case FLOAT:
        return "float";
      case INT:
        return "int";
      case LONG:
        return "long";
      case SHORT:
        return "short";
    }

    return null;
  }

  public String getNamespace() {
    return "http://www.w3.org/2001/XMLSchema";
  }

  public QName getQname() {
    return new QName(getNamespace(), getName());
  }

  public boolean isAnonymous() {
    return false;
  }

  public boolean isSimple() {
    return true;
  }
}
