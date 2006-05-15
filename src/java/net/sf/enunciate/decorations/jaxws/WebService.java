package net.sf.enunciate.decorations.jaxws;

import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.PackageDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import net.sf.jelly.apt.decorations.declaration.DecoratedTypeDeclaration;

import javax.jws.soap.SOAPBinding;
import java.util.*;

/**
 * A type or interface specified as a web service.
 *
 * @author Ryan Heaton
 */
public class WebService extends DecoratedTypeDeclaration {

  private final javax.jws.WebService annotation;

  public WebService(TypeDeclaration delegate) {
    super(delegate);

    if (!isWebService(delegate)) {
      throw new IllegalArgumentException(delegate + " is not a web service!");
    }

    annotation = getAnnotation(javax.jws.WebService.class);

    if (isInterface() && !isEndpointInterface()) {
      throw new IllegalArgumentException(getAnnotations().get(javax.jws.WebService.class.getName()).getPosition() +
          ": an endpoint interface must not be specified on an interface type declaration.");
    }
  }

  /**
   * Whether a declaration is a web service.
   *
   * @param declaration The declaration to consider as a web service.
   * @return Whether a declaration is a web service.
   */
  public static boolean isWebService(TypeDeclaration declaration) {
    return (declaration.getAnnotation(javax.jws.WebService.class) != null);
  }

  /**
   * The name of this web service.
   *
   * @return The name of this web service.
   */
  public String getName() {
    String name = annotation.name();

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
    String serviceName = annotation.serviceName();

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
    String targetNamespace = annotation.targetNamespace();

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
    if (pkg == null) {
      throw new IllegalStateException(getPosition() + ": A web service in no package must specify a target namespace.");
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
   * The endpoint interface specified for this web service, or null if endpoint interface isn't specified.
   *
   * @return The endpoint interface specified for this web service
   */
  public String getEndpointInterface() {
    String endpointInterface = annotation.endpointInterface();

    if ((endpointInterface == null) || ("".equals(endpointInterface))) {
      endpointInterface = null;
    }

    return endpointInterface;
  }

  /**
   * Whether this web service is an endpoint interface.  If it is <i>not</i> an endpoint interface, it
   * is assumed to be an endpoint implementation. See JAXRPC 2.0 specification, section 3.2.
   *
   * @return Whether this web service is an endpoint interface.
   */
  public boolean isEndpointInterface() {
    return getEndpointInterface() == null;
  }

  /**
   * Whether this web service is an endpoint implementation.  If it is <i>not</i> an endpoint interface, it
   * is assumed to be an endpoint implementation. See JAXRPC 2.0 specification, section 3.2.
   *
   * @return Whether this web service is an endpoint implementation.
   */
  public boolean isEndpointImplmentation() {
    return !isEndpointInterface();
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
      if (WebMethod.isWebMethod(method)) {
        webMethods.add(new WebMethod(method));
      }
    }

    return webMethods;
  }

  /**
   * The port name for this web service.
   *
   * @return The port name for this web service.
   */
  public String getPortName() {
    String portName = annotation.portName();

    if ((portName == null) || ("".equals(portName))) {
      portName = getName() + "Port";
    }

    return portName;
  }

  /**
   * Get the binding type for this web service, or null if none is specified.
   *
   * @return The binding type for this web service.
   */
  public String getBindingType() {
    javax.xml.ws.BindingType bindingType = getAnnotation(javax.xml.ws.BindingType.class);

    if (bindingType != null) {
      if ((bindingType.value() != null) && (!"".equals(bindingType.value()))) {
        return bindingType.value();
      }
    }

    return null;
  }

  /**
   * A map of binding types to binding names for this web service.
   *
   * @return A map of binding types to binding names for this web service.
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
   * A map of binding types to port names for this web service.
   *
   * @return A map of binding types to port names for this web service.
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
   * A map of binding types to port addresses for this web service.
   *
   * @return A map of binding types to port addresses for this web service.
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

  /**
   * The SOAP binding style of this web service.
   *
   * @return The SOAP binding style of this web service.
   */
  public SOAPBinding.Style getSoapBindingStyle() {
    SOAPBinding.Style style = SOAPBinding.Style.DOCUMENT;
    SOAPBinding bindingInfo = getAnnotation(SOAPBinding.class);

    if (bindingInfo != null) {
      style = bindingInfo.style();

      if (style == SOAPBinding.Style.RPC) {
        throw new UnsupportedOperationException(getPosition() + ": Sorry, " + bindingInfo.style() + "-style web services aren't supported yet.");
      }
    }

    return style;
  }

}
