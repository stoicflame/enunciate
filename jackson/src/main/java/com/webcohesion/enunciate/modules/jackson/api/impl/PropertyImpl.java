package com.webcohesion.enunciate.modules.jackson.api.impl;

import com.webcohesion.enunciate.api.Styles;
import com.webcohesion.enunciate.api.datatype.DataTypeReference;
import com.webcohesion.enunciate.api.datatype.Property;
import com.webcohesion.enunciate.javac.decorations.element.ElementUtils;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;
import com.webcohesion.enunciate.modules.jackson.model.Member;

import javax.lang.model.element.AnnotationMirror;
import java.util.Map;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public class PropertyImpl implements Property {

  private final Member member;

  public PropertyImpl(Member member) {
    this.member = member;
  }

  @Override
  public String getName() {
    return this.member.getName();
  }

  @Override
  public DataTypeReference getDataType() {
    return new DataTypeReferenceImpl(this.member.getJsonType());
  }

  @Override
  public String getDescription() {
    return this.member.getJavaDoc().toString();
  }

  @Override
  public String getDeprecated() {
    return ElementUtils.findDeprecationMessage(this.member);
  }

  @Override
  public Map<String, AnnotationMirror> getAnnotations() {
    return this.member.getAnnotations();
  }

  @Override
  public boolean isRequired() {
    return member.isRequired();
  }

  public String getConstraints() {
    return isRequired() ? "required" : null;
  }

  public String getDefaultValue() {
    return member.getDefaultValue();
  }

  @Override
  public JavaDoc getJavaDoc() {
    return member.getJavaDoc();
  }

  @Override
  public Set<String> getStyles() {
    return Styles.gatherStyles(this.member, this.member.getContext().getContext().getConfiguration().getAnnotationStyles());
  }
}
