/**
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webcohesion.enunciate.modules.jaxws.model;

import com.webcohesion.enunciate.EnunciateException;
import com.webcohesion.enunciate.facets.Facet;
import com.webcohesion.enunciate.facets.HasFacets;
import com.webcohesion.enunciate.javac.TypeElementComparator;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedTypeElement;
import com.webcohesion.enunciate.javac.decorations.type.TypeVariableContext;
import com.webcohesion.enunciate.metadata.ClientName;
import com.webcohesion.enunciate.metadata.soap.SoapBindingName;
import com.webcohesion.enunciate.modules.jaxws.EnunciateJaxwsContext;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import java.util.*;

/**
 * A type or interface specified as a web service endpoint interface.  This decorator can only be applied to either interfaces or classes
 * that implicitly define an endpoint interface (see spec, section 3.3).
 *
 * @author Ryan Heaton
 */
public class EndpointInterface extends DecoratedTypeElement implements HasFacets {

  private final javax.jws.WebService annotation;
  private final List<WebMethod> webMethods;
  private final Collection<EndpointImplementation> impls;
  private final Map<String, Object> metaData = new HashMap<String, Object>();
  private final Set<Facet> facets = new TreeSet<Facet>();
  private final boolean aggressiveWebMethodExcludePolicy;
  private final EnunciateJaxwsContext context;

  /**
   * Construct an endoint interface.
   *
   * @param delegate The delegate.
   * @param implementationCandidates The type declarations to be considered as implementation candidates (the ones that can't be seen by APT.)
   */
  public EndpointInterface(TypeElement delegate, Set<? extends Element> implementationCandidates, EnunciateJaxwsContext context) {
    this(delegate, implementationCandidates, false, context);
  }

  /**
   * Construct an endoint interface.
   *
   * @param delegate The delegate.
   * @param implementationCandidates The type declarations to be considered as implementation candidates (the ones that can't be seen by APT.)
   * @param aggressiveWebMethodExcludePolicy Whether an aggressive policy for excluding web methods should be used. See https://jira.codehaus.org/browse/ENUNCIATE-796.
   */
  public EndpointInterface(TypeElement delegate, Set<? extends Element> implementationCandidates, boolean aggressiveWebMethodExcludePolicy, EnunciateJaxwsContext context) {
    super(delegate, context.getContext().getProcessingEnvironment());
    this.context = context;
    this.aggressiveWebMethodExcludePolicy = aggressiveWebMethodExcludePolicy;

    this.facets.addAll(Facet.gatherFacets(delegate, context.getContext()));
    annotation = getAnnotation(javax.jws.WebService.class);
    impls = new ArrayList<EndpointImplementation>();
    if (annotation != null) {
      if (isClass()) {
        //if the declaration is a class, the endpoint interface is implied...
        impls.add(new EndpointImplementation(getDelegate(), this, context));
      }
      else {
        Set<TypeElement> potentialImpls = new TreeSet<TypeElement>(new TypeElementComparator());
        potentialImpls.addAll(ElementFilter.typesIn(context.getContext().getApiElements()));
        if (implementationCandidates != null) {
          potentialImpls.addAll(ElementFilter.typesIn(implementationCandidates));
        }
        for (TypeElement declaration : potentialImpls) {
          if (isEndpointImplementation(declaration)) {
            WebService ws = declaration.getAnnotation(WebService.class);
            if (getQualifiedName().toString().equals(ws.endpointInterface())) {
              impls.add(new EndpointImplementation(declaration, this, context));
            }
          }
        }
      }
    }

    TypeVariableContext variableContext = new TypeVariableContext();
    List<WebMethod> webMethods = new ArrayList<WebMethod>();
    for (ExecutableElement method : getMethods()) {
      if (isWebMethod(method)) {
        webMethods.add(new WebMethod(method, this, context, variableContext));
      }
    }

    if (delegate.getKind() == ElementKind.CLASS) {
      //the spec says we need to consider superclass methods, too...
      TypeMirror superclass = delegate.getSuperclass();
      if (superclass instanceof DeclaredType) {
        Element declaration = ((DeclaredType) superclass).asElement();
        if (declaration instanceof TypeElement) {
          while ((declaration != null) && (!Object.class.getName().equals(((TypeElement)declaration).getQualifiedName().toString()))) {
            variableContext = variableContext.push(((TypeElement) declaration).getTypeParameters(), ((DeclaredType) superclass).getTypeArguments());
            for (ExecutableElement method : ElementFilter.methodsIn(declaration.getEnclosedElements())) {
              if (isWebMethod(method)) {
                //todo: if this method is overridden, don't add it.
                webMethods.add(new WebMethod(method, this, context, variableContext));
              }
            }

            superclass = ((TypeElement)declaration).getSuperclass();
            if (superclass == null || superclass.getKind() == TypeKind.NONE) {
              declaration = null;
            }
            else {
              declaration = ((DeclaredType) superclass).asElement();
            }
          }
        }
      }
    }

    this.webMethods = webMethods;
  }

  public EnunciateJaxwsContext getContext() {
    return context;
  }

  /**
   * The name of this web service.
   *
   * @return The name of this web service.
   */
  public String getPortTypeName() {
    String name = null;

    if (annotation != null) {
      name = annotation.name();
    }

    if ((name == null) || ("".equals(name))) {
      name = getSimpleName().toString();
    }

    return name;
  }

  /**
   * The service name of this web service.
   *
   * @return The service name of this web service.
   */
  public String getServiceName() {
    String serviceName = null;

    if (annotation != null) {
      serviceName = annotation.serviceName();
    }

    if (((serviceName == null) || ("".equals(serviceName))) && getEndpointImplementations().size() == 1) {
      WebService implAnnotation = getEndpointImplementations().iterator().next().getAnnotation(WebService.class);
      if (implAnnotation != null) {
        serviceName = implAnnotation.serviceName();
      }
    }

    if ((serviceName == null) || ("".equals(serviceName))) {
      serviceName = getSimpleName() + "Service";
    }

    return serviceName;
  }

  /**
   * The path where this service is mounted.
   *
   * @return The path where this service is mounted.
   */
  public String getPath() {
    for (EndpointImplementation implementation : getEndpointImplementations()) {
      String path = implementation.getPath();
      if (path != null) {
        return path;
      }
    }

    return "/" + getServiceName();
  }

  /**
   * Gets the target namespace of this web service.
   *
   * @return the target namespace of this web service.
   */
  public String getTargetNamespace() {
    String targetNamespace = null;

    if (annotation != null) {
      targetNamespace = annotation.targetNamespace();
    }

    if ((targetNamespace == null) || ("".equals(targetNamespace))) {
      targetNamespace = calculateNamespaceURI();
    }

    return targetNamespace;
  }

  /**
   * The simple name for client-side code generation.
   *
   * @return The simple name for client-side code generation.
   */
  public String getClientSimpleName() {
    String clientSimpleName = getSimpleName().toString();
    ClientName clientName = getAnnotation(ClientName.class);
    if (clientName != null) {
      clientSimpleName = clientName.value();
    }
    return clientSimpleName;
  }

  /**
   * Calculates a namespace URI for a given package.  Default implementation uses the algorithm defined in
   * section 3.2 of the jax-ws spec.
   *
   * @return The calculated namespace uri.
   */
  protected String calculateNamespaceURI() {
    PackageElement pkg = getPackage();
    if ((pkg == null) || ("".equals(pkg.getQualifiedName().toString()))) {
      throw new EnunciateException(getQualifiedName() + ": a web service in no package must specify a target namespace.");
    }

    String[] tokens = pkg.getQualifiedName().toString().split("\\.");
    String uri = "http://";
    for (int i = tokens.length - 1; i >= 0; i--) {
      uri += tokens[i];
      if (i != 0) {
        uri += ".";
      }
    }
    uri += "/";
    return uri;
  }

  /**
   * The set of namespace URIs that this web service references.
   *
   * @return The set of namespace URIs that this web service references.
   */
  public Set<String> getReferencedNamespaces() {
    HashSet<String> namespaces = new HashSet<String>();
    namespaces.add(getTargetNamespace());
    Collection<WebMethod> webMethods = getWebMethods();
    for (WebMethod webMethod : webMethods) {
      namespaces.addAll(webMethod.getReferencedNamespaces());
    }
    return namespaces;
  }

  /**
   * Get the web methods for this web service.
   *
   * @return the web methods for this web service.
   */
  public List<WebMethod> getWebMethods() {
    return this.webMethods;
  }

  /**
   * A quick check to see if a method is a web method.
   */
  public boolean isWebMethod(ExecutableElement method) {
    boolean isWebMethod = method.getModifiers().contains(Modifier.PUBLIC);
    javax.jws.WebMethod annotation = method.getAnnotation(javax.jws.WebMethod.class);
    if (annotation != null) {
      isWebMethod &= !annotation.exclude();
    }
    else if (this.aggressiveWebMethodExcludePolicy) {
      isWebMethod = false;
    }

    return isWebMethod;
  }

  /**
   * Finds the endpoint implemenations of this interface.
   *
   * @return The endpoint implementations of this interface.
   */
  public Collection<EndpointImplementation> getEndpointImplementations() {
    return this.impls;
  }

  /**
   * A quick check to see if a declaration is an endpoint implementation.
   */
  protected boolean isEndpointImplementation(TypeElement declaration) {
    if (declaration.getKind() == ElementKind.CLASS && !declaration.getQualifiedName().equals(getQualifiedName())) {
      WebService webServiceInfo = declaration.getAnnotation(WebService.class);
      return webServiceInfo != null && getQualifiedName().toString().equals(webServiceInfo.endpointInterface());
    }

    return false;
  }

  /**
   * The SOAP binding style specified on this endpoint interface.
   * <p/>
   * Note: the specification is unclear on how to deal with the specific binding annotations of the
   * associated endpoint implementation classes.  It is a assumed that the annotations of the endpoint
   * implementation classes override the annotations on the endpoint interface.
   *
   * @return The SOAP binding style specified on this endpoint interface.
   */
  public SOAPBinding.Style getSoapBindingStyle() {
    SOAPBinding.Style style = SOAPBinding.Style.DOCUMENT;
    SOAPBinding bindingInfo = getAnnotation(SOAPBinding.class);

    if (bindingInfo != null) {
      style = bindingInfo.style();
    }

    return style;
  }

  public Collection<BindingType> getBindingTypes() {
    ArrayList<BindingType> bindingTypes = new ArrayList<BindingType>();

    for (EndpointImplementation implementation : getEndpointImplementations()) {
      bindingTypes.add(implementation.getBindingType());
    }

    if (bindingTypes.isEmpty()) {
      //spec says if no bindings are present, use SOAP 1.1
      bindingTypes.add(BindingType.SOAP_1_1);
    }

    return bindingTypes;
  }
  /**
   * The name of the soap binding. This is just used in the WSDL, so it's not really necessary-- it's more for aesthetic purposes.
   *
   * @return The name of the soap binding.
   * @see com.webcohesion.enunciate.metadata.soap.SoapBindingName
   */
  public String getSoapBindingName() {
    String name = getSimpleName() + "PortBinding";
    SoapBindingName bindingNameInfo = getAnnotation(SoapBindingName.class);
    if (bindingNameInfo != null) {
      name = bindingNameInfo.value();
    }
    return name;
  }

  /**
   * The SOAP binding use of this web method.
   *
   * @return The SOAP binding use of this web method.
   */
  public SOAPBinding.Use getSoapUse() {
    SOAPBinding.Use use = SOAPBinding.Use.LITERAL;
    SOAPBinding bindingInfo = getAnnotation(SOAPBinding.class);

    if (bindingInfo != null) {
      use = bindingInfo.use();
    }

    return use;
  }

  /**
   * The SOAP parameter style of this web method.
   *
   * @return The SOAP parameter style of this web method.
   */
  public SOAPBinding.ParameterStyle getSoapParameterStyle() {
    SOAPBinding.ParameterStyle style = SOAPBinding.ParameterStyle.WRAPPED;
    SOAPBinding bindingInfo = getAnnotation(SOAPBinding.class);

    if (bindingInfo != null) {
      style = bindingInfo.parameterStyle();
    }

    return style;
  }

  /**
   * The metadata associated with this endpoint interface.
   *
   * @return The metadata associated with this endpoint interface.
   */
  public Map<String, Object> getMetaData() {
    return Collections.unmodifiableMap(this.metaData);
  }

  /**
   * Set the metadata associated with this endpoint interface.
   *
   * @param name The name of the metadata.
   * @param data The data.
   */
  public void putMetaData(String name, Object data) {
    this.metaData.put(name, data);
  }

  /**
   * The facets here applicable.
   *
   * @return The facets here applicable.
   */
  public Set<Facet> getFacets() {
    return facets;
  }

}
