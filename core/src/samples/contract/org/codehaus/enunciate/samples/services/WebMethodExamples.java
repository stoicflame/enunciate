/*
 * Copyright 2006 Web Cohesion
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

import org.codehaus.enunciate.samples.schema.BeanOne;
import org.codehaus.enunciate.samples.schema.BeanTwo;
import org.codehaus.enunciate.samples.schema.BeanThree;

import javax.jws.*;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.Holder;
import java.util.Collection;

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

  @WebMethod
  @SOAPBinding (
    use = SOAPBinding.Use.ENCODED
  )
  public Boolean encodedMethod() {
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

  public int headerCollectionParam(@WebParam(header = true) Collection<Integer> headerCollection) {
    return 0;
  }

  @WebResult (
    header = true
  )
  public Collection<Double> headerCollectionReturn(int param) {
    return null;
  }

  @SOAPBinding (
    style = SOAPBinding.Style.RPC,
    parameterStyle = SOAPBinding.ParameterStyle.BARE
  )
  public Boolean rpcBareMethod() {
    return Boolean.TRUE;
  }

  @SOAPBinding (
    style = SOAPBinding.Style.RPC
  )
  public float rpcCollectionParam(int[] param) {
    return 0;
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
    Holder<Boolean> bool1, Boolean bool2) {
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
    Holder<Boolean> bool1,
    @WebParam ( mode = WebParam.Mode.INOUT )
    Holder<Boolean> bool2) {
  }

  public void invalidInOutParameter ( @WebParam (mode = WebParam.Mode.INOUT) java.awt.Image hi) {

  }

  @WebMethod (
    operationName = "special-operation-name",
    action = "urn:specialNameMethod",
    exclude = false
  )
  public void specialNameMethod() {

  }

  @WebResult (
    name = "doc-lit-wrapped-return",
    targetNamespace = "urn:docLitWrapped",
    header = true,
    partName = "doc-lit-wrapped-part"
  )
  public BeanThree docLitWrappedMethod(@WebParam(name = "hah", partName="hoo") boolean b, int i, @WebParam(header = true) short s,
                                       @WebParam( mode = WebParam.Mode.INOUT ) Holder<Float> c) throws ExplicitFaultBean, ImplicitWebFault {
    return null;
  }

  @SOAPBinding (
    style = SOAPBinding.Style.RPC
  )
  public BeanThree rpcLitWrappedMethod(@WebParam(name = "hah", partName="hoo") boolean b, int i, @WebParam(header = true) short s,
                                       @WebParam( mode = WebParam.Mode.INOUT ) Holder<Float> c) throws ExplicitFaultBean, ImplicitWebFault {
    return null;
  }


}
