package org.codehaus.enunciate.samples.jaxws;

/**
 * @author Ryan Heaton
 */
public class NonConflictingFault extends Exception {

  private String hi;
  private String hello;

  public String getHi() {
    return hi;
  }

  public void setHi(String hi) {
    this.hi = hi;
  }

  public String getHello() {
    return hello;
  }

  public void setHello(String hello) {
    this.hello = hello;
  }
}
