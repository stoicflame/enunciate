package com.webcohesion.enunciate.modules.jaxws.api.impl;

import com.webcohesion.enunciate.api.datatype.DataTypeReference;
import com.webcohesion.enunciate.api.services.Parameter;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;
import com.webcohesion.enunciate.modules.jaxb.api.impl.DataTypeReferenceImpl;
import com.webcohesion.enunciate.modules.jaxb.model.types.KnownXmlType;
import com.webcohesion.enunciate.modules.jaxws.model.HttpHeader;

import javax.lang.model.element.AnnotationMirror;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public class HttpHeaderParameter implements Parameter {

  private final HttpHeader header;

  public HttpHeaderParameter(HttpHeader header) {
    this.header = header;
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
  public String getName() {
    return this.header.getName();
  }

  @Override
  public String getDescription() {
    return this.header.getDescription();
  }

  @Override
  public DataTypeReference getDataType() {
    return new DataTypeReferenceImpl(KnownXmlType.STRING, false, null);
  }

  @Override
  public JavaDoc getJavaDoc() {
    return new JavaDoc(getDescription(), null, null, null);
  }
}
