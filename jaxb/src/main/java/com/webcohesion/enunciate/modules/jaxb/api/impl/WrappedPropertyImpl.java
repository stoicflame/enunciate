package com.webcohesion.enunciate.modules.jaxb.api.impl;

import com.webcohesion.enunciate.api.datatype.PropertyMetadata;
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

  public PropertyMetadata getWrapper() {
    if (this.wrapperNamespace != null && !this.wrapperNamespace.isEmpty() && !this.wrapperNamespace.equals(getNamespace())) {
      //if the namespace differs, we need a value and a title.
      return new PropertyMetadata(this.wrapperName, "{" + this.wrapperNamespace + "}" + this.wrapperName, null);
    }
    else {
      return new PropertyMetadata(this.wrapperName);
    }
  }

}
