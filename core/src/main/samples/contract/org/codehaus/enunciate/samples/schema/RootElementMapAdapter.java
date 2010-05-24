package org.codehaus.enunciate.samples.schema;

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
      if (v.entry != null) {
        for (RootElementMapAdaptedEntry entry : v.entry) {
          Object value = entry.value == null ? null : entry.value.value;
          map.put(entry.key, value);
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
        adapted.entry = new ArrayList<RootElementMapAdaptedEntry>(v.size());
        for (Map.Entry<String, Object> entry : v.entrySet()) {
          RootElementMapAdaptedEntry adaptedEntry = new RootElementMapAdaptedEntry();
          adaptedEntry.key = entry.getKey();
          adaptedEntry.value = new RootElementMapAdaptedValue();
          adaptedEntry.value.value = entry.getValue();
          adapted.entry.add(adaptedEntry);
        }
      }
    }

    return adapted;
  }
}
