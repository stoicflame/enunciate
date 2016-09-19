package com.webcohesion.enunciate.modules.lombok;

import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedTypeElement;
import com.webcohesion.enunciate.module.BasicEnunicateModule;
import com.webcohesion.enunciate.module.ContextModifyingModule;
import lombok.Data;

import javax.lang.model.element.Element;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public class LombokModule extends BasicEnunicateModule implements ContextModifyingModule {

  @Override
  public String getName() {
    return "lombok";
  }

  @Override
  public void call(EnunciateContext context) {
    Set<Element> apiElements = context.getApiElements();
    for (Element apiElement : apiElements) {
      if (apiElement instanceof DecoratedTypeElement && apiElement.getAnnotation(Data.class) != null) {
        DecoratedTypeElement typeElement = (DecoratedTypeElement) apiElement;
        //todo: modify the element as needed

      }
    }
  }

}
