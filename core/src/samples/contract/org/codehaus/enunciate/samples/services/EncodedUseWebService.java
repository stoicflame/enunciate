package org.codehaus.enunciate.samples.services;

import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.soap.SOAPBinding;

/**
 * @author Ryan Heaton
 */
@WebService (
  targetNamespace = "http://enunciate.codehaus.org/samples/contract",
  name = "annotated-web-service"
)
@SOAPBinding (
  use = SOAPBinding.Use.ENCODED
)
public class EncodedUseWebService {

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
