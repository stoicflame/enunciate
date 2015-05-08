package com.webcohesion.enunciate;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;

/**
 * @author Ryan Heaton
 */
public interface EnunciateLogger {

  void debug(String message, Object... formatArgs);

  void info(String message, Object... formatArgs);

  void warn(String message, Object... formatArgs);

  public static final class ListWriter {
    private final Collection list;

    public ListWriter(Collection list) {
      this.list = list;
    }

    @Override
    public String toString() {
      StringWriter out = new StringWriter();
      PrintWriter writer = new PrintWriter(out);
      writer.println("[");
      for (Object item : this.list) {
        writer.println("  " + String.valueOf(item));
      }
      writer.print("]");
      writer.flush();
      writer.close();
      return out.toString();
    }
  }
}
