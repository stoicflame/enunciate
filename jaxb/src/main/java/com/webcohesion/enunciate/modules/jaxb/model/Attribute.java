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
package com.webcohesion.enunciate.modules.jaxb.model;

import com.webcohesion.enunciate.modules.jaxb.EnunciateJaxbContext;
import com.webcohesion.enunciate.util.BeanValidationUtils;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlNsForm;
import javax.xml.namespace.QName;

/**
 * An accessor that is marshalled in xml to an xml attribute.
 *
 * @author Ryan Heaton
 */
public class Attribute extends Accessor {

  private final XmlAttribute xmlAttribute;

  public Attribute(javax.lang.model.element.Element delegate, TypeDefinition typedef, EnunciateJaxbContext context) {
    super(delegate, typedef, context);

    xmlAttribute = getAnnotation(XmlAttribute.class);
  }

  // Inherited.
  public String getName() {
    String name = getSimpleName().toString();

    if ((xmlAttribute != null) && (!"##default".equals(xmlAttribute.name()))) {
      name = xmlAttribute.name();
    }

    return name;
  }

  // Inherited.
  public String getNamespace() {
    String namespace = null;

    if (getForm() == XmlNsForm.QUALIFIED) {
      namespace = getTypeDefinition().getNamespace();
    }

    if ((xmlAttribute != null) && (!"##default".equals(xmlAttribute.namespace()))) {
      namespace = xmlAttribute.namespace();
    }

    return namespace;
  }

  /**
   * The form of this attribute.
   *
   * @return The form of this attribute.
   */
  public XmlNsForm getForm() {
    XmlNsForm form = getTypeDefinition().getSchema().getAttributeFormDefault();

    if (form == null || form == XmlNsForm.UNSET) {
      form = XmlNsForm.UNQUALIFIED;
    }
    
    return form;
  }

  /**
   * Whether the form of this attribute is qualified.
   *
   * @return Whether the form of this attribute is qualified.
   */
  public boolean isFormQualified() {
    return getForm() == XmlNsForm.QUALIFIED;
  }


  /**
   * An attribute is a ref if its namespace differs from that of its type definition (JAXB spec 8.9.7.2).
   *
   * @return The ref or null.
   */
  @Override
  public QName getRef() {
    String typeNamespace = getTypeDefinition().getNamespace();
    typeNamespace = typeNamespace == null ? "" : typeNamespace;
    String namespace = getNamespace();
    namespace = namespace == null ? "" : namespace;

    if ((!namespace.equals(typeNamespace)) && (isFormQualified() || !"".equals(namespace))) {
      return new QName(namespace, getName());
    }

    return null;
  }

  /**
   * Whether the attribute is required.
   *
   * @return Whether the attribute is required.
   */
  public boolean isRequired() {
    boolean required = BeanValidationUtils.isNotNull(this, this.env);

    if (xmlAttribute != null && !required) {
      required = xmlAttribute.required();
    }

    return required || (getAccessorType().isPrimitive());
  }

  /**
   * @return true
   */
  @Override
  public boolean isAttribute() {
    return true;
  }

}
