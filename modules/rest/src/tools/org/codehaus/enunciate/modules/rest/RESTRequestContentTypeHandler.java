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

/**
 * Content type handler interface.  Supported by the REST module as a
 * {@link org.codehaus.enunciate.rest.annotations.ContentTypeHandler}.
 *
 * @author Ryan Heaton
 */
public interface RESTRequestContentTypeHandler {

  /**
   * Read data from the specified REST request. (Optional operation.)
   *
   * @param request The REST request.
   * @return The data that was read.
   * @throws UnsupportedOperationException If this content type handler doesn't
   * support reading.
   */
  Object read(RESTRequest request) throws Exception;

  /**
   * Write data to the specified response.
   *
   * @param data The data to write.
   * @param request The request.
   * @param response The response.
   */
  void write(Object data, RESTRequest request, HttpServletResponse response) throws Exception;
  
}
