package net.sf.enunciate.modules.xfire;

import net.sf.enunciate.contract.jaxws.EndpointInterface;
import net.sf.enunciate.contract.jaxws.WebMethod;
import net.sf.enunciate.contract.jaxws.WebParam;
import net.sf.enunciate.contract.validation.BaseValidator;
import net.sf.enunciate.contract.validation.ValidationResult;

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

    EndpointInterface visited = visitedEndpoints.put(ei.getServiceName(), ei);
    if (visited != null) {
      if (!visited.getTargetNamespace().equals(ei.getTargetNamespace())) {
        // todo: support multiple versions of web services.
        result.addError(ei.getPosition(), "Enunciate doesn't support two endpoint interfaces with the same service name, " +
          "even though they have different namespaces.  Future support for this is pending...");
      }
      else {
        result.addError(ei.getPosition(), "Ummm... you already have a service named " + ei.getServiceName() + " at " +
          visited.getPosition() + ".  You need to disambiguate.");
      }
    }

    for (WebMethod webMethod : ei.getWebMethods()) {
      for (WebParam webParam : webMethod.getWebParameters()) {
        if ((webParam.isHeader()) && ("".equals(webParam.getAnnotation(javax.jws.WebParam.class).name()))) {
          //todo: lift this constraint by serializing the parameter names to some file you can load for metadata...
          result.addError(webParam.getPosition(), "For now, Enunciate requires you to specify a 'name' on the @WebParam annotation if it's a header.");
        }
      }
    }

    return result;
  }

}
