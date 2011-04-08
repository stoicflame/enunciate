package org.codehaus.enunciate;

import org.codehaus.enunciate.qname.XmlQNameEnum;
import org.codehaus.enunciate.qname.XmlQNameEnumValue;

/**
 * @author Ryan Heaton
 */
@XmlQNameEnum (namespace = "urn:special")
public enum SpecialQNameEnum {

  appropriate,

  best,

  @XmlQNameEnumValue (namespace = "urn:definite", localPart = "unique")
  certain,

  @XmlQNameEnumValue(namespace = "urn:definite")
  chief
}
