package org.codehaus.enunciate.samples.schema;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
public class MapExtendedAdapter extends XmlAdapter<RootElementMapAdapted, MapExtended> {

  @Override
  public MapExtended unmarshal(RootElementMapAdapted v) throws Exception {
    MapExtended map = null;

    if (v != null) {
      map = new MapExtended();
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
  public RootElementMapAdapted marshal(MapExtended v) throws Exception {
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