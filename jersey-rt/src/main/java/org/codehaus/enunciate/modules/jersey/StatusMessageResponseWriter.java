/*
 * Copyright 2006-2008 Web Cohesion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.enunciate.modules.jersey;

import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseWriter;
import org.codehaus.enunciate.modules.jersey.response.HasStatusMessage;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

//todo: it would be SO much nicer just to extend com.sun.jersey.spi.container.servlet.WebComponent.Writer, but alas, it's private and final.
/**
 * Response writer that writes the status message along with the rest of the response.
 *
 * @author Ryan Heaton
 */
public class StatusMessageResponseWriter extends OutputStream implements ContainerResponseWriter {

  private final HttpServletResponse response;
  private ContainerResponse cResponse;
  private long contentLength;
  private OutputStream out;

  StatusMessageResponseWriter(HttpServletResponse response) {
    this.response = response;
  }

  public OutputStream writeStatusAndHeaders(long contentLength, ContainerResponse cResponse) throws IOException {
    this.contentLength = contentLength;
    this.cResponse = cResponse;
    return this;
  }

  public void finish() throws IOException {
    if (out != null) {
      return;
    }

    // Note that the writing of headers MUST be performed before
    // the invocation of sendError as on some Servlet implementations
    // modification of the response headers will have no effect
    // after the invocation of sendError.
    writeHeaders();

    if (cResponse instanceof HasStatusMessage) {
      response.setStatus(cResponse.getStatus(), ((HasStatusMessage) cResponse).getStatusMessage());
    }
    else {
      response.setStatus(cResponse.getStatus());
    }
    
    response.flushBuffer();
  }

  public void write(int b) throws IOException {
    initiate();
    out.write(b);
  }

  @Override
  public void write(byte b[]) throws IOException {
    if (b.length > 0) {
      initiate();
      out.write(b);
    }
  }

  @Override
  public void write(byte b[], int off, int len) throws IOException {
    if (len > 0) {
      initiate();
      out.write(b, off, len);
    }
  }

  @Override
  public void flush() throws IOException {
    if (out != null) {
      out.flush();
    }
  }

  @Override
  public void close() throws IOException {
    initiate();
    out.close();
  }

  void initiate() throws IOException {
    if (out == null) {
      writeStatusAndHeaders();
      out = response.getOutputStream();
    }
  }

  void writeStatusAndHeaders() {
    writeHeaders();
    if (cResponse instanceof HasStatusMessage) {
      response.setStatus(cResponse.getStatus(), ((HasStatusMessage) cResponse).getStatusMessage());
    }
    else {
      response.setStatus(cResponse.getStatus());
    }
  }

  void writeHeaders() {
    if (contentLength != -1 && contentLength < Integer.MAX_VALUE) {
      response.setContentLength((int) contentLength);
    }

    MultivaluedMap<String, Object> headers = cResponse.getHttpHeaders();
    for (Map.Entry<String, List<Object>> e : headers.entrySet()) {
      for (Object v : e.getValue()) {
        response.addHeader(e.getKey(), ContainerResponse.getHeaderValue(v));
      }
    }
  }
}
