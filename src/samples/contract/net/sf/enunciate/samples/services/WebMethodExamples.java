package net.sf.enunciate.samples.services;

import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

/**
 * @author Ryan Heaton
 */
@WebService
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

//  todo: support rpc encoding
//  @SOAPBinding (
//    style = SOAPBinding.Style.RPC,
//    parameterStyle = SOAPBinding.ParameterStyle.BARE
//  )
//  public Boolean rpcBareMethod() {
//    return Boolean.TRUE;
//  }

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
  public void docBareVoid2OutputMethod(
    @WebParam ( mode = WebParam.Mode.INOUT )
    Boolean bool1,
    @WebParam ( mode = WebParam.Mode.INOUT )
    Boolean bool2) {
  }

  @WebMethod (
    operationName = "special-operation-name"
  )
  public void specialNameMethod() {

  }
}
