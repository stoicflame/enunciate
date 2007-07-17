/*
 * Copyright 2006 Web Cohesion
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

package org.codehaus.enunciate.modules.xfire;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Convenience class for accessing the current HTTP request context.
 *
 * @author Ryan Heaton
 */
public class HTTPRequestContext {

  private static final ThreadLocal<HTTPRequestContext> TL_CONTEXT = new ThreadLocal<HTTPRequestContext>();

  private HttpServletRequest request;
  private HttpServletResponse response;

  protected HTTPRequestContext(HttpServletRequest request, HttpServletResponse response) {
    this.request = request;
    this.response = response;
  }

  /**
   * Get the current context.
   *
   * @return The current context, or null if none has been established.
   */
  public static HTTPRequestContext get() {
    return TL_CONTEXT.get();
  }

  /**
   * The request.
   *
   * @return The request.
   */
  public HttpServletRequest getRequest() {
    return request;
  }

  /**
   * The request.
   *
   * @return The request.
   */
  public HttpServletResponse getResponse() {
    return response;
  }
}
