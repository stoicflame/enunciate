package org.codehaus.enunciate.examples.xfire_client.schema;

import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlList;
import java.util.Collection;

/**
 * @author Ryan Heaton
 */
public abstract class Figure {

  private String id;
  private Collection<Label> labels;

  @XmlID
  @XmlAttribute
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @XmlList
  public Collection<Label> getLabels() {
    return labels;
  }

  public void setLabels(Collection<Label> labels) {
    this.labels = labels;
  }
}
