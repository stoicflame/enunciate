package org.codehaus.enunciate.contract.validation;

/**
 * @author Ryan Heaton
 */
public class Counter {

  private int count = 0;

  public void increment() {
    count++;
  }

  public void reset() {
    count = 0;
  }

  public int getCount() {
    return count;
  }
}
