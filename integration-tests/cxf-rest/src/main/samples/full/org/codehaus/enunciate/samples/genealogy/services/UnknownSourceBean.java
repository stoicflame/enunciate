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

package org.codehaus.enunciate.samples.genealogy.services;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Information about an unknown source.
 * @author Ryan Heaton
 */
@XmlRootElement (
  namespace = "http://enunciate.codehaus.org/samples/full"
)
public class UnknownSourceBean {

  private String sourceId;
  private int errorCode;

  /**
   * The id of the source.
   *
   * @return The id of the source.
   */
  public String getSourceId() {
    return sourceId;
  }

  /**
   * The id of the source.
   *
   * @param sourceId The id of the source.
   */
  public void setSourceId(String sourceId) {
    this.sourceId = sourceId;
  }

  /**
   * The error code.
   *
   * @return The error code.
   */
  public int getErrorCode() {
    return errorCode;
  }

  /**
   * The error code.
   *
   * @param errorCode The error code.
   */
  public void setErrorCode(int errorCode) {
    this.errorCode = errorCode;
  }
}
