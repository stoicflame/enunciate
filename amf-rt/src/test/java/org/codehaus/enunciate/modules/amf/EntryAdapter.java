package org.codehaus.enunciate.modules.amf;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.Map;

public class EntryAdapter extends XmlAdapter<MapCarryObject, Map.Entry<Object, Object>> {

  @Override
  public Map.Entry<Object, Object> unmarshal(MapCarryObject v) throws Exception {
    final Object key = v.getKey();
    final Object value = v.getValue();
    return new Map.Entry<Object, Object>() {
      public Object getKey() {
        return key;
      }

      public Object getValue() {
        return value;
      }

      public Object setValue(Object value) {
        throw new UnsupportedOperationException();
      }
    };
  }

  @Override
  public MapCarryObject marshal(Map.Entry<Object, Object> v) throws Exception {
    return new MapCarryObject(v.getKey(), v.getValue());
  }

}
