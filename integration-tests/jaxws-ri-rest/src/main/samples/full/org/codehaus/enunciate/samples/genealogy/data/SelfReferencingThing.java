package org.codehaus.enunciate.samples.genealogy.data;

/**
 * @author Ryan Heaton
 */
public class SelfReferencingThing<S extends SelfReferencingThing<S>> {

  private S me;

  public S getMe() {
    return me;
  }

  public void setMe(S me) {
    this.me = me;
  }
}
