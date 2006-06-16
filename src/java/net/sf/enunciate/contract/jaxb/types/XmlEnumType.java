package net.sf.enunciate.contract.jaxb.types;

import com.sun.mirror.type.EnumType;

/**
 * Decorator for an xml enum type.
 *
 * @author Ryan Heaton
 */
public class XmlEnumType extends XmlClassType {

  public XmlEnumType(EnumType delegate) throws XmlTypeException {
    super(delegate);
  }

  @Override
  public boolean isEnum() {
    return true;
  }

}
