package org.codehaus.enunciate;

import org.codehaus.enunciate.qname.XmlQNameEnum;
import org.codehaus.enunciate.qname.XmlQNameEnumValue;

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
  chief
}
