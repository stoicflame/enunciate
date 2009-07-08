package org.codehaus.enunciate.contract.jaxb;

import com.sun.mirror.declaration.Declaration;

/**
 * Common interface for an element declaration.
 * 
 * @author Ryan Heaton
 */
public interface ElementDeclaration extends Declaration {
  
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
   * Generate some example XML for this root element.
   *
   * @return Some example XML.
   */
  String generateExampleXml();
  
}
