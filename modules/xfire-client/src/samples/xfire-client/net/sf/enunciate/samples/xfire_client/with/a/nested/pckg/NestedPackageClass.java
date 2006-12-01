package net.sf.enunciate.samples.xfire_client.with.a.nested.pckg;

import java.util.Collection;

/**
 * @author Ryan Heaton
 */
public class NestedPackageClass {

  private Collection<NestedPackageItem> items;
  private NestedPackageEnum type;

  public Collection<NestedPackageItem> getItems() {
    return items;
  }

  public void setItems(Collection<NestedPackageItem> items) {
    this.items = items;
  }

  public NestedPackageEnum getType() {
    return type;
  }

  public void setType(NestedPackageEnum type) {
    this.type = type;
  }
}
