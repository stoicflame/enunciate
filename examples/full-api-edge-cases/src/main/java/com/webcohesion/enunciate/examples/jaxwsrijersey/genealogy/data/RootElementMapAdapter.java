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
package com.webcohesion.enunciate.examples.jaxwsrijersey.genealogy.data;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
public class RootElementMapAdapter extends XmlAdapter<RootElementMapAdapted, RootElementMap> {

  @Override
  public RootElementMap unmarshal(RootElementMapAdapted v) throws Exception {
    RootElementMap map = null;

    if (v != null) {
      map = new RootElementMap();
      if (v.getEntry() != null) {
        for (RootElementMapAdaptedEntry entry : v.getEntry()) {
          Object value = entry.getValue() == null ? null : entry.getValue().getValue();
          map.put(entry.getKey(), value);
        }
      }
    }

    return map;
  }

  @Override
  public RootElementMapAdapted marshal(RootElementMap v) throws Exception {
    RootElementMapAdapted adapted = null;

    if (v != null) {
      adapted = new RootElementMapAdapted();
      if (v.size() > 0) {
        adapted.setEntry(new ArrayList<RootElementMapAdaptedEntry>(v.size()));
        for (Map.Entry<String, Object> entry : v.entrySet()) {
          RootElementMapAdaptedEntry adaptedEntry = new RootElementMapAdaptedEntry();
          adaptedEntry.setKey(entry.getKey());
          adaptedEntry.setValue(new RootElementMapAdaptedValue());
          adaptedEntry.getValue().setValue(entry.getValue());
          adapted.getEntry().add(adaptedEntry);
        }
      }
    }

    return adapted;
  }
}