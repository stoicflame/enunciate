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

package org.codehaus.enunciate.modules.gwt;

import java.math.BigDecimal;

/**
 * @author Ryan Heaton
 */
public class BigDecimalGWTMapper implements CustomGWTMapper<BigDecimal, String> {

  public String toGWT(BigDecimal jaxbObject, GWTMappingContext context) throws GWTMappingException {
    return jaxbObject == null ? null : jaxbObject.toString();
  }

  public BigDecimal toJAXB(String gwtObject, GWTMappingContext context) throws GWTMappingException {
    return gwtObject == null ? null : new BigDecimal(gwtObject);
  }

  public Class<? extends BigDecimal> getJaxbClass() {
    return BigDecimal.class;
  }

  public Class<? extends String> getGwtClass() {
    return String.class;
  }
}
