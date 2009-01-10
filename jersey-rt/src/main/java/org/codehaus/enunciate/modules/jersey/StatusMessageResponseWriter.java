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

import java.io.IOException;
import java.io.OutputStream;

/**
 * Response writer that writes the status message along with the rest of the response.
 *
 * @author Ryan Heaton
 */
public class StatusMessageResponseWriter implements ContainerResponseWriter {
  //todo: it would be SO much nicer just to extend com.sun.jersey.spi.container.servlet.ServletContainer$Writer, but alas, it's private.

  private final ContainerResponseWriter delegate;

  /**
   * Construct a new status message response writer.
   *
   * @param delegate The delegate.
   */
  public StatusMessageResponseWriter(ContainerResponseWriter delegate) {
    this.delegate = delegate;
  }

  // Inherited.
  public OutputStream writeStatusAndHeaders(long contentLength, ContainerResponse response) throws IOException {
    OutputStream out = this.delegate.writeStatusAndHeaders(contentLength, response);
    if (response.getResponse() instanceof HasStatusMessage && EnunciateSpringServlet.CURRENT_RESPONSE.get() != null) {
      EnunciateSpringServlet.CURRENT_RESPONSE.get().setStatus(response.getStatus(), ((HasStatusMessage)response.getResponse()).getStatusMessage());
    }
    return out;
  }

  // Inherited.
  public void finish() throws IOException {
    this.delegate.finish();
  }
}
