package org.codehaus.enunciate.samples.schema;

import javax.xml.bind.annotation.XmlAnyElement;

/**
 * @author Ryan Heaton
 */
public class RootElementMapAdaptedValue {

  @XmlAnyElement
  public Object value;
}
