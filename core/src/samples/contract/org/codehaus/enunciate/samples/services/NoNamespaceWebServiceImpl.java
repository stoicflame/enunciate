package org.codehaus.enunciate.samples.services;

import javax.jws.WebService;

/**
 * @author Ryan Heaton
 */
@WebService (
  endpointInterface = "org.codehaus.enunciate.samples.services.NoNamespaceWebService"
)
public class NoNamespaceWebServiceImpl implements NoNamespaceWebService {

  public boolean myImplicitlyPublicMethod() {
    return false;
  }

  public boolean myExplicitlyPublicMethod() {
    return false;
  }

  public boolean myExcludedPublicMethod() {
    return false;
  }
}
