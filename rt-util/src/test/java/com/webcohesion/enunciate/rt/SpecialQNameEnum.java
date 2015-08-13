package com.webcohesion.enunciate.rt;

import com.webcohesion.enunciate.metadata.qname.XmlQNameEnum;
import com.webcohesion.enunciate.metadata.qname.XmlQNameEnumValue;

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
