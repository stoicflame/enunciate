package com.webcohesion.enunciate.models.xml;

/**
 * @author Ryan Heaton
 */
public class XmlComplexTypeDefinition extends XmlTypeDefinition {


  private ComplexContentType contentType;


  @Override
  public boolean isComplex() {
    return true;
  }
}
