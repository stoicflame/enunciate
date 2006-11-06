package net.sf.enunciate.samples.anotherschema;

import javax.xml.bind.annotation.XmlEnumValue;

/**
 * @author Ryan Heaton
 */
public enum EnumBeanOne {

  @XmlEnumValue ( "value1")
  VALUE1,

  @XmlEnumValue ( "blobby1")
  VALUE2,

  @XmlEnumValue ( "justice")
  VALUE3,

  @XmlEnumValue ( "peace")
  VALUE4
}
