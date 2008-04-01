package org.codehaus.enunciate.modules.rest;

import java.io.PrintWriter;

/**
 * Handler for writing the JSONP paramter.
 *
 * @author Ryan Heaton
 */
public abstract class JsonPHandler {

  private final String callbackName;

  public JsonPHandler(String callbackName) {
    this.callbackName = callbackName;
  }

  public void writeTo(PrintWriter outStream) throws Exception {
    if ((callbackName != null) && (callbackName.trim().length() > 0)) {
      outStream.print(callbackName);
      outStream.print("(");
    }

    writeBody(outStream);

    if (callbackName != null) {
      outStream.print(")");
    }
  }

  public abstract void writeBody(PrintWriter outStream) throws Exception;
}
