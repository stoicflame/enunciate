package net.sf.enunciate.contract.jaxws;

import com.sun.mirror.declaration.ClassDeclaration;
import net.sf.jelly.apt.decorations.declaration.DecoratedClassDeclaration;

/**
 * A class specified as a web service endpoint implementation.  Remember an endpoint implementation could
 * possibly implicitly define an endpoint interface (see spec, section 3.3).
 *
 * @author Ryan Heaton
 */
public class EndpointImplementation extends DecoratedClassDeclaration {

  private final EndpointInterface endpointInterface;

  public EndpointImplementation(ClassDeclaration delegate, EndpointInterface endpointInterface) {
    super(delegate);

    this.endpointInterface = endpointInterface;
  }

  /**
   * The endpoint interface specified for this web service.
   *
   * @return The endpoint interface specified for this web service
   */
  public EndpointInterface getEndpointInterface() {
    return endpointInterface;
  }

  /**
   * Get the binding type for this endpoint implementation, or null if none is specified.
   *
   * @return The binding type for this endpoint implementation.
   */
  public BindingType getBindingType() {
    javax.xml.ws.BindingType bindingType = getAnnotation(javax.xml.ws.BindingType.class);

    if (bindingType != null) {
      if ((bindingType.value() != null) && (!"".equals(bindingType.value()))) {
        return BindingType.fromNamespace(bindingType.value());
      }
    }

    return BindingType.SOAP_1_1;
  }

}
