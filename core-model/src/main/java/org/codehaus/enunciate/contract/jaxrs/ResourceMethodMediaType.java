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

package org.codehaus.enunciate.contract.jaxrs;

import java.util.Set;

/**
 * A media type for a resource method.
 *
 * @author Ryan Heaton
 */
public class ResourceMethodMediaType {

  private String type;
  private boolean consumable;
  private boolean produceable;
  private Set<String> subcontexts;

  /**
   * The content type.
   *
   * @return The content type.
   */
  public String getType() {
    return type;
  }

  /**
   * The content type.
   *
   * @param type The content type.
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * Whether the content type is consumable by the resource.
   *
   * @return Whether the content type is consumable by the resource.
   */
  public boolean isConsumable() {
    return consumable;
  }

  /**
   * Whether the content type is consumable by the resource.
   *
   * @param consumable Whether the content type is consumable by the resource.
   */
  public void setConsumable(boolean consumable) {
    this.consumable = consumable;
  }

  /**
   * Whether the content type is produceable by the resource.
   *
   * @return Whether the content type is produceable by the resource.
   */
  public boolean isProduceable() {
    return produceable;
  }

  /**
   * Whether the content type is produceable by the resource.
   *
   * @param produceable Whether the content type is produceable by the resource.
   */
  public void setProduceable(boolean produceable) {
    this.produceable = produceable;
  }

  /**
   * Any additional subcontexts where requests for this specific type might be mounted.
   *
   * @return Any additional subcontexts where requests for this specific type might be mounted.
   */
  public Set<String> getSubcontexts() {
    return subcontexts;
  }

  /**
   * Any additional subcontexts where requests for this specific type might be mounted.
   *
   * @param subcontexts Any additional subcontexts where requests for this specific type might be mounted.
   */
  public void setSubcontexts(Set<String> subcontexts) {
    this.subcontexts = subcontexts;
  }

}
