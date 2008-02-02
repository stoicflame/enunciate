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

import javax.activation.DataHandler;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.Marshaller;
import javax.xml.bind.JAXBException;
import java.util.Map;

/**
 * Spring view for a data handler.
 *
 * @author Ryan Heaton
 */
public class DataHandlerView extends RESTResultView<DataHandler> {

  public DataHandlerView(RESTOperation operation, DataHandler dataHandler, Map<String, String> ns2prefix) {
    super(operation, dataHandler, ns2prefix);
  }

  @Override
  protected void marshal(Marshaller marshaller, HttpServletRequest request, HttpServletResponse response) throws Exception {
    if (getResult() != null) {
      getResult().writeTo(response.getOutputStream());
    }
  }

  @Override
  protected String getContentType() {
    return getResult() != null && getResult().getContentType() != null ? getResult().getContentType() : "application/octet-stream";
  }

  @Override
  protected Marshaller getMarshaller() throws JAXBException {
    return null;
  }
}
