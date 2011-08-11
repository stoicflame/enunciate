package org.codehaus.enunciate.samples.petclinic.schema;

import org.codehaus.enunciate.qname.XmlQNameEnum;

/**
 * @author Ryan Heaton
 */
@XmlQNameEnum (
  base = XmlQNameEnum.BaseType.URI
)
public enum DefenseTraining {

  knife,

  staff,

  sword
}
