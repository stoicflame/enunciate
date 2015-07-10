package com.webcohesion.enunciate.modules.jaxb.api.impl;

import com.webcohesion.enunciate.modules.jaxb.model.Element;

/**
 * @author Ryan Heaton
 */
public class WrappedPropertyImpl extends PropertyImpl {

  private final String wrapperName;
  private final String wrapperNamespace;

  public WrappedPropertyImpl(Element accessor, String wrapperName, String wrapperNamespace) {
    super(accessor);
    this.wrapperName = wrapperName;
    this.wrapperNamespace = wrapperNamespace;
  }

  public String getWrapper() {
    StringBuilder builder = new StringBuilder();

    if (this.wrapperNamespace != null && !this.wrapperNamespace.isEmpty() && !this.wrapperNamespace.equals(getNamespace())) {
      builder.append('{').append(this.wrapperNamespace).append('}');
    }

    builder.append(this.wrapperName);

    return builder.toString();
  }

}
