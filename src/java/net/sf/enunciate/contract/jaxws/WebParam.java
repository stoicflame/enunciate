package net.sf.enunciate.contract.jaxws;

import com.sun.mirror.declaration.ParameterDeclaration;
import net.sf.jelly.apt.decorations.declaration.DecoratedParameterDeclaration;

import javax.jws.soap.SOAPBinding;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author Ryan Heaton
 */
public class WebParam extends DecoratedParameterDeclaration implements SimpleWebMessage, ComplexWebMessage, WebMessagePart {

  private final javax.jws.WebParam annotation;
  private final WebMethod method;

  public WebParam(ParameterDeclaration delegate, WebMethod method) {
    super(delegate);

    this.method = method;
    if (this.method == null) {
      throw new IllegalArgumentException("A web method must be provided.");
    }

    annotation = delegate.getAnnotation(javax.jws.WebParam.class);
    method.getDeclaringEndpointInterface().getValidator().validate(this);
  }

  /**
   * The name of this web param.
   *
   * @return The name of this web param.
   */
  public String getName() {
    String name = getSimpleName();

    if ((annotation != null) && (annotation.name() != null) && (!"".equals(annotation.name()))) {
      name = annotation.name();
    }

    return name;
  }

  /**
   * The part name of the message for this parameter.
   *
   * @return The part name of the message for this parameter.
   */
  public String getPartName() {
    if (isHeader()) {
      throw new UnsupportedOperationException("The part name is used for the WSDL and WSDL doesn't support header elements except " +
        "through the extensibility mechanism.");
    }

    return getSimpleName();
  }

  /**
   * The message name of the message for this parameter.
   *
   * @return The message name of the message for this parameter.
   */
  public String getMessageName() {
    if (isHeader()) {
      throw new UnsupportedOperationException("The message name is used for the WSDL and WSDL doesn't support header elements except " +
        "through the extensibility mechanism.");
    }

    return method.getMessageName();
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
      targetNamespace = this.method.getDeclaringEndpointInterface().getTargetNamespace();
    }

    return targetNamespace;
  }

  /**
   * The mode of this web param.
   *
   * @return The mode of this web param.
   */
  public javax.jws.WebParam.Mode getMode() {
    javax.jws.WebParam.Mode mode = javax.jws.WebParam.Mode.IN;

    if ((annotation != null) && (annotation.mode() != null)) {
      mode = annotation.mode();
    }

    return mode;
  }

  /**
   * Whether this is a header param.
   *
   * @return Whether this is a header param.
   */
  public boolean isHeader() {
    boolean header = false;

    if (annotation != null) {
      header = annotation.header();
    }

    return header;
  }

  /**
   * Whether this is a simple param.  A web parameter can only be a simple parameter if it's a header parameter.
   *
   * @return Whether this is a simple param.
   */
  public boolean isSimple() {
    return isHeader();
  }

  /**
   * This web param can be considered complex only if it is BARE.
   *
   * @return This web param can be considered complex only if it is BARE.
   */
  public boolean isComplex() {
    return method.getSoapParameterStyle() == SOAPBinding.ParameterStyle.BARE;
  }

  /**
   * Whether this is an input message depends on its mode.
   *
   * @return Whether this is an input message depends on its mode.
   */
  public boolean isInput() {
    return (getMode() == javax.jws.WebParam.Mode.IN) || (getMode() == javax.jws.WebParam.Mode.INOUT);
  }

  /**
   * Whether this is an output message depends on its mode.
   *
   * @return Whether this is an output message depends on its mode.
   */
  public boolean isOutput() {
    return (getMode() == javax.jws.WebParam.Mode.OUT) || (getMode() == javax.jws.WebParam.Mode.INOUT);
  }

  /**
   * @return false
   */
  public boolean isFault() {
    return false;
  }

  /**
   * If this web param is complex, it will only have one part: itself.
   *
   * @return this.
   * @throws UnsupportedOperationException if this web param isn't complex.
   */
  public Collection<WebMessagePart> getParts() {
    if (!isComplex()) {
      throw new UnsupportedOperationException("Web param doesn't represent a complex method input/output.");
    }

    return new ArrayList<WebMessagePart>(Arrays.asList(this));
  }

}
