package org.codehaus.enunciate.samples.anotherschema;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Ryan Heaton
 */
@XmlRootElement (
  name="bean1_1",
  namespace="urn:bean1_1"
)
public class BeanOneDotOne extends BeanOne {

  private String oneDotOne;

  public String getOneDotOne() {
    return oneDotOne;
  }

  public void setOneDotOne(String oneDotOne) {
    this.oneDotOne = oneDotOne;
  }
}
