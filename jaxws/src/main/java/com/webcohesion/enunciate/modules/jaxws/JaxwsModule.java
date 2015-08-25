package com.webcohesion.enunciate.modules.jaxws;

import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.api.ApiRegistry;
import com.webcohesion.enunciate.module.*;
import com.webcohesion.enunciate.modules.jaxb.JaxbModule;
import com.webcohesion.enunciate.modules.jaxb.model.ImplicitChildElement;
import com.webcohesion.enunciate.modules.jaxws.model.*;
import org.reflections.adapters.MetadataAdapter;

import javax.jws.WebService;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.xml.bind.annotation.XmlTransient;
import java.util.*;

/**
 * @author Ryan Heaton
 */
@SuppressWarnings ( "unchecked" )
public class JaxwsModule extends BasicEnunicateModule implements TypeFilteringModule, ApiRegistryAwareModule, ApiProviderModule {

  private JaxbModule jaxbModule;
  private ApiRegistry apiRegistry;
  private EnunciateJaxwsContext jaxwsContext;

  @Override
  public String getName() {
    return "jaxws";
  }

  @Override
  public List<DependencySpec> getDependencySpecifications() {
    return Arrays.asList((DependencySpec) new JaxbDependencySpec());
  }

  public EnunciateJaxwsContext getJaxwsContext() {
    return jaxwsContext;
  }

  private boolean isUseSourceParameterNames() {
    return this.config.getBoolean("[@useSourceParameterNames]", false);
  }

  private boolean isAggressiveWebMethodExcludePolicy() {
    return this.config.getBoolean("[@aggressiveWebMethodExcludePolicy]", false);
  }

  @Override
  public void setApiRegistry(ApiRegistry registry) {
    this.apiRegistry = registry;
  }

  @Override
  public void call(EnunciateContext context) {
    jaxwsContext = new EnunciateJaxwsContext(this.jaxbModule.getJaxbContext(), isUseSourceParameterNames());
    boolean aggressiveWebMethodExcludePolicy = isAggressiveWebMethodExcludePolicy();
    Set<Element> elements = context.getApiElements();
    for (Element declaration : elements) {
      if (declaration instanceof TypeElement) {
        TypeElement element = (TypeElement) declaration;
        if (isEndpointInterface(element)) {
          jaxwsContext.add(new EndpointInterface(element, elements, aggressiveWebMethodExcludePolicy, jaxwsContext));
        }
      }
    }

    List<EndpointInterface> endpoints = jaxwsContext.getEndpointInterfaces();
    for (EndpointInterface endpoint : endpoints) {
      addReferencedDataTypeDefinitions(endpoint);
    }

    if (jaxwsContext.getEndpointInterfaces().size() > 0) {
      this.apiRegistry.getServiceApis().add(jaxwsContext);
      if (!this.apiRegistry.getSyntaxes().contains(jaxwsContext.getJaxbContext())) {
        this.apiRegistry.getSyntaxes().add(jaxwsContext.getJaxbContext());
      }
    }
  }

  protected void addReferencedDataTypeDefinitions(EndpointInterface ei) {
    LinkedList<Element> contextStack = new LinkedList<Element>();
    contextStack.push(ei);
    try {
      for (WebMethod webMethod : ei.getWebMethods()) {
        addReferencedTypeDefinitions(webMethod, contextStack);
      }
    }
    finally {
      contextStack.pop();
    }
  }

  protected void addReferencedTypeDefinitions(WebMethod webMethod, LinkedList<Element> contextStack) {
    contextStack.push(webMethod);
    try {
      WebResult result = webMethod.getWebResult();
      this.jaxbModule.getJaxbContext().addReferencedTypeDefinitions(result.isAdapted() ? result.getAdapterType() : result.getType(), contextStack);
      for (WebParam webParam : webMethod.getWebParameters()) {
        this.jaxbModule.getJaxbContext().addReferencedTypeDefinitions(webParam.isAdapted() ? webParam.getAdapterType() : webParam.getType(), contextStack);
      }
      for (WebFault webFault : webMethod.getWebFaults()) {
        addReferencedTypeDefinitions(webFault, contextStack);
      }
    }
    finally {
      contextStack.pop();
    }
  }

  protected void addReferencedTypeDefinitions(WebFault webFault, LinkedList<Element> contextStack) {
    contextStack.push(webFault);
    try {
      if (webFault.isImplicitSchemaElement()) {
        for (ImplicitChildElement childElement : webFault.getChildElements()) {
          WebFault.FaultBeanChildElement fbce = (WebFault.FaultBeanChildElement) childElement;
          this.jaxbModule.getJaxbContext().addReferencedTypeDefinitions(fbce.isAdapted() ? fbce.getAdapterType() : fbce.getType(), contextStack);
        }
      }
      else {
        DeclaredType faultBeanType = webFault.getExplicitFaultBeanType();
        if (faultBeanType != null) {
          this.jaxbModule.getJaxbContext().addReferencedTypeDefinitions(faultBeanType, contextStack);
        }
      }
    }
    finally {
      contextStack.pop();
    }
  }

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
      if (module instanceof JaxbModule) {
        jaxbModule = ((JaxbModule) module);
        jaxbModule.setDefaultDataTypeDetectionStrategy(MediaTypeDefinitionModule.DataTypeDetectionStrategy.passive);
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
