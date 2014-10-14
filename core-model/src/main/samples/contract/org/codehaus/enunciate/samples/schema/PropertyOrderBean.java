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

import javax.xml.namespace.QName;
import java.util.UUID;

/**
 * @author Ryan Heaton
 */
public class PropertyOrderBean {

  private int propertyA;
  private QName propertyB;
  private UUID propertyC;
  private String propertyD;
  private Boolean propertyE;

  public QName getPropertyB() {
    return propertyB;
  }

  public void setPropertyB(QName propertyB) {
    this.propertyB = propertyB;
  }

  public int getPropertyA() {
    return propertyA;
  }

  public void setPropertyA(int propertyA) {
    this.propertyA = propertyA;
  }

  public String getPropertyD() {
    return propertyD;
  }

  public void setPropertyD(String propertyD) {
    this.propertyD = propertyD;
  }

  public Boolean getPropertyE() {
    return propertyE;
  }

  public void setPropertyE(Boolean propertyE) {
    this.propertyE = propertyE;
  }

  public UUID getPropertyC() {
    return propertyC;
  }

  public void setPropertyC(UUID propertyC) {
    this.propertyC = propertyC;
  }

}
