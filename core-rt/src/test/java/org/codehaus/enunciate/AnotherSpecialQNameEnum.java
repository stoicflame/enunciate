package org.codehaus.enunciate;

import org.codehaus.enunciate.qname.XmlQNameEnum;
import org.codehaus.enunciate.qname.XmlQNameEnumValue;
import org.codehaus.enunciate.qname.XmlUnknownQNameEnumValue;

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
