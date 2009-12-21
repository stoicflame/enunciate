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

package org.codehaus.enunciate.webapp;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Servlet filter that redirects all requests with ?wsdl appended to a specified location.
 *
 * @author Ryan Heaton
 */
public class WSDLRedirectFilter implements Filter {

  public static final String WSDL_LOCATION_PARAM = "wsdl-location";

  private String wsdlLocation = null;

  public void init(FilterConfig filterConfig) throws ServletException {
    this.wsdlLocation = filterConfig.getInitParameter(WSDL_LOCATION_PARAM);
  }

  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
    if (this.wsdlLocation != null && servletRequest.getParameterMap().containsKey("wsdl")) {
      HttpServletResponse res = (HttpServletResponse) servletResponse;
      res.sendRedirect(((HttpServletRequest)servletRequest).getContextPath() + this.wsdlLocation);
    }
    else {
      chain.doFilter(servletRequest, servletResponse);
    }
  }

  public void destroy() {
  }

  public String getWsdlLocation() {
    return wsdlLocation;
  }

  public void setWsdlLocation(String wsdlLocation) {
    this.wsdlLocation = wsdlLocation;
  }
}