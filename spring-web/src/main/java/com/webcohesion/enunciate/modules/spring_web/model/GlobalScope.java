package com.webcohesion.enunciate.modules.spring_web.model;

import javax.lang.model.element.Element;

/**
 * @author Ryan Heaton
 */
public class GlobalScope implements AdviceScope {

  @Override
  public boolean applies(Element el) {
    return true;
  }
}
