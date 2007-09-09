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

package org.codehaus.enunciate.modules.gwt;

import java.util.Date;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
public class GWTBeanOne {
  private String property1;
  private int property2;
  private Date property3;
  private GWTBeanOneDotOne property4;
  private GWTBeanOneDotTwo[] property5;
  private String property6;
  private Map property7;
  private String property8;
  private Date property9;

  public String getProperty1() {
    return property1;
  }

  public void setProperty1(String property1) {
    this.property1 = property1;
  }

  public int getProperty2() {
    return property2;
  }

  public void setProperty2(int property2) {
    this.property2 = property2;
  }

  public Date getProperty3() {
    return property3;
  }

  public void setProperty3(Date property3) {
    this.property3 = property3;
  }

  public GWTBeanOneDotOne getProperty4() {
    return property4;
  }

  public void setProperty4(GWTBeanOneDotOne property4) {
    this.property4 = property4;
  }

  public GWTBeanOneDotTwo[] getProperty5() {
    return property5;
  }

  public void setProperty5(GWTBeanOneDotTwo[] property5) {
    this.property5 = property5;
  }


  public String getProperty6() {
    return property6;
  }

  public void setProperty6(String property6) {
    this.property6 = property6;
  }

  public Map getProperty7() {
    return property7;
  }

  public void setProperty7(Map property7) {
    this.property7 = property7;
  }

  public String getProperty8() {
    return property8;
  }

  public void setProperty8(String property8) {
    this.property8 = property8;
  }

  public Date getProperty9() {
    return property9;
  }

  public void setProperty9(Date property9) {
    this.property9 = property9;
  }
}
