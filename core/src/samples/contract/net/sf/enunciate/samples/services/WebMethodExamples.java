package net.sf.enunciate.samples.services;

import net.sf.enunciate.samples.schema.BeanOne;
import net.sf.enunciate.samples.schema.BeanTwo;
import net.sf.enunciate.samples.schema.BeanThree;

import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

/**
 * @author Ryan Heaton
 */
@WebService (
  targetNamespace = "urn:web-method-examples"
)
public class WebMethodExamples {

  private Boolean privateMethod() {
    return Boolean.TRUE;
  }

  protected Boolean protectedMethod() {
    return privateMethod();
  }

  @WebMethod (
    exclude = true
  )
  public Boolean excludedMethod() {
    return Boolean.TRUE;
  }

  @Oneway
  public Boolean nonVoidOneWayMethod() {
    return Boolean.TRUE;
  }

  @Oneway
  public void exceptionThrowingOneWayMethod() throws Exception {
  }

  @Oneway
  public void runtimeExceptionThrowingOneWayMethod() throws RuntimeException {
  }

  @SOAPBinding (
    style = SOAPBinding.Style.RPC,
    parameterStyle = SOAPBinding.ParameterStyle.BARE
  )
  public Boolean rpcBareMethod() {
    return Boolean.TRUE;
  }

  @SOAPBinding (
    style = SOAPBinding.Style.DOCUMENT,
    parameterStyle = SOAPBinding.ParameterStyle.BARE
  )
  public Boolean docBare2ParamMethod(Boolean bool1, Boolean bool2) {
    return Boolean.TRUE;
  }

  @SOAPBinding (
    style = SOAPBinding.Style.DOCUMENT,
    parameterStyle = SOAPBinding.ParameterStyle.BARE
  )
  public Boolean docBare2OutputMethod(
    @WebParam ( mode = WebParam.Mode.INOUT )
    Boolean bool1, Boolean bool2) {
    return Boolean.TRUE;
  }

  @SOAPBinding (
    style = SOAPBinding.Style.DOCUMENT,
    parameterStyle = SOAPBinding.ParameterStyle.BARE
  )
  public Boolean docBareWithHeadersMethod(
    @WebParam ( header = true )
    Boolean bool1, Boolean bool2) {
    return Boolean.TRUE;
  }

  @SOAPBinding (
    style = SOAPBinding.Style.DOCUMENT,
    parameterStyle = SOAPBinding.ParameterStyle.BARE
  )
  public void docBareVoidMethod() {
  }

  @SOAPBinding (
    style = SOAPBinding.Style.DOCUMENT,
    parameterStyle = SOAPBinding.ParameterStyle.BARE
  )
  public BeanOne docLitBareMethod(BeanTwo beanTwo, @WebParam(header=true) boolean bool) throws ExplicitFaultBean, ImplicitWebFault {
    return null;
  }

  @SOAPBinding (
    style = SOAPBinding.Style.DOCUMENT,
    parameterStyle = SOAPBinding.ParameterStyle.BARE
  )
  public void docBareVoid2OutputMethod(
    @WebParam ( mode = WebParam.Mode.INOUT )
    Boolean bool1,
    @WebParam ( mode = WebParam.Mode.INOUT )
    Boolean bool2) {
  }

  @WebMethod (
    operationName = "special-operation-name",
    action = "urn:specialNameMethod",
    exclude = false
  )
  public void specialNameMethod() {

  }

  public BeanThree docLitWrappedMethod(boolean b, int i, @WebParam(header = true) short s, @WebParam( mode = WebParam.Mode.INOUT ) char c) throws ExplicitFaultBean, ImplicitWebFault {
    return null;
  }

  @SOAPBinding (
    style = SOAPBinding.Style.RPC
  )
  public BeanThree rpcLitWrappedMethod(boolean b, int i, @WebParam(header = true) short s, @WebParam( mode = WebParam.Mode.INOUT ) char c) throws ExplicitFaultBean, ImplicitWebFault {
    return null;
  }


}
