package org.codehaus.enunciate.modules.amf;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class MapCarryObject {
    private Object key;
    private Object value;

    public MapCarryObject() {
    }

    public MapCarryObject(Object key, Object value) {
        this.key = key;
        this.value = value;
    }

    public Object getKey() {
        return key;
    }

    public void setKey(Object key) {
        this.key = key;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MapCarryObject that = (MapCarryObject) o;

        if (key != null ? !key.equals(that.key) : that.key != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return key != null ? key.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "MapCarryObject{" +
                "key='" + key + '\'' +
                ", value=" + value +
                '}';
    }
}
