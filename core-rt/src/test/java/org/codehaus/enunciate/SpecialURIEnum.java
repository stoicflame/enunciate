package org.codehaus.enunciate;

import com.webcohesion.enunciate.metadata.qname.XmlQNameEnum;
import com.webcohesion.enunciate.metadata.qname.XmlQNameEnumValue;

/**
 * @author Ryan Heaton
 */
@XmlQNameEnum (
  base = XmlQNameEnum.BaseType.URI,
  namespace = "urn:special#"
)
public enum SpecialURIEnum {

  appropriate,

  best,

  @XmlQNameEnumValue (namespace = "urn:definite#", localPart = "unique")
  certain,

  @XmlQNameEnumValue(namespace = "urn:definite#")
  chief,

  @XmlQNameEnumValue(namespace = "http://domain.com/definite/sure/")
  cool
}
