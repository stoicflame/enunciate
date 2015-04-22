package com.webcohesion.enunciate;

/**
 * Thread safety object lock to cater for Maven 3 Parallel builds
 * to force synchronisation between mojos as the Enunciate core currently is
 * not thread-safe.
 *
 * @author Ryan Heaton
 */
public class ThreadSafety {

  public static final ThreadSafety lock = new ThreadSafety();

  private ThreadSafety() {
  }
}
