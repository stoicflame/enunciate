package org.codehaus.enunciate.samples.genealogy.services;

import javax.xml.ws.WebFault;

/**
 * Generic fault for the genealogy API.
 *
 * @author Ryan Heaton
 */
@WebFault (
  targetNamespace = "http://enunciate.codehaus.org/samples/full"
)
public class ServiceException extends Exception {

  private String anotherMessage;

  public ServiceException(String message, String anotherMessage) {
    super(message);
    this.anotherMessage = anotherMessage;
  }

  /**
   * Some other message to pass in addition to the original message.
   *
   * @return Some other message to pass in addition to the original message.
   */
  public String getAnotherMessage() {
    return anotherMessage;
  }

  /**
   * Some other message to pass in addition to the original message.
   *
   * @param anotherMessage Some other message to pass in addition to the original message.
   */
  public void setAnotherMessage(String anotherMessage) {
    this.anotherMessage = anotherMessage;
  }
}
