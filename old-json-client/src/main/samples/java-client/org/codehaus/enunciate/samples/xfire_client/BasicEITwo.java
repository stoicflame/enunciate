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
