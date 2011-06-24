package org.codehaus.enunciate.modules.amf;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.Map;

public class EntryAdapter extends XmlAdapter<MapCarryObject[], Map.Entry<Object, Object>[]> {

  private class MapEntryImpl implements Map.Entry<Object, Object>{
      private Object key;
      private Object value;

      private MapEntryImpl(Object key, Object value) {
          this.key = key;
          this.value = value;
      }

      public Object getKey() {
          return key;
      }

      public Object getValue() {
          return value;
      }

      public Object setValue(Object value) {
          throw new UnsupportedOperationException();
      }

      public boolean equals(Object o) {
          if (this == o) return true;
          if (o == null || getClass() != o.getClass()) return false;

          MapEntryImpl mapEntry = (MapEntryImpl) o;

          if (key != null ? !key.equals(mapEntry.key) : mapEntry.key != null) return false;
          if (value != null ? !value.equals(mapEntry.value) : mapEntry.value != null) return false;

          return true;
      }

      public int hashCode() {
          int result = key != null ? key.hashCode() : 0;
          result = 31 * result + (value != null ? value.hashCode() : 0);
          return result;
      }
  }

  @Override
  public Map.Entry<Object, Object>[] unmarshal(MapCarryObject[] v) throws Exception {
    Map.Entry[] entries = new Map.Entry[v.length];
    for (int i = 0; i<v.length; i++) {
      entries[i] = new MapEntryImpl(v[i].getKey(), v[i].getValue());
    }
    return entries;
  }

  @Override
  public MapCarryObject[] marshal(Map.Entry<Object, Object>[] v) throws Exception {
    MapCarryObject[] list = new MapCarryObject[v.length];
    for (int i = 0; i<v.length; i++){
      list[i] = new MapCarryObject(v[i].getKey(), v[i].getValue());
    }
    return list;
  }

}
