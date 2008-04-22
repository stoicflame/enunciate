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

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.RequestDispatcher;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * REST controller that routes requests for specific content types.
 *
 * @author Ryan Heaton
 */
public class RESTContentTypeRoutingController extends AbstractController {

  private final String defaultContentType;
  private final Map<String, String> contentTypesToIds;
  private Pattern replacePattern = Pattern.compile("/rest");
  private String contentTypeParameter = "contentType";

  public RESTContentTypeRoutingController(RESTResource resource, Map<String, String> contentTypesToIds) {
    this.contentTypesToIds = contentTypesToIds;
    this.defaultContentType = resource.getDefaultContentType();
  }

  protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
    String requestContext = request.getRequestURI().substring(request.getContextPath().length());
    Matcher matcher = replacePattern.matcher(requestContext);
    if (matcher.find()) {
      String contentType = request.getParameter(getContentTypeParameter());
      if (contentType == null) {
        contentType = this.defaultContentType;
      }

      String contentTypeId = null;
      if (contentType != null) {
        contentTypeId = this.contentTypesToIds.get(contentType);
      }

      if (contentTypeId != null) {
        String redirect = matcher.replaceFirst("/" + contentTypeId);
        RequestDispatcher dispatcher = request.getRequestDispatcher(redirect);
        if (dispatcher != null) {
          dispatcher.forward(request, response);
          return null;
        }
      }
    }

    response.sendError(HttpServletResponse.SC_NOT_FOUND);
    return null;

  }

  public Pattern getReplacePattern() {
    return replacePattern;
  }

  public void setReplacePattern(Pattern replacePattern) {
    this.replacePattern = replacePattern;
  }

  public String getContentTypeParameter() {
    return contentTypeParameter;
  }

  public void setContentTypeParameter(String contentTypeParameter) {
    this.contentTypeParameter = contentTypeParameter;
  }
}
