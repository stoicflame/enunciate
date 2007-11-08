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

package org.codehaus.enunciate.modules.amf;

import javax.activation.DataHandler;
import java.net.URI;
import java.util.*;

/**
 * @author Ryan Heaton
 */
public class MyBean {

  private String property1;
  private float property2;
  private int property3;
  private Boolean property4;
  private Collection<Date> property5;
  private Map<String, String> property6 = new HashMap<String, String>();

  //test for the jaxb types
  private MyEnum myEnum;
  private URI uri;
  private Object object;
  private byte[] bytes;
  private DataHandler dataHandler;
  private UUID uuid;

  public String getProperty1() {
    return property1;
  }

  public void setProperty1(String property1) {
    this.property1 = property1;
  }

  public float getProperty2() {
    return property2;
  }

  public void setProperty2(float property2) {
    this.property2 = property2;
  }

  public int getProperty3() {
    return property3;
  }

  public void setProperty3(int property3) {
    this.property3 = property3;
  }

  public Boolean getProperty4() {
    return property4;
  }

  public void setProperty4(Boolean property4) {
    this.property4 = property4;
  }

  public Collection<Date> getProperty5() {
    return property5;
  }

  public void setProperty5(Collection<Date> property5) {
    this.property5 = property5;
  }

  public Map<String, String> getProperty6() {
    return property6;
  }

  public void setProperty6(Map<String, String> property6) {
    this.property6 = property6;
  }

  public URI getUri() {
    return uri;
  }

  public void setUri(URI uri) {
    this.uri = uri;
  }

  public Object getObject() {
    return object;
  }

  public void setObject(Object object) {
    this.object = object;
  }

  public byte[] getBytes() {
    return bytes;
  }

  public void setBytes(byte[] bytes) {
    this.bytes = bytes;
  }

  public DataHandler getDataHandler() {
    return dataHandler;
  }

  public void setDataHandler(DataHandler dataHandler) {
    this.dataHandler = dataHandler;
  }

  public UUID getUuid() {
    return uuid;
  }

  public void setUuid(UUID uuid) {
    this.uuid = uuid;
  }

  public MyEnum getMyEnum() {
    return myEnum;
  }

  public void setMyEnum(MyEnum myEnum) {
    this.myEnum = myEnum;
  }
}
