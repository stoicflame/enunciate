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

import javax.activation.DataHandler;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Spring view for a data handler.
 *
 * @author Ryan Heaton
 */
public class DataHandlerView implements View {

  private final DataHandler dataHandler;

  public DataHandlerView(DataHandler dataHandler) {
    this.dataHandler = dataHandler;
  }

  public void render(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {
    response.setStatus(HttpServletResponse.SC_OK);
    if (dataHandler != null) {
      response.setContentType(this.dataHandler.getContentType());
      this.dataHandler.writeTo(response.getOutputStream());
    }
    response.flushBuffer();
  }
}
