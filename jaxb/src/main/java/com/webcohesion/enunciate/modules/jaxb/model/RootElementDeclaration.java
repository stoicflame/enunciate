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

package com.webcohesion.enunciate.modules.jaxb.model;

import com.sun.mirror.declaration.ClassDeclaration;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedClassDeclaration;
import org.codehaus.enunciate.ClientName;
import org.codehaus.enunciate.contract.Facet;
import org.codehaus.enunciate.contract.HasFacets;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;
import java.beans.Introspector;
import java.util.Set;
import java.util.TreeSet;

/**
 * A class declaration decorated so as to be able to describe itself as an XML-Schema root element declaration.
 *
 * @author Ryan Heaton
 */
public class RootElementDeclaration extends DecoratedClassDeclaration implements ElementDeclaration, HasFacets {

  private final XmlRootElement rootElement;
  private final TypeDefinition typeDefinition;
  private final Schema schema;
  private final Set<Facet> facets = new TreeSet<Facet>();

  public RootElementDeclaration(ClassDeclaration delegate, TypeDefinition typeDefinition) {
    super(delegate);

    this.rootElement = getAnnotation(XmlRootElement.class);
    this.typeDefinition = typeDefinition;
    Package pckg;
    try {
      //if this is an already-compiled class, APT has a problem looking up the package info on the classpath...
      pckg = Class.forName(getQualifiedName()).getPackage();
    }
    catch (Throwable e) {
      pckg = null;
    }
    this.schema = new Schema(delegate.getPackage(), pckg);
    this.facets.addAll(Facet.gatherFacets(delegate));
    this.facets.addAll(this.schema.getFacets());
  }

  /**
   * The type definition for this root element.  Note that the type definition may be unknown, in which case, return null.
   *
   * @return The type definition for this root element.
   */
  public TypeDefinition getTypeDefinition() {
    return this.typeDefinition;
  }

  /**
   * The name of the xml element declaration.
   *
   * @return The name of the xml element declaration.
   */
  public String getName() {
    String name = Introspector.decapitalize(getSimpleName());

    if ((rootElement != null) && (!"##default".equals(rootElement.name()))) {
      name = rootElement.name();
    }

    return name;
  }

  /**
   * The namespace of the xml element.
   *
   * @return The namespace of the xml element.
   */
  public String getNamespace() {
    String namespace = getPackage().getNamespace();

    if ((rootElement != null) && (!"##default".equals(rootElement.namespace()))) {
      namespace = rootElement.namespace();
    }

    return namespace;
  }

  /**
   * The qname of the element.
   *
   * @return The qname of the element.
   */
  public QName getQname() {
    return new QName(getNamespace(), getName());
  }

  /**
   * The simple name for client-side code generation.
   *
   * @return The simple name for client-side code generation.
   */
  public String getClientSimpleName() {
    String clientSimpleName = getSimpleName();
    ClientName clientName = getAnnotation(ClientName.class);
    if (clientName != null) {
      clientSimpleName = clientName.value();
    }
    return clientSimpleName;
  }

  /**
   * The schema for this complex type.
   *
   * @return The schema for this complex type.
   */
  public Schema getSchema() {
    return schema;
  }

  // Inherited.
  @Override
  public Schema getPackage() {
    return getSchema();
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
