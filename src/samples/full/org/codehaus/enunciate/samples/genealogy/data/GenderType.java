package org.codehaus.enunciate.samples.genealogy.data;

import javax.xml.bind.annotation.XmlEnumValue;

/**
 * The types of gender.
 *
 * @author Ryan Heaton
 */
public enum GenderType {

  /**
   * Male gender.
   */
  @XmlEnumValue (
    "m"
  )
  MALE,

  /**
   * Female gender.
   */
  @XmlEnumValue (
    "f"
  )
  FEMALE
}
