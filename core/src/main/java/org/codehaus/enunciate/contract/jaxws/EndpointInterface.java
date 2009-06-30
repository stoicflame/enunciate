/*
 * Copyright 2006-2008 Web Cohesion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.enunciate.contract.jaxws;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.*;
import com.sun.mirror.type.ClassType;
import org.codehaus.enunciate.contract.validation.ValidationException;
import org.codehaus.enunciate.contract.ServiceEndpoint;
import org.codehaus.enunciate.util.TypeDeclarationComparator;
import org.codehaus.enunciate.ClientName;
import net.sf.jelly.apt.Context;
import net.sf.jelly.apt.decorations.declaration.DecoratedTypeDeclaration;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import java.util.*;

/**
 * A type or interface specified as a web service endpoint interface.  This decorator can only be applied to either interfaces or classes
 * that implicitly define an endpoint interface (see spec, section 3.3).
 *
 * @author Ryan Heaton
 */
public class EndpointInterface extends DecoratedTypeDeclaration implements ServiceEndpoint {

  private final javax.jws.WebService annotation;
  private final List<WebMethod> webMethods;
  private final Collection<EndpointImplementation> impls;
  private final Map<String, Object> metaData = new HashMap<String, Object>();

  /**
   * Construct an endoint interface.
   *
   * @param delegate The delegate.
   * @param implementationCandidates The type declarations to be considered as implementation candidates (the ones that can't be seen by APT.)
   */
  public EndpointInterface(TypeDeclaration delegate, TypeDeclaration... implementationCandidates) {
    super(delegate);

    annotation = getAnnotation(javax.jws.WebService.class);
    impls = new ArrayList<EndpointImplementation>();
    if (annotation != null) {
      if (isClass()) {
        //if the declaration is a class, the endpoint interface is implied...
        impls.add(new EndpointImplementation((ClassDeclaration) getDelegate(), this));
      }
      else {
        Set<TypeDeclaration> potentialImpls = new TreeSet<TypeDeclaration>(new TypeDeclarationComparator());
        potentialImpls.addAll(getAnnotationProcessorEnvironment().getTypeDeclarations());
        if (implementationCandidates != null) {
          potentialImpls.addAll(Arrays.asList(implementationCandidates));
        }
        for (TypeDeclaration declaration : potentialImpls) {
          if (isEndpointImplementation(declaration)) {
            WebService ws = declaration.getAnnotation(WebService.class);
            if (getQualifiedName().equals(ws.endpointInterface())) {
              impls.add(new EndpointImplementation((ClassDeclaration) declaration, this));
            }
          }
        }
      }
    }

    List<WebMethod> webMethods = new ArrayList<WebMethod>();
    for (MethodDeclaration method : getMethods()) {
      if (isWebMethod(method)) {
        webMethods.add(new WebMethod(method, this));
      }
    }

    if (isClass()) {
      //the spec says we need to consider superclass methods, too...
      ClassType superclass = ((ClassDeclaration) getDelegate()).getSuperclass();
      if (superclass != null) {
        ClassDeclaration declaration = superclass.getDeclaration();
        while ((declaration != null) && (!Object.class.getName().equals(declaration.getQualifiedName()))) {
          for (MethodDeclaration method : declaration.getMethods()) {
            if (isWebMethod(method)) {
              //todo: if this method is overridden, don't add it.
              webMethods.add(new WebMethod(method, this));
            }
          }

          superclass = declaration.getSuperclass();
          if (superclass == null) {
            declaration = null;
          }
          else {
            declaration = superclass.getDeclaration();
          }
        }
      }
    }

    this.webMethods = webMethods;
  }

  // Inherited.
  public String getServiceEndpointId() {
    return "enunciate:service:" + getSimpleName();
  }

  // Inherited.
  public TypeDeclaration getServiceEndpointInterface() {
    return this;
  }

  // Inherited.
  public TypeDeclaration getServiceEndpointDefaultImplementation() {
    return impls.isEmpty() ? null : impls.iterator().next();
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
      name = getSimpleName();
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

    if ((serviceName == null) || ("".equals(serviceName))) {
      serviceName = getSimpleName() + "Service";
    }

    return serviceName;
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
    String clientSimpleName = getSimpleName();
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
    PackageDeclaration pkg = getPackage();
    if ((pkg == null) || ("".equals(pkg.getQualifiedName()))) {
      throw new ValidationException(getPosition(), getQualifiedName() + ": a web service in no package must specify a target namespace.");
    }

    String[] tokens = pkg.getQualifiedName().split("\\.");
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
  public Collection<WebMethod> getWebMethods() {
    return this.webMethods;
  }

  /**
   * A quick check to see if a method is a web method.
   */
  public boolean isWebMethod(MethodDeclaration method) {
    boolean isWebMethod = method.getModifiers().contains(Modifier.PUBLIC);
    javax.jws.WebMethod annotation = method.getAnnotation(javax.jws.WebMethod.class);
    if (annotation != null) {
      isWebMethod &= !annotation.exclude();
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
  protected boolean isEndpointImplementation(TypeDeclaration declaration) {
    if (declaration instanceof ClassDeclaration && !declaration.getQualifiedName().equals(getQualifiedName())) {
      WebService webServiceInfo = declaration.getAnnotation(WebService.class);
      return webServiceInfo != null && getQualifiedName().equals(webServiceInfo.endpointInterface());
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

  // Inherited.
  public boolean isClass() {
    return (getDelegate() instanceof ClassDeclaration);
  }

  // Inherited.
  public boolean isInterface() {
    return (getDelegate() instanceof InterfaceDeclaration);
  }

  // Inherited.
  public boolean isEnum() {
    return (getDelegate() instanceof EnumDeclaration);
  }

  // Inherited.
  public boolean isAnnotatedType() {
    return (getDelegate() instanceof AnnotationTypeDeclaration);
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
   * The current annotation processing environment.
   *
   * @return The current annotation processing environment.
   */
  protected AnnotationProcessorEnvironment getAnnotationProcessorEnvironment() {
    return Context.getCurrentEnvironment();
  }

}
