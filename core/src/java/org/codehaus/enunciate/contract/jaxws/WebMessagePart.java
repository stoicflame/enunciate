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

package org.codehaus.enunciate.contract.jaxws;

import javax.xml.namespace.QName;

/**
 * A part of a complex web message.
 *
 * @author Ryan Heaton
 */
public interface WebMessagePart {

  public enum ParticleType {
    ELEMENT,
    TYPE
  }

  /**
   * The part name.
   *
   * @return The part name.
   */
  String getPartName();

  /**
   * The documentation for this web message part.
   *
   * @return The documentation for this web message part.
   */
  String getPartDocs();

  /**
   * The particle type for this part.
   *
   * @return The particle type for this part.
   */
  ParticleType getParticleType();

  /**
   * The qname of the schema particle (element or type) for this part.
   *
   * @return The qname of the schema particle (element or type) for this part.
   */
  QName getParticleQName();

  /**
   * Whether this web message part defines an implicit schema element.
   *
   * @return Whether this web message part defines an implicit schema element.
   */
  boolean isImplicitSchemaElement();

}
