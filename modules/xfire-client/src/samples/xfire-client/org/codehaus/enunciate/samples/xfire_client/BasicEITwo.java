package org.codehaus.enunciate.samples.xfire_client;

import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebResult;
import javax.jws.WebParam;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

/**
 * @author Ryan Heaton
 */
@WebService (
  name = "ei2",
  targetNamespace = "urn:xfire_client",
  serviceName = "ei2-service",
  wsdlLocation = "urn:ei2.wsdl",
  portName = "ei2port"
)
@SOAPBinding (
  style = SOAPBinding.Style.RPC,
  parameterStyle = SOAPBinding.ParameterStyle.BARE,
  use = SOAPBinding.Use.LITERAL
)
public class BasicEITwo {

  @WebMethod (
    operationName = "doBool",
    action = "urn:doBool"
  )
  @WebResult (
    name = "boolResult",
    targetNamespace = "urn:boolOpResult",
    partName = "boolOpResultPartName"
  )
  @RequestWrapper (
    localName = "doBoolReq",
    targetNamespace = "urn:doBoolReq",
    className = "net.nothing.BoolOpRequest"
  )
  @ResponseWrapper (
    localName = "doBoolRes",
    targetNamespace = "urn:doBoolRes",
    className = "net.nothing.BoolOpResponse"
  )
  @SOAPBinding (
    style = SOAPBinding.Style.DOCUMENT,
    use = SOAPBinding.Use.LITERAL
  )
  public boolean boolOp(@WebParam (name = "param1", targetNamespace = "urn:param1", partName = "param1Part") short s) throws BasicFaultTwo {
    return false;
  }

  public float floatOp(double d, long l) throws BasicFaultOne {
    return 0;
  }
}
