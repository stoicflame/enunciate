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

package org.codehaus.enunciate.modules.xfire_client.jaxws;

/**
 * A dummy bean used for tests.
 * 
 * @author Ryan Heaton
 */
public class DummyMethod {

  private String order;
  private int out;
  private float back;
  private double in;
  private short and;
  private boolean of;

  public String getOrder() {
    return order;
  }

  public void setOrder(String order) {
    this.order = order;
  }

  public int getOut() {
    return out;
  }

  public void setOut(int out) {
    this.out = out;
  }

  public float getBack() {
    return back;
  }

  public void setBack(float back) {
    this.back = back;
  }

  public double getIn() {
    return in;
  }

  public void setIn(double in) {
    this.in = in;
  }

  public short getAnd() {
    return and;
  }

  public void setAnd(short and) {
    this.and = and;
  }

  public boolean isOf() {
    return of;
  }

  public void setOf(boolean of) {
    this.of = of;
  }
}
