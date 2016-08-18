/**
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webcohesion.enunciate.examples.epcis.impl;

import epcglobal.epcis.wsdl._1.*;
import epcglobal.epcis_query.xsd._1.*;

import javax.jws.WebService;

/**
 * @author Ryan Heaton
 */
@WebService (
  endpointInterface = "epcglobal.epcis.wsdl._1.EPCISServicePortType"
)
public class EPCISServiceImpl implements EPCISServicePortType {

  public ArrayOfString getQueryNames(EmptyParms parms) throws ImplementationExceptionResponse, SecurityExceptionResponse, ValidationExceptionResponse {
    ArrayOfString queryNames = new ArrayOfString();
    queryNames.getString().add("hello");
    queryNames.getString().add("how");
    queryNames.getString().add("are");
    queryNames.getString().add("you?");
    return queryNames;
  }

  public VoidHolder subscribe( Subscribe parms) throws DuplicateSubscriptionExceptionResponse, ImplementationExceptionResponse, InvalidURIExceptionResponse, NoSuchNameExceptionResponse, QueryParameterExceptionResponse, QueryTooComplexExceptionResponse, SecurityExceptionResponse, SubscribeNotPermittedExceptionResponse, SubscriptionControlsExceptionResponse, ValidationExceptionResponse {
    return null;
  }

  public VoidHolder unsubscribe( Unsubscribe parms) throws ImplementationExceptionResponse, NoSuchSubscriptionExceptionResponse, SecurityExceptionResponse, ValidationExceptionResponse {
    return null;
  }

  public ArrayOfString getSubscriptionIDs( GetSubscriptionIDs parms) throws ImplementationExceptionResponse, NoSuchNameExceptionResponse, SecurityExceptionResponse, ValidationExceptionResponse {
    return null;
  }

  public QueryResults poll(Poll parms) throws ImplementationExceptionResponse, NoSuchNameExceptionResponse, QueryParameterExceptionResponse, QueryTooComplexExceptionResponse, QueryTooLargeExceptionResponse, SecurityExceptionResponse, ValidationExceptionResponse {
    return null;
  }

  public String getStandardVersion( EmptyParms parms ) throws ImplementationExceptionResponse, SecurityExceptionResponse, ValidationExceptionResponse {
    return null;
  }

  public String getVendorVersion( EmptyParms parms ) throws ImplementationExceptionResponse, SecurityExceptionResponse, ValidationExceptionResponse {
    return null;
  }
}
