package org.codehaus.enunciate.examples.ruby.schema.structures;

import org.codehaus.enunciate.qname.XmlQNameEnum;
import org.codehaus.enunciate.qname.XmlUnknownQNameEnumValue;

/**
 * @author Ryan Heaton
 */
@XmlQNameEnum ( base = XmlQNameEnum.BaseType.URI )
public enum HouseColor {

  blue,

  red,

  yellow,

  @XmlUnknownQNameEnumValue
  unknown
}
