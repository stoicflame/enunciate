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

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.IOException;

/**
 * REST controller that routes requests for specific content types.
 *
 * @author Ryan Heaton
 */
public class RESTContentTypeRoutingController extends AbstractController {

  private final String defaultContentType;
  private final ContentTypeSupport contentTypeSupport;
  private Pattern replacePattern = Pattern.compile("^/?rest/");
  private String contentTypeParameter = "contentType";

  public RESTContentTypeRoutingController(RESTResource resource, ContentTypeSupport contentTypeSupport) {
    this.defaultContentType = resource.getDefaultContentType();
    this.contentTypeSupport = contentTypeSupport;
    super.setSupportedMethods(new String[]{"GET", "PUT", "POST", "DELETE"});
  }

  /**
   * Redirects the request to the location of the specific content type.
   *
   * @param request The request.
   * @param response The response.
   * @return null
   */
  protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
    String requestContext = request.getRequestURI().substring(request.getContextPath().length());
    Matcher matcher = replacePattern.matcher(requestContext);
    if (matcher.find()) {
      String contentType = getContentType(request);

      String contentTypeId = null;
      if (contentType != null) {
        contentTypeId = lookupContentTypeId(contentType);
      }

      if (contentTypeId != null) {
        String redirect = matcher.replaceFirst("/" + contentTypeId + "/");
        RequestDispatcher dispatcher = request.getRequestDispatcher(redirect);
        if (dispatcher != null) {
          try {
            dispatcher.forward(request, response);
          }
          catch (ServletException e) {
            if (e.getRootCause() instanceof Exception) {
              throw (Exception) e.getRootCause();
            }
            else {
              throw e;
            }
          }
          return null;
        }
      }
    }

    response.sendError(HttpServletResponse.SC_NOT_FOUND);
    return null;

  }

  /**
   * Lookup the content type id.
   *
   * @param contentType The content type.
   * @return The content type id.
   */
  protected String lookupContentTypeId(String contentType) {
    return this.contentTypeSupport.lookupIdByContentType(contentType);
  }

  /**
   * Get the content type for the specified request.
   *
   * @param request The request.
   * @return The content type.
   */
  protected String getContentType(HttpServletRequest request) {
    String contentType = request.getParameter(getContentTypeParameter());
    if (contentType == null) {
      contentType = this.defaultContentType;
    }
    return contentType;
  }

  /**
   * The pattern of the URL to replace with the content type id.
   *
   * @return The pattern of the URL to replace with the content type id.
   */
  public Pattern getReplacePattern() {
    return replacePattern;
  }

  /**
   * The pattern of the URL to replace with the content type id.
   *
   * @param replacePattern The pattern of the URL to replace with the content type id.
   */
  public void setReplacePattern(Pattern replacePattern) {
    this.replacePattern = replacePattern;
  }

  /**
   * The subcontext at which this controller is mounted.
   *
   * @param subcontext The subcontext at which this controller is mounted.
   */
  public void setSubcontext(String subcontext) {
    if (subcontext.charAt(0) == '/') {
      subcontext = subcontext.substring(1);
    }

    if (!subcontext.endsWith("/")) {
      subcontext = subcontext + "/";
    }

    setReplacePattern(Pattern.compile("^/?" + subcontext));
  }

  /**
   * The name of the parameter that specifies the content type.
   *
   * @return The name of the parameter that specifies the content type.
   */
  public String getContentTypeParameter() {
    return contentTypeParameter;
  }

  /**
   * The name of the parameter that specifies the content type.
   *
   * @param contentTypeParameter The name of the parameter that specifies the content type.
   */
  public void setContentTypeParameter(String contentTypeParameter) {
    this.contentTypeParameter = contentTypeParameter;
  }
}
