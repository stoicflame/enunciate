package com.webcohesion.enunciate.modules.jaxws;

import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.module.EnunciateModuleContext;
import com.webcohesion.enunciate.modules.jaxb.EnunciateJaxbContext;

/**
 * @author Ryan Heaton
 */
@SuppressWarnings ( "unchecked" )
public class EnunciateJaxwsContext extends EnunciateModuleContext {

  private final EnunciateJaxbContext jaxbContext;
  private final boolean forceJAXWSSpecCompliance;

  public EnunciateJaxwsContext(EnunciateJaxbContext jaxbContext, boolean forceJAXWSSpecCompliance) {
    super(jaxbContext.getContext());
    this.jaxbContext = jaxbContext;
    this.forceJAXWSSpecCompliance = forceJAXWSSpecCompliance;
  }

  public EnunciateContext getContext() {
    return context;
  }

  public EnunciateJaxbContext getJaxbContext() {
    return jaxbContext;
  }

  public boolean isForceJAXWSSpecCompliance() {
    return forceJAXWSSpecCompliance;
  }
}
