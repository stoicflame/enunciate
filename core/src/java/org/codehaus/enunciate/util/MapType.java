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

package org.codehaus.enunciate.util;

import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.type.InterfaceType;
import net.sf.jelly.apt.decorations.type.DecoratedInterfaceType;

/**
 * A decorated map type.
 *
 * @author Ryan Heaton
 */
public class MapType extends DecoratedInterfaceType {

  private TypeMirror keyType;
  private TypeMirror valueType;

  public MapType(InterfaceType interfaceType, TypeMirror keyType, TypeMirror valueType) {
    super(interfaceType);
    this.keyType = keyType;
    this.valueType = valueType;
  }

  /**
   * The key type associated with this map type.
   *
   * @return The key type associated with this map type.
   */
  public TypeMirror getKeyType() {
    return keyType;
  }

  /**
   * The key type associated with this map type.
   *
   * @param keyType The key type associated with this map type.
   */
  public void setKeyType(TypeMirror keyType) {
    this.keyType = keyType;
  }

  /**
   * The key type associated with this map type.
   *
   * @return The key type associated with this map type.
   */
  public TypeMirror getValueType() {
    return valueType;
  }

  /**
   * The key type associated with this map type.
   *
   * @param valueType The key type associated with this map type.
   */
  public void setValueType(TypeMirror valueType) {
    this.valueType = valueType;
  }

  /**
   * @return true.
   */
  public boolean isMap() {
    return true;
  }
}
