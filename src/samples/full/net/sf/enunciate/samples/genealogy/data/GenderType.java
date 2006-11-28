package net.sf.enunciate.samples.genealogy.data;

import javax.xml.bind.annotation.XmlEnumValue;

/**
 * @author Ryan Heaton
 */
public enum GenderType {

  @XmlEnumValue (
    "m"
  )
  MALE,

  @XmlEnumValue (
    "f"
  )
  FEMALE
}
