package com.webcohesion.enunciate.modules.jaxws.model.util;

import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.modules.jaxb.EnunciateJaxbContext;
import com.webcohesion.enunciate.modules.jaxb.model.adapters.AdapterType;
import com.webcohesion.enunciate.modules.jaxb.model.util.JAXBUtil;
import com.webcohesion.enunciate.modules.jaxws.model.WebParam;

import javax.lang.model.element.Element;

/**
 * @author Ryan Heaton
 */
public class JAXWSUtil extends JAXBUtil {

  public static AdapterType findAdapterType(WebParam parameter, EnunciateJaxbContext context) {
    return findAdapterType((DecoratedTypeMirror) parameter.asType(), parameter, parameter.getWebMethod().getDeclaringEndpointInterface().getPackage(), context);
  }
}
