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

import org.springframework.web.servlet.View;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.Marshaller;
import javax.xml.bind.JAXBException;
import java.util.Map;
import java.io.IOException;

/**
 * A view for the result of a REST operation.
 *
 * @author Ryan Heaton
 */
public class RESTResultView implements View {

  private final RESTOperation operation;
  private final Object result;

  /**
   * Construct a view for the result of a REST operation.
   *
   * @param operation The operation.
   * @param result The result.
   */
  public RESTResultView(RESTOperation operation, Object result) {
    this.operation = operation;
    this.result = result;
  }

  /**
   * The operation used to render this view.
   *
   * @return The operation used to render this view.
   */
  public RESTOperation getOperation() {
    return operation;
  }

  /**
   * The result of invoking the operation.
   *
   * @return The result of invoking the operation.
   */
  public Object getResult() {
    return result;
  }

  /**
   * Renders the XML view of the result.
   *
   * @param map The model.
   * @param request The request.
   * @param response The response.
   * @throws Exception If a problem occurred during serialization.
   */
  public void render(Map map, HttpServletRequest request, HttpServletResponse response) throws Exception {
    response.setStatus(HttpServletResponse.SC_OK);
    if (result != null) {
      response.setContentType("text/xml");
      Marshaller marshaller = operation.getSerializationContext().createMarshaller();
      marshaller.setAttachmentMarshaller(RESTAttachmentMarshaller.INSTANCE);
      marshal(marshaller, request, response);
    }
  }

  /**
   * Does the marshalling operation.
   *
   * @param marshaller The marshaller to use.
   * @param response The response.
   */
  protected void marshal(Marshaller marshaller, HttpServletRequest request, HttpServletResponse response) throws Exception {
    marshaller.marshal(getResult(), response.getOutputStream());
  }
}
