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
package com.webcohesion.enunciate.facets;

import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.metadata.Facets;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Used to declare a "facet" for the sake of grouping resources and APIs together for simpler browsing.
 *
 * @author Ryan Heaton
 */
public class Facet implements Comparable<Facet> {

  private final String name;

  public Facet(com.webcohesion.enunciate.metadata.Facet facet) {
    this(facet.value());
  }

  public Facet(String name) {
    if (name == null) {
      throw new NullPointerException();
    }
    this.name = name;
  }

  /**
   * Gather facets.
   *
   * @param declaration The declaration on which to gather facets.
   * @param context The context.
   * @return The facets gathered on the declaration.
   */
  public static Set<Facet> gatherFacets(Element declaration, EnunciateContext context) {
    Set<Facet> bucket = new TreeSet<Facet>();
    if (declaration != null) {
      com.webcohesion.enunciate.metadata.Facet facet = declaration.getAnnotation(com.webcohesion.enunciate.metadata.Facet.class);
      if (facet != null) {
        bucket.add(new Facet(facet));
      }

      Facets facets = declaration.getAnnotation(Facets.class);
      if (facets != null) {
        for (com.webcohesion.enunciate.metadata.Facet f : facets.value()) {
          bucket.add(new Facet(f));
        }
      }

      List<? extends AnnotationMirror> annotationMirrors = declaration.getAnnotationMirrors();
      for (AnnotationMirror annotationMirror : annotationMirrors) {
        DeclaredType annotationType = annotationMirror.getAnnotationType();
        if (annotationType != null) {
          Element annotationDeclaration = annotationType.asElement();
          facet = annotationDeclaration.getAnnotation(com.webcohesion.enunciate.metadata.Facet.class);
          if (facet != null) {
            bucket.add(new Facet(facet));
          }
          facets = annotationDeclaration.getAnnotation(Facets.class);
          if (facets != null) {
            for (com.webcohesion.enunciate.metadata.Facet f : facets.value()) {
              bucket.add(new Facet(f));
            }
          }
        }
      }

      if (declaration instanceof TypeElement && context != null) {
        Set<String> configuredFacets = context.getConfiguredFacets(((TypeElement) declaration).getQualifiedName().toString());
        for (String configuredFacet : configuredFacets) {
          bucket.add(new Facet(configuredFacet));
        }
      }
    }
    return bucket;
  }

  public String getName() {
    return name;
  }

  public int compareTo(Facet o) {
    return this.name.compareTo(o.name);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Facet)) {
      return false;
    }

    Facet facet = (Facet) o;
    return name.equals(facet.name);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }
}
