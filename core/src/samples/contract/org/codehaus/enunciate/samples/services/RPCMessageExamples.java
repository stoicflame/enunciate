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
import javax.jws.soap.SOAPBinding;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.datatype.Duration;
import javax.xml.ws.Holder;
import java.net.URI;
import java.util.Calendar;

/**
 * @author Ryan Heaton
 */
@WebService
public class RPCMessageExamples {

  @SOAPBinding (
    style = SOAPBinding.Style.RPC
  )
  public void voidMethod(Calendar param) {

  }

  @SOAPBinding (
    style = SOAPBinding.Style.RPC
  )
  public boolean simpleMethod(URI param1, XMLGregorianCalendar xmlCalendar) {
    return false;
  }

  @SOAPBinding (
    style = SOAPBinding.Style.RPC
  )
  public short withHeader(@WebParam (header = true) Duration param1, Short param2) {
    return 0;
  }

  @SOAPBinding (
    style = SOAPBinding.Style.RPC
  )
  public boolean withInOut(@WebParam (mode = WebParam.Mode.INOUT)
  Holder<Float> param1, Double param2) {
    return false;
  }


}
