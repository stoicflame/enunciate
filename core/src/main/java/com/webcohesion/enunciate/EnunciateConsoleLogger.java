package com.webcohesion.enunciate;

/**
 * @author Ryan Heaton
 */
public class EnunciateConsoleLogger implements EnunciateLogger {

  private boolean debugEnabled = false;
  private boolean infoEnabled = true;

  public void setDebugEnabled(boolean debugEnabled) {
    this.debugEnabled = debugEnabled;
  }

  public void setInfoEnabled(boolean infoEnabled) {
    this.infoEnabled = infoEnabled;
  }

  @Override
  public void debug(String message, Object... formatArgs) {
    if (this.debugEnabled) {
      System.out.print("[ENUNCIATE] ");
      System.out.println(String.format(message, formatArgs));
    }
  }

  @Override
  public void info(String message, Object... formatArgs) {
    if (this.infoEnabled) {
      System.out.print("[ENUNCIATE] ");
      System.out.println(String.format(message, formatArgs));
    }
  }

  @Override
  public void warn(String message, Object... formatArgs) {
    System.out.print("[ENUNCIATE] ");
    System.out.println(String.format(message, formatArgs));
  }
}
