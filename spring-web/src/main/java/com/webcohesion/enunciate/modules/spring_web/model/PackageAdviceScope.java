package com.webcohesion.enunciate.modules.spring_web.model;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public class PackageAdviceScope implements AdviceScope {

  private final Set<String> packages;

  public PackageAdviceScope(Set<String> packages) {
    this.packages = packages;
  }

  @Override
  public boolean applies(Element el) {
    while (el != null && !(el instanceof PackageElement)) {
      el = el.getEnclosingElement();
    }

    return el != null && this.packages.contains(((PackageElement) el).getQualifiedName().toString());
  }
}
