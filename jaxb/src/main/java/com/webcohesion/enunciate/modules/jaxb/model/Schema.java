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

import com.webcohesion.enunciate.facets.Facet;
import com.webcohesion.enunciate.facets.HasFacets;
import com.webcohesion.enunciate.javac.decorations.DecoratedProcessingEnvironment;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedPackageElement;

import javax.lang.model.element.PackageElement;
import jakarta.xml.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A package declaration decorated so as to be able to describe itself an XML-Schema root element.
 *
 * @author Ryan Heaton
 * @see "The JAXB 2.0 Specification"
 * @see <a href="http://www.w3.org/TR/2004/REC-xmlschema-1-20041028/structures.html">XML Schema Part 1: Structures Second Edition</a>
 */
public class Schema extends DecoratedPackageElement implements Comparable<Schema>, HasFacets {

  private final XmlSchema xmlSchema;
  private final XmlAccessorType xmlAccessorType;
  private final XmlAccessorOrder xmlAccessorOrder;
  private final PackageElement pckg;
  private final Set<Facet> facets;

  public Schema(PackageElement delegate, DecoratedProcessingEnvironment env) {
    super(delegate, env);
    this.pckg = delegate;
    this.xmlSchema = delegate != null ? delegate.getAnnotation(XmlSchema.class) : null;
    this.xmlAccessorType = delegate != null ? delegate.getAnnotation(XmlAccessorType.class) : null;
    this.xmlAccessorOrder = delegate != null ? delegate.getAnnotation(XmlAccessorOrder.class) : null;
    this.facets = Facet.gatherFacets(delegate, null);
  }

  /**
   * The namespace of this package, or null if none.
   *
   * @return The namespace of this package.
   */
  public String getNamespace() {
    String namespace = null;

    if (xmlSchema != null) {
      namespace = xmlSchema.namespace();
    }

    return namespace;
  }

  /**
   * The element form default of this namespace.
   *
   * @return The element form default of this namespace.
   */
  public XmlNsForm getElementFormDefault() {
    XmlNsForm form = null;

    if ((xmlSchema != null) && (xmlSchema.elementFormDefault() != XmlNsForm.UNSET)) {
      form = xmlSchema.elementFormDefault();
    }

    return form;
  }

  /**
   * The attribute form default of this namespace.
   *
   * @return The attribute form default of this namespace.
   */
  public XmlNsForm getAttributeFormDefault() {
    XmlNsForm form = null;

    if ((xmlSchema != null) && (xmlSchema.attributeFormDefault() != XmlNsForm.UNSET)) {
      form = xmlSchema.attributeFormDefault();
    }

    return form;
  }

  /**
   * The default access type for the beans in this package.
   *
   * @return The default access type for the beans in this package.
   */
  public XmlAccessType getAccessType() {
    XmlAccessType accessType = XmlAccessType.PUBLIC_MEMBER;

    if (xmlAccessorType != null) {
      accessType = xmlAccessorType.value();
    }

    return accessType;
  }

  /**
   * The default accessor order of the beans in this package.
   *
   * @return The default accessor order of the beans in this package.
   */
  public XmlAccessOrder getAccessorOrder() {
    XmlAccessOrder order = XmlAccessOrder.UNDEFINED;

    if (xmlAccessorOrder != null) {
      order = xmlAccessorOrder.value();
    }

    return order;
  }

  /**
   * Gets the specified namespace prefixes for this package.
   *
   * @return The specified namespace prefixes for this package.
   */
  public Map<String, String> getSpecifiedNamespacePrefixes() {
    HashMap<String, String> namespacePrefixes = new HashMap<String, String>();
    if (xmlSchema != null) {
      XmlNs[] xmlns = xmlSchema.xmlns();
      if (xmlns != null) {
        for (XmlNs ns : xmlns) {
          if (!ns.prefix().isEmpty()) {
            namespacePrefixes.put(ns.namespaceURI(), ns.prefix());
          }
        }
      }
    }

    return namespacePrefixes;
  }

  /**
   * Two "schemas" are equal if they decorate the same package.
   *
   * @param schema The schema to which to compare this schema.
   * @return The comparison.
   */
  public int compareTo(Schema schema) {
    return this.pckg.getQualifiedName().toString().compareTo(schema.pckg.getQualifiedName().toString());
  }

  /**
   * The facets here applicable.
   *
   * @return The facets here applicable.
   */
  public Set<Facet> getFacets() {
    return facets;
  }
}
