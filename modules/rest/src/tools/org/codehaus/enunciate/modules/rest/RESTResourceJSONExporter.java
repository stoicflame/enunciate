/*
 * Copyright 2006 Web Cohesion
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

import org.codehaus.enunciate.rest.annotations.VerbType;
import org.springframework.beans.BeansException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A controller for the JSON API.
 *
 * @author Ryan Heaton
 */
public class RESTResourceJSONExporter extends RESTResourceXMLExporter {

  private boolean enabled = false;

  public RESTResourceJSONExporter(RESTResource resource) {
    super(resource);
  }

  protected void initApplicationContext() throws BeansException {
    super.initApplicationContext();

    try {
      Class.forName("org.codehaus.jettison.mapped.MappedXMLOutputFactory", true, RESTResourceJSONExporter.class.getClassLoader());
      enabled = true;
    }
    catch (ClassNotFoundException e) {
      enabled = false;
    }
  }

  @Override
  protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
    if (enabled) {
      return super.handleRequestInternal(request, response);
    }
    else {
      response.sendError(HttpServletResponse.SC_NOT_FOUND, request.getRequestURI());
      return null;
    }
  }

  @Override
  protected boolean isOperationAllowed(RESTOperation operation) {
    return super.isOperationAllowed(operation) && operation.getVerb() == VerbType.read;
  }

  @Override
  protected View createView(RESTOperation operation, Object result) {
    return new JSONResultView(operation, result, getNamespaces2Prefixes());
  }

}
