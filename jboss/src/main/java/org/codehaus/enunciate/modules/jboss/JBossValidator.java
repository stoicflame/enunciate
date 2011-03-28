package org.codehaus.enunciate.modules.jboss;

import com.sun.mirror.declaration.MethodDeclaration;
import org.codehaus.enunciate.contract.jaxws.EndpointImplementation;
import org.codehaus.enunciate.contract.jaxws.EndpointInterface;
import org.codehaus.enunciate.contract.validation.BaseValidator;
import org.codehaus.enunciate.contract.validation.ValidationResult;

import javax.jws.WebService;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Ryan Heaton
 */
public class JBossValidator extends BaseValidator {

  private final HashMap<String, EndpointInterface> visitedEndpoints = new HashMap<String, EndpointInterface>();

  @Override
  public ValidationResult validateEndpointInterface(EndpointInterface ei) {
    ValidationResult result = super.validateEndpointInterface(ei);

    WebService eiAnnotation = ei.getAnnotation(WebService.class);
    if (ei.isInterface()) {
      if (!"".equals(eiAnnotation.serviceName())) {
        result.addError(ei, "JBoss fails if you specify 'serviceName' on an endpoint interface.");
      }
      if (!"".equals(eiAnnotation.portName())) {
        result.addError(ei, "JBoss fails if you specify 'portName' on an endpoint interface.");
      }
      for (MethodDeclaration m : ei.getMethods()) {
        javax.jws.WebMethod wm = m.getAnnotation(javax.jws.WebMethod.class);
        if (wm.exclude()) {
          result.addError(m, "JBoss fails if you specify 'exclude=true' on an endpoint interface.");
        }
      }
    }

    if (ei.getEndpointImplementations().size() > 1) {
      ArrayList<String> impls = new ArrayList<String>();
      for (EndpointImplementation impl : ei.getEndpointImplementations()) {
        impls.add(impl.getQualifiedName());
      }
      result.addError(ei, "Sorry, JBoss doesn't support two endpoint implementations for interface '" + ei.getQualifiedName() +
        "'.  Found " + ei.getEndpointImplementations().size() + " implementations (" + impls.toString() + ").");
    }
    else if (ei.getEndpointImplementations().isEmpty()) {
      result.addError(ei, "JBoss requires an implementation for each service interface.");
    }

    EndpointInterface visited = visitedEndpoints.put(ei.getServiceName(), ei);
    if (visited != null) {
      if (visited.getTargetNamespace().equals(ei.getTargetNamespace())) {
        result.addError(ei, "Ummm... you already have a service named " + ei.getServiceName() + " at " +
          visited.getPosition() + ".  You need to disambiguate.");
      }
    }

    return result;
  }
}
