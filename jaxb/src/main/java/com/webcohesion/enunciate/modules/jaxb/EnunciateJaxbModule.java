package com.webcohesion.enunciate.modules.jaxb;

import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.module.BasicEnunicateModule;
import com.webcohesion.enunciate.module.DependencySpec;
import com.webcohesion.enunciate.modules.jaxb.model.Registry;

import javax.lang.model.element.Element;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public class EnunciateJaxbModule extends BasicEnunicateModule {

  @Override
  public String getName() {
    return "jaxb";
  }

  @Override
  public List<DependencySpec> getDependencies() {
    return Collections.emptyList();
  }

  @Override
  public boolean isEnabled() {
    return !this.context.getConfiguration().getSource().getBoolean("enunciate.modules.jaxb[@disabled]")
      && (this.dependingModules == null || !this.dependingModules.isEmpty());
  }

  @Override
  public void call(EnunciateContext context) {
    Set<Element> elements = context.getApiElements();
    for (Element declaration : elements) {
      XmlRegistry registry = declaration.getAnnotation(XmlRegistry.class);
      if (registry != null) {
        debug("%s.%s to be considered as an XML registry.", packageOf(declaration), declaration.getSimpleName());
        Registry registryBuilder = new Registry(declaration);
        continue;
      }

      XmlRootElement xmlRootElement = declaration.getAnnotation(XmlRootElement.class);
      if (xmlRootElement != null) {
        debug("%s.%s to be considered as an XML root element.", packageOf(declaration), declaration.getSimpleName());
        //todo:
        continue;
      }
    }
  }

  protected CharSequence packageOf(Element declaration) {
    return this.context.getProcessingEnvironment().getElementUtils().getPackageOf(declaration).getQualifiedName();
  }

}
