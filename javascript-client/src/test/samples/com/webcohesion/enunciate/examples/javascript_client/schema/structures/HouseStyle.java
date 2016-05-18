package com.webcohesion.enunciate.examples.javascript_client.schema.structures;

import com.webcohesion.enunciate.metadata.qname.XmlQNameEnum;

/**
 * @author Ryan Heaton
 */
@XmlQNameEnum(base = XmlQNameEnum.BaseType.URI)
public enum HouseStyle {

  victorian,

  contemporary,

  latin
}
