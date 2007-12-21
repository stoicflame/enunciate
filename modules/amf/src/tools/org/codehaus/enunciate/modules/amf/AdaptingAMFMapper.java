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

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @author Ryan Heaton
 */
public class AdaptingAMFMapper implements AMFMapper {

  private final XmlAdapter adapter;
  private final AMFMapper adaptingMapper;

  public AdaptingAMFMapper(XmlAdapter adapter, AMFMapper adaptingMapper) {
    this.adapter = adapter;
    this.adaptingMapper = adaptingMapper;
  }

  public Object toAMF(Object jaxbObject, AMFMappingContext context) throws AMFMappingException {
    try {
      return adaptingMapper.toAMF(adapter.marshal(jaxbObject), context);
    }
    catch (Exception e) {
      throw new AMFMappingException(e);
    }
  }

  public Object toJAXB(Object amfObject, AMFMappingContext context) throws AMFMappingException {
    try {
      return adapter.unmarshal(adaptingMapper.toJAXB(amfObject, context));
    }
    catch (Exception e) {
      throw new AMFMappingException(e);
    }
  }
}
