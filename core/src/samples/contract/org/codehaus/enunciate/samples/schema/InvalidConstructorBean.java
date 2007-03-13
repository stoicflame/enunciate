package org.codehaus.enunciate.samples.schema;

/**
 * @author Ryan Heaton
 */
public class InvalidConstructorBean {

  private boolean flag;

  public InvalidConstructorBean(boolean flag) {
    this.flag = flag;
  }

  protected InvalidConstructorBean() {
  }

  public boolean isFlag() {
    return flag;
  }

  public void setFlag(boolean flag) {
    this.flag = flag;
  }
}
