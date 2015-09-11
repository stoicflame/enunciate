package com.webcohesion.enunciate.examples.jaxwsrijersey.genealogy.data;

/**
 * @author Ryan Heaton
 */
public class SelfReferencingThing<S extends SelfReferencingThing<S>> {

  private S me;
  private SelfReferencingThing[] clones;

  public S getMe() {
    return me;
  }

  public void setMe(S me) {
    this.me = me;
  }

  public SelfReferencingThing[] getClones() {
    return clones;
  }

  public void setClones(SelfReferencingThing[] clones) {
    this.clones = clones;
  }
}
