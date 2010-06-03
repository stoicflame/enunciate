package org.codehaus.enunciate.samples.genealogy.data;

import org.codehaus.enunciate.samples.genealogy.cite.Source;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * @author Ryan Heaton
 */
@XmlRootElement
@XmlSeeAlso ( {
  Person.class,
  Source.class
} )
public class RootElementMapWrapper {

  private RootElementMap map;

  @XmlJavaTypeAdapter ( RootElementMapAdapter.class )
  public RootElementMap getMap() {
    return map;
  }

  public void setMap(RootElementMap map) {
    this.map = map;
  }
}
