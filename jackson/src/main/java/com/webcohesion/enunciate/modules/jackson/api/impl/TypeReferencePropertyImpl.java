package com.webcohesion.enunciate.modules.jackson.api.impl;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;

import com.webcohesion.enunciate.api.datatype.DataTypeReference;
import com.webcohesion.enunciate.api.datatype.Property;
import com.webcohesion.enunciate.facets.Facet;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;
import com.webcohesion.enunciate.modules.jackson.model.types.KnownJsonType;

/**
 * @author Ryan Heaton
 */
public class TypeReferencePropertyImpl implements Property {

  private final String name;
  private final String description;
  private final DataTypeReference type;

  public TypeReferencePropertyImpl(String name) {
    this.name = name;
    this.description = "The JSON object type.";
    this.type = new DataTypeReferenceImpl(KnownJsonType.STRING, null);
  }

  @Override
  public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
    return null;
  }

  @Override
  public Map<String, AnnotationMirror> getAnnotations() {
    return Collections.emptyMap();
  }

  @Override
  public Set<String> getStyles() {
    return Collections.emptySet();
  }

  @Override
  public Set<Facet> getFacets() {
    return Collections.emptySet();
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public String getDescription() {
    return this.description;
  }

  @Override
  public DataTypeReference getDataType() {
    return this.type;
  }

  @Override
  public String getDeprecated() {
    return null;
  }

  @Override
  public boolean isRequired() {
    return false;
  }

  @Override
  public boolean isReadOnly() {
    return false;
  }

  @Override
  public JavaDoc getJavaDoc() {
    return new JavaDoc(this.description, null, null, null);
  }

  @Override
  public String getSince() {
    return null;
  }
}
