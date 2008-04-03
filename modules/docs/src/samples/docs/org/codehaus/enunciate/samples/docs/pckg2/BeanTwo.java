/*
 * Copyright 2006-2008 Web Cohesion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.enunciate.samples.docs.pckg2;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Text for BeanTwo
 *
 * @author Ryan Heaton
 */
@XmlRootElement
public class BeanTwo {

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
