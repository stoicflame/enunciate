package org.codehaus.enunciate.samples.petclinic.schema;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * @author Ryan Heaton
 */
@XmlRootElement
public class MetaData<E extends Entity> {

  private E entity;

  public E getEntity() {
    return this.entity;
  }

  public void setEntity(E entity) {
    this.entity = entity;
  }
}
