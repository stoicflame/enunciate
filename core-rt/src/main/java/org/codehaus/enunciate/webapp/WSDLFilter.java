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
import javax.servlet.http.HttpServletRequest;
import java.io.*;

/**
 * @author Ryan Heaton
 */
public class WSDLFilter implements Filter {

  public static final String ASSUMED_BASE_ADDRESS_PARAM = "assumed-base-address";
  public static final String MATCH_PREFIX_PARAM = "match-prefix";
  public static final String MATCH_SUFFIX_PARAM = "match-suffix";

  private String matchPrefix = ":address location=\"";
  private String matchSuffix = "";
  private String assumedBaseAddress = null;
  private ServletContext servletContext = null;

  public void init(FilterConfig filterConfig) throws ServletException {
    this.assumedBaseAddress = filterConfig.getInitParameter(ASSUMED_BASE_ADDRESS_PARAM);
    if (this.assumedBaseAddress != null) {
      while (this.assumedBaseAddress.endsWith("/")) {
        this.assumedBaseAddress = this.assumedBaseAddress.substring(0, this.assumedBaseAddress.length() - 1);
      }
    }

    String matchPrefix = filterConfig.getInitParameter(MATCH_PREFIX_PARAM);
    if (matchPrefix != null) {
      this.matchPrefix = matchPrefix;
    }

    String matchSuffix = filterConfig.getInitParameter(MATCH_SUFFIX_PARAM);
    if (matchSuffix != null) {
      this.matchSuffix = matchSuffix;
    }

    this.servletContext = filterConfig.getServletContext();
  }

  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
    if (this.assumedBaseAddress != null) {
      HttpServletRequest request = (HttpServletRequest) servletRequest;
      String requestURI = request.getRequestURI();
      String contextPath = request.getContextPath();
      if (requestURI.contains(contextPath)) {
        int splitIndex = requestURI.indexOf(contextPath) + contextPath.length();
        String fullContextPath = requestURI.substring(0, splitIndex);
        String postContextPath = requestURI.substring(splitIndex);
        InputStream wsdlStream = getServletContext().getResourceAsStream(postContextPath);
        if (wsdlStream != null) {
          StringBuffer match = new StringBuffer(matchPrefix).append(assumedBaseAddress).append(matchSuffix);
          StringBuffer replacement = new StringBuffer(matchPrefix).append(fullContextPath).append(matchSuffix);
          BufferedReader reader = new BufferedReader(new InputStreamReader(wsdlStream, "utf-8"));
          String line = reader.readLine();
          servletResponse.setContentType("application/xml");
          PrintWriter out = servletResponse.getWriter();
          while (line != null) {
            out.println(line.replace(match, replacement));
            line = reader.readLine();
          }
          out.flush();
          out.close();
          return;
        }
      }
    }

    chain.doFilter(servletRequest, servletResponse);
  }

  public void destroy() {
  }

  public String getMatchPrefix() {
    return matchPrefix;
  }

  public void setMatchPrefix(String matchPrefix) {
    this.matchPrefix = matchPrefix;
  }

  public String getMatchSuffix() {
    return matchSuffix;
  }

  public void setMatchSuffix(String matchSuffix) {
    this.matchSuffix = matchSuffix;
  }

  public String getAssumedBaseAddress() {
    return assumedBaseAddress;
  }

  public void setAssumedBaseAddress(String assumedBaseAddress) {
    this.assumedBaseAddress = assumedBaseAddress;
  }

  public ServletContext getServletContext() {
    return servletContext;
  }

  public void setServletContext(ServletContext servletContext) {
    this.servletContext = servletContext;
  }
}
