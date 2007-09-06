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

/**
 * Maps JAXB objects to GWT objects, and vice-versa.  If a mapper exists for a certain JAXB class, it is assumed to
 * exist in the "gwt" package relative to the JAXB class package, same name with "GWTMapper" appended to the class name.
 *
 * @author Ryan Heaton
 */
public interface GWTMapper<J, G> {

  /**
   * Maps a JAXB object to a GWT object.
   *
   * @param jaxbObject The jaxb object.
   * @param context The mapping context.
   * @return The GWT object.
   */
  G toGWT(J jaxbObject, GWTMappingContext context) throws GWTMappingException;

  /**
   * Maps a GWT object to a JAXB object.
   *
   * @param gwtObject The gwt object.
   * @param context The mapping context.
   * @return The JAXB object.
   */
  J toJAXB(G gwtObject, GWTMappingContext context) throws GWTMappingException;
}
