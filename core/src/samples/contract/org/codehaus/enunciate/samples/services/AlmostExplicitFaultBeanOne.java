package org.codehaus.enunciate.samples.services;

import org.codehaus.enunciate.samples.schema.BeanThree;

import javax.xml.ws.WebFault;

/**
 * @author Ryan Heaton
 */
@WebFault (
  name = "ignored-name",
  targetNamespace = "urn:ignored",
  faultBean = "org.codehaus.enunciate.IgnoredFaultBean"
)
public class AlmostExplicitFaultBeanOne extends Exception {

  private BeanThree faultInfo;

  public AlmostExplicitFaultBeanOne(String message, BeanThree faultInfo) {
    super(message);
    this.faultInfo = faultInfo;
  }

  public AlmostExplicitFaultBeanOne(String message, BeanThree faultInfo, Exception throwable) {
    super(message, throwable);
    this.faultInfo = faultInfo;
  }

  public BeanThree getFaultInfo() {
    return faultInfo;
  }

}
