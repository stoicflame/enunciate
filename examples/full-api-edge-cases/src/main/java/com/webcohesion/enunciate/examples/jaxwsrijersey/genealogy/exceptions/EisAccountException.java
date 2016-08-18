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
package com.webcohesion.enunciate.examples.jaxwsrijersey.genealogy.exceptions;

import javax.xml.ws.WebFault;

@WebFault ( faultBean = "com.webcohesion.enunciate.samples.genealogy.exceptions.EisFault" )
public class EisAccountException extends EisExceptionBase {

  private static final long serialVersionUID = 6609084036637969280L;
  private EisFault faultInfo;


  public EisAccountException(String message, EisFault faultInfo, Throwable cause) {
    super(message, cause);
    this.faultInfo = faultInfo;
  }


  public EisAccountException(String message, EisFault faultInfo) {
    super(message);
    this.faultInfo = faultInfo;
  }


  public EisFault getFaultInfo() {
    return faultInfo;
  }

}