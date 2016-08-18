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
package com.webcohesion.enunciate.modules.jackson1.model;

import com.webcohesion.enunciate.facets.Facet;
import com.webcohesion.enunciate.facets.HasFacets;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedElement;
import com.webcohesion.enunciate.metadata.ClientName;
import com.webcohesion.enunciate.modules.jackson1.EnunciateJackson1Context;

import java.util.Set;
import java.util.TreeSet;

/**
 * Used to wrap @JsonAnyGetter.
 *
 * @author Ryan Heaton
 */
public class WildcardMember extends DecoratedElement<javax.lang.model.element.Element> implements HasFacets {

  private final Set<Facet> facets = new TreeSet<Facet>();

  public WildcardMember(javax.lang.model.element.Element delegate, TypeDefinition typeDef, EnunciateJackson1Context context) {
    super(delegate, context.getContext().getProcessingEnvironment());
    this.facets.addAll(Facet.gatherFacets(delegate));
    this.facets.addAll(typeDef.getFacets());
  }

  public String getName() {
    return getSimpleName().toString();
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
