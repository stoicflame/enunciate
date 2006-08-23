package net.sf.enunciate.contract.jaxws;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.*;
import com.sun.mirror.type.ClassType;
import net.sf.enunciate.contract.validation.ValidationException;
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
  private final List<WebMethod> webMethods;
  private final Collection<EndpointImplementation> impls;

  public EndpointInterface(TypeDeclaration delegate) {
    super(delegate);

    annotation = getAnnotation(javax.jws.WebService.class);
    impls = new ArrayList<EndpointImplementation>();
    if (annotation != null) {
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
              //todo: care about overridden methods?
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
        throw new ValidationException(getPosition(), "Sorry, " + bindingInfo.style() + "-style endpoint implementations aren't supported yet.");
      }
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
   * The current annotation processing environment.
   *
   * @return The current annotation processing environment.
   */
  protected AnnotationProcessorEnvironment getAnnotationProcessorEnvironment() {
    return Context.getCurrentEnvironment();
  }

}
