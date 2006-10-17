package net.sf.enunciate.modules.xfire;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Ryan Heaton
 */
@XmlRootElement (
  name = "simpleBean",
  namespace = "http://net.sf.enunciate/modules/xfire"
)
@XmlType (
  name = "simpleBean",
  namespace = "http://net.sf.enunciate/modules/xfire",
  propOrder = { "stringProp" }
)
public class SimpleBean {

  private String stringProp;

  public String getStringProp() {
    return stringProp;
  }

  public void setStringProp(String stringProp) {
    this.stringProp = stringProp;
  }

}
