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

package org.codehaus.enunciate.modules.amf;

import javax.activation.DataHandler;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author Ryan Heaton
 */
public class DataHandlerAMFMapper implements AMFMapper<DataHandler, byte[]> {

  public byte[] toAMF(DataHandler jaxbObject, AMFMappingContext context) throws AMFMappingException {
    if (jaxbObject == null) {
      return null;
    }

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try {
      jaxbObject.writeTo(out);
      out.flush();
    }
    catch (IOException e) {
      throw new AMFMappingException(e);
    }

    return out.toByteArray();
  }

  public DataHandler toJAXB(byte[] amfObject, AMFMappingContext context) throws AMFMappingException {
    if (amfObject == null) {
      return null;
    }

    throw new AMFMappingException("Can't convert from a byte[] to a data handler.");
  }
}
