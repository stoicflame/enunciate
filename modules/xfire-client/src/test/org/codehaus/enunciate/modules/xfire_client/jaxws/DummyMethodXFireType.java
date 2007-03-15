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

package org.codehaus.enunciate.modules.xfire_client.jaxws;

import org.codehaus.xfire.aegis.type.Type;
import org.codehaus.xfire.aegis.MessageReader;
import org.codehaus.xfire.aegis.MessageWriter;
import org.codehaus.xfire.MessageContext;
import org.codehaus.xfire.fault.XFireFault;

import javax.xml.namespace.QName;

/**
 * @author Ryan Heaton
 */
public class DummyMethodXFireType extends Type {

  public Object readObject(MessageReader reader, MessageContext context) throws XFireFault {
    return null;
  }

  public void writeObject(Object object, MessageWriter writer, MessageContext context) throws XFireFault {
  }

  @Override
  public Class getTypeClass() {
    return DummyMethod.class;
  }

  @Override
  public QName getSchemaType() {
    return new QName("urn:doesntmatter", "anything");
  }
}
