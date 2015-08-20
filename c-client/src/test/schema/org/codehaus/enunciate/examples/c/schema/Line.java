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

package org.codehaus.enunciate.examples.c.schema;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlID;

/**
 * @author Ryan Heaton
 */
@XmlRootElement
public class Line {

  private String id;
  private int startX;
  private int startY;
  private int endX;
  private int endY;

  @XmlID
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public int getStartX() {
    return startX;
  }

  public void setStartX(int startX) {
    this.startX = startX;
  }

  public int getStartY() {
    return startY;
  }

  public void setStartY(int startY) {
    this.startY = startY;
  }

  public int getEndX() {
    return endX;
  }

  public void setEndX(int endX) {
    this.endX = endX;
  }

  public int getEndY() {
    return endY;
  }

  public void setEndY(int endY) {
    this.endY = endY;
  }
}
