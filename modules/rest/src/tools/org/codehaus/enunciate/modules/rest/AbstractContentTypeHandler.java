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

package org.codehaus.enunciate.modules.rest;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Base implementation for a content type handler.
 *
 * @author Ryan Heaton
 */
public abstract class AbstractContentTypeHandler implements RESTRequestContentTypeHandler {

  /**
   * Reads an object from the request body.
   *
   * @param request The request.
   * @return The object.
   */
  public Object read(RESTRequest request) throws Exception {
    return read(request.getInputStream());
  }

  /**
   * Writes an object to the response body.
   *
   * @param data The data to write.
   * @param request The request.
   * @param response The response.
   */
  public void write(Object data, RESTRequest request, HttpServletResponse response) throws Exception {
    write(data, response.getOutputStream());
  }

  /**
   * Read an object from a stream.
   *
   * @param in The stream.
   * @return The object.
   * @throws UnsupportedOperationException If reading isn't supported.
   */
  public abstract Object read(InputStream in) throws Exception;

  /**
   * Write an object to a stream.
   *
   * @param data The data to write.
   * @param out The stream to write to.
   */
  public abstract void write(Object data, OutputStream out) throws Exception;

}
