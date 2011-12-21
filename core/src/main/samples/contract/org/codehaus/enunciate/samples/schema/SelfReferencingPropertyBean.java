package org.codehaus.enunciate.samples.schema;

/**
 * @author Ryan Heaton
 */
public class SelfReferencingPropertyBean {

  private SelfReferencingClass thing;

  public SelfReferencingClass getThing() {
    return thing;
  }

  public void setThing(SelfReferencingClass thing) {
    this.thing = thing;
  }
}
