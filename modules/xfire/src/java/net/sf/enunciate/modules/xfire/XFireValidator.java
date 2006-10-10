package net.sf.enunciate.modules.xfire;

import net.sf.enunciate.contract.jaxws.EndpointInterface;
import net.sf.enunciate.contract.jaxws.WebMethod;
import net.sf.enunciate.contract.validation.BaseValidator;
import net.sf.enunciate.contract.validation.ValidationResult;

import javax.jws.soap.SOAPBinding;
import java.util.HashMap;

/**
 * Validator for the xfire server.
 *
 * @author Ryan Heaton
 */
public class XFireValidator extends BaseValidator {

  private final HashMap<String, EndpointInterface> visitedEndpoints = new HashMap<String, EndpointInterface>();

  @Override
  public ValidationResult validateEndpointInterface(EndpointInterface ei) {
    ValidationResult result = super.validateEndpointInterface(ei);
    for (WebMethod webMethod : ei.getWebMethods()) {
      if (webMethod.getSoapBindingStyle() == SOAPBinding.Style.RPC) {
        result.addError(webMethod.getPosition(), "XFire doesn't support RPC-style web methods.");
      }
    }

    EndpointInterface visited = visitedEndpoints.put(ei.getServiceName(), ei);
    if (visited != null) {
      if (!visited.getTargetNamespace().equals(ei.getTargetNamespace())) {
        // todo: support multiple versions of web services.
        result.addError(ei.getPosition(), "Enunciate doesn't support two endpoint interfaces with the same service name, " +
          "even though they have different namespaces.  Future support for this is coming, though....");
      }
    }

    return result;
  }

}
