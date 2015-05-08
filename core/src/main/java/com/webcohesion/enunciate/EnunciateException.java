package com.webcohesion.enunciate;

/**
 * @author Ryan Heaton
 */
public class EnunciateException extends RuntimeException {

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
