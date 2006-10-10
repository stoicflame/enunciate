package net.sf.enunciate.modules.jaxws;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import net.sf.enunciate.contract.jaxws.*;
import net.sf.enunciate.contract.validation.BaseValidator;
import net.sf.enunciate.contract.validation.ValidationResult;
import net.sf.jelly.apt.Context;

/**
 * Validator for the xml module.
 *
 * @author Ryan Heaton
 */
public class JAXWSValidator extends BaseValidator {

  @Override
  public ValidationResult validateEndpointInterface(EndpointInterface ei) {
    ValidationResult result = super.validateEndpointInterface(ei);
    AnnotationProcessorEnvironment ape = Context.getCurrentEnvironment();

    for (WebMethod webMethod : ei.getWebMethods()) {
      for (WebMessage webMessage : webMethod.getMessages()) {
        if (webMessage instanceof RequestWrapper) {
          String requestBeanName = ((RequestWrapper) webMessage).getRequestBeanName();
          if (ape.getTypeDeclaration(requestBeanName) != null) {
            result.addError(webMethod.getPosition(), requestBeanName + " is an existing class.  Either move it, or customize the request bean " +
              "class name with the @RequestWrapper annotation.");
          }
        }
        else if (webMessage instanceof ResponseWrapper) {
          String responseBeanName = ((ResponseWrapper) webMessage).getResponseBeanName();
          if (ape.getTypeDeclaration(responseBeanName) != null) {
            result.addError(webMethod.getPosition(), responseBeanName + " is an existing class.  Either move it, or customize the response bean " +
              "class name with the @ResponseWrapper annotation.");
          }
        }
        else if (webMessage instanceof WebFault) {
          WebFault webFault = (WebFault) webMessage;
          if (webFault.isImplicitSchemaElement()) {
            String faultBeanFQN = webFault.getImplicitFaultBeanQualifiedName();
            if (ape.getTypeDeclaration(faultBeanFQN) != null) {
              result.addError(webFault.getPosition(), faultBeanFQN + " is an existing class.  Either move it, or customize the fault bean name with the " +
                "@WebFault annotation.");
            }
          }
        }
      }
    }

    return result;
  }

}
