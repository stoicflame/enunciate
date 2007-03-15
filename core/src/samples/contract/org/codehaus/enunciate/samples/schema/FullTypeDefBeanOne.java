/*
 * Copyright 2006 Web Cohesion
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

import javax.xml.bind.annotation.*;

/**
 * @author Ryan Heaton
 */
@XmlAccessorType (
  XmlAccessType.PROPERTY
)
public class FullTypeDefBeanOne {

  private String property1;
  private String property2;
  private String property3;
  private ElementBeanOne property4;
  private String property5;

  @XmlAttribute
  public String getProperty1() {
    return property1;
  }

  public void setProperty1(String property1) {
    this.property1 = property1;
  }

  @XmlValue
  public String getProperty2() {
    return property2;
  }

  public void setProperty2(String property2) {
    this.property2 = property2;
  }

  @XmlElement
  @XmlID
  public String getProperty3() {
    return property3;
  }

  public void setProperty3(String property3) {
    this.property3 = property3;
  }

  @XmlElementRef
  public ElementBeanOne getProperty4() {
    return property4;
  }

  public void setProperty4(ElementBeanOne property4) {
    this.property4 = property4;
  }

  public String getProperty5() {
    return property5;
  }

  public void setProperty5(String property5) {
    this.property5 = property5;
  }

}
