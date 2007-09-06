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

import java.util.Calendar;
import java.util.Date;

/**
 * @author Ryan Heaton
 */
public class CalendarGWTMapper implements GWTMapper<Calendar, Date> {

  public Date toGWT(Calendar jaxbObject, GWTMappingContext context) throws GWTMappingException {
    return jaxbObject == null ? null : jaxbObject.getTime();
  }

  public Calendar toJAXB(Date gwtObject, GWTMappingContext context) throws GWTMappingException {
    if (gwtObject == null) {
      return null;
    }

    Calendar calendar = Calendar.getInstance();
    calendar.setTime(gwtObject);
    return calendar;
  }
}
