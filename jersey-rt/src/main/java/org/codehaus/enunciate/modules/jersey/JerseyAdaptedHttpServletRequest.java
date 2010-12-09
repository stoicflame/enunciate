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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.ws.rs.core.MediaType;
import java.util.*;

/**
 * @author Ryan Heaton
 */
public class JerseyAdaptedHttpServletRequest extends HttpServletRequestWrapper {

  public static final String PROPERTY_SERVLET_PATH = "org.codehaus.enunciate.modules.jersey.config.ServletPath";
  public static final String PROPERTY_RESOURCE_PROVIDER_FACTORY = "org.codehaus.enunciate.modules.jersey.config.ResourceProviderFactory";
  public static final String FEATURE_PATH_BASED_CONNEG = "org.codehaus.enunciate.modules.jersey.config.PathBasedConneg";

  private final MediaType mediaType;

  /**
   * Create a request adapted for Jersey.
   *
   * @param request The request.
   * @param mediaType The specific media type requested.
   */
  public JerseyAdaptedHttpServletRequest(HttpServletRequest request, MediaType mediaType) {
    super(request);
    this.mediaType = mediaType;
  }

  /**
   * If the media type is specified we override the "Accept" header.
   *
   * @param headerName The header name.
   * @return The header value.
   */
  @Override
  public String getHeader(String headerName) {
    String header = super.getHeader(headerName);

    //intercept the "Accept" header if the media type is specified on the path.
    if (mediaType != null && "Accept".equalsIgnoreCase(headerName)) {
      header = mediaType.toString();
    }
    return header;
  }

  /**
   * If the media type is specified we override the "Accept" header.
   *
   * @param headerName The header name.
   * @return The header value.
   */
  @Override
  public Enumeration getHeaders(String headerName) {
    Enumeration headers = super.getHeaders(headerName);

    //intercept the "Accept" header if the media type is specified on the path.
    if (mediaType != null && "Accept".equalsIgnoreCase(headerName)) {
      headers = Collections.enumeration(Arrays.asList(mediaType.toString()));
    }
    return headers;
  }

  /**
   * If the media type is specified we ensure the "Accept" header is supplied.
   *
   * @return The header names.
   */
  @Override
  public Enumeration getHeaderNames() {
    Enumeration headerNames = super.getHeaderNames();
    List<String> copy = new ArrayList<String>();
    if (mediaType != null) {
      copy.add("Accept");
    }

    while (headerNames.hasMoreElements()) {
      String headerName = (String) headerNames.nextElement();
      if (mediaType == null || !"Accept".equalsIgnoreCase(headerName)) {
        copy.add(headerName);
      }
    }
    return Collections.enumeration(copy);
  }
}
