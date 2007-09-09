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

/**
 * @author Ryan Heaton
 */
public class GWTBeansServiceException extends com.google.gwt.user.client.rpc.SerializableException {

  private int property1;
  private byte property2;
  private Character property3;
  private String message;

  public int getProperty1() {
    return property1;
  }

  public void setProperty1(int property1) {
    this.property1 = property1;
  }

  public byte getProperty2() {
    return property2;
  }

  public void setProperty2(byte property2) {
    this.property2 = property2;
  }

  public Character getProperty3() {
    return property3;
  }

  public void setProperty3(Character property3) {
    this.property3 = property3;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
