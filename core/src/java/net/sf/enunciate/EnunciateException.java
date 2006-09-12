package net.sf.enunciate;

/**
 * General enunciate exception.
 *
 * @author Ryan Heaton
 */
public class EnunciateException extends Exception {

  public EnunciateException() {
  }

  public EnunciateException(String message) {
    super(message);
  }

  public EnunciateException(String message, Throwable cause) {
    super(message, cause);
  }

  public EnunciateException(Throwable cause) {
    super(cause);
  }
}
