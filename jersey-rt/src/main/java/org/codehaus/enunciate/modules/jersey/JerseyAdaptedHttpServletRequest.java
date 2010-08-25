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

import com.sun.jersey.api.core.ResourceConfig;

import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.util.*;

/**
 * @author Ryan Heaton
 */
public class JerseyAdaptedHttpServletRequest extends HttpServletRequestWrapper {

  public static final String PROPERTY_SERVLET_PATH = "org.codehaus.enunciate.modules.jersey.config.ServletPath";
  public static final String PROPERTY_RESOURCE_PROVIDER_FACTORY = "org.codehaus.enunciate.modules.jersey.config.ResourceProviderFactory";
  public static final String FEATURE_PATH_BASED_CONNEG = "org.codehaus.enunciate.modules.jersey.config.PathBasedConneg";

  private final String rawpath;
  private final String servletPath;
  private final String mediaKey;
  private final MediaType mediaType;

  /**
   * Create a request adapted for Jersey.
   *
   * @param request The request.
   * @param resourceConfig The jersey resource configuration.
   */
  public JerseyAdaptedHttpServletRequest(HttpServletRequest request, ResourceConfig resourceConfig) {
    super(request);

    String mediaKey = null;
    MediaType mediaType = null;
    this.rawpath = request.getRequestURI();
    if (resourceConfig != null && resourceConfig.getFeature(FEATURE_PATH_BASED_CONNEG)) {
      String path = rawpath;
      //if we're doing path-based conneg, we're going to look for the media-type mapping key on the URL after the context path.
      if (path.startsWith(request.getContextPath())) {
        path = path.substring(request.getContextPath().length());
      }

      if (path.startsWith("/")) {
        path = path.substring(1);
      }

      for (Map.Entry<String, MediaType> mediaMapping : resourceConfig.getMediaTypeMappings().entrySet()) {
        if (path.startsWith(mediaMapping.getKey())) {
          mediaKey = mediaMapping.getKey();
          mediaType = mediaMapping.getValue();
          break;
        }
      }
    }

    String servletPath = null;
    if (resourceConfig != null && resourceConfig.getProperty(PROPERTY_SERVLET_PATH) != null) {
      servletPath = (String) resourceConfig.getProperty(PROPERTY_SERVLET_PATH);
    }

    this.servletPath = servletPath;
    this.mediaKey = mediaKey;
    this.mediaType = mediaType;
  }

  @Override
  public String getPathInfo() {
    return this.rawpath.substring(getContextPath().length() + getServletPath().length());
  }

  /**
   * The servlet path is based on the media key if we're doing path-based conneg.
   * 
   * @return The servlet path is the media key, or null.
   */
  @Override
  public String getServletPath() {
    if (mediaKey != null && !"".equals(mediaKey)) {
      return ('/' + mediaKey);
    }
    else if (servletPath != null) {
      return servletPath;
    }
    else {
      return "";
    }
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
