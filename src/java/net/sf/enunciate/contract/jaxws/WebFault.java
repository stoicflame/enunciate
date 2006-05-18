package net.sf.enunciate.contract.jaxws;

import com.sun.mirror.declaration.PackageDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import net.sf.jelly.apt.decorations.declaration.DecoratedTypeDeclaration;

/**
 * A fault that is declared potentially thrown in some web service call.
 *
 * @author Ryan Heaton
 */
public class WebFault extends DecoratedTypeDeclaration implements SimpleWebMessage {

  private javax.xml.ws.WebFault annotation;

  public WebFault(TypeDeclaration delegate) {
    super(delegate);

    this.annotation = getAnnotation(javax.xml.ws.WebFault.class);
  }

  /**
   * The message name of this fault.
   *
   * @return The message name of this fault.
   */
  public String getMessageName() {
    return getName();
  }

  /**
   * The name of this web service.
   *
   * @return The name of this web service.
   */
  public String getName() {
    String name = getSimpleName();

    if ((annotation != null) && (annotation.name() != null) && (!"".equals(annotation.name()))) {
      name = annotation.name();
    }

    return name;
  }

  /**
   * The part name of this web fault as it would appear in wsdl.
   *
   * @return The part name of this web fault as it would appear in wsdl.
   */
  public String getPartName() {
    return getSimpleName();
  }

  /**
   * The target namespace of this web service.
   *
   * @return The target namespace of this web service.
   */
  public String getTargetNamespace() {
    String targetNamespace;

    if (annotation != null) {
      targetNamespace = annotation.targetNamespace();
    }
    else {
      targetNamespace = calculateNamespaceURI();
    }

    return targetNamespace;
  }

  /**
   * Calculates a namespace URI for a given package.  Default implementation uses the algorithm defined in
   * section 3.2 of the jax-ws spec.  The spec is unclear to the default namespace of a web fault, whether its
   * based off the package or based off the JAXB 2.0 default namespace of its fault bean.  We're assuming
   * calculated from the package like the web service.
   *
   * @return The calculated namespace uri.
   */
  protected String calculateNamespaceURI() {
    //The spec is unclear to the default namespace of a web fault, whether its
    //based off the package or based off the JAXB 2.0 default namespace of its fault bean.  We're assuming
    //calculated from the package like the web service.

    PackageDeclaration pkg = getPackage();
    if (pkg == null) {
      throw new IllegalStateException(getPosition() + ": A web fault in no package must specify a target namespace.");
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
   * The fault bean of this web service.
   *
   * @return The fault bean of this web service.
   */
  public String getFaultBean() {
    String faultBean = getPackage().getQualifiedName() + ".jaxws." + getSimpleName() + "Bean";

    if (annotation != null) {
      faultBean = annotation.faultBean();
    }

    return faultBean;
  }

  /**
   * @return false
   */
  public boolean isSimple() {
    return true;
  }

  /**
   * @return false
   */
  public boolean isComplex() {
    return false;
  }

  /**
   * @return false
   */
  public boolean isInput() {
    return false;
  }

  /**
   * @return true
   */
  public boolean isOutput() {
    return true;
  }

  /**
   * @return false
   */
  public boolean isHeader() {
    return false;
  }

  /**
   * @return true
   */
  public boolean isFault() {
    return true;
  }

}
