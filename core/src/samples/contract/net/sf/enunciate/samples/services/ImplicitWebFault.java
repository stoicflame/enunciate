package net.sf.enunciate.samples.services;

import java.util.Collection;

/**
 * @author Ryan Heaton
 */
public class ImplicitWebFault extends Exception {

  private boolean property1;
  private int property2;
  private Collection<String> property3;

  public boolean isProperty1() {
    return property1;
  }

  public void setProperty1(boolean property1) {
    this.property1 = property1;
  }

  public int getProperty2() {
    return property2;
  }

  public void setProperty2(int property2) {
    this.property2 = property2;
  }

  public Collection<String> getProperty3() {
    return property3;
  }

  public void setProperty3(Collection<String> property3) {
    this.property3 = property3;
  }
}
