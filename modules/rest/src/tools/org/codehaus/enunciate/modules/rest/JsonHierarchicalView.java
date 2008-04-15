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

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * Hierarchical JSON view (using XStream).
 * 
 * @author Ryan Heaton
 */
public class JsonHierarchicalView extends RESTOperationView {

  public JsonHierarchicalView(RESTOperation operation) {
    super(operation);
  }

  @Override
  protected void renderResult(final Object result, HttpServletRequest request, final HttpServletResponse response) throws Exception {
    String callbackName = null;
    String jsonpParameter = getOperation().getJSONPParameter();
    if (jsonpParameter != null) {
      callbackName = request.getParameter(jsonpParameter);
      callbackName = ((callbackName != null) && (callbackName.trim().length() > 0)) ? callbackName : null;
    }

    new JsonPHandler(callbackName) {
      public void writeBody(PrintWriter outStream) throws Exception {    
        XStream xstream = new XStream(new JsonHierarchicalStreamDriver());
        xstream.autodetectAnnotations(true);
        xstream.toXML(result, outStream);
      }
    }.writeTo(response.getWriter());
  }

  @Override
  protected String getContentType(Object result) {
    return "application/json";
  }

}
