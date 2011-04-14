package org.codehaus.enunciate.modules.amf;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.HashMap;
import java.util.Map;

public class MapXmlAdapter extends XmlAdapter<MapCarryObject[],Map<Object, Object>> {
    @Override
    public Map<Object, Object> unmarshal(MapCarryObject[] v) throws Exception {
        Map<Object, Object> result = new HashMap<Object, Object>();
        for (MapCarryObject object : v){
            result.put(object.getKey(), object.getValue());
        }
        return result;
    }

    @Override
    public MapCarryObject[] marshal(Map<Object, Object> v) throws Exception {
        MapCarryObject[] result = new MapCarryObject[v.size()];
        int i = 0;
        for (Map.Entry<Object, Object> entry : v.entrySet() ) {
            result[i] = new MapCarryObject(entry.getKey(), entry.getValue());
            i++;
        }
        return result;
    }
}
