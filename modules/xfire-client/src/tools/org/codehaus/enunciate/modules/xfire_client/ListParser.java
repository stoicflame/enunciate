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
import org.codehaus.xfire.aegis.AbstractMessageReader;
import org.codehaus.xfire.aegis.MessageReader;
import org.codehaus.xfire.aegis.type.Type;
import org.codehaus.xfire.aegis.type.TypeMapping;
import org.codehaus.xfire.fault.XFireFault;

import javax.xml.namespace.QName;
import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Parses an xml list from a value.
 *
 * @author Ryan Heaton
 */
public class ListParser extends AbstractMessageReader {

  private final Object list;
  private String value;

  /**
   * Construct a list parser that will parse a space-separated xml list into a list of the specified type.  Because a
   * component type must be known, The listType must be an array in this case.
   *
   * @param value       The space-separated list xml list.
   * @param listType    The type of list.
   * @param typeMapping The type mapping.
   * @param context     The context.
   * @throws IllegalArgumentException If the type isn't an array.
   */
  public ListParser(String value, Class listType, TypeMapping typeMapping, MessageContext context) throws XFireFault {
    this(value, listType, listType.getComponentType(), typeMapping, context);
  }

  /**
   * Construct a list parser that will parse a space-separated xml list into a list of the specified type.  Since the component
   * type is specified, the list type can be a collection.
   *
   * @param value         The space-separated list xml list.
   * @param listType      The type of list.
   * @param componentType The component type.
   * @param typeMapping   The type mapping.
   * @param context       The context.
   * @throws IllegalArgumentException If the type isn't a collection or an array.
   */
  public ListParser(String value, Class listType, Class componentType, TypeMapping typeMapping, MessageContext context) throws XFireFault {
    if ((componentType == null) && (!listType.isArray())) {
      throw new IllegalArgumentException(listType.getName() + " is not an array type.  " +
        "It can be an instance of java.util.Collection, but the component type must be specified.");
    }

    String[] tokens;
    if ((value == null) || (value.trim().length() == 0)) {
      tokens = new String[0];
    }
    else {
      tokens = value.split(" ");
    }

    Type xfireComponentType = typeMapping.getType(componentType);
    Object array = Array.newInstance(componentType, tokens.length);
    for (int i = 0; i < tokens.length; i++) {
      this.value = tokens[i];
      Object item = xfireComponentType.readObject(this, context);
      Array.set(array, i, item);
    }

    if (Collection.class.isAssignableFrom(listType)) {
      Collection collection = newCollectionInstance(listType);
      collection.addAll(Arrays.asList((Object[]) array));
      this.list = collection;
    }
    else {
      this.list = array;
    }
  }

  /**
   * Create a new instance of something of the specified collection type.
   *
   * @param collectionType The collection type.
   * @return the new instance.
   */
  public static Collection newCollectionInstance(Class collectionType) {
    if (Collection.class.isAssignableFrom(collectionType)) {
      Collection collection;
      if ((collectionType.isInterface()) || (Modifier.isAbstract(collectionType.getModifiers()))) {
        if (Set.class.isAssignableFrom(collectionType)) {
          collection = new TreeSet();
        }
        else {
          collection = new ArrayList();
        }
      }
      else {
        try {
          collection = (Collection) collectionType.newInstance();
        }
        catch (Exception e) {
          throw new IllegalArgumentException("Unable to create an instance of " + collectionType.getName() + ".", e);
        }
      }
      return collection;
    }
    else {
      throw new IllegalArgumentException("Invalid list type: " + collectionType.getName());
    }
  }

  /**
   * Get the list.  It will be of the type specified in the constructor.
   *
   * @return The list.
   */
  public Object getList() {
    return list;
  }

  /**
   * The value for the current item in the list.
   *
   * @return The value for the current item in the list.
   */
  public String getValue() {
    return this.value;
  }

  /**
   * @throws UnsupportedOperationException
   */
  public MessageReader getAttributeReader(QName qName) {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException
   */
  public boolean hasMoreAttributeReaders() {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException
   */
  public MessageReader getNextAttributeReader() {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException
   */
  public boolean hasMoreElementReaders() {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException
   */
  public MessageReader getNextElementReader() {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException
   */
  public QName getName() {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException
   */
  public String getLocalName() {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException
   */
  public String getNamespace() {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException
   */
  public String getNamespaceForPrefix(String prefix) {
    throw new UnsupportedOperationException();
  }

}
