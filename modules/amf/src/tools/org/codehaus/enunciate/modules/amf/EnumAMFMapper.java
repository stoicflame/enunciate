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

package org.codehaus.enunciate.modules.amf;

/**
 * @author Ryan Heaton
 */
public class EnumAMFMapper implements CustomAMFMapper<Enum, String> {

  private final Class<Enum> enumClass;

  public EnumAMFMapper(Class<Enum> enumClass) {
    this.enumClass = enumClass;
  }

  public String toAMF(Enum jaxbObject, AMFMappingContext context) throws AMFMappingException {
    return jaxbObject.toString();
  }

  public Enum toJAXB(String amfObject, AMFMappingContext context) throws AMFMappingException {
    return Enum.valueOf(enumClass, amfObject);
  }

  public Class<? extends Enum> getJaxbClass() {
    return this.enumClass;
  }

  public Class<? extends String> getAmfClass() {
    return String.class;
  }
}
