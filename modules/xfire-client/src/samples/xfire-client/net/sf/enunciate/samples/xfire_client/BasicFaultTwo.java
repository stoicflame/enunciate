package net.sf.enunciate.samples.xfire_client;

import javax.xml.ws.WebFault;

/**
 * @author Ryan Heaton
 */
@WebFault (
  name = "bf2",
  targetNamespace = "urn:bf2",
  faultBean = "net.nothing.BasicFault2"
)
public class BasicFaultTwo extends Exception {
}
