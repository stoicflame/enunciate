package com.webcohesion.enunciate.facets;

import com.webcohesion.enunciate.metadata.Facets;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
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
  private final String value;
  private final String documentation;

  public Facet(com.webcohesion.enunciate.metadata.Facet facet, String defaultValue) {
    this(facet.name(), "##default".equals(facet.value()) ? defaultValue : facet.value(), "##default".equals(facet.documentation()) ? null : facet.documentation());
  }

  public Facet(String name, String value) {
    this(name, value, null);
  }

  public Facet(String name, String value, String documentation) {
    if (name == null) {
      throw new NullPointerException();
    }
    this.name = name;

    if (value == null) {
      throw new NullPointerException();
    }
    this.value = value;
    this.documentation = documentation;
  }

  /**
   * Gather facets.
   *
   * @param declaration The declaration on which to gather facets.
   * @return The facets gathered on the declaration.
   */
  public static Set<Facet> gatherFacets(Element declaration) {
    Set<Facet> bucket = new TreeSet<Facet>();
    if (declaration != null) {
      com.webcohesion.enunciate.metadata.Facet facet = declaration.getAnnotation(com.webcohesion.enunciate.metadata.Facet.class);
      if (facet != null) {
        bucket.add(new Facet(facet, declaration.getSimpleName().toString()));
      }

      Facets facets = declaration.getAnnotation(Facets.class);
      if (facets != null) {
        for (com.webcohesion.enunciate.metadata.Facet f : facets.value()) {
          bucket.add(new Facet(f, declaration.getSimpleName().toString()));
        }
      }

      List<? extends AnnotationMirror> annotationMirrors = declaration.getAnnotationMirrors();
      for (AnnotationMirror annotationMirror : annotationMirrors) {
        DeclaredType annotationType = annotationMirror.getAnnotationType();
        if (annotationType != null) {
          Element annotationDeclaration = annotationType.asElement();
          facet = annotationDeclaration.getAnnotation(com.webcohesion.enunciate.metadata.Facet.class);
          if (facet != null) {
            bucket.add(new Facet(facet, annotationDeclaration.getSimpleName().toString()));
          }
          facets = annotationDeclaration.getAnnotation(Facets.class);
          if (facets != null) {
            for (com.webcohesion.enunciate.metadata.Facet f : facets.value()) {
              bucket.add(new Facet(f, annotationDeclaration.getSimpleName().toString()));
            }
          }
        }
      }
    }
    return bucket;
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }

  public String getDocumentation() {
    return documentation;
  }

  public int compareTo(Facet o) {
    String comparison1 = this.name + this.value;
    String comparison2 = o.name + o.value;
    return comparison1.compareTo(comparison2);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Facet facet = (Facet) o;

    if (!name.equals(facet.name)) {
      return false;
    }
    if (!value.equals(facet.value)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + value.hashCode();
    return result;
  }

}
