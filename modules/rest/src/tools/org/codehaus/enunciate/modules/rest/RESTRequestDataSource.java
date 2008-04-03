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

import javax.activation.DataSource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
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
