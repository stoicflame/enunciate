package org.codehaus.enunciate.samples.petclinic.schema;

import org.codehaus.enunciate.qname.XmlQNameEnum;
import org.codehaus.enunciate.qname.XmlUnknownQNameEnumValue;

/**
 * @author Ryan Heaton
 */
@XmlQNameEnum
public enum FavoriteFood {

  spaghetti,

  pizza,

  lasagna,

  @XmlUnknownQNameEnumValue
  unknown
}
