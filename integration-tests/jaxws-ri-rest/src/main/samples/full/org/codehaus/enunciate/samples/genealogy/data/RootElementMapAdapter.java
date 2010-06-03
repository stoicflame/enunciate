package org.codehaus.enunciate.samples.genealogy.data;

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