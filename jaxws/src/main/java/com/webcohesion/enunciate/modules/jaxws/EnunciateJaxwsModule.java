package com.webcohesion.enunciate.modules.jaxws;

import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.module.*;
import com.webcohesion.enunciate.modules.jaxb.EnunciateJaxbModule;
import org.reflections.adapters.MetadataAdapter;

import javax.jws.WebService;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
@SuppressWarnings ( "unchecked" )
public class EnunciateJaxwsModule extends BasicEnunicateModule implements TypeFilteringModule {

  private EnunciateJaxbModule jaxbModule;

  @Override
  public String getName() {
    return "jaxws";
  }

  @Override
  public List<DependencySpec> getDependencySpecifications() {
    return Arrays.asList((DependencySpec) new JaxbDependencySpec());
  }

  private boolean isForceJAXWSSpecCompliance() {
    return this.enunciate.getConfiguration().getSource().getBoolean(getConfigPath() + "[@forceJAXWSSpecCompliance]", false);
  }

  @Override
  public void call(EnunciateContext context) {
    EnunciateJaxwsContext jaxwsContext = new EnunciateJaxwsContext(this.jaxbModule.getJaxbContext(), isForceJAXWSSpecCompliance());
    Set<Element> elements = context.getApiElements();
    for (Element declaration : elements) {
      if (declaration instanceof TypeElement) {
        TypeElement element = (TypeElement) declaration;
        if (isEndpointInterface(element)) {

        }
      }
    }

//    List<EndpointInterface> endpoints = jaxwsContext.getEndpointInterfaces();
//    for (EndpointInterface endpoint : endpoints) {
//      addReferencedDataTypeDefinitions(endpoint, contextStack);
//    }
  }

//  protected void addReferencedDataTypeDefinitions(EndpointInterface endpoint) {
//    LinkedList<Element> contextStack = new LinkedList<Element>();
//    contextStack.push(endpoint);
//    try {
//
//    }
//    finally {
//      contextStack.pop();
//    }
//  }

  @Override
  public boolean acceptType(Object type, MetadataAdapter metadata) {
    List<String> classAnnotations = metadata.getClassAnnotationNames(type);
    if (classAnnotations != null) {
      for (String classAnnotation : classAnnotations) {
        if (WebService.class.getName().equals(classAnnotation)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * A quick check to see if a declaration is an endpoint interface.
   */
  public boolean isEndpointInterface(TypeElement declaration) {
    WebService ws = declaration.getAnnotation(WebService.class);
    return declaration.getAnnotation(XmlTransient.class) == null
      && ws != null
      && ((declaration.getKind() == ElementKind.INTERFACE)
      //if this is a class declaration, then it has an "implicit" endpoint interface if it doesn't reference another.
      || (ws.endpointInterface() == null) || ("".equals(ws.endpointInterface())));
  }

  public class JaxbDependencySpec implements DependencySpec {

    @Override
    public boolean accept(EnunciateModule module) {
      if (module instanceof EnunciateJaxbModule) {
        jaxbModule = ((EnunciateJaxbModule) module);
        jaxbModule.setDefaultDataTypeDetectionStrategy(MediaTypeDefinitionModule.DataTypeDetectionStrategy.PASSIVE);
        return true;
      }

      return false;
    }

    @Override
    public boolean isFulfilled() {
      return jaxbModule != null;
    }
  }
}
