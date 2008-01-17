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
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

/**
 * Spring view for a REST payload.
 *
 * @author Ryan Heaton
 */
public class RESTPayloadView implements View {

  private final RESTOperation operation;
  private final Object payload;

  public RESTPayloadView(RESTOperation operation, Object payload) {
    this.operation = operation;
    this.payload = payload;
  }

  public void render(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {
    response.setStatus(HttpServletResponse.SC_OK);
    if (payload != null) {
      Object payloadBody = operation.getPayloadBodyMethod().invoke(this.payload);
      if (payloadBody != null) {
        String contentType = null;
        if (operation.getPayloadContentTypeMethod() != null) {
          contentType = (String) operation.getPayloadContentTypeMethod().invoke(payload);
        }
        else if (payloadBody instanceof DataHandler) {
          contentType = ((DataHandler) payloadBody).getContentType();
        }
        if (contentType == null) {
          contentType = "application/octet-stream";
        }
        response.setContentType(contentType);

        if (operation.getPayloadHeadersMethod() != null) {
          Map headers = (Map) operation.getPayloadHeadersMethod().invoke(this.payload);
          if (headers != null) {
            for (Object header : headers.keySet()) {
              response.setHeader(String.valueOf(header), String.valueOf(headers.get(header)));
            }
          }
        }

        InputStream bodyStream;
        if (payloadBody instanceof InputStream) {
          bodyStream = (InputStream) payloadBody;
        }
        else if (payloadBody instanceof DataHandler) {
          bodyStream = ((DataHandler) payloadBody).getInputStream();
        }
        else if (payloadBody instanceof byte[]) {
          bodyStream = new ByteArrayInputStream((byte[]) payloadBody);
        }
        else {
          throw new IllegalStateException("A payload body must be of type byte[], javax.activation.DataHandler, or java.io.InputStream.");
        }

        ServletOutputStream out = response.getOutputStream();
        byte[] buffer = new byte[1024 * 2]; //2 kb buffer should suffice.
        int len;
        while ((len = bodyStream.read(buffer)) > 0) {
          out.write(buffer, 0, len);
        }
      }
    }
    response.flushBuffer();
  }
}
