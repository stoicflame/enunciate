package org.codehaus.enunciate.samples.schema;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * @author Ryan Heaton
 */
public class MapExtendedReference {

  private MapExtended extended;

  @XmlJavaTypeAdapter( MapExtendedAdapter.class )
  public MapExtended getExtended() {
    return extended;
  }

  public void setExtended(MapExtended extended) {
    this.extended = extended;
  }
}
