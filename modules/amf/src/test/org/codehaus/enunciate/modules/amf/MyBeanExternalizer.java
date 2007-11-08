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

import org.granite.messaging.amf.io.util.externalizer.Externalizer;
import org.granite.messaging.amf.io.util.Converter;
import org.granite.context.GraniteContext;

import javax.xml.namespace.QName;
import javax.activation.DataHandler;
import java.io.ObjectInput;
import java.io.IOException;
import java.io.ObjectOutput;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Field;
import java.util.*;
import java.net.URI;

/**
 * @author Ryan Heaton
 */
public class MyBeanExternalizer implements Externalizer {

  public Object newInstance(String classname, ObjectInput input) throws IOException, ClassNotFoundException, InstantiationException, InvocationTargetException, IllegalAccessException {
    if (!MyBean.class.getName().equals(classname)) {
      throw new InstantiationException("Illegal class name: " + classname);
    }

    return new MyBean();
  }

  public void readExternal(Object object, ObjectInput input) throws IOException, ClassNotFoundException, IllegalAccessException {

    Converter converter = GraniteContext.getCurrentInstance().getGraniteConfig().getConverter();
    ((MyBean)object).setProperty1((String) converter.convertForDeserialization(input.readObject(), String.class));
    ((MyBean)object).setProperty2((Float) converter.convertForDeserialization(input.readObject(), Float.class));
    ((MyBean)object).setProperty3((Integer) converter.convertForDeserialization(input.readObject(), Integer.class));
    ((MyBean)object).setProperty4((Boolean) converter.convertForDeserialization(input.readObject(), Boolean.class));
    ((MyBean)object).setProperty5((ArrayList) converter.convertForDeserialization(input.readObject(), ArrayList.class));
    ((MyBean)object).setProperty6((HashMap) converter.convertForDeserialization(input.readObject(), HashMap.class));

    ((MyBean)object).setBytes((byte[]) converter.convertForDeserialization(input.readObject(), byte[].class));
    ((MyBean)object).setDataHandler((DataHandler)converter.convertForDeserialization(input.readObject(), DataHandler.class));
    ((MyBean)object).setObject(converter.convertForDeserialization(input.readObject(), Object.class));
    ((MyBean)object).setUri((URI)converter.convertForDeserialization(input.readObject(), URI.class));
    ((MyBean)object).setUuid((UUID)converter.convertForDeserialization(input.readObject(), UUID.class));
    ((MyBean)object).setMyEnum((MyEnum)converter.convertForDeserialization(input.readObject(), MyEnum.class));
  }

  public void writeExternal(Object object, ObjectOutput output) throws IOException, IllegalAccessException {
    output.writeObject(((MyBean)object).getProperty1());
    output.writeObject(((MyBean)object).getProperty2());
    output.writeObject(((MyBean)object).getProperty3());
    output.writeObject(((MyBean)object).getProperty4());
    output.writeObject(((MyBean)object).getProperty5());
    output.writeObject(((MyBean)object).getProperty6());

    output.writeObject(((MyBean)object).getBytes());
    output.writeObject(((MyBean)object).getDataHandler());
    output.writeObject(((MyBean)object).getObject());
    output.writeObject(((MyBean)object).getUri());
    output.writeObject(((MyBean)object).getUuid());
    output.writeObject(((MyBean)object).getMyEnum());
  }

  public List<Field> findOrderedFields(Class<?> aClass) {
    throw new UnsupportedOperationException();
  }
}
