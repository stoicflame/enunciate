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
import com.webcohesion.enunciate.javac.decorations.Annotations;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedExecutableElement;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedTypeElement;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.modules.jaxb.EnunciateJaxbContext;
import com.webcohesion.enunciate.modules.jaxb.model.types.XmlType;
import com.webcohesion.enunciate.modules.jaxb.model.types.XmlTypeFactory;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.namespace.QName;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;

/**
 * A declaration of a "local" element (defined by a registry).
 *
 * @author Ryan Heaton
 */
public class LocalElementDeclaration extends DecoratedExecutableElement implements HasFacets, ElementDeclaration {

  private final TypeMirror elementType;
  private final XmlElementDecl elementDecl;
  private final Registry registry;
  private final Set<Facet> facets = new TreeSet<Facet>();
  private final EnunciateJaxbContext context;

  public LocalElementDeclaration(ExecutableElement element, Registry registry, EnunciateJaxbContext context) {
    super(element, context.getContext().getProcessingEnvironment());
    this.registry = registry;
    elementDecl = element.getAnnotation(XmlElementDecl.class);
    if (elementDecl == null) {
      throw new IllegalArgumentException(element + ": a local element declaration must be annotated with @XmlElementDecl.");
    }

    List<? extends VariableElement> params = element.getParameters();
    if (params.size() != 1) {
      throw new IllegalArgumentException(element + ": a local element declaration must have only one parameter.");
    }

    elementType = params.get(0).asType();
    this.facets.addAll(Facet.gatherFacets(registry, context.getContext()));
    this.facets.addAll(Facet.gatherFacets(element, context.getContext()));
    this.context = context;
  }

  /**
   * The name of the local element.
   *
   * @return The name of the local element.
   */
  public String getName() {
    return elementDecl.name();
  }

  /**
   * The namespace of the local element.
   *
   * @return The namespace of the local element.
   */
  public String getNamespace() {
    String namespace = elementDecl.namespace();
    if ("##default".equals(namespace)) {
      namespace = this.registry.getSchema().getNamespace();
    }
    return "".equals(namespace) ? null : namespace;
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
   * The scope of the local element.
   *
   * @return The scope of the local element.
   */
  public DecoratedTypeElement getElementScope() {
    DecoratedTypeElement declaration = null;

    DecoratedTypeMirror typeMirror = Annotations.mirrorOf(elementDecl::scope, this.env, XmlElementDecl.GLOBAL.class);

    if (typeMirror != null) {
      declaration = (DecoratedTypeElement) ((DeclaredType) typeMirror).asElement();
    }

    return declaration;
  }

  /**
   * The name of the substitution head.
   *
   * @return The name of the substitution head.
   */
  public String getSubstitutionHeadName() {
    String shn = elementDecl.substitutionHeadName();
    if ("".equals(shn)) {
      shn = null;
    }
    return shn;
  }

  /**
   * The namespace of the substitution head.
   *
   * @return The namespace of the substitution head.
   */
  public String getSubstitutionHeadNamespace() {
    String shn = elementDecl.substitutionHeadNamespace();
    if ("##default".equals(shn)) {
      shn = this.registry.getSchema().getNamespace();
    }
    return shn;
  }

  /**
   * The substitution group qname.
   *
   * @return The substitution group qname.
   */
  public QName getSubstitutionGroupQName() {
    String localPart = getSubstitutionHeadName();
    if (localPart == null) {
      return null;
    }
    return new QName(getSubstitutionHeadNamespace(), localPart);
  }

  /**
   * The default value.
   *
   * @return The default value.
   */
  public String getDefaultElementValue() {
    String defaultValue = elementDecl.defaultValue();
    if ("\u0000".equals(defaultValue)) {
      defaultValue = null;
    }
    return defaultValue;
  }

  /**
   * The type definition for the local element.
   *
   * @return The type definition for the local element.
   */
  public TypeMirror getElementType() {
    return elementType;
  }

  /**
   * The element xml type.
   *
   * @return The element xml type.
   */
  public XmlType getElementXmlType() {
    return XmlTypeFactory.getXmlType(getParameters().get(0).asType(), this.context);
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
