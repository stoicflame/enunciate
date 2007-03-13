package org.codehaus.enunciate.modules.xfire_client.annotations;

import java.io.Serializable;

/**
 * JDK 1.4-usable metadata for a request wrapper.
 *
 * @author Ryan Heaton
 * @see javax.xml.ws.RequestWrapper
 */
public class RequestWrapperAnnotation implements Serializable {

  private String localName;
  private String targetNamespace;
  private String className;

  public RequestWrapperAnnotation(String localName, String targetNamespace, String className) {
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
