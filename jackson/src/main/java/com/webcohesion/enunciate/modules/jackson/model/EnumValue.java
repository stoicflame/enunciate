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
package com.webcohesion.enunciate.modules.jackson.model;

import com.webcohesion.enunciate.facets.Facet;
import com.webcohesion.enunciate.facets.HasFacets;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedVariableElement;

import javax.lang.model.element.VariableElement;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Ryan Heaton
 */
public class EnumValue extends DecoratedVariableElement implements HasFacets {

  private final EnumTypeDefinition typeDefinition;
  private final String name;
  private final String value;
  private final Set<Facet> facets = new TreeSet<Facet>();

  public EnumValue(EnumTypeDefinition typeDefinition, VariableElement delegate, String name, String value) {
    super(delegate, typeDefinition.getContext().getContext().getProcessingEnvironment());
    this.typeDefinition = typeDefinition;
    this.name = name;
    this.value = value;
    this.facets.addAll(Facet.gatherFacets(delegate, typeDefinition.getContext().getContext()));
    this.facets.addAll(typeDefinition.getFacets());
  }

  public EnumTypeDefinition getTypeDefinition() {
    return typeDefinition;
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }

  @Override
  public Set<Facet> getFacets() {
    return this.facets;
  }
}
