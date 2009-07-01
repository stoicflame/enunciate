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

package org.codehaus.enunciate.contract.jaxb.types;

import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.namespace.QName;

/**
 * An xml type that has been specified.
 *
 * @author Ryan Heaton
 */
public class SpecifiedXmlType implements XmlType {

  private final XmlSchemaType annotation;

  public SpecifiedXmlType(XmlSchemaType annotation) {
    this.annotation = annotation;
  }

  /**
   * The specified name.
   *
   * @return The specified name.
   */
  public String getName() {
    return annotation.name();
  }

  /**
   * The specified namespace.
   *
   * @return The specified namespace.
   */
  public String getNamespace() {
    return annotation.namespace();
  }

  /**
   * The qname.
   *
   * @return The qname.
   */
  public QName getQname() {
    return new QName(getNamespace(), getName());
  }

  /**
   * A specified type is never anonymous.
   *
   * @return A specified type is never anonymous.
   */
  public boolean isAnonymous() {
    return false;
  }

  /**
   * A specified type is assumed to be simple.
   *
   * @return true
   */
  public boolean isSimple() {
    return true;
  }

  // Inherited.
  public void generateExampleXml(org.jdom.Element node, String specifiedValue) {
    node.addContent(new org.jdom.Text(specifiedValue == null ? "..." : specifiedValue));
  }
}
