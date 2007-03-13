package org.codehaus.enunciate.modules.xfire_client.annotations;

import java.io.Serializable;

/**
 * JDK 1.4-usable metadata for a response wrapper.
 *
 * @author Ryan Heaton
 * @see javax.xml.ws.ResponseWrapper
 */
public class ResponseWrapperAnnotation implements Serializable {

  private String localName;
  private String targetNamespace;
  private String className;

  public ResponseWrapperAnnotation(String localName, String targetNamespace, String className) {
    this.localName = localName;
    this.targetNamespace = targetNamespace;
    this.className = className;
  }

  public String localName() {
    return this.localName;
  }

  public String targetNamespace() {
    return this.targetNamespace;
  }

  public String className() {
    return this.className;
  }

}
