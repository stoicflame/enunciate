package com.webcohesion.enunciate.modules.jackson.api.impl;

import com.webcohesion.enunciate.api.datatype.DataTypeReference;
import com.webcohesion.enunciate.api.datatype.Property;
import com.webcohesion.enunciate.javac.decorations.element.ElementUtils;
import com.webcohesion.enunciate.modules.jackson.model.Member;

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

}
