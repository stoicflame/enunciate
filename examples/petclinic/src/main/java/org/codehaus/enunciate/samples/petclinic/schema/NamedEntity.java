package org.codehaus.enunciate.samples.petclinic.schema;

/**
 * Simple JavaBean domain object adds a name property to <code>Entity</code>.
 * Used as a base class for objects needing these properties.
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 */
public class NamedEntity extends Entity {

  private String name;

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return this.name;
  }

}
