package net.sf.enunciate.samples.genealogy.cite;

import javax.xml.bind.annotation.XmlValue;

/**
 * An email address.
 *
 * @author Ryan Heaton
 */
public class EMail {

  private String value;

  /**
   * The value of the e-mail.
   *
   * @return The value of the e-mail.
   */
  @XmlValue
  public String getValue() {
    return value;
  }

  /**
   * The value of the e-mail.
   *
   * @param value The value of the e-mail.
   */
  public void setValue(String value) {
    this.value = value;
  }
}
