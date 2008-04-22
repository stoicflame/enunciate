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
 * View of a REST operation (that returns a  result).
 *
 * @author Ryan Heaton
 */
public class RESTResourceView implements View {

  public static final String MODEL_RESULT = "result";

  protected final RESTOperation operation;
  protected final RESTRequestContentTypeHandler handler;
  protected final String contentType;

  public RESTResourceView(RESTOperation operation, RESTRequestContentTypeHandler handler, String contentType) {
    this.operation = operation;
    this.handler = handler;
    this.contentType = contentType;
  }

  public void render(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {
    this.handler.write(model.get(MODEL_RESULT),
                       new RESTRequest(request, this.operation, this.contentType),
                       response);
  }
}