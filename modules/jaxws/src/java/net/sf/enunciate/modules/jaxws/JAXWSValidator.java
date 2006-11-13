package net.sf.enunciate.modules.jaxws;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import net.sf.enunciate.contract.jaxws.*;
import net.sf.enunciate.contract.validation.BaseValidator;
import net.sf.enunciate.contract.validation.ValidationResult;
import net.sf.jelly.apt.Context;

import java.util.HashSet;
import java.util.Set;

/**
 * Validator for the xml module.
 *
 * @author Ryan Heaton
 */
public class JAXWSValidator extends BaseValidator {

  @Override
  public ValidationResult validateEndpointInterface(EndpointInterface ei) {
    ValidationResult result = super.validateEndpointInterface(ei);
    HashSet<String> jaxwsBeans = new HashSet<String>();

    for (WebMethod webMethod : ei.getWebMethods()) {
      for (WebMessage webMessage : webMethod.getMessages()) {
        if (webMessage instanceof RequestWrapper) {
          result.aggregate(validateRequestWrapper((RequestWrapper) webMessage, jaxwsBeans));
        }
        else if (webMessage instanceof ResponseWrapper) {
          result.aggregate(validateResponseWrapper((ResponseWrapper)webMessage, jaxwsBeans));
        }
        else if (webMessage instanceof WebFault) {
          result.aggregate(validateWebFault((WebFault) webMessage, jaxwsBeans));
        }
      }
    }

    return result;
  }

  /**
   * Validates a request wrapper.
   *
   * @param wrapper The wrapper.
   * @param alreadyVisited The list of bean names already visited.
   * @return The validation result.
   */
  public ValidationResult validateRequestWrapper(RequestWrapper wrapper, Set<String> alreadyVisited) {
    AnnotationProcessorEnvironment ape = Context.getCurrentEnvironment();
    ValidationResult result = new ValidationResult();

    String requestBeanName = wrapper.getRequestBeanName();
    if (!alreadyVisited.add(requestBeanName)) {
      result.addError(wrapper.getWebMethod().getPosition(), requestBeanName + " conflicts with another generated bean name.  Please use the @RequestWrapper " +
        "annotation to customize the bean name.");
    }
    else if (ape.getTypeDeclaration(requestBeanName) != null) {
      result.addError(wrapper.getWebMethod().getPosition(), requestBeanName + " is an existing class.  Either move it, or customize the request bean " +
        "class name with the @RequestWrapper annotation.");
    }
    
    return result;
  }

  /**
   * Validates a response wrapper.
   *
   * @param wrapper The wrapper.
   * @param alreadyVisited The list of bean names already visited.
   * @return The validation result.
   */
  public ValidationResult validateResponseWrapper(ResponseWrapper wrapper, Set<String> alreadyVisited) {
    AnnotationProcessorEnvironment ape = Context.getCurrentEnvironment();
    ValidationResult result = new ValidationResult();

    String responseBeanName = wrapper.getResponseBeanName();
    if (!alreadyVisited.add(responseBeanName)) {
      result.addError(wrapper.getWebMethod().getPosition(), responseBeanName + " conflicts with another generated bean name.  Please use the @ResponseWrapper " +
        "annotation to customize the bean name.");
    }
    else if (ape.getTypeDeclaration(responseBeanName) != null) {
      result.addError(wrapper.getWebMethod().getPosition(), responseBeanName + " is an existing class.  Either move it, or customize the response bean " +
        "class name with the @ResponseWrapper annotation.");
    }

    return result;
  }

  /**
   * Validates a web fault.
   *
   * @param webFault The web fault to validate.
   * @param alreadyVisited The bean names that have alrady been visited.
   * @return The validation result.
   */
  public ValidationResult validateWebFault(WebFault webFault, Set<String> alreadyVisited) {
    AnnotationProcessorEnvironment ape = Context.getCurrentEnvironment();
    ValidationResult result = new ValidationResult();
    
    if (webFault.isImplicitSchemaElement()) {
      String faultBeanFQN = webFault.getImplicitFaultBeanQualifiedName();
      if (!alreadyVisited.add(faultBeanFQN)) {
        result.addError(webFault.getPosition(), faultBeanFQN + " conflicts with another generated bean name.  Please use the @WebFault annotation " +
          "to customize the fault bean name.");
      }
      else if (ape.getTypeDeclaration(faultBeanFQN) != null) {
        result.addError(webFault.getPosition(), faultBeanFQN + " is an existing class.  Either move it, or customize the fault bean name with the " +
          "@WebFault annotation.");
      }
    }

    return result;
  }

}
