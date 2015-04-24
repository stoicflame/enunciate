package org.codehaus.enunciate.samples.genealogy.data;

import com.webcohesion.enunciate.metadata.qname.XmlQNameEnum;
import com.webcohesion.enunciate.metadata.qname.XmlUnknownQNameEnumValue;

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
