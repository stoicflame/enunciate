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

package org.codehaus.enunciate.modules.xfire_client;

import org.codehaus.xfire.aegis.type.Type;
import org.codehaus.xfire.aegis.MessageReader;
import org.codehaus.xfire.aegis.MessageWriter;
import org.codehaus.xfire.MessageContext;
import org.codehaus.xfire.fault.XFireFault;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

/**
 * An xfire type that reads/write a map.
 *
 * @author Ryan Heaton
 */
public class MapType extends Type {

  private final Type keyType;
  private final Type valueType;

  public MapType(Type keyType, Type valueType) {
    this.keyType = keyType;
    this.valueType = valueType;

    if (keyType == null) {
      throw new IllegalArgumentException("A map type must be supplied a key type.");
    }
    if (valueType == null) {
      throw new IllegalArgumentException("A map type must be supplied a value type.");
    }
  }

  public Object readObject(MessageReader reader, MessageContext context) throws XFireFault {
    Map map = new HashMap();
    while (reader.hasMoreElementReaders()) {
      MessageReader entryReader = reader.getNextElementReader();
      Object key = keyType.readObject(entryReader.getNextElementReader(), context);
      Object value = valueType.readObject(entryReader.getNextElementReader(), context);
      entryReader.readToEnd();
      map.put(key, value);
    }
    reader.readToEnd();
    return map;
  }

  public void writeObject(Object value, MessageWriter writer, MessageContext context) throws XFireFault {
    Map map = (Map) value;
    Iterator it = map.entrySet().iterator();
    while (it.hasNext()) {
      MessageWriter entryWriter = writer.getElementWriter("entry");
      Map.Entry entry = (Map.Entry) it.next();
      
      MessageWriter keyWriter = entryWriter.getElementWriter("key");
      this.keyType.writeObject(entry.getKey(), keyWriter, context);
      keyWriter.close();

      MessageWriter valueWriter = entryWriter.getElementWriter("value");
      this.valueType.writeObject(entry.getValue(), valueWriter, context);
      valueWriter.close();
      entryWriter.close();
    }
  }
}
