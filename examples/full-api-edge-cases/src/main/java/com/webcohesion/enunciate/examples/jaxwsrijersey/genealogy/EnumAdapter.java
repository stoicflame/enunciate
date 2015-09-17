package com.webcohesion.enunciate.examples.jaxwsrijersey.genealogy;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @author Ryan Heaton
 */
public abstract class EnumAdapter<T extends Enum<T>> extends XmlAdapter<String, T> {

  @Override
  public T unmarshal(String v) throws Exception {
    return null;
  }

  @Override
  public String marshal(T v) throws Exception {
    return null;
  }
}
