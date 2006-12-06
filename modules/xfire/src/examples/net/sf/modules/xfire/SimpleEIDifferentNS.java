package net.sf.modules.xfire;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

/**
 * @author Ryan Heaton
 */
@WebService (
  serviceName = "SimpleEI",
  targetNamespace = "urn:hi"
)
public class SimpleEIDifferentNS {

  @SOAPBinding (
    style = SOAPBinding.Style.RPC
  )
  public void doNothing(boolean someParam, double anotherParam) {

  }

}
