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

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * GWT mapper that applies an XmlAdapter before mapping to an GWT object.
 *
 * @author Ryan Heaton
 */
public class AdaptingGWTMapper implements CustomGWTMapper {

  private final XmlAdapter adapter;
  private final GWTMapper adaptingMapper;
  private final Class jaxbClass;
  private final Class gwtClass;

  public AdaptingGWTMapper(XmlAdapter adapter, GWTMapper adaptingMapper, Class jaxbClass, Class gwtClass) {
    this.adapter = adapter;
    this.adaptingMapper = adaptingMapper;
    this.jaxbClass = jaxbClass;
    this.gwtClass = gwtClass;
  }

  public Object toGWT(Object jaxbObject, GWTMappingContext context) throws GWTMappingException {
    try {
      return adaptingMapper.toGWT(adapter.marshal(jaxbObject), context);
    }
    catch (Exception e) {
      throw new GWTMappingException(e);
    }
  }

  public Object toJAXB(Object gwtObject, GWTMappingContext context) throws GWTMappingException {
    try {
      return adapter.unmarshal(adaptingMapper.toJAXB(gwtObject, context));
    }
    catch (Exception e) {
      throw new GWTMappingException(e);
    }
  }

  public Class getJaxbClass() {
    return this.jaxbClass;
  }

  public Class getGwtClass() {
    return this.gwtClass;
  }
}
