package com.webcohesion.enunciate.modules.jaxb.model;

import javax.xml.namespace.QName;

/**
 * Common interface for an element declaration.
 * 
 * @author Ryan Heaton
 */
public interface ElementDeclaration extends javax.lang.model.element.Element {
  
  /**
   * The name of the xml element declaration.
   *
   * @return The name of the xml element declaration.
   */
  String getName();

  /**
   * The namespace of the xml element.
   *
   * @return The namespace of the xml element.
   */
  String getNamespace();

  /**
   * The qname for this root element.
   *
   * @return The qname for this root element.
   */
  QName getQname();

}
