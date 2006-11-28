package net.sf.enunciate.samples.genealogy.services;

import javax.xml.ws.WebFault;

/**
 * @author Ryan Heaton
 */
@WebFault (
  targetNamespace = "http://enunciate.sf.net/samples/full"
)
public class ServiceException extends Exception {

  private String anotherMessage;

  public ServiceException(String message, String id) {
    super(message);
    this.anotherMessage = id;
  }

  public String getAnotherMessage() {
    return anotherMessage;
  }

  public void setAnotherMessage(String anotherMessage) {
    this.anotherMessage = anotherMessage;
  }
}
