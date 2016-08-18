/**
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webcohesion.enunciate.metadata.qname;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Special Enunciate support for qname enumerations. See http://docs.codehaus.org/display/ENUNCIATE/QName+Enums.
 *
 * @author Ryan Heaton
 */
@Retention (RUNTIME) @Target ({TYPE})
public @interface XmlQNameEnum {

  /**
   * The namespace for this qname enumeration. If left unspecified (i.e. the value is "##default"), then
   * standard JAXB namespacing applies, which looks like this:
   *
   * <ol>
   *  <li>If the enclosing package has {@link javax.xml.bind.annotation.XmlSchema} annotation,
   *  and its {@link javax.xml.bind.annotation.XmlSchema#elementFormDefault() elementFormDefault}
   *  is {@link javax.xml.bind.annotation.XmlNsForm#QUALIFIED QUALIFIED}, then the namespace of
   *  the enclosing class.</li>
   *  <li>Otherwise "" (which produces unqualified element in the default namespace).</li>
   * </ol>
   *
   * @return The namespace for this qname enumeration.
   */
  String namespace() default "##default";

  /**
   * The base type for this QName enum.
   *
   * @return The base type for this QName enum.
   */
  BaseType base() default BaseType.QNAME;

  /**
   * Enumeration of known base types of QName enums.
   */
  public enum BaseType {

    QNAME,

    URI
  }

}
