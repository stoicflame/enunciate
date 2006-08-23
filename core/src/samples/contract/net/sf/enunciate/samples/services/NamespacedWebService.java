package net.sf.enunciate.samples.services;

import javax.jws.WebMethod;
import javax.jws.WebService;

/**
 * @author Ryan Heaton
 */
@WebService (
  targetNamespace = "http://enunciate.sf.net/samples/contract",
  name = "annotated-web-service"
)
public class NamespacedWebService {

  private boolean myPrivateMethod() {
    return false;
  }

  protected boolean myProtectedMethod() {
    return myPrivateMethod();
  }

  public boolean myPublicMethod() {
    return myProtectedMethod();
  }

  @WebMethod (
    exclude = true
  )
  public boolean myExcludedMethod() {
    return myPublicMethod();
  }
}
