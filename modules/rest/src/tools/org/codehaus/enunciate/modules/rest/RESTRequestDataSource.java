package org.codehaus.enunciate.modules.rest;

import javax.activation.DataSource;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
   * A data source for a REST request.
 */
public class RESTRequestDataSource implements DataSource {

  private final HttpServletRequest request;
  private final String name;

  /**
   * @param request The servlet request.
   */
  public RESTRequestDataSource(HttpServletRequest request, String name) {
    this.request = request;
    this.name = name;
  }

  public InputStream getInputStream() throws IOException {
    return this.request.getInputStream();
  }

  public OutputStream getOutputStream() throws IOException {
    throw new IOException();
  }

  public String getContentType() {
    return request.getContentType();
  }

  public String getName() {
    return name;
  }

  public long getSize() {
    return request.getContentLength();
  }
}
