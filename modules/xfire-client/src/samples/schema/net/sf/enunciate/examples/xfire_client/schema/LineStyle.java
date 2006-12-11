package net.sf.enunciate.examples.xfire_client.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

/**
 * @author Ryan Heaton
 */
@XmlEnum (
  Integer.class
)
public enum LineStyle {

  @XmlEnumValue ("1")
  solid,

  @XmlEnumValue ("2")
  dotted,

  @XmlEnumValue ("3")
  dashed
}
