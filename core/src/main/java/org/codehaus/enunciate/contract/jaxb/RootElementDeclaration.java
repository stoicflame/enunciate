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

package org.codehaus.enunciate.contract.jaxb;

import com.sun.mirror.declaration.ClassDeclaration;
import net.sf.jelly.apt.decorations.declaration.DecoratedClassDeclaration;

import javax.xml.bind.annotation.XmlRootElement;
import java.beans.Introspector;

import org.codehaus.enunciate.ClientName;

/**
 * A class declaration decorated so as to be able to describe itself as an XML-Schema root element declaration.
 *
 * @author Ryan Heaton
 */
public class RootElementDeclaration extends DecoratedClassDeclaration {

  private final XmlRootElement rootElement;
  private final TypeDefinition typeDefinition;
  private final Schema schema;

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
}
