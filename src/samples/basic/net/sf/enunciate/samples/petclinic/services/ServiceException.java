package net.sf.enunciate.samples.petclinic.services;

import javax.xml.ws.WebFault;

///CLOVER:OFF 

/**
 * @author Ryan Heaton
 */
@WebFault (
  targetNamespace = "http://net.sf.enunciate/samples/petclinic"
)
public class ServiceException extends Exception {

  /**
   * Constructs a ServiceException with no detail message.
   */
  public ServiceException() {
    super();
  }

  /**
   * Constructs a ServiceException with the specified detail message.
   *
   * @param message the detail message
   */
  public ServiceException(String message) {
    super(message);
  }

  /**
   * Constructs a ServiceException with the specified detail message and cause.
   *
   * @param message the detail message
   * @param cause the cause
   */
  public ServiceException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs a ServiceException with the specified cause.
   *
   * @param cause the cause
   */
  public ServiceException(Throwable cause) {
    super(cause);
  }
}