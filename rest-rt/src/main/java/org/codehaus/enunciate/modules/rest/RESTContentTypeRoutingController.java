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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.*;

/**
 * REST controller that routes requests for specific content types.
 *
 * @author Ryan Heaton
 */
public class RESTContentTypeRoutingController extends AbstractController {

  private static final Log LOG = LogFactory.getLog(RESTContentTypeRoutingController.class);

  private final MimeType defaultMimeType;
  private ContentTypeSupport contentTypeSupport;
  private Pattern replacePattern = Pattern.compile("^/?rest/");
  private String contentTypeParameter = "contentType";

  public RESTContentTypeRoutingController(RESTResource resource) {
    this.defaultMimeType = MimeType.parse(resource.getDefaultContentType());
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
    if (getContentTypeSupport() != null) {
      String requestContext = request.getRequestURI().substring(request.getContextPath().length());
      Matcher matcher = replacePattern.matcher(requestContext);
      if (matcher.find()) {
        List<String> contentTypes = getContentTypesByPreference(request);

        for (String contentType : contentTypes) {
          String contentTypeId = lookupContentTypeId(contentType);
          if (contentTypeId != null) {
            String redirect = matcher.replaceFirst("/" + URLEncoder.encode(contentTypeId, "UTF-8") + "/");
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
  protected List<String> getContentTypesByPreference(HttpServletRequest request) {
    String contentTypeParam = request.getParameter(getContentTypeParameter());
    if (contentTypeParam != null) {
      return Arrays.asList(contentTypeParam);
    }
    else {
      Set<MimeType> mimeTypes = new TreeSet<MimeType>();
      Enumeration acceptHeaders = request.getHeaders("Accept");
      if (acceptHeaders != null) {
        Float defaultQuality = null;
        while (acceptHeaders.hasMoreElements()) {
          String acceptHeader = (String) acceptHeaders.nextElement();
          for (StringTokenizer acceptTokens = new StringTokenizer(acceptHeader, ","); acceptTokens.hasMoreTokens();) {
            String token = acceptTokens.nextToken();
            try {
              MimeType acceptType = MimeType.parse(token.trim());
              mimeTypes.add(acceptType);
              if (acceptType.isAcceptable(this.defaultMimeType) && (defaultQuality == null || defaultQuality < acceptType.getQuality())) {
                defaultQuality = acceptType.getQuality();
              }
            }
            catch (Exception e) {
              //ignore the invalid type in the "Accept" header
              LOG.info(e.getMessage());
            }
          }
        }

        if (defaultQuality != null) {
          mimeTypes.add(new MimeType(defaultMimeType.getType(), defaultMimeType.getSubtype(), defaultQuality));
        }
      }
      else {
        //add the default content types at the end.
        mimeTypes.add(this.defaultMimeType);
      }


      ArrayList<String> values = new ArrayList<String>();
      for (MimeType mimeType : mimeTypes) {
        values.add(mimeType.toString());
      }
      return values;
    }
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

  /**
   * The content type support.
   *
   * @return The content type support.
   */
  public ContentTypeSupport getContentTypeSupport() {
    return contentTypeSupport;
  }

  /**
   * Set the content type support.
   *
   * @param support the content type support.
   */
  @Autowired
  public void setContentTypeSupport(ContentTypeSupport support) {
    this.contentTypeSupport = support;
  }

}
