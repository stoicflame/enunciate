package com.webcohesion.enunciate;

/**
 * @author Ryan Heaton
 */
public interface EnunciateLogger {

  void debug(String message, Object... formatArgs);

  void info(String message, Object... formatArgs);

  void warn(String message, Object... formatArgs);
}
