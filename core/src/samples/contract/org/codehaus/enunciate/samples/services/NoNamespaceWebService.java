package org.codehaus.enunciate.samples.services;

import javax.jws.WebMethod;
import javax.jws.WebService;

/**
 * @author Ryan Heaton
 */
@WebService
public interface NoNamespaceWebService {

  boolean myImplicitlyPublicMethod();

  public boolean myExplicitlyPublicMethod();

  @WebMethod (
    exclude = true
  )
  public boolean myExcludedPublicMethod();

}
