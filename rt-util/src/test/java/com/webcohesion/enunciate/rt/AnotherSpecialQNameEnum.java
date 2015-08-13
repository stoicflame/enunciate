package com.webcohesion.enunciate.rt;

import com.webcohesion.enunciate.metadata.qname.XmlQNameEnum;
import com.webcohesion.enunciate.metadata.qname.XmlQNameEnumValue;
import com.webcohesion.enunciate.metadata.qname.XmlUnknownQNameEnumValue;

/**
 * @author Ryan Heaton
 */
@XmlQNameEnum
public enum AnotherSpecialQNameEnum {

  unusual,

  uncommon,

  specific,

  @XmlQNameEnumValue (exclude = true)
  not_a_qname_enum,

  @XmlUnknownQNameEnumValue
  other
}
