package com.webcohesion.enunciate.models.xml;

import javax.xml.namespace.QName;
import java.util.List;

/**
 * @author Ryan Heaton
 */
public class XmlEnumTypeDefinition<T> extends XmlTypeDefinition {

  private final Class<T> constantType;
  private List<XmlEnumConstant<T>> constants;

  public XmlEnumTypeDefinition(Class<T> constantType) {
    this.constantType = constantType;
  }

  @Override
  public boolean isEnum() {
    return String.class.isAssignableFrom(this.constantType);
  }

  public boolean isQNameEnum() {
    return QName.class.isAssignableFrom(this.constantType);
  }

  public List<XmlEnumConstant<T>> getConstants() {
    return constants;
  }

  public void setConstants(List<XmlEnumConstant<T>> constants) {
    this.constants = constants;
  }
}
