package net.sf.enunciate.contract.jaxws;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.*;
import com.sun.mirror.type.ClassType;
import net.sf.enunciate.contract.ValidationException;
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
public class EndpointInterface extends DecoratedTypeDeclaration {

  private final javax.jws.WebService annotation;

  public EndpointInterface(TypeDeclaration delegate) {
    super(delegate);

    annotation = getAnnotation(javax.jws.WebService.class);
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
   * Calculates a namespace URI for a given package.  Default implementation uses the algorithm defined in
   * section 3.2 of the jax-ws spec.
   *
   * @return The calculated namespace uri.
   */
  protected String calculateNamespaceURI() {
    PackageDeclaration pkg = getPackage();
    if ((pkg == null) || ("".equals(pkg.getQualifiedName()))) {
      throw new ValidationException(getPosition(), "A web service in no package must specify a target namespace.");
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
    TreeSet<String> namespaces = new TreeSet<String>();
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
    ArrayList<WebMethod> webMethods = new ArrayList<WebMethod>();

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
            if ((isWebMethod(method)) &&
              //NOTE: the spec doesn't say anything about overriding methods.  So, because methods can't have the
              //same operation name, we're going to just exclude any methods of superclasses that have the same name.
              (!webMethods.contains(method))) {
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

    return webMethods;
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
    if (annotation == null) {
      return null;
    }

    ArrayList<EndpointImplementation> impls = new ArrayList<EndpointImplementation>();
    if (isClass()) {
      //if the declaration is a class, the endpoint interface is implied...
      impls.add(new EndpointImplementation((ClassDeclaration) getDelegate(), this));
    }
    else {
      for (TypeDeclaration declaration : getAnnotationProcessorEnvironment().getTypeDeclarations()) {
        if (isEndpointImplementation(declaration)) {
          WebService ws = declaration.getAnnotation(WebService.class);
          if (getQualifiedName().equals(ws.endpointInterface())) {
            impls.add(new EndpointImplementation((ClassDeclaration) declaration, this));
          }
        }
      }
    }
    return impls;
  }

  /**
   * A quick check to see if a declaration is an endpoint implementation.
   */
  protected boolean isEndpointImplementation(TypeDeclaration declaration) {
    return ((declaration instanceof ClassDeclaration) && (declaration.getAnnotation(WebService.class) != null));
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

      if (style == SOAPBinding.Style.RPC) {
        throw new UnsupportedOperationException(getPosition() + ": Sorry, " + bindingInfo.style() + "-style endpoint implementations aren't supported yet.");
      }
    }

    return style;
  }

  /**
   * A map of binding types to binding names for this endpoint implementation.
   *
   * @return A map of binding types to binding names for this endpoint implementation.
   */
  public Map<BindingType, String> getBindingNames() {
    Map<BindingType, String> bindingNames = new HashMap<BindingType, String>();
    bindingNames.put(BindingType.SOAP_1_1, getSimpleName() + "SOAPBinding");
    bindingNames.put(BindingType.SOAP_1_2, getSimpleName() + "SOAPBinding");
    bindingNames.put(BindingType.HTTP, getSimpleName() + "HTTPBinding");
    return bindingNames;
  }

  /**
   * The binding name for the specified binding type.
   *
   * @param bindingType The binding type.
   * @return The binding name for the specified binding type.
   */
  public String getBindingNameFor(BindingType bindingType) {
    return getBindingNames().get(bindingType);
  }

  /**
   * A map of binding types to port names for this endpoint implementation.
   *
   * @return A map of binding types to port names for this endpoint implementation.
   */
  public Map<BindingType, String> getPortNames() {
    Map<BindingType, String> portNames = new HashMap<BindingType, String>();
    portNames.put(BindingType.SOAP_1_1, getSimpleName() + "SOAPPort");
    portNames.put(BindingType.SOAP_1_2, getSimpleName() + "SOAPPort");
    portNames.put(BindingType.HTTP, getSimpleName() + "HTTPPort");
    return portNames;
  }

  /**
   * The port name for the given binding type.
   *
   * @param bindingType The binding type.
   * @return The port name for the given binding type.
   */
  public String getPortNameFor(BindingType bindingType) {
    return getPortNames().get(bindingType);
  }

  /**
   * A map of binding types to port addresses for this endpoint implementation.
   *
   * @return A map of binding types to port addresses for this endpoint implementation.
   */
  public Map<BindingType, String> getPortAddresses() {
    Map<BindingType, String> portAddresses = new HashMap<BindingType, String>();
    portAddresses.put(BindingType.SOAP_1_1, "TBD");
    portAddresses.put(BindingType.SOAP_1_2, "TBD");
    portAddresses.put(BindingType.HTTP, "TBD");
    return portAddresses;
  }

  /**
   * The port address for the specified binding type.
   *
   * @param bindingType The binding type.
   * @return The port address for the specified binding type.
   */
  public String getPortAddressFor(BindingType bindingType) {
    return getPortAddresses().get(bindingType);
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
   * The current annotation processing environment.
   *
   * @return The current annotation processing environment.
   */
  protected AnnotationProcessorEnvironment getAnnotationProcessorEnvironment() {
    return Context.getCurrentEnvironment();
  }

}
