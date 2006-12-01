package net.sf.enunciate.samples.xfire_client;

import javax.xml.ws.WebFault;

/**
 * @author Ryan Heaton
 */
@WebFault (
  targetNamespace = "urn:xfire_client"
)
public class BasicFaultOne extends Exception {
}
