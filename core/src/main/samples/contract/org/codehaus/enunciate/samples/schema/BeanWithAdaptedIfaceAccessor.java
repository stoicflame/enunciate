package org.codehaus.enunciate.samples.schema;

/**
 * @author Ryan Heaton
 */
public class BeanWithAdaptedIfaceAccessor {

  private AdaptedIface iface;

  public AdaptedIface getIface() {
    return iface;
  }

  public void setIface(AdaptedIface iface) {
    this.iface = iface;
  }
}
