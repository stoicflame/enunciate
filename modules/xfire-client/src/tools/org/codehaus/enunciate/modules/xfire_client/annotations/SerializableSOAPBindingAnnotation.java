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

package org.codehaus.enunciate.modules.xfire_client.annotations;

import org.codehaus.xfire.annotations.soap.SOAPBindingAnnotation;

import java.io.IOException;
import java.io.Serializable;

/**
 * @author Ryan Heaton
 */
public class SerializableSOAPBindingAnnotation extends SOAPBindingAnnotation implements Serializable {

  private void writeObject(java.io.ObjectOutputStream out) throws IOException {
    out.writeInt(getParameterStyle());
    out.writeInt(getStyle());
    out.writeInt(getUse());
  }

  private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
    setParameterStyle(in.readInt());
    setStyle(in.readInt());
    setUse(in.readInt());
  }

}
