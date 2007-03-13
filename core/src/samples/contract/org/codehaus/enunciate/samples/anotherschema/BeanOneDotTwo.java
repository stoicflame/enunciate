package org.codehaus.enunciate.samples.anotherschema;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Ryan Heaton
 */
@XmlRootElement (
  name="bean1_2",
  namespace="urn:bean1_2"
)
public class BeanOneDotTwo extends BeanOne {

  private String oneDotTwo;

  public String getOneDotTwo() {
    return oneDotTwo;
  }

  public void setOneDotTwo(String oneDotTwo) {
    this.oneDotTwo = oneDotTwo;
  }
}
