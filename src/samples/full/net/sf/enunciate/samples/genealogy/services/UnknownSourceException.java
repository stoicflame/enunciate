package net.sf.enunciate.samples.genealogy.services;

import javax.xml.ws.WebFault;

/**
 * Thrown if an attempt was made to access an unknown source.
 *
 * @author Ryan Heaton
 */
@WebFault (
  targetNamespace = "http://enunciate.sf.net/samples/full"
)
public class UnknownSourceException extends Exception {

  private UnknownSourceBean faultInfo;

  public UnknownSourceException(String message, UnknownSourceBean faultInfo) {
    super(message);
    this.faultInfo = faultInfo;
  }

  public UnknownSourceException(String message, UnknownSourceBean faultInfo, Throwable cause) {
    super(message, cause);
    this.faultInfo = faultInfo;
  }

  public UnknownSourceBean getFaultInfo() {
    return faultInfo;
  }
}
