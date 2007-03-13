package org.codehaus.enunciate.samples.docs.pckg1;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * some text that can be used to describe
 * a class on multiple lines <a href="#hi">with some markup</a>
 * that should be included:
 * <ul>
 *   <li>as raw text
 *   <li>in the xml
 * <ul>
 *
 * @author Ryan Heaton
 * @sometag some <b>value<b> that has some markup
 */
@XmlRootElement
public class BeanOne {

  private String property1;
  private String property2;
  private String property3;

  /**
   * property1: <b>text</b>
   *
   * @return the text for property 1
   */
  public String getProperty1() {
    return property1;
  }

  /**
   * property1: <b>text</b>
   *
   * @param property1 the text for property 1
   */
  public void setProperty1(String property1) {
    this.property1 = property1;
  }

  /**
   * property2: <b>text</b>
   *
   * @return the text for property 2
   */
  public String getProperty2() {
    return property2;
  }

  /**
   * property2: <b>text</b>
   *
   * @param property2 the text for property 2
   */
  public void setProperty2(String property2) {
    this.property2 = property2;
  }

  /**
   * property3: <b>text</b>
   *
   * @return the text for property 3
   */
  public String getProperty3() {
    return property3;
  }

  /**
   * property3: <b>text</b>
   *
   * @param property3 the text for property 3
   */
  public void setProperty3(String property3) {
    this.property3 = property3;
  }

}
