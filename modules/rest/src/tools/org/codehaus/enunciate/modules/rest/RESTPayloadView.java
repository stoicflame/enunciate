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

import org.codehaus.enunciate.rest.annotations.ContentType;

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
public class RESTPayloadView extends RESTOperationView {

  public RESTPayloadView(RESTOperation operation) {
    super(operation);
  }

  @Override
  protected void renderResult(Object result, HttpServletRequest request, HttpServletResponse response) throws Exception {
    if (result != null) {
      Object payloadBody;
      try {
        payloadBody = getOperation().getPayloadDeliveryMethod().invoke(result);
      }
      catch (Exception e) {
        throw new RuntimeException(e);
      }

      if (getOperation().getPayloadHeadersMethod() != null) {
        Map headers = (Map) getOperation().getPayloadHeadersMethod().invoke(result);
        if (headers != null) {
          for (Object header : headers.keySet()) {
            response.setHeader(String.valueOf(header), String.valueOf(headers.get(header)));
          }
        }
      }

      if (payloadBody != null) {
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

        if (bodyStream != null) {
          marshalPayloadStream(bodyStream, request, response, isXML(result));
        }
      }
    }
  }

  /**
   * Whether the result is XML.
   *
   * @param result Whether the result is XML.
   * @return Whether the result is XML.
   */
  protected boolean isXML(Object result) {
    return true;
  }

  /**
   * Marshals the payload stream to the servlet output stream.
   *
   * @param payloadStream The payload stream
   * @param request       The request.
   * @param response      The response.
   * @param xml           Whether the payload is XML.
   */
  protected void marshalPayloadStream(InputStream payloadStream, HttpServletRequest request, HttpServletResponse response, boolean xml) throws Exception {
    ServletOutputStream out = response.getOutputStream();
    byte[] buffer = new byte[1024 * 2]; //2 kb buffer should suffice.
    int len;
    while ((len = payloadStream.read(buffer)) > 0) {
      out.write(buffer, 0, len);
    }
  }

  @Override
  protected String getContentType(Object result) {
    String contentType = null;
    if (getOperation().getPayloadContentTypeMethod() != null) {
      try {
        contentType = (String) getOperation().getPayloadContentTypeMethod().invoke(result);
      }
      catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    else {
      Object payloadBody;
      try {
        payloadBody = getOperation().getPayloadDeliveryMethod().invoke(result);
      }
      catch (Exception e) {
        throw new RuntimeException(e);
      }

      if (payloadBody instanceof DataHandler) {
        contentType = ((DataHandler) payloadBody).getContentType();
      }
    }

    if (contentType == null) {
      contentType = getOperation().getMethod().isAnnotationPresent(ContentType.class) ? getOperation().getMethod().getAnnotation(ContentType.class).value() : "application/octet-stream";
    }

    return contentType;
  }
}
