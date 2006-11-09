package net.sf.enunciate.samples.services;

import javax.xml.ws.WebFault;

/**
 * @author Ryan Heaton
 */
@WebFault (
  name = "implicit-fault",
  targetNamespace = "urn:implicit-fault",
  faultBean = "net.sf.enunciate.ImplicitFaultBean"
)
public class ImplicitWebFaultTwo extends ImplicitWebFault {

  private double property4;

  public double getProperty4() {
    return property4;
  }

  public void setProperty4(double property4) {
    this.property4 = property4;
  }
}
