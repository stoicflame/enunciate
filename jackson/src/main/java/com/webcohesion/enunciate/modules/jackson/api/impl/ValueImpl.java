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
package com.webcohesion.enunciate.modules.jackson.api.impl;

import com.webcohesion.enunciate.api.datatype.Value;
import com.webcohesion.enunciate.facets.Facet;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;

import java.util.Set;
import java.util.function.Supplier;

/**
 * @author Ryan Heaton
 */
public class ValueImpl implements Value {

  private final String value;
  private final Supplier<JavaDoc> doc;
  private final Set<String> styles;
  private final Set<Facet> facets;

  public ValueImpl(String value, Supplier<JavaDoc> javaDoc, Set<String> styles, Set<Facet> facets) {
    this.value = value;
    this.doc = javaDoc;
    this.styles = styles;
    this.facets = facets;
  }

  @Override
  public String getValue() {
    return value;
  }

  @Override
  public String getDescription() {
    return this.doc.get().toString();
  }

  @Override
  public Set<String> getStyles() {
    return this.styles;
  }

  @Override
  public Set<Facet> getFacets() {
    return this.facets;
  }

  @Override
  public String getSince() {
    JavaDoc.JavaDocTagList sinceTags = this.doc.get().get("since");
    return sinceTags != null ? sinceTags.toString() : null;
  }
}
