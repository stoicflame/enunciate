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

import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletRequest;

/**
 * A REST request.
 *
 * @author Ryan Heaton
 */
public class RESTRequest extends HttpServletRequestWrapper {

  private final RESTOperation operation;
  private final String configuredContentType;

  public RESTRequest(HttpServletRequest request, RESTOperation operation, String configuredContentType) {
    super(request);

    this.operation = operation;
    this.configuredContentType = configuredContentType;
  }

  /**
   * The operation assigned to handle this request.
   *
   * @return The operation assigned to handle this request.
   */
  public RESTOperation getOperation() {
    return operation;
  }

  /**
   * The content type configured for this request.
   *
   * @return The content type configured for this request.
   */
  public String getConfiguredContentType() {
    return configuredContentType;
  }
}
