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
 * @author Ryan Heaton
 */
public class EnumGWTMapper implements GWTMapper<Enum, String> {

  private final Class<Enum> enumClass;

  public EnumGWTMapper(Class<Enum> enumClass) {
    this.enumClass = enumClass;
  }

  public String toGWT(Enum jaxbObject, GWTMappingContext context) throws GWTMappingException {
    return jaxbObject.toString();
  }

  public Enum toJAXB(String gwtObject, GWTMappingContext context) throws GWTMappingException {
    return Enum.valueOf(enumClass, gwtObject);
  }
}
