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

package org.codehaus.enunciate.samples.schema;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author Ryan Heaton
 */
public class AccessorFilterBean {

  private String property1;
  private String property2;
  private String property3;
  private String property4;
  private String property5;

  @XmlTransient
  public String field1;

  public String field2;
  protected String field3;
  String field4;
  public static String field5;
  public transient String field6;
  @XmlElement
  public String field7;

  @XmlTransient
  public String getProperty1() {
    return property1;
  }

  public void setProperty1(String property1) {
    this.property1 = property1;
  }

  public String getProperty2() {
    return property2;
  }

  public void setProperty3(String property3) {
    this.property3 = property3;
  }

  public String getProperty4() {
    return property4;
  }

  public void setProperty4(String property4) {
    this.property4 = property4;
  }

  @XmlElement
  public String getProperty5() {
    return property5;
  }

  public void setProperty5(String property5) {
    this.property5 = property5;
  }

}
