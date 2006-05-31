package net.sf.enunciate.contract.jaxb;

import com.sun.mirror.declaration.MemberDeclaration;
import com.sun.mirror.type.TypeMirror;

interface is wrong.  The "accessor" needs to be divided into attributes, xmlValue, and child elements.
  TypeDefinition should have a getXmlValue() method.  ComplexTypeDefinition should have getAttributes and
  getChildElements.  The AccessorFilter should include XmlValue and XmlAttribute.  The AccessorComparator should
  only compare child elements.

public interface Accessor extends MemberDeclaration {

  /**
   * The property name.
   *
   * @return The property name.
   */
  String getPropertyName();

  /**
   * The property type.
   *
   * @return The property type.
   */
  TypeMirror getPropertyType();

  /**
   * Whether this accessor is the xml value of the type definition.
   *
   * @return Whether this accessor is the xml value of the type definition.
   */
  boolean isXmlValue();

  /**
   * Whether this accessor is the mixed content of the type definition.
   *
   * @return Whether this accessor is the mixed content of the type definition.
   */
  boolean isXmlMixed();

}
