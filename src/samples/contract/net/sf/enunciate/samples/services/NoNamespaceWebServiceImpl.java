package net.sf.enunciate.samples.services;

import javax.jws.WebService;

/**
 * @author Ryan Heaton
 */
@WebService (
  endpointInterface = "net.sf.enunciate.samples.services.NoNamespaceWebService"
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
