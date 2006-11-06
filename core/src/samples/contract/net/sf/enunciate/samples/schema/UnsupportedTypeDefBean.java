package net.sf.enunciate.samples.schema;

import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAnyAttribute;

/**
 * @author Ryan Heaton
 */
public class UnsupportedTypeDefBean {

  @XmlMixed
  public String mixedProperty;
  @XmlAnyElement
  public String anyElementProperty;
  @XmlAnyAttribute
  public String anyAttributeProperty;
}
