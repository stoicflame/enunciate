package com.webcohesion.enunciate.modules.jaxws;

import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.module.EnunciateModuleContext;

/**
 * @author Ryan Heaton
 */
@SuppressWarnings ( "unchecked" )
public class EnunciateJaxwsContext extends EnunciateModuleContext {


  public EnunciateJaxwsContext(EnunciateContext context) {
    super(context);
  }

  public EnunciateContext getContext() {
    return context;
  }

}
