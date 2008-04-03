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

import javax.activation.DataHandler;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Spring view for a data handler.
 *
 * @author Ryan Heaton
 */
public class DataHandlerView extends RESTOperationView<DataHandler> {

  public DataHandlerView(RESTOperation operation) {
    super(operation);
  }

  @Override
  protected void renderResult(DataHandler result, HttpServletRequest request, HttpServletResponse response) throws Exception {
    if (result != null) {
      result.writeTo(response.getOutputStream());
    }
  }

  @Override
  protected String getContentType(DataHandler result) {
    return result != null && result.getContentType() != null ? result.getContentType() : "application/octet-stream";
  }

}
