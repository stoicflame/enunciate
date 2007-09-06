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

package org.codehaus.enunciate.modules.gwt;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;

/**
 * @author Ryan Heaton
 */
public class XMLGregorianCalendarGWTMapper implements GWTMapper<XMLGregorianCalendar, Date> {

  public Date toGWT(XMLGregorianCalendar jaxbObject, GWTMappingContext context) throws GWTMappingException {
    return jaxbObject == null ? null : jaxbObject.toGregorianCalendar().getTime();
  }

  public XMLGregorianCalendar toJAXB(Date gwtObject, GWTMappingContext context) throws GWTMappingException {
    if (gwtObject == null) {
      return null;
    }

    throw new GWTMappingException("Cannot map from GWT to an instance of XMLGregorianCalendar.");
  }
}
