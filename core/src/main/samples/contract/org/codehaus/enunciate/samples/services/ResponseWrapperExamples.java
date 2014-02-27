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

package org.codehaus.enunciate.samples.services;

import javax.jws.WebService;
import javax.jws.WebParam;
import javax.xml.namespace.QName;
import javax.xml.ws.ResponseWrapper;
import javax.xml.ws.Holder;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import javax.jws.WebMethod;

/**
 * @author Ryan Heaton
 */
@WebService
public class ResponseWrapperExamples {

  @ResponseWrapper (
    localName = "fully-annotated",
    targetNamespace = "urn:fully-annotated",
    className = "org.codehaus.enunciate.samples.services.FullyAnnotatedMethod"
  )
  public String fullyAnnotated(Date date, QName qname) {
    return null;
  }

  public java.awt.Image defaultAnnotated(BigDecimal decimal, BigInteger integer) {
    return null;
  }

  public int withInOut(@WebParam(mode = WebParam.Mode.INOUT) Holder<Byte> b, String str) {
    return 0;
  }
  
  @WebMethod(operationName = "operation-name")
  public String withOperationName() {
    return null;
  }

}
