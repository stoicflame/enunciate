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

package org.codehaus.enunciate.modules.xfire_client;

import org.codehaus.xfire.MessageContext;
import org.codehaus.xfire.aegis.MessageReader;
import org.codehaus.xfire.fault.XFireFault;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.AbstractList;
import java.util.RandomAccess;
import java.lang.reflect.Array;
import java.io.Serializable;

/**
 * Utility for reading and writing collections and arrays as (possibly wrapped) xml elements.
 *
 * @author Ryan Heaton
 */
public class ElementsUtil {

  /**
   * Read each child element of the specified wrapper reader and issue the callback command for each.
   *
   * @param wrapperReader The wrapper reader.
   * @param context The context.
   * @param itemCallback The callback.
   */
  public static void readWrappedElements(MessageReader wrapperReader, MessageContext context, WrappedItemCallback itemCallback) throws XFireFault {
    while (wrapperReader.hasMoreElementReaders()) {
      MessageReader elementReader = wrapperReader.getNextElementReader();
      QName name = elementReader.getName();
      itemCallback.handleChildElement(name, elementReader, context);
      elementReader.readToEnd();
    }
  }

  /**
   * Converts a collection or array object to a collection.  Of course, if the object is a collection, no conversion is necessary...
   *
   * @param collectionOrArray The collection or array to convert.
   * @return The collection.
   * @throws ClassCastException If the object to be converted isn't a collection or an array.
   */
  public static Collection asCollection(Object collectionOrArray) {
    if (collectionOrArray == null) {
      return null;
    }
    else if (collectionOrArray.getClass().isArray()) {
      return new ArrayList(collectionOrArray);
    }
    else {
      return (Collection) collectionOrArray;
    }
  }

  /**
   * A simple implementation of an unmodifiable list given an object that is known to be an array.
   */
  private static class ArrayList extends AbstractList implements RandomAccess, Serializable {

    final Object array;

    public ArrayList(Object array) {
      this.array = array;
    }

    public Object get(int index) {
      return Array.get(this.array, index);
    }

    public int size() {
      return Array.getLength(this.array);
    }
  }
}
