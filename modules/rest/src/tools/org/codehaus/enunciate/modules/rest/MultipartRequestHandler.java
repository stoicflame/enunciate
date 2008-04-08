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

import javax.activation.DataHandler;
import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

/**
 * Handler for multipart requests.
 *
 * @author Ryan Heaton
 */
public interface MultipartRequestHandler {

  /**
   * Whether the request represents a multipart request.
   *
   * @param request The request.
   * @return Whether the request represents a multipart request.
   */
  boolean isMultipart(HttpServletRequest request);

  /**
   * Handles the multipart request.
   *
   * @param request The multipart request to handle.
   * @return The handled form of the request.
   */
  HttpServletRequest handleMultipartRequest(HttpServletRequest request) throws Exception;

  /**
   * Parses the parts of the specified multipart request into a collection of javax.activation.DataHandler.
   *
   * @param request The (already {@link #handleMultipartRequest(javax.servlet.http.HttpServletRequest) handled) request.
   * @return The parsed parts.
   */
  Collection<DataHandler> parseParts(HttpServletRequest request) throws Exception;

}