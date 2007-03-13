package org.codehaus.enunciate.samples.services;

import javax.jws.WebService;

/**
 * @author Ryan Heaton
 */
@WebService
public class SuperNoNamespaceWebServiceImpl extends NoNamespaceWebServiceImpl {

  public boolean anotherPublicMethod() {
    return false;
  }

}
