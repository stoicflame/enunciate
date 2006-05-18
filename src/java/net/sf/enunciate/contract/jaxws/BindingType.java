package net.sf.enunciate.contract.jaxws;

import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.soap.SOAPBinding;

/**
 * Enumeration of the supported binding types.
 *
 * @author Ryan Heaton
 */
public enum BindingType {

  SOAP_1_1(SOAPBinding.SOAP11HTTP_BINDING),

  SOAP_1_2(SOAPBinding.SOAP12HTTP_BINDING),

  HTTP(HTTPBinding.HTTP_BINDING);

  private final String namespace;

  BindingType(String namespace) {
    this.namespace = namespace;
  }

  /**
   * The namespace for this binding type.
   *
   * @return The namespace for this binding type.
   */
  public String getNamespace() {
    return namespace;
  }

  /**
   * Whether this binding type is SOAP 1.1.
   *
   * @return Whether this binding type is SOAP 1.1.
   */
  public boolean isSoap11() {
    return SOAPBinding.SOAP11HTTP_BINDING.equals(getNamespace());
  }

  /**
   * Whether this binding type is SOAP 1.2.
   *
   * @return Whether this binding type is SOAP 1.2.
   */
  public boolean isSoap12() {
    return SOAPBinding.SOAP12HTTP_BINDING.equals(getNamespace());
  }

  /**
   * Whether this binding type is HTTP.
   *
   * @return Whether this binding type is HTTP.
   */
  public boolean isHttp() {
    return HTTPBinding.HTTP_BINDING.equals(getNamespace());
  }

  /**
   * The binding type from a given namespace.
   *
   * @param namespace The namespace
   * @return The binding type from a given namespace.
   */
  public static BindingType fromNamespace(String namespace) {
    for (BindingType bindingType : values()) {
      if (bindingType.getNamespace().equals(namespace)) {
        return bindingType;
      }
    }

    throw new IllegalArgumentException("Unsupported binding type: " + namespace);
  }

}
