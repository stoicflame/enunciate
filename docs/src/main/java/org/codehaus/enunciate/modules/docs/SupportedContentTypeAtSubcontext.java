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

package org.codehaus.enunciate.modules.docs;

import org.codehaus.enunciate.contract.common.rest.SupportedContentType;

/**
 * A supported content type that is at a specific subcontext.
 *
 * @author Ryan Heaton
 */
public class SupportedContentTypeAtSubcontext extends SupportedContentType {

  private String subcontext;

  /**
   * The subcontext at which the content type is explicitly invoked.
   *
   * @return The subcontext at which the content type is explicitly invoked.
   */
  public String getSubcontext() {
    return subcontext;
  }

  /**
   * The subcontext at which the content type is explicitly invoked.
   *
   * @param subcontext The subcontext at which the content type is explicitly invoked.
   */
  public void setSubcontext(String subcontext) {
    this.subcontext = subcontext;
  }
}
