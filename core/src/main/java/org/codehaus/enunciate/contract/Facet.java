package org.codehaus.enunciate.contract;

import com.sun.mirror.declaration.AnnotationMirror;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.type.AnnotationType;
import org.codehaus.enunciate.Facets;

import java.util.Collection;
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

  public Facet(org.codehaus.enunciate.Facet facet) {
    this(facet.name(), facet.value(), "##default".equals(facet.documentation()) ? null : facet.documentation());
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

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }

  public String getDocumentation() {
    return documentation;
  }

  @Override
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

  /**
   * Gather facets.
   *
   * @param declaration The declaration on which to gather facets.
   * @return The facets gathered on the declaration.
   */
  public static Set<Facet> gatherFacets(Declaration declaration) {
    Set<Facet> bucket = new TreeSet<Facet>();
    if (declaration != null) {
      org.codehaus.enunciate.Facet facet = declaration.getAnnotation(org.codehaus.enunciate.Facet.class);
      if (facet != null) {
        bucket.add(new Facet(facet));
      }
      Facets facets = declaration.getAnnotation(Facets.class);
      if (facets != null) {
        for (org.codehaus.enunciate.Facet f : facets.value()) {
          bucket.add(new Facet(f));
        }
      }

      Collection<AnnotationMirror> annotationMirrors = declaration.getAnnotationMirrors();
      for (AnnotationMirror annotationMirror : annotationMirrors) {
        AnnotationType annotationType = annotationMirror.getAnnotationType();
        if (annotationType != null) {
          AnnotationTypeDeclaration annotationDeclaration = annotationType.getDeclaration();
          facet = annotationDeclaration.getAnnotation(org.codehaus.enunciate.Facet.class);
          if (facet != null) {
            bucket.add(new Facet(facet));
          }
          facets = annotationDeclaration.getAnnotation(Facets.class);
          if (facets != null) {
            for (org.codehaus.enunciate.Facet f : facets.value()) {
              bucket.add(new Facet(f));
            }
          }
        }
      }
    }
    return bucket;
  }
}
