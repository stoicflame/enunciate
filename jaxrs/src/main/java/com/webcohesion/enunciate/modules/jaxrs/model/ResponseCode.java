/**
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webcohesion.enunciate.modules.jaxrs.model;

import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author Ryan Heaton
 */
public class ResponseCode {

  private final ResourceMethod resourceMethod;
  private int code;
  private String condition;
  private Map<String, String> additionalHeaders = new TreeMap<String, String>();
  private DecoratedTypeMirror type;

  public ResponseCode(ResourceMethod resourceMethod) {
    this.resourceMethod = resourceMethod;
  }

  public ResourceMethod getResourceMethod() {
    return resourceMethod;
  }

  public int getCode() {
    return code;
  }

  public void setCode(int code) {
    this.code = code;
  }

  public String getCondition() {
    return condition;
  }

  public void setCondition(String condition) {
    this.condition = condition;
  }

  public Map<String, String> getAdditionalHeaders() {
    return additionalHeaders;
  }

  public void setAdditionalHeader(String key, String value) {
    this.additionalHeaders.put(key, value);
  }

  public DecoratedTypeMirror getType() {
    return type;
  }

  public void setType(DecoratedTypeMirror type) {
    this.type = type;
  }

}
