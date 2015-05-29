package com.webcohesion.enunciate.modules.jaxrs;

import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.module.BasicEnunicateModule;
import com.webcohesion.enunciate.module.DependencySpec;
import com.webcohesion.enunciate.module.TypeFilteringModule;
import com.webcohesion.enunciate.modules.jaxrs.model.RootResource;
import org.reflections.adapters.MetadataAdapter;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
@SuppressWarnings ( "unchecked" )
public class EnunciateJaxrsModule extends BasicEnunicateModule implements TypeFilteringModule {

  @Override
  public String getName() {
    return "jaxrs";
  }

  @Override
  public List<DependencySpec> getDependencies() {
    return Collections.emptyList();
  }

  @Override
  public boolean isEnabled() {
    return !this.enunciate.getConfiguration().getSource().getBoolean("enunciate.modules.jaxb[@disabled]", false)
      && (this.dependingModules == null || !this.dependingModules.isEmpty());
  }

  @Override
  public void call(EnunciateContext context) {
    EnunciateJaxrsContext jaxrsContext = new EnunciateJaxrsContext(context);
    Set<Element> elements = context.getApiElements();
    for (Element declaration : elements) {
      if (declaration instanceof TypeElement) {
        TypeElement element = (TypeElement) declaration;
        Path pathInfo = declaration.getAnnotation(Path.class);
        if (pathInfo != null) {
          //add root resource.
          RootResource rootResource = new RootResource(element, jaxrsContext);
          debug("%s to be considered as a JAX-RS root resource.", element.getQualifiedName());
          jaxrsContext.add(rootResource);
        }

        Provider providerInfo = declaration.getAnnotation(Provider.class);
        if (providerInfo != null) {
          //add jax-rs provider
          debug("%s to be considered as a JAX-RS provider.", element.getQualifiedName());
          jaxrsContext.addJAXRSProvider(element);
        }

        ApplicationPath applicationPathInfo = declaration.getAnnotation(ApplicationPath.class);
        if (applicationPathInfo != null) {
          //todo: configure application path
        }
      }
    }
  }

  @Override
  public boolean acceptType(Object type, MetadataAdapter metadata) {
    List<String> classAnnotations = metadata.getClassAnnotationNames(type);
    if (classAnnotations != null) {
      for (String classAnnotation : classAnnotations) {
        if ((Path.class.getName().equals(classAnnotation))
          || (Provider.class.getName().equals(classAnnotation))
          || (ApplicationPath.class.getName().equals(classAnnotation))) {
          return true;
        }
      }
    }
    return false;
  }
}
