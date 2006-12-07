package net.sf.enunciate.samples.xfire_client.with.a.nested.pckg;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;

/**
 * @author Ryan Heaton
 */
@XmlRootElement (
  name = "nested-pckg",
  namespace = "urn:nested-pckg"
)
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
