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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;
import javax.xml.bind.Marshaller;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Map;

/**
 * Spring view for a REST payload.
 *
 * @author Ryan Heaton
 */
public class RESTPayloadView extends RESTResultView {

  private final Object payloadBody;

  public RESTPayloadView(RESTOperation operation, Object payload, Map<String, String> ns2prefix) {
    super(operation, payload, ns2prefix);
    try {
      this.payloadBody = operation.getPayloadBodyMethod().invoke(payload);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void marshal(Marshaller marshaller, HttpServletRequest request, HttpServletResponse response) throws Exception {
    if (getOperation().getPayloadHeadersMethod() != null) {
      Map headers = (Map) getOperation().getPayloadHeadersMethod().invoke(getResult());
      if (headers != null) {
        for (Object header : headers.keySet()) {
          response.setHeader(String.valueOf(header), String.valueOf(headers.get(header)));
        }
      }
    }

    if (this.payloadBody != null) {
      InputStream bodyStream;
      if (this.payloadBody instanceof InputStream) {
        bodyStream = (InputStream) this.payloadBody;
      }
      else if (this.payloadBody instanceof DataHandler) {
        bodyStream = ((DataHandler) this.payloadBody).getInputStream();
      }
      else if (this.payloadBody instanceof byte[]) {
        bodyStream = new ByteArrayInputStream((byte[]) this.payloadBody);
      }
      else {
        throw new IllegalStateException("A payload body must be of type byte[], javax.activation.DataHandler, or java.io.InputStream.");
      }

      if (bodyStream != null) {
        marshalPayloadStream(bodyStream, request, response);
      }
    }
  }

  /**
   * Marshals the payload stream to the servlet output stream.
   *
   * @param payloadStream The payload stream
   * @param request The request.
   * @param response The response.
   */
  protected void marshalPayloadStream(InputStream payloadStream, HttpServletRequest request, HttpServletResponse response) throws IOException, XMLStreamException {
    ServletOutputStream out = response.getOutputStream();
    byte[] buffer = new byte[1024 * 2]; //2 kb buffer should suffice.
    int len;
    while ((len = payloadStream.read(buffer)) > 0) {
      out.write(buffer, 0, len);
    }
  }

  @Override
  protected Marshaller getMarshaller() throws JAXBException {
    return null;
  }

  @Override
  protected String getContentType() {
    String contentType = null;
    if (getOperation().getPayloadContentTypeMethod() != null) {
      try {
        contentType = (String) getOperation().getPayloadContentTypeMethod().invoke(getResult());
      }
      catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    else if (this.payloadBody instanceof DataHandler) {
      contentType = ((DataHandler) payloadBody).getContentType();
    }

    if (contentType == null) {
      contentType = getOperation().getMethod().isAnnotationPresent(ContentType.class) ? getOperation().getMethod().getAnnotation(ContentType.class).value() : "application/octet-stream";;
    }

    return contentType;
  }
}
