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

package org.codehaus.enunciate.modules.xfire_client.annotations;

import org.codehaus.xfire.annotations.WebServiceAnnotation;

import java.io.IOException;
import java.io.Serializable;

/**
 * @author Ryan Heaton
 */
public class SerializableWebServiceAnnotation extends WebServiceAnnotation implements Serializable {

  private void writeObject(java.io.ObjectOutputStream out) throws IOException {
    out.writeObject(getEndpointInterface());
    out.writeObject(getName());
    out.writeObject(getPortName());
    out.writeObject(getServiceName());
    out.writeObject(getTargetNamespace());
    out.writeObject(getWsdlLocation());
  }

  private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
    setEndpointInterface((String) in.readObject());
    setName((String) in.readObject());
    setPortName((String) in.readObject());
    setServiceName((String) in.readObject());
    setTargetNamespace((String) in.readObject());
    setWsdlLocation((String) in.readObject());
  }

}
