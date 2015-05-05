package org.codehaus.enunciate.samples.petclinic.schema;

import com.webcohesion.enunciate.metadata.qname.XmlQNameEnum;

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
