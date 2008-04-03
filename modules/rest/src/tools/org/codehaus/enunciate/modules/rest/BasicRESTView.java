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

import org.springframework.web.servlet.View;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Basic view for REST responses. Renders only the HTTP status code and HTTP status message.
 *
 * @author Ryan Heaton
 */
public class BasicRESTView implements View {

  public static final String MODEL_STATUS = "status";
  public static final String MODEL_STATUS_MESSAGE = "status_message";
  public static final String MODEL_CONTENT_TYPE = "content-type";

  /**
   * Renders only the HTTP status code and HTTP status message.
   *
   * @param model The model.
   * @param request The request.
   * @param response The response.
   */
  public void render(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {
    Integer status = HttpServletResponse.SC_OK;
    if (model.get(MODEL_STATUS) != null) {
      status = (Integer) model.get(MODEL_STATUS);
    }

    String statusMessage = null;
    if (model.get(MODEL_STATUS_MESSAGE) != null) {
      statusMessage = model.get(MODEL_STATUS_MESSAGE).toString();
    }

    if (statusMessage == null) {
      response.setStatus(status);
    }
    else {
      response.setStatus(status, statusMessage);
    }

    String contentType = getContentType(model);
    if (contentType != null) {
      response.setContentType(contentType);
    }

    renderBody(model, request, response);
  }

  /**
   * Gets the content type (MIME type) for this REST view.
   *
   * @param model The model.
   * @return The content type, or null if no content type is to be specified.
   */
  protected String getContentType(Map model) {
    String contentType = null;

    if (model.get(MODEL_CONTENT_TYPE) != null) {
      contentType = model.get(MODEL_CONTENT_TYPE).toString();
    }

    return contentType;
  }

  /**
   * Render the body of the response. Nothing is rendered for the basic REST view.
   *
   * @param model The model.
   * @param request The request.
   * @param response The response.
   */
  protected void renderBody(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {
  }
}
