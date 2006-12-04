package net.sf.modules.xfire;

import javax.jws.WebService;

/**
 * @author Ryan Heaton
 */
@WebService (
  serviceName = "SimpleEI",
  targetNamespace = "urn:hi"
)
public class SimpleEIDifferentNS {

  public void doNothing(boolean someParam, double anotherParam) {

  }

}
