package org.codehaus.enunciate.modules.jaxws_client;

import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;

/**
 * @author Ryan Heaton
 */
@XmlRootElement
public class ElementWithMapProperty {

  private HashMap stuff;

  @XmlElementWrapper (name = "hello")
  public HashMap getStuff() {
    return stuff;
  }

  public void setStuff(HashMap stuff) {
    this.stuff = stuff;
  }
}
