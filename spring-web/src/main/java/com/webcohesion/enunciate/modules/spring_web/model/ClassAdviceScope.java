package com.webcohesion.enunciate.modules.spring_web.model;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public class ClassAdviceScope implements AdviceScope {

  private final Set<String> classes;

  public ClassAdviceScope(Set<String> classes) {
    this.classes = classes;
  }

  @Override
  public boolean applies(Element el) {
    while (el != null && !(el instanceof TypeElement)) {
      el = el.getEnclosingElement();
    }

    return el != null && this.classes.contains(((TypeElement) el).getQualifiedName().toString());
  }
}
