package net.sf.enunciate.modules.xfire_client.annotations;

import java.io.Serializable;

/**
 * JDK 1.4-usable metadata for a web fault.
 *
 * @author Ryan Heaton
 * @see javax.xml.ws.WebFault
 */
public class WebFaultAnnotation implements Serializable {

  private String name;
  private String targetNamespace;
  private String faultBean;
  private boolean implicitFaultBean;

  public WebFaultAnnotation(String name, String targetNamespace, String faultBean, boolean implicitFaultBean) {
    this.name = name;
    this.targetNamespace = targetNamespace;
    this.faultBean = faultBean;
    this.implicitFaultBean = implicitFaultBean;
  }

  /**
   * The local name of the web fault.
   *
   * @return The local name of the web fault.
   */
  public String name() {
    return this.name;
  }

  /**
   * The namespace of the web fault.
   *
   * @return The namespace of the web fault.
   */
  public String targetNamespace() {
    return this.targetNamespace;
  }

  /**
   * Whether the fault bean is implicit.
   *
   * @return true if the fault bean is implicit, false if it is explicit.
   */
  public boolean implicitFaultBean() {
    return this.implicitFaultBean;
  }

  /**
   * The fault bean.
   *
   * @return The fault bean.
   */
  public String faultBean() {
    return this.faultBean;
  }
}
