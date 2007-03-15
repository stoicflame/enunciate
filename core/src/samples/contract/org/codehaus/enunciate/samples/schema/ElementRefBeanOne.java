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

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.JAXBElement;

/**
 * @author Ryan Heaton
 */
public class ElementRefBeanOne {

  private ElementBeanOne property1;
  private Object property2;
  private JAXBElement<BeanOne> property3;

  @XmlElementRef
  public ElementBeanOne getProperty1() {
    return property1;
  }

  public void setProperty1(ElementBeanOne property1) {
    this.property1 = property1;
  }

  @XmlElementRef (
    type=BeanThree.class
  )
  public Object getProperty2() {
    return property2;
  }

  public void setProperty2(Object property2) {
    this.property2 = property2;
  }

  @XmlElementRef (
    name = "beanone",
    namespace = "urn:beanone"
  )
  public JAXBElement<BeanOne> getProperty3() {
    return property3;
  }

  public void setProperty3(JAXBElement<BeanOne> property3) {
    this.property3 = property3;
  }

}
