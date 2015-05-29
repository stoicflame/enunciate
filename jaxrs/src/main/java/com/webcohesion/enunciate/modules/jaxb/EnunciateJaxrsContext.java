package com.webcohesion.enunciate.modules.jaxb;

import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.module.EnunciateModuleContext;

/**
 * @author Ryan Heaton
 */
@SuppressWarnings ( "unchecked" )
public class EnunciateJaxrsContext extends EnunciateModuleContext {

  public EnunciateJaxrsContext(EnunciateContext context) {
    super(context);
  }

  public EnunciateContext getContext() {
    return context;
  }

}
