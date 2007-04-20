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

package org.codehaus.enunciate.contract.jaxb.types;

import com.sun.mirror.type.ClassType;
import com.sun.mirror.type.ReferenceType;
import org.codehaus.enunciate.contract.jaxb.TypeDefinition;

import javax.xml.namespace.QName;

/**
 * An XML type that is adapted.
 *
 * @author Ryan Heaton
 */
public class AdaptedXmlType implements XmlType {

  private final ReferenceType adaptedType;
  private final ClassType adaptorType;
  private final XmlType adaptingType;

  public AdaptedXmlType(ReferenceType adaptedType, ClassType adaptorType, XmlType adaptingType) {
    this.adaptedType = adaptedType;
    this.adaptorType = adaptorType;
    this.adaptingType = adaptingType;
  }

  /**
   * The XML name of the adapting type.
   *
   * @return The XML name of the adapting type.
   */
  public String getName() {
    return adaptingType.getName();
  }

  /**
   * The XML namespace of the adapting type.
   *
   * @return The XML namespace of the adapting type.
   */
  public String getNamespace() {
    return adaptingType.getNamespace();
  }

  /**
   * The qname of the adapting type.
   *
   * @return The qname of the adapting type.
   */
  public QName getQname() {
    return adaptingType.getQname();
  }

  /**
   * Whether the adapting type is anonymous.
   *
   * @return Whether the adapting type is anonymous.
   */
  public boolean isAnonymous() {
    return adaptingType.isAnonymous();
  }

  /**
   * Whether the adapting type is simple.
   *
   * @return Whether the adapting type is simple.
   */
  public boolean isSimple() {
    return false;
  }

  /**
   * Get the type definition for the adapting type.
   *
   * @return the type definition for the adapting type, or null.
   */
  public TypeDefinition getTypeDefinition() {
    TypeDefinition typeDef = null;
    if (adaptingType instanceof XmlClassType) {
      typeDef = ((XmlClassType) adaptingType).getTypeDefinition();
    }
    return typeDef;
  }

  /**
   * The type that is being adapted.
   *
   * @return The type that is being adapted.
   */
  public ReferenceType getAdaptedType() {
    return adaptedType;
  }

  /**
   * The class type of the adaptor.
   *
   * @return The class type of the adaptor.
   */
  public ClassType getAdaptorType() {
    return adaptorType;
  }
}
