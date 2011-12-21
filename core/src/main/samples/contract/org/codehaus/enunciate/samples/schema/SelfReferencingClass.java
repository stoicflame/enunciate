package org.codehaus.enunciate.samples.schema;

/**
 * @author Ryan Heaton
 */
public class SelfReferencingClass<S extends SelfReferencingClass<S>> {

  private S me;

  public S getMe() {
    return me;
  }

  public void setMe(S me) {
    this.me = me;
  }
}
