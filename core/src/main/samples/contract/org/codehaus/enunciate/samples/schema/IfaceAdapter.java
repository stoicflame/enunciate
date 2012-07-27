package org.codehaus.enunciate.samples.schema;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @author Ryan Heaton
 */
public class IfaceAdapter extends XmlAdapter<AdaptedIfaceImpl, AdaptedIface> {

  @Override
  public AdaptedIface unmarshal(AdaptedIfaceImpl v) throws Exception {
    return null;
  }

  @Override
  public AdaptedIfaceImpl marshal(AdaptedIface v) throws Exception {
    return null;
  }
}
