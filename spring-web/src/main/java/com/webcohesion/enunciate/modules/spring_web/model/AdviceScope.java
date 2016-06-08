package com.webcohesion.enunciate.modules.spring_web.model;

import javax.lang.model.element.Element;

/**
 * @author Ryan Heaton
 */
public interface AdviceScope {

  boolean applies(Element el);

}
