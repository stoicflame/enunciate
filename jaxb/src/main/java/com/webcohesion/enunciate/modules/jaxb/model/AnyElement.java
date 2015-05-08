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

import com.webcohesion.enunciate.EnunciateException;
import com.webcohesion.enunciate.facets.Facet;
import com.webcohesion.enunciate.facets.HasFacets;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedElement;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.metadata.ClientName;
import com.webcohesion.enunciate.modules.jaxb.EnunciateJaxbContext;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import java.util.*;

/**
 * Used to wrap @XmlAnyElement.
 *
 * @author Ryan Heaton
 */
public class AnyElement extends DecoratedElement<javax.lang.model.element.Element> implements HasFacets {

  private final boolean lax;
  private final List<ElementRef> refs;
  private final Set<Facet> facets = new TreeSet<Facet>();

  public AnyElement(javax.lang.model.element.Element delegate, TypeDefinition typeDef, EnunciateJaxbContext context) {
    super(delegate, context.getContext().getProcessingEnvironment());

    XmlAnyElement info = delegate.getAnnotation(XmlAnyElement.class);
    if (info == null) {
      throw new EnunciateException("No @XmlAnyElement annotation.");
    }
    
    this.lax = info.lax();
    ArrayList<ElementRef> elementRefs = new ArrayList<ElementRef>();
    XmlElementRefs elementRefInfo = delegate.getAnnotation(XmlElementRefs.class);
    if (elementRefInfo != null && elementRefInfo.value() != null) {
      for (XmlElementRef elementRef : elementRefInfo.value()) {
        elementRefs.add(new ElementRef(delegate, typeDef, elementRef, context));
      }
    }
    refs = Collections.unmodifiableList(elementRefs);
    this.facets.addAll(Facet.gatherFacets(delegate));
    this.facets.addAll(typeDef.getFacets());
  }

  /**
   * Whether this is lax.
   *
   * @return Whether this is lax.
   */
  public boolean isLax() {
    return lax;
  }

  /**
   * Whether the any element is a collection.
   *
   * @return Whether the any element is a collection.
   */
  public boolean isCollectionType() {
    return ((DecoratedTypeMirror) asType()).isCollection();
  }

  /**
   * The element refs.
   *
   * @return The element refs.
   */
  public List<ElementRef> getElementRefs() {
    return refs;
  }

  /**
   * The simple name for client-side code generation.
   *
   * @return The simple name for client-side code generation.
   */
  public String getClientSimpleName() {
    String clientSimpleName = getSimpleName().toString();
    ClientName clientName = getAnnotation(ClientName.class);
    if (clientName != null) {
      clientSimpleName = clientName.value();
    }
    return clientSimpleName;
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
