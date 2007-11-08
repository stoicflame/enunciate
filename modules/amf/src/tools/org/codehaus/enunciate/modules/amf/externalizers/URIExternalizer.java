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

package org.codehaus.enunciate.modules.amf.externalizers;

import org.granite.messaging.amf.io.util.externalizer.DefaultExternalizer;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Externalizer for a QName.
 *
 * @author Ryan Heaton
 */
public class URIExternalizer extends DefaultExternalizer {

  @Override
  public Object newInstance(String type, ObjectInput in) throws IOException, ClassNotFoundException, InstantiationException, InvocationTargetException, IllegalAccessException {
    return new URIInstanciator();
  }

  @Override
  public void writeExternal(Object uri, ObjectOutput out) throws IOException, IllegalAccessException {
    out.writeObject(uri.toString());
  }

  @Override
  public List<Field> findOrderedFields(Class<?> clazz) {
    throw new UnsupportedOperationException();
  }
}
