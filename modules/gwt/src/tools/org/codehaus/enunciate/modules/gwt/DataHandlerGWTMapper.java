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

package org.codehaus.enunciate.modules.gwt;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import java.io.*;

/**
 * @author Ryan Heaton
 */
public class DataHandlerGWTMapper implements CustomGWTMapper<DataHandler, byte[]> {

  public byte[] toGWT(DataHandler jaxbObject, GWTMappingContext context) throws GWTMappingException {
    if (jaxbObject == null) {
      return null;
    }

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try {
      jaxbObject.writeTo(out);
      out.flush();
    }
    catch (IOException e) {
      throw new GWTMappingException(e);
    }

    return out.toByteArray();
  }

  public DataHandler toJAXB(final byte[] gwtObject, GWTMappingContext context) throws GWTMappingException {
    if (gwtObject == null) {
      return null;
    }

    return new DataHandler(new DataSource() {
      public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(gwtObject);
      }

      public OutputStream getOutputStream() throws IOException {
        throw new IOException();
      }

      public String getContentType() {
        return "application/octet-stream";
      }

      public String getName() {
        return "";
      }
    });
  }

  public Class<? extends DataHandler> getJaxbClass() {
    return DataHandler.class;
  }

  public Class<? extends byte[]> getGwtClass() {
    return byte[].class;
  }
}
